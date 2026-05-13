const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    const tripList = document.getElementById("tripList");
    const resultTitle = document.getElementById("resultTitle");
    const routeSearchForm = document.getElementById("routeSearchForm");
    const swapRouteBtn = document.getElementById("swapRouteBtn");
    const clearFilterBtn = document.getElementById("clearFilterBtn");
    const companyFilter = document.getElementById("companyFilter");
    const typeFilter = document.getElementById("typeFilter");
    const searchFrom = document.getElementById("searchFrom");
    const searchTo = document.getElementById("searchTo");
    const searchDate = document.getElementById("searchDate");

    let allTrips = [];
    let companyOptions = [];
    let busTypeOptions = [];

    searchFrom.value = getParam("from") || searchFrom.value || "";
    searchTo.value = getParam("to") || searchTo.value || "";
    searchDate.value = getParam("date") || searchDate.value || "";

    loadFilterOptions();
    loadTripsFromApi();

    if (routeSearchForm) {
        routeSearchForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const from = searchFrom.value.trim();
            const to = searchTo.value.trim();
            const date = searchDate.value;

            if (!from) {
                alert("Vui lòng nhập điểm đi.");
                return;
            }

            if (!to) {
                alert("Vui lòng nhập điểm đến.");
                return;
            }

            if (!date) {
                alert("Vui lòng chọn ngày đi.");
                return;
            }

            const pageParams = new URLSearchParams({
                from: from,
                to: to,
                date: date
            });

            window.history.pushState({}, "", `search-result.html?${pageParams.toString()}`);
            loadTripsFromApi();
        });
    }

    if (swapRouteBtn) {
        swapRouteBtn.addEventListener("click", function () {
            const temp = searchFrom.value;
            searchFrom.value = searchTo.value;
            searchTo.value = temp;
            loadTripsFromApi();
        });
    }

    if (clearFilterBtn) {
        clearFilterBtn.addEventListener("click", function () {
            document.querySelectorAll("input[type='checkbox']").forEach(input => {
                input.checked = false;
            });

            const defaultSort = document.querySelector("input[name='sort'][value='default']");
            if (defaultSort) defaultSort.checked = true;

            document.querySelectorAll(".quick-filter-btn").forEach(button => {
                button.classList.remove("active");
            });

            renderTrips();
        });
    }

    document.addEventListener("change", function (event) {
        if (event.target.matches("input[name='sort'], input[type='checkbox']")) {
            renderTrips();
        }
    });

    document.querySelectorAll(".quick-filter-btn").forEach(button => {
        button.addEventListener("click", function () {
            this.classList.toggle("active");
            renderTrips();
        });
    });

    async function loadTripsFromApi() {
        const diemDi = searchFrom.value.trim();
        const diemDen = searchTo.value.trim();
        const ngayDi = searchDate.value;

        if (!diemDi || !diemDen || !ngayDi) {
            allTrips = [];
            renderTrips();
            return;
        }

        showLoading();

        try {
            const params = new URLSearchParams({
                diemDi: diemDi,
                diemDen: diemDen,
                ngayDi: ngayDi
            });

            const url = `${API_BASE_URL}/api/chuyen-xe/search?${params.toString()}`;

            console.log("CALL API SEARCH:", url);

            const response = await fetch(url, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            const result = await response.json();

            console.log("API SEARCH RESULT:", result);

            if (!response.ok) {
                allTrips = [];
                renderApiError(result.message || "Không tìm được chuyến xe.");
                return;
            }

            const rawTrips = extractTripArray(result);

            allTrips = rawTrips.map(mapApiTripToView);

            console.log("TRIPS AFTER MAP:", allTrips);

            renderTrips();
        } catch (error) {
            console.error("Lỗi gọi API tìm chuyến:", error);
            allTrips = [];
            renderApiError("Không thể kết nối server.");
        }
    }

    async function loadFilterOptions() {
        await Promise.allSettled([
            loadCompanyOptions(),
            loadBusTypeOptions()
        ]);
    }

    async function loadCompanyOptions() {
        if (!companyFilter) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/nha-xe`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            const result = await response.json();

            if (!response.ok) {
                console.error("Lỗi tải nhà xe:", result.message || result);
                return;
            }

            companyOptions = normalizeFilterOptions(result, "maNhaXe", "tenNhaXe");
            renderFilterOptions(companyFilter, "company", companyOptions);
        } catch (error) {
            console.error("Lỗi gọi API nhà xe:", error);
        }
    }

    async function loadBusTypeOptions() {
        if (!typeFilter) return;

        try {
            const response = await fetch(`${API_BASE_URL}/api/loai-xe`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            const result = await response.json();

            if (!response.ok) {
                console.error("Lỗi tải loại xe:", result.message || result);
                return;
            }

            busTypeOptions = normalizeFilterOptions(result, "maLoaiXe", "tenLoaiXe");
            renderFilterOptions(typeFilter, "busType", busTypeOptions);
        } catch (error) {
            console.error("Lỗi gọi API loại xe:", error);
        }
    }

    function renderFilterOptions(container, name, options) {
        if (!container || !Array.isArray(options) || !options.length) {
            return;
        }

        const selectedValues = [...container.querySelectorAll("input[type='checkbox']:checked")].map(input => input.value);

        container.innerHTML = options.map(option => `
            <label class="check-row">
                <input type="checkbox" name="${name}" value="${escapeHtml(option.id)}" ${selectedValues.includes(option.id) || selectedValues.includes(option.label) ? "checked" : ""}>
                <span>${escapeHtml(option.label)}</span>
            </label>
        `).join("");
    }

    function extractTripArray(result) {
        if (Array.isArray(result)) {
            return result;
        }

        if (Array.isArray(result.data)) {
            return result.data;
        }

        if (Array.isArray(result.result)) {
            return result.result;
        }

        if (Array.isArray(result.content)) {
            return result.content;
        }

        return [];
    }

    function mapApiTripToView(item) {
        const thoiGianKhoiHanh = item.thoiGianKhoiHanh || item.departureTime || "";
        const thoiGianDen = item.thoiGianDen || item.arrivalTimeFull || "";
        const imageList = normalizeList(
            item.images ||
            item.imageUrls ||
            item.imageUrl ||
            item.hinhAnh ||
            item.image
        );
        const amenityList = normalizeList(
            item.amenities ||
            item.tienIchs ||
            item.tienIch ||
            item.utilities
        );

        const date = item.ngayDi || getDateFromApi(thoiGianKhoiHanh);
        const time = item.gioDi || getTimeFromApi(thoiGianKhoiHanh);
        const arrivalTime = item.gioDen || getTimeFromApi(thoiGianDen);

        return {
            id: item.maChuyen || item.id || "",
            company: item.tenNhaXe || item.nhaXe || item.company || "Nhà xe",
            busType: item.tenLoaiXe || item.loaiXe || item.busType || "Xe khách",

            from: item.tenBenDi || item.diemDi || item.benDi || "",
            to: item.tenBenDen || item.diemDen || item.benDen || "",

            date: date,
            time: time || "00:00",
            arrivalTime: arrivalTime || "--:--",
            arrivalDate: item.ngayDen || getDateFromApi(thoiGianDen) || date,

            startStation: item.tenBenDi || item.benDi || item.diemDi || "",
            endStation: item.tenBenDen || item.benDen || item.diemDen || "",

            duration: buildDurationText(item),

            price: Number(item.giaVe || item.price || 0),
            emptySeats: Number(item.gheTrong || item.soGheTrong || item.availableSeats || item.emptySeats || 0),
            totalSeats: Number(item.tongGhe || item.soLuongGhe || item.totalSeats || item.capacity || 0),

            image: imageList[0] || "https://placehold.co/500x320?text=Bus",
            images: imageList,
            amenities: amenityList,
            rating: Number(item.rating || 4.8),
            reviewCount: Number(item.reviewCount || 0),

            pickup: Boolean(item.pickup || item.coDonTanNoi || false),
            gps: Boolean(item.gps || false),
            deal: Boolean(item.deal || false),

            note: item.note || item.ghiChu || "Vui lòng có mặt trước giờ khởi hành ít nhất 15 phút."
        };
    }

    function renderTrips() {
        let trips = [...allTrips];

        trips = applyFilters(trips);
        trips = applySort(trips);

        if (resultTitle) {
            resultTitle.textContent = `Kết quả: ${trips.length} chuyến`;
        }

        if (!tripList) return;

        if (!trips.length) {
            tripList.innerHTML = `
                <div class="no-result">
                    <h5>Không tìm thấy chuyến phù hợp</h5>
                    <p class="mb-0">Bạn thử đổi điểm đi, điểm đến, ngày đi hoặc xóa bớt bộ lọc.</p>
                </div>
            `;
            return;
        }

        tripList.innerHTML = trips.map(renderTripCard).join("");

        document.querySelectorAll(".detail-link").forEach(button => {
            button.addEventListener("click", function () {
                this.closest(".trip-result-card")
                    ?.querySelector(".trip-details")
                    ?.classList.toggle("d-none");
            });
        });
    }

    function renderTripCard(trip) {
        const extraImages = trip.images.slice(1, 4);
        const amenities = trip.amenities.slice(0, 6);
        const pickupDropoffText = trip.pickup
            ? "Có hỗ trợ đón tận nơi"
            : "Đón tại bến / điểm quy định";
        const discountText = trip.deal
            ? "Đang có ưu đãi giảm giá"
            : "Chưa có ưu đãi giảm giá";

        return `
            <article class="trip-result-card">
                <div class="trip-main">
                    <div>
                        <div class="trip-image">
                            <img src="${escapeHtml(trip.image)}" alt="${escapeHtml(trip.company)}">
                        </div>
                        ${extraImages.length ? `
                            <div class="trip-image-strip">
                                ${extraImages.map(image => `
                                    <img src="${escapeHtml(image)}" alt="${escapeHtml(trip.company)}">
                                `).join("")}
                            </div>
                        ` : ``}
                        ${trip.pickup ? `<span class="pickup-label">ĐÓN TẬN NƠI</span>` : ``}
                    </div>

                    <div class="trip-info">
                        <div class="trip-company-row">
                            <h4>${escapeHtml(trip.company)}</h4>
                            <span class="rating-badge">
                                <i class="fa-solid fa-star"></i> ${trip.rating} (${trip.reviewCount})
                            </span>
                        </div>

                        <div class="bus-type">${escapeHtml(trip.busType)}</div>

                        ${amenities.length ? `
                            <div class="amenity-list">
                                ${amenities.map(amenity => `
                                    <span class="amenity-chip"><i class="fa-solid fa-circle-check"></i> ${escapeHtml(amenity)}</span>
                                `).join("")}
                            </div>
                        ` : `
                            <div class="amenity-empty">Chưa cập nhật tiện ích xe</div>
                        `}

                        <div class="timeline">
                            <div class="timeline-item">
                                <span class="timeline-dot"></span>
                                <div>
                                    <span class="timeline-time">${trip.time}</span>
                                    <span class="timeline-place"> • ${trip.startStation}</span>
                                    <div class="timeline-date">${trip.duration}</div>
                                </div>
                            </div>

                            <div class="timeline-item">
                                <span class="timeline-dot"></span>
                                <div>
                                    <span class="timeline-time">${trip.arrivalTime}</span>
                                    <span class="timeline-place"> • ${trip.endStation}</span>
                                    <div class="timeline-date">(${formatShortDate(trip.arrivalDate)})</div>
                                </div>
                            </div>
                        </div>

                        <div class="text-muted small mt-2">
                            Còn ${trip.emptySeats} ghế trống
                        </div>
                    </div>

                    <div class="trip-price-action">
                        <div class="trip-price">${moneySafe(trip.price)}</div>
                        <div>
                            <button class="detail-link" type="button">
                                Thông tin chi tiết <i class="fa-solid fa-caret-down"></i>
                            </button>
                            <a class="book-btn" href="trip-detail.html?id=${trip.id}">
                                Đặt vé
                            </a>
                        </div>
                    </div>
                </div>

                <div class="trip-details d-none">
                    <div class="trip-details-header">
                        <span class="note-badge">Thông tin chi tiết</span>
                        <span class="trip-details-summary">
                            ${trip.rating} sao • ${trip.reviewCount} đánh giá
                        </span>
                    </div>

                    <div class="detail-grid">
                        <div class="detail-section">
                            <h6>Đánh giá</h6>
                            <div class="detail-rating">
                                <i class="fa-solid fa-star"></i>
                                <strong>${trip.rating}</strong>
                                <span>(${trip.reviewCount} lượt đánh giá)</span>
                            </div>
                            <p>${escapeHtml(trip.note)}</p>
                        </div>

                        <div class="detail-section">
                            <h6>Giảm giá</h6>
                            <p>${escapeHtml(discountText)}</p>
                            <p class="detail-muted">Giá vé hiện tại: ${moneySafe(trip.price)}</p>
                        </div>

                        <div class="detail-section">
                            <h6>Điểm đón / trả</h6>
                            <p><strong>Đón:</strong> ${escapeHtml(trip.startStation)}</p>
                            <p><strong>Trả:</strong> ${escapeHtml(trip.endStation)}</p>
                            <p class="detail-muted">${escapeHtml(pickupDropoffText)}</p>
                        </div>

                        <div class="detail-section">
                            <h6>Tiện ích</h6>
                            ${amenities.length ? `
                                <div class="detail-tags">
                                    ${amenities.map(amenity => `
                                        <span class="detail-tag"><i class="fa-solid fa-check"></i> ${escapeHtml(amenity)}</span>
                                    `).join("")}
                                </div>
                            ` : `
                                <p>Chưa cập nhật tiện ích.</p>
                            `}
                        </div>

                        <div class="detail-section detail-section-wide">
                            <h6>Hình ảnh</h6>
                            <div class="detail-images">
                                ${trip.images.slice(0, 6).map(image => `
                                    <img src="${escapeHtml(image)}" alt="${escapeHtml(trip.company)}">
                                `).join("")}
                            </div>
                        </div>
                    </div>
                </div>
            </article>
        `;
    }

    function showLoading() {
        if (resultTitle) {
            resultTitle.textContent = "Đang tìm chuyến...";
        }

        if (tripList) {
            tripList.innerHTML = `
                <div class="no-result">
                    <h5>Đang tìm chuyến...</h5>
                    <p class="mb-0">Vui lòng chờ trong giây lát.</p>
                </div>
            `;
        }
    }

    function renderApiError(message) {
        if (resultTitle) {
            resultTitle.textContent = "Kết quả: 0 chuyến";
        }

        if (tripList) {
            tripList.innerHTML = `
                <div class="no-result">
                    <h5>${message}</h5>
                    <p class="mb-0">Bạn thử kiểm tra lại server hoặc dữ liệu tìm kiếm.</p>
                </div>
            `;
        }
    }

    function applyFilters(trips) {
        const timeRanges = getCheckedValues("timeRange");
        const companies = getCheckedValues("company");
        const priceRanges = getCheckedValues("priceRange");
        const busTypes = getCheckedValues("busType");

        const quickDeal = document.querySelector("[data-quick='deal']")?.classList.contains("active");
        const quickGps = document.querySelector("[data-quick='gps']")?.classList.contains("active");

        return trips.filter(trip => {
            if (timeRanges.length && !timeRanges.includes(getTimeRange(trip.time))) return false;
            if (companies.length && !matchesSelectedOption(companies, trip.company, companyOptions)) return false;
            if (priceRanges.length && !priceRanges.includes(getPriceRange(trip.price))) return false;
            if (busTypes.length && !matchesSelectedOption(busTypes, trip.busType, busTypeOptions)) return false;
            if (quickDeal && !trip.deal) return false;
            if (quickGps && !trip.gps) return false;

            return true;
        });
    }

    function applySort(trips) {
        const sort = document.querySelector("input[name='sort']:checked")?.value || "default";

        if (sort === "time-asc") {
            return trips.sort((a, b) => timeToMinute(a.time) - timeToMinute(b.time));
        }

        if (sort === "time-desc") {
            return trips.sort((a, b) => timeToMinute(b.time) - timeToMinute(a.time));
        }

        if (sort === "price-asc") {
            return trips.sort((a, b) => a.price - b.price);
        }

        if (sort === "price-desc") {
            return trips.sort((a, b) => b.price - a.price);
        }

        if (sort === "rating-desc") {
            return trips.sort((a, b) => b.rating - a.rating);
        }

        return trips;
    }

    function getCheckedValues(name) {
        return [...document.querySelectorAll(`input[name='${name}']:checked`)].map(input => input.value);
    }

    function normalizeFilterOptions(result, idKey, labelKey) {
        const items = extractTripArray(result);

        return items
            .map(item => ({
                id: String(item?.[idKey] ?? "").trim(),
                label: String(item?.[labelKey] ?? "").trim()
            }))
            .filter(item => item.id && item.label);
    }

    function matchesSelectedOption(selectedValues, tripValue, options) {
        const valueText = String(tripValue || "").trim();

        if (!selectedValues.length) {
            return true;
        }

        return selectedValues.some(selectedValue => {
            if (selectedValue === valueText) {
                return true;
            }

            const option = options.find(item => item.id === selectedValue);

            return option ? option.label === valueText : false;
        });
    }

    function getTimeRange(time) {
        const hour = Number(String(time || "00:00").split(":")[0]);

        if (hour >= 0 && hour < 6) return "morning";
        if (hour >= 6 && hour < 18) return "day";

        return "night";
    }

    function getPriceRange(price) {
        if (price < 250000) return "low";
        if (price <= 350000) return "mid";

        return "high";
    }

    function timeToMinute(time) {
        const [hour, minute] = String(time || "00:00").split(":").map(Number);

        return (hour || 0) * 60 + (minute || 0);
    }

    function formatShortDate(value) {
        if (!value) return "--/--";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}`;
    }

    function getParam(name) {
        return new URLSearchParams(window.location.search).get(name);
    }

    function getTimeFromApi(value) {
        if (!value) return "";

        const text = String(value);

        if (text.includes("T")) {
            return text.split("T")[1].substring(0, 5);
        }

        return text.substring(0, 5);
    }

    function getDateFromApi(value) {
        if (!value) return "";

        const text = String(value);

        if (text.includes("T")) {
            return text.split("T")[0];
        }

        return text.substring(0, 10);
    }

    function buildDurationText(item) {
        if (item.thoiGianDuKien) {
            const minutes = Number(item.thoiGianDuKien);
            const h = Math.floor(minutes / 60);
            const m = minutes % 60;

            if (h > 0 && m > 0) return `${h} giờ ${m} phút`;
            if (h > 0) return `${h} giờ`;

            return `${m} phút`;
        }

        if (item.khoangCach) {
            return `${item.khoangCach} km`;
        }

        return "Đang cập nhật";
    }

    function normalizeList(value) {
        if (!value) return [];

        if (Array.isArray(value)) {
            return value.map(item => String(item || "").trim()).filter(Boolean);
        }

        return String(value)
            .split("||")
            .map(item => item.trim())
            .filter(Boolean);
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#39;");
    }

    function moneySafe(value) {
        if (typeof money === "function") {
            return money(value);
        }

        return Number(value || 0).toLocaleString("vi-VN") + "đ";
    }
});