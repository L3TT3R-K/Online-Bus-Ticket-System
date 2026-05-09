const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", async function () {
    const tripId = getParam("id");

    const tripTitle = document.getElementById("tripTitle");
    const tripDetail = document.getElementById("tripDetail");
    const seatGrid = document.getElementById("seatGrid");
    const selectedSeatsEl = document.getElementById("selectedSeats");
    const totalPriceEl = document.getElementById("totalPrice");
    const continueBookingBtn = document.getElementById("continueBooking");

    const pickupPointSelect = document.getElementById("pickupPointSelect");
    const dropoffPointSelect = document.getElementById("dropoffPointSelect");
    const selectedPickupText = document.getElementById("selectedPickupText");
    const selectedDropoffText = document.getElementById("selectedDropoffText");

    let selectedSeats = [];
    let selectedSeatData = [];
    let currentTrip = null;
    let currentSeats = [];

    if (!tripId) {
        showError("Không tìm thấy mã chuyến xe trên URL.");
        return;
    }

    await loadTripDetailPage();

    async function loadTripDetailPage() {
        try {
            showLoading();

            currentTrip = findTripFromSession(tripId);

            if (!currentTrip) {
                currentTrip = await loadTripInfoFromBackend(tripId);
            }

            if (!currentTrip) {
                currentTrip = createFallbackTrip(tripId);
            }

            renderTripInfo(currentTrip);
            renderPickupDropoffOptions(currentTrip);

            currentSeats = await loadSeatsFromBackend(tripId);

            renderSeatMap(currentSeats);
            updateSummary();
        } catch (error) {
            console.error("Lỗi tải chi tiết chuyến:", error);
            showError("Không thể tải chi tiết chuyến xe hoặc sơ đồ ghế.");
        }
    }

    function showLoading() {
        if (tripTitle) {
            tripTitle.textContent = "Đang tải chi tiết chuyến xe...";
        }

        if (tripDetail) {
            tripDetail.innerHTML = `
                <div class="text-muted">
                    <i class="fa-solid fa-spinner fa-spin"></i>
                    Đang tải thông tin chuyến xe...
                </div>
            `;
        }

        if (seatGrid) {
            seatGrid.innerHTML = `
                <div class="text-muted text-center py-4">
                    <i class="fa-solid fa-spinner fa-spin"></i>
                    Đang tải sơ đồ ghế...
                </div>
            `;
        }

        if (pickupPointSelect) {
            pickupPointSelect.innerHTML = `<option value="">Đang tải điểm đón...</option>`;
        }

        if (dropoffPointSelect) {
            dropoffPointSelect.innerHTML = `<option value="">Đang tải điểm trả...</option>`;
        }
    }

    function showError(message) {
        if (tripTitle) {
            tripTitle.textContent = "Chi tiết chuyến xe";
        }

        if (tripDetail) {
            tripDetail.innerHTML = `
                <div class="alert alert-danger mb-0">
                    ${message}
                </div>
            `;
        }

        if (seatGrid) {
            seatGrid.innerHTML = "";
        }

        if (pickupPointSelect) {
            pickupPointSelect.innerHTML = `<option value="">Không có điểm đón</option>`;
        }

        if (dropoffPointSelect) {
            dropoffPointSelect.innerHTML = `<option value="">Không có điểm trả</option>`;
        }
    }

    function createFallbackTrip(maChuyen) {
        return {
            id: maChuyen,
            company: "Đang cập nhật",
            busType: "Đang cập nhật",
            bienSo: "",
            from: "Điểm đi",
            to: "Điểm đến",
            date: "",
            time: "--:--",
            arrivalTime: "--:--",
            arrivalDate: "",
            startStation: "Đang cập nhật",
            endStation: "Đang cập nhật",
            duration: "Đang cập nhật",
            price: 0,
            emptySeats: 0,
            totalSeats: 0,
            images: [],
            amenities: [],
            diemDon: [],
            diemTra: [],
            rating: 0,
            reviewCount: 0,
            note: "Vui lòng có mặt trước giờ khởi hành ít nhất 15 phút."
        };
    }

    function findTripFromSession(maChuyen) {
        try {
            const raw = sessionStorage.getItem("tripSearchResults");

            if (!raw) return null;

            const parsed = JSON.parse(raw);
            const list = extractArray(parsed);

            const found = list.find(item =>
                String(item.maChuyen || item.id || "") === String(maChuyen)
            );

            if (!found) return null;

            return mapTripToView(found);
        } catch (error) {
            console.warn("Không đọc được tripSearchResults:", error);
            return null;
        }
    }

    async function loadTripInfoFromBackend(maChuyen) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${maChuyen}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                console.warn("Chưa có hoặc lỗi API chi tiết chuyến:", response.status);
                return null;
            }

            const result = await response.json();
            const data = result && result.data ? result.data : result;

            return mapTripToView(data);
        } catch (error) {
            console.warn("Không gọi được API chi tiết chuyến:", error);
            return null;
        }
    }

    async function loadSeatsFromBackend(maChuyen) {
        const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${maChuyen}/ghe`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || "Không tải được sơ đồ ghế.");
        }

        const data = extractArray(result);

        return data.map(item => ({
            maGhe: item.maGhe || item.id || "",
            soGhe: item.soGhe || item.seatNo || "",
            trangThai: item.trangThai || "TRONG"
        }));
    }

    function extractArray(result) {
        if (Array.isArray(result)) return result;
        if (result && Array.isArray(result.data)) return result.data;
        if (result && Array.isArray(result.result)) return result.result;
        if (result && Array.isArray(result.content)) return result.content;

        return [];
    }

    function mapTripToView(item) {
        const thoiGianKhoiHanh = item.thoiGianKhoiHanh || item.departureTime || "";
        const thoiGianDen = item.thoiGianDen || item.arrivalTimeFull || "";

        const date = item.ngayDi || getDateFromApi(thoiGianKhoiHanh);
        const time = item.gioDi || getTimeFromApi(thoiGianKhoiHanh);
        const arrivalTime = item.gioDen || getTimeFromApi(thoiGianDen);

        return {
            id: item.maChuyen || item.id || tripId,

            company: item.tenNhaXe || item.nhaXe || item.company || "Nhà xe",
            busType: item.tenLoaiXe || item.loaiXe || item.busType || "Xe khách",
            bienSo: item.bienSo || "",

            from: item.diemDi || item.tenBenDi || item.benDi || "",
            to: item.diemDen || item.tenBenDen || item.benDen || "",

            date: date,
            time: time || "00:00",
            arrivalTime: arrivalTime || "--:--",
            arrivalDate: item.ngayDen || getDateFromApi(thoiGianDen) || date,

            startStation: item.diemDi || item.tenBenDi || item.benDi || "",
            endStation: item.diemDen || item.tenBenDen || item.benDen || "",

            duration: buildDurationText(item),

            price: Number(item.giaVe || item.price || 0),
            emptySeats: Number(item.soGheTrong || item.gheTrong || item.emptySeats || 0),
            totalSeats: Number(item.soLuongGhe || item.tongGhe || item.totalSeats || 0),

            images: normalizeStringList(item.images || item.imageUrls),
            amenities: normalizeStringList(item.amenities),

            diemDon: normalizePointList(item.diemDon),
            diemTra: normalizePointList(item.diemTra),

            rating: Number(item.rating || 0),
            reviewCount: Number(item.reviewCount || 0),

            note: item.note || item.ghiChu || "Vui lòng có mặt trước giờ khởi hành ít nhất 15 phút."
        };
    }

    function normalizeStringList(value) {
        if (Array.isArray(value)) {
            return value.filter(Boolean);
        }

        if (typeof value === "string") {
            return value.split("||").filter(Boolean);
        }

        return [];
    }

    function normalizePointList(value) {
        if (!Array.isArray(value)) return [];

        return value.map((item, index) => ({
            maDiem: item.maDiem || item.id || item.stationId || item.maBen || "",
            tenDiem: item.tenDiem || item.name || item.tenBen || item.diaChi || `Điểm ${index + 1}`,
            thoiGian: item.thoiGian || item.time || "",
            diaChi: item.diaChi || ""
        }));
    }

    function renderTripInfo(trip) {
        if (tripTitle) {
            tripTitle.textContent = `${trip.from || "Điểm đi"} → ${trip.to || "Điểm đến"}`;
        }

        if (!tripDetail) return;

        const imageHtml = trip.images && trip.images.length
            ? `
                <div class="mt-3">
                    <img src="${trip.images[0]}" alt="${trip.company}" class="img-fluid rounded">
                </div>
            `
            : "";

        const amenitiesHtml = trip.amenities && trip.amenities.length
            ? `
                <div class="mt-3">
                    <strong>Tiện ích:</strong>
                    <div class="d-flex flex-wrap gap-2 mt-2">
                        ${trip.amenities.map(item => `
                            <span class="badge bg-light text-dark border">
                                <i class="fa-solid fa-check text-success"></i> ${item}
                            </span>
                        `).join("")}
                    </div>
                </div>
            `
            : "";

        tripDetail.innerHTML = `
            <h4 class="fw-bold">${trip.company}</h4>

            <p class="text-muted mb-2">
                ${trip.busType}
                ${trip.bienSo ? ` - ${trip.bienSo}` : ""}
            </p>

            ${
                trip.rating
                    ? `<p class="mb-2">
                        <span class="badge bg-primary">
                            <i class="fa-solid fa-star"></i>
                            ${trip.rating} (${trip.reviewCount})
                        </span>
                    </p>`
                    : ""
            }

            ${imageHtml}

            <hr>

            <p><strong>Mã chuyến:</strong> ${trip.id}</p>
            <p><strong>Giờ đi:</strong> ${trip.time} - ${formatShortDate(trip.date)}</p>
            <p><strong>Giờ đến:</strong> ${trip.arrivalTime}</p>
            <p><strong>Điểm đi:</strong> ${trip.startStation || trip.from}</p>
            <p><strong>Điểm đến:</strong> ${trip.endStation || trip.to}</p>
            <p><strong>Thời gian:</strong> ${trip.duration}</p>
            <p><strong>Ghế trống:</strong> ${trip.emptySeats}/${trip.totalSeats || "?"}</p>
            <p>
                <strong>Giá vé:</strong>
                <span class="text-danger fw-bold">${moneySafe(trip.price)}</span>
            </p>

            ${amenitiesHtml}
        `;
    }

    function renderPickupDropoffOptions(trip) {
        renderPointSelect(
            pickupPointSelect,
            trip.diemDon,
            "Chọn điểm đón",
            trip.startStation || trip.from,
            "PICKUP"
        );

        renderPointSelect(
            dropoffPointSelect,
            trip.diemTra,
            "Chọn điểm trả",
            trip.endStation || trip.to,
            "DROPOFF"
        );

        updateSummary();
    }

    function renderPointSelect(selectEl, points, placeholder, fallbackName, fallbackType) {
        if (!selectEl) return;

        let finalPoints = Array.isArray(points) ? [...points] : [];

        if (!finalPoints.length && fallbackName) {
            finalPoints = [{
                maDiem: fallbackType,
                tenDiem: fallbackName,
                thoiGian: ""
            }];
        }

        if (!finalPoints.length) {
            selectEl.innerHTML = `<option value="">Không có dữ liệu</option>`;
            return;
        }

        selectEl.innerHTML = `
            <option value="">-- ${placeholder} --</option>
            ${finalPoints.map(point => `
                <option
                    value="${point.maDiem || point.tenDiem}"
                    data-name="${point.tenDiem || ""}"
                    data-time="${point.thoiGian || ""}"
                >
                    ${point.tenDiem || "Không rõ điểm"}
                    ${point.thoiGian ? ` - ${formatPointTime(point.thoiGian)}` : ""}
                </option>
            `).join("")}
        `;

        if (finalPoints.length === 1) {
            selectEl.selectedIndex = 1;
        }

        selectEl.addEventListener("change", updateSummary);
    }

    function renderSeatMap(seats) {
        if (!seatGrid) return;

        if (!Array.isArray(seats) || !seats.length) {
            seatGrid.innerHTML = `
                <div class="alert alert-warning mb-0">
                    Chuyến này chưa có dữ liệu ghế.
                </div>
            `;
            return;
        }

        seatGrid.innerHTML = seats.map(seat => {
            const status = seat.trangThai || "TRONG";

            let cls = "seat";
            let disabled = "";

            if (status === "DA_DAT") {
                cls += " booked";
                disabled = "disabled";
            } else if (status === "DANG_GIU") {
                cls += " holding";
                disabled = "disabled";
            }

            return `
                <button
                    class="${cls}"
                    type="button"
                    data-ma-ghe="${seat.maGhe || ""}"
                    data-seat="${seat.soGhe}"
                    data-status="${status}"
                    title="${mapSeatStatusText(status)}"
                    ${disabled}
                >
                    ${seat.soGhe}
                </button>
            `;
        }).join("");
    }

    if (seatGrid) {
        seatGrid.addEventListener("click", function (event) {
            const btn = event.target.closest(".seat");

            if (!btn) return;

            const status = btn.dataset.status;

            if (status === "DA_DAT" || status === "DANG_GIU") return;

            const soGhe = btn.dataset.seat;
            const maGhe = btn.dataset.maGhe;

            if (!soGhe) return;

            const existedIndex = selectedSeats.indexOf(soGhe);

            if (existedIndex >= 0) {
                selectedSeats.splice(existedIndex, 1);
                selectedSeatData = selectedSeatData.filter(item => item.soGhe !== soGhe);
                btn.classList.remove("selected");
            } else {
                selectedSeats.push(soGhe);
                selectedSeatData.push({
                    soGhe,
                    maGhe
                });
                btn.classList.add("selected");
            }

            updateSummary();
        });
    }

    if (continueBookingBtn) {
        continueBookingBtn.addEventListener("click", function () {
            if (!selectedSeats.length) {
                alert("Vui lòng chọn ít nhất 1 ghế.");
                return;
            }

            const pickup = getSelectedPoint(pickupPointSelect);
            const dropoff = getSelectedPoint(dropoffPointSelect);

            if (!pickup) {
                alert("Vui lòng chọn điểm đón.");
                pickupPointSelect?.focus();
                return;
            }

            if (!dropoff) {
                alert("Vui lòng chọn điểm trả.");
                dropoffPointSelect?.focus();
                return;
            }

            const bookingData = {
                trip: currentTrip,
                seats: selectedSeatData,
                pickup,
                dropoff,
                totalPrice: selectedSeats.length * Number(currentTrip?.price || 0)
            };

            sessionStorage.setItem("bookingTrip", JSON.stringify(currentTrip));
            sessionStorage.setItem("bookingSeats", JSON.stringify(selectedSeatData));
            sessionStorage.setItem("bookingPickup", JSON.stringify(pickup));
            sessionStorage.setItem("bookingDropoff", JSON.stringify(dropoff));
            sessionStorage.setItem("bookingData", JSON.stringify(bookingData));

            const query = new URLSearchParams({
                tripId: currentTrip.id,
                seats: selectedSeats.join(","),
                maGhe: selectedSeatData.map(item => item.maGhe).join(","),
                pickupId: pickup.id,
                dropoffId: dropoff.id
            });

            window.location.href = "booking.html?" + query.toString();
        });
    }

    function getSelectedPoint(selectEl) {
        if (!selectEl || !selectEl.value) return null;

        const option = selectEl.options[selectEl.selectedIndex];

        return {
            id: selectEl.value,
            name: option?.dataset?.name || option?.textContent?.trim() || selectEl.value,
            time: option?.dataset?.time || ""
        };
    }

    function updateSummary() {
        if (selectedSeatsEl) {
            selectedSeatsEl.textContent = selectedSeats.length
                ? selectedSeats.join(", ")
                : "Chưa chọn";
        }

        if (selectedPickupText) {
            const pickup = getSelectedPoint(pickupPointSelect);
            selectedPickupText.textContent = pickup ? pickup.name : "Chưa chọn";
        }

        if (selectedDropoffText) {
            const dropoff = getSelectedPoint(dropoffPointSelect);
            selectedDropoffText.textContent = dropoff ? dropoff.name : "Chưa chọn";
        }

        if (totalPriceEl) {
            const total = selectedSeats.length * Number(currentTrip?.price || 0);
            totalPriceEl.textContent = moneySafe(total);
        }
    }

    function mapSeatStatusText(status) {
        switch (status) {
            case "TRONG":
                return "Ghế trống";
            case "DANG_GIU":
                return "Đang giữ";
            case "DA_DAT":
                return "Đã đặt";
            default:
                return "Không rõ";
        }
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

    function formatShortDate(value) {
        if (!value) return "--/--";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}`;
    }

    function formatPointTime(value) {
        if (!value) return "";

        const text = String(value);

        if (text.includes("T")) {
            return text.split("T")[1].substring(0, 5);
        }

        return text.substring(0, 5);
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

    function moneySafe(value) {
        if (typeof money === "function") {
            return money(value);
        }

        return Number(value || 0).toLocaleString("vi-VN") + "đ";
    }
});