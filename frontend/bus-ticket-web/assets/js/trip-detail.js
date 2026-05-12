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

            currentTrip = await loadTripDetailFromBackend(tripId);

            if (!currentTrip) {
                currentTrip = findTripFromSession(tripId);
            }

            if (!currentTrip) {
                currentTrip = findTripFromBookingSession(tripId);
            }

            if (!currentTrip) {
                currentTrip = findTripFromLocalData(tripId);
            }

            if (!currentTrip) {
                currentTrip = createFallbackTrip(tripId);
            }

            renderTripInfo(currentTrip);

            const stopData = await loadDiemDonTraForTrip(tripId);
            renderPickupDropoffOptions(stopData, currentTrip);

            currentSeats = await loadSeatsFromBackend(tripId);
            renderSeatMap(currentSeats);

            updateSummary();
        } catch (error) {
            console.error("Lỗi tải chi tiết chuyến:", error);
            showError("Không thể tải chi tiết chuyến xe hoặc sơ đồ ghế.");
        }
    }

    async function loadTripDetailFromBackend(maChuyen) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}`, {
                method: "GET",
                headers: buildAuthHeaders()
            });

            const result = await response.json();

            if (!response.ok) {
                console.warn("Không tải được chi tiết chuyến:", result.message || response.status);
                return null;
            }

            const data = result && result.data ? result.data : result;
            return mapTripToView(data);
        } catch (error) {
            console.warn("Lỗi gọi API chi tiết chuyến:", error);
            return null;
        }
    }

    async function loadSeatsFromBackend(maChuyen) {
        const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}/ghe`, {
            method: "GET",
            headers: buildAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            throw new Error(result.message || "Không tải được sơ đồ ghế.");
        }

        const data = extractArray(result);

        return data.map(item => ({
            maGhe: item.maGhe || item.id || "",
            soGhe: item.soGhe || item.seatNo || item.tenGhe || "",
            trangThai: normalizeSeatStatus(item.trangThai || item.status || "TRONG")
        }));
    }

    async function loadDiemDonTraForTrip(maChuyen) {
        try {
            const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}/diem-don-tra`, {
                method: "GET",
                headers: buildAuthHeaders()
            });

            const result = await response.json();

            if (!response.ok) {
                console.warn("Không tải được điểm đón/trả:", result.message || response.status);
                return {
                    diemDon: [],
                    diemTra: []
                };
            }

            const data = result && result.data ? result.data : result;

            return {
                diemDon: normalizePointList(data?.diemDon),
                diemTra: normalizePointList(data?.diemTra)
            };
        } catch (error) {
            console.warn("Lỗi gọi API điểm đón/trả:", error);
            return {
                diemDon: [],
                diemTra: []
            };
        }
    }

    function renderTripInfo(trip) {
        if (tripTitle) {
            tripTitle.textContent = `${trip.from || "Điểm đi"} → ${trip.to || "Điểm đến"}`;
        }

        if (!tripDetail) return;

        const imageHtml = trip.images && trip.images.length
            ? `
                <div class="mt-3">
                    <img src="${escapeHtml(trip.images[0])}" alt="${escapeHtml(trip.company)}" class="img-fluid rounded">
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
                                <i class="fa-solid fa-check text-success"></i> ${escapeHtml(item)}
                            </span>
                        `).join("")}
                    </div>
                </div>
            `
            : "";

        const promotionsHtml = trip.promotions && trip.promotions.length
            ? `
                <div class="mt-3">
                    <strong>Khuyến mãi:</strong>
                    <div class="d-grid gap-2 mt-2">
                        ${trip.promotions.map(item => `
                            <div class="border rounded-3 p-2 bg-light">
                                <div class="fw-semibold">${escapeHtml(item.name)}</div>
                                <div class="text-muted small">${escapeHtml(item.detail)}</div>
                            </div>
                        `).join("")}
                    </div>
                </div>
            `
            : "";

        tripDetail.innerHTML = `
            <h4 class="fw-bold">${escapeHtml(trip.company || "Đang cập nhật")}</h4>

            <p class="text-muted mb-2">
                ${escapeHtml(trip.busType || "Đang cập nhật")}
                ${trip.bienSo ? ` - ${escapeHtml(trip.bienSo)}` : ""}
            </p>

            <div class="d-flex flex-wrap gap-2 mb-3">
                <span class="badge bg-secondary">
                    <i class="fa-solid fa-circle-info"></i>
                    ${escapeHtml(trip.status || "Đang cập nhật")}
                </span>
            </div>

            ${
                trip.rating
                    ? `<p class="mb-2">
                        <span class="badge bg-primary">
                            <i class="fa-solid fa-star"></i>
                            ${escapeHtml(trip.rating)} (${escapeHtml(trip.reviewCount)})
                        </span>
                    </p>`
                    : ""
            }

            ${imageHtml}

            <hr>

            <p><strong>Mã chuyến:</strong> ${escapeHtml(trip.id)}</p>
            <p><strong>Giờ đi:</strong> ${escapeHtml(trip.time || "--:--")} - ${escapeHtml(formatShortDate(trip.date))}</p>
            <p><strong>Giờ đến:</strong> ${escapeHtml(trip.arrivalTime || "--:--")}</p>
            <p><strong>Điểm đi:</strong> ${escapeHtml(trip.startStation || trip.from || "Đang cập nhật")}</p>
            <p><strong>Điểm đến:</strong> ${escapeHtml(trip.endStation || trip.to || "Đang cập nhật")}</p>
            <p><strong>Thời gian:</strong> ${escapeHtml(trip.duration || "Đang cập nhật")}</p>
            <p><strong>Ghế trống:</strong> ${escapeHtml(trip.emptySeats)}/${escapeHtml(trip.totalSeats || "?")}</p>
            <p><strong>Ghi chú:</strong> ${escapeHtml(trip.note || "Vui lòng có mặt trước giờ khởi hành ít nhất 15 phút.")}</p>
            <p>
                <strong>Giá vé:</strong>
                <span class="text-danger fw-bold">${moneySafe(trip.price)}</span>
            </p>

            ${amenitiesHtml}
            ${promotionsHtml}
        `;
    }

    function renderPickupDropoffOptions(stopData, trip) {
        const pickupPoints = stopData?.diemDon?.length ? stopData.diemDon : trip.diemDon;
        const dropoffPoints = stopData?.diemTra?.length ? stopData.diemTra : trip.diemTra;

        renderPointSelect(pickupPointSelect, pickupPoints, "Chọn điểm đón", trip.startStation || trip.from, "PICKUP");
        renderPointSelect(dropoffPointSelect, dropoffPoints, "Chọn điểm trả", trip.endStation || trip.to, "DROPOFF");

        updateSummary();
    }

    function renderPointSelect(selectEl, points, placeholder, fallbackName, fallbackType) {
        if (!selectEl) return;

        let finalPoints = Array.isArray(points) ? [...points] : [];

        finalPoints.sort((a, b) => Number(a.thuTu || 0) - Number(b.thuTu || 0));

        if (!finalPoints.length && fallbackName) {
            finalPoints = [{
                maDiem: fallbackType,
                tenDiem: fallbackName,
                thoiGian: "",
                diaChi: "",
                thuTu: 1
            }];
        }

        if (!finalPoints.length) {
            selectEl.innerHTML = `<option value="">Không có dữ liệu</option>`;
            return;
        }

        selectEl.innerHTML = `
            <option value="">-- ${placeholder} --</option>
            ${finalPoints.map(point => {
                const tenDiem = point.tenDiem || "Không rõ điểm";
                const diaChi = point.diaChi || "";
                const thoiGian = point.thoiGian || "";

                const label = [
                    tenDiem,
                    diaChi,
                    thoiGian ? formatPointTime(thoiGian) : ""
                ].filter(Boolean).join(" - ");

                return `
                    <option
                        value="${escapeHtml(point.maDiem || point.tenDiem || "")}"
                        data-code="${escapeHtml(point.maDiem || "")}"
                        data-name="${escapeHtml(tenDiem)}"
                        data-time="${escapeHtml(thoiGian)}"
                        data-address="${escapeHtml(diaChi)}"
                    >
                        ${escapeHtml(label)}
                    </option>
                `;
            }).join("")}
        `;

        if (finalPoints.length === 1) {
            selectEl.selectedIndex = 1;
        }

        selectEl.removeEventListener("change", updateSummary);
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
            const status = normalizeSeatStatus(seat.trangThai || "TRONG");

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
                    data-ma-ghe="${escapeHtml(seat.maGhe || "")}"
                    data-seat="${escapeHtml(seat.soGhe || "")}"
                    data-status="${escapeHtml(status)}"
                    title="${escapeHtml(mapSeatStatusText(status))}"
                    ${disabled}
                >
                    ${escapeHtml(seat.soGhe || "")}
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

            if (!soGhe || !maGhe) {
                alert("Không xác định được mã ghế.");
                return;
            }

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
        continueBookingBtn.addEventListener("click", async function () {
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

            const maGhes = selectedSeatData
                .map(item => item.maGhe)
                .filter(Boolean);

            if (!maGhes.length) {
                alert("Không xác định được danh sách ghế để tạo đặt vé.");
                return;
            }

            const maKhachHang = await resolveCurrentCustomerId();

            if (!maKhachHang) {
                alert("Bạn cần đăng nhập tài khoản khách hàng trước khi đặt vé.");
                window.location.href = "login.html";
                return;
            }

            if (!pickup.id || pickup.id === "PICKUP") {
                alert("Điểm đón không hợp lệ. Vui lòng chọn điểm đón từ dữ liệu chuyến xe.");
                return;
            }

            if (!dropoff.id || dropoff.id === "DROPOFF") {
                alert("Điểm trả không hợp lệ. Vui lòng chọn điểm trả từ dữ liệu chuyến xe.");
                return;
            }

            const generatedMaDatVe = generateMaDatVe();
            const originalBtnText = continueBookingBtn.textContent;

            try {
                continueBookingBtn.disabled = true;
                continueBookingBtn.textContent = "Đang tạo đặt vé...";

                const response = await fetch(`${API_BASE_URL}/api/dat-ve/create`, {
                    method: "POST",
                    headers: buildAuthHeaders(),
                    body: JSON.stringify({
                        maDatVe: generatedMaDatVe,
                        maChuyen: currentTrip.id,
                        maKhachHang: maKhachHang,
                        maDiemDon: pickup.id,
                        maDiemTra: dropoff.id,
                        maGhes: maGhes
                    })
                });

                const result = await response.json().catch(() => null);

                if (!response.ok || result?.success === false) {
                    throw new Error(result?.message || "Không tạo được đơn đặt vé.");
                }

                const data = result?.data || result || {};
                const createdMaDatVe = data.maDatVe || generatedMaDatVe;
                const veList = Array.isArray(data.veList) ? data.veList : [];
                const totalFromServer = Number(data.tongTien || 0);

                const bookingData = {
                    trip: currentTrip,
                    seats: selectedSeatData,
                    tickets: veList,
                    pickup,
                    dropoff,
                    totalPrice: totalFromServer > 0 ? totalFromServer : selectedSeats.length * Number(currentTrip?.price || 0),
                    maDatVe: createdMaDatVe,
                    maKhachHang: maKhachHang,
                    trangThai: data.trangThai || "Chờ thanh toán"
                };

                sessionStorage.setItem("maDatVe", createdMaDatVe);
                sessionStorage.setItem("maKhachHang", maKhachHang);
                sessionStorage.setItem("maKH", maKhachHang);

                localStorage.setItem("maKhachHang", maKhachHang);
                localStorage.setItem("maKH", maKhachHang);

                sessionStorage.setItem("bookingTrip", JSON.stringify(currentTrip));
                sessionStorage.setItem("bookingSeats", JSON.stringify(selectedSeatData));
                sessionStorage.setItem("bookingTickets", JSON.stringify(veList));
                sessionStorage.setItem("bookingPickup", JSON.stringify(pickup));
                sessionStorage.setItem("bookingDropoff", JSON.stringify(dropoff));
                sessionStorage.setItem("bookingData", JSON.stringify(bookingData));

                const query = new URLSearchParams({
                    tripId: currentTrip.id,
                    seats: selectedSeats.join(","),
                    maGhe: maGhes.join(","),
                    pickupId: pickup.id,
                    dropoffId: dropoff.id,
                    maDatVe: createdMaDatVe
                });

                window.location.href = "checkout.html?" + query.toString();
            } catch (error) {
                console.error("Không tạo được đặt vé:", error);
                alert(error.message || "Không tạo được đơn đặt vé. Vui lòng thử lại.");
            } finally {
                continueBookingBtn.disabled = false;
                continueBookingBtn.textContent = originalBtnText;
            }
        });
    }

    function getSelectedPoint(selectEl) {
        if (!selectEl || !selectEl.value) return null;

        const option = selectEl.options[selectEl.selectedIndex];

        return {
            id: option?.dataset?.code || selectEl.value,
            code: option?.dataset?.code || "",
            name: option?.dataset?.name || option?.textContent?.trim() || selectEl.value,
            time: option?.dataset?.time || "",
            address: option?.dataset?.address || ""
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

    function mapTripToView(item) {
        const thoiGianKhoiHanh = item.thoiGianKhoiHanh || item.departureTime || item.gioKhoiHanh || "";
        const thoiGianDen = item.thoiGianDen || item.arrivalTimeFull || item.gioDenDuKien || "";

        const date = item.ngayDi || item.date || getDateFromApi(thoiGianKhoiHanh);
        const time = item.gioDi || item.time || getTimeFromApi(thoiGianKhoiHanh);
        const arrivalTime = item.gioDen || item.arrivalTime || getTimeFromApi(thoiGianDen);

        return {
            id: item.maChuyen || item.id || tripId,

            company: item.tenNhaXe || item.nhaXe || item.company || "Đang cập nhật",
            busType: item.tenLoaiXe || item.loaiXe || item.busType || "Đang cập nhật",
            bienSo: item.bienSo || "",

            from: item.diemDi || item.tenBenDi || item.benDi || item.from || "Đang cập nhật",
            to: item.diemDen || item.tenBenDen || item.benDen || item.to || "Đang cập nhật",

            date: date || "",
            time: time || "--:--",
            arrivalTime: arrivalTime || "--:--",
            arrivalDate: item.ngayDen || item.arrivalDate || getDateFromApi(thoiGianDen) || date || "",

            startStation: item.diemDi || item.tenBenDi || item.benDi || item.startStation || item.from || "Đang cập nhật",
            endStation: item.diemDen || item.tenBenDen || item.benDen || item.endStation || item.to || "Đang cập nhật",

            duration: item.duration || buildDurationText(item),

            price: Number(item.giaVe || item.price || 0),
            emptySeats: Number(item.soGheTrong || item.gheTrong || item.availableSeats || item.emptySeats || 0),
            totalSeats: Number(item.soLuongGhe || item.tongGhe || item.totalSeats || 0),

            images: normalizeStringList(item.images || item.imageUrls || item.image),
            amenities: normalizeStringList(item.amenities || item.tienIch),

            diemDon: normalizePointList(item.diemDon),
            diemTra: normalizePointList(item.diemTra),

            rating: Number(item.rating || 0),
            reviewCount: Number(item.reviewCount || 0),

            status: item.trangThai || item.status || "Đang cập nhật",

            promotions: normalizePromotionList(item.khuyenMai),

            note: item.note || item.ghiChu || "Vui lòng có mặt trước giờ khởi hành ít nhất 15 phút."
        };
    }

    function normalizePointList(value) {
        if (!Array.isArray(value)) return [];

        return value.map((item, index) => ({
            maDiem:
                item.maDiem ||
                item.id ||
                item.stationId ||
                item.maBen ||
                item.code ||
                "",

            tenDiem:
                item.tenDiem ||
                item.name ||
                item.tenBen ||
                item.diaChi ||
                `Điểm ${index + 1}`,

            thoiGian:
                item.thoiGian ||
                item.time ||
                item.gio ||
                "",

            diaChi:
                item.diaChi ||
                item.address ||
                "",

            loai:
                item.loai ||
                item.type ||
                "",

            thuTu:
                Number(item.thuTu || item.orderNo || index + 1)
        }));
    }

    function normalizeStringList(value) {
        if (Array.isArray(value)) {
            return value.filter(Boolean);
        }

        if (typeof value === "string") {
            return value.split("||").map(item => item.trim()).filter(Boolean);
        }

        return [];
    }

    function normalizePromotionList(value) {
        if (!Array.isArray(value)) return [];

        return value.map(item => {
            const parts = [];

            if (item.phanTramGiam) {
                parts.push(`Giảm ${item.phanTramGiam}%`);
            }

            if (item.soTienGiam) {
                parts.push(`Giảm ${moneySafe(item.soTienGiam)}`);
            }

            if (item.ngayBatDau || item.ngayKetThuc) {
                parts.push(`${formatPromotionDate(item.ngayBatDau)} - ${formatPromotionDate(item.ngayKetThuc)}`);
            }

            return {
                name: item.tenKhuyenMai || item.name || item.maKhuyenMai || "Khuyến mãi",
                detail: parts.length ? parts.join(" | ") : "Đang áp dụng"
            };
        });
    }

    function normalizeSeatStatus(status) {
        const text = String(status || "")
            .trim()
            .toUpperCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");

        if (
            text === "TRONG" ||
            text === "EMPTY" ||
            text === "AVAILABLE"
        ) {
            return "TRONG";
        }

        if (
            text === "DANG_GIU" ||
            text === "GIU_CHO" ||
            text === "GIU CHO" ||
            text === "HOLDING"
        ) {
            return "DANG_GIU";
        }

        if (
            text === "DA_DAT" ||
            text === "DA DAT" ||
            text === "BOOKED" ||
            text === "DA_THANH_TOAN" ||
            text === "DA THANH TOAN" ||
            text === "DA DUNG"
        ) {
            return "DA_DAT";
        }

        return text || "TRONG";
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
                    ${escapeHtml(message)}
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

            from: "Đang cập nhật",
            to: "Đang cập nhật",

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
        const keys = [
            "tripSearchResults",
            "searchResults",
            "currentSearchResults",
            "lastSearchResults"
        ];

        for (const key of keys) {
            try {
                const raw = sessionStorage.getItem(key) || localStorage.getItem(key);

                if (!raw) continue;

                const parsed = JSON.parse(raw);
                const list = extractArray(parsed);

                const found = list.find(item =>
                    String(item.maChuyen || item.id || "") === String(maChuyen)
                );

                if (found) {
                    return mapTripToView(found);
                }
            } catch (error) {
                console.warn(`Không đọc được ${key}:`, error);
            }
        }

        return null;
    }

    function findTripFromBookingSession(maChuyen) {
        try {
            const raw = sessionStorage.getItem("bookingTrip");

            if (!raw) return null;

            const parsed = JSON.parse(raw);

            if (String(parsed.maChuyen || parsed.id || "") !== String(maChuyen)) {
                return null;
            }

            return mapTripToView(parsed);
        } catch (error) {
            console.warn("Không đọc được bookingTrip:", error);
            return null;
        }
    }

    function findTripFromLocalData(maChuyen) {
        if (typeof BUS_TRIPS === "undefined" || !Array.isArray(BUS_TRIPS)) {
            return null;
        }

        const found = BUS_TRIPS.find(item =>
            String(item.maChuyen || item.id || "") === String(maChuyen)
        );

        if (!found) return null;

        return mapTripToView(found);
    }

    function buildAuthHeaders() {
        const headers = {
            "Content-Type": "application/json"
        };

        const token = localStorage.getItem("token") || sessionStorage.getItem("token");
        const maTK = localStorage.getItem("maTK") || sessionStorage.getItem("maTK");

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        if (maTK) {
            headers["X-MaTK"] = maTK;
        }

        return headers;
    }

    async function resolveCurrentCustomerId() {
        const cachedMaKhachHang =
            localStorage.getItem("maKhachHang") ||
            localStorage.getItem("maKH") ||
            localStorage.getItem("maKh") ||
            sessionStorage.getItem("maKhachHang") ||
            sessionStorage.getItem("maKH") ||
            sessionStorage.getItem("maKh");

        if (cachedMaKhachHang) {
            return cachedMaKhachHang;
        }

        const maTK = localStorage.getItem("maTK") || sessionStorage.getItem("maTK");

        if (!maTK) {
            return "";
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/account/${encodeURIComponent(maTK)}`, {
                method: "GET",
                headers: buildAuthHeaders()
            });

            const result = await response.json().catch(() => null);

            if (!response.ok) {
                console.warn("Không lấy được thông tin tài khoản:", result);
                return "";
            }

            const data = result?.data || result || {};

            const maKhachHang =
                data.maKhachHang ||
                data.maKH ||
                data.maKh ||
                data.khachHang?.maKhachHang ||
                data.khachHang?.maKH ||
                data.khachHang?.maKh ||
                "";

            if (maKhachHang) {
                localStorage.setItem("maKhachHang", maKhachHang);
                localStorage.setItem("maKH", maKhachHang);
                sessionStorage.setItem("maKhachHang", maKhachHang);
                sessionStorage.setItem("maKH", maKhachHang);
            }

            return maKhachHang;
        } catch (error) {
            console.warn("Lỗi lấy mã khách hàng:", error);
            return "";
        }
    }

    function extractArray(result) {
        if (Array.isArray(result)) return result;
        if (result && Array.isArray(result.data)) return result.data;
        if (result && Array.isArray(result.result)) return result.result;
        if (result && Array.isArray(result.content)) return result.content;
        return [];
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

        if (text.includes(" ")) {
            return text.split(" ")[1].substring(0, 5);
        }

        return text.substring(0, 5);
    }

    function getDateFromApi(value) {
        if (!value) return "";

        const text = String(value);

        if (text.includes("T")) {
            return text.split("T")[0];
        }

        if (text.includes(" ")) {
            return text.split(" ")[0];
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

    function generateMaDatVe() {
        return "DV" + Date.now();
    }

    function formatPointTime(value) {
        if (!value) return "";

        const text = String(value);

        if (text.includes("T")) {
            return text.split("T")[1].substring(0, 5);
        }

        if (text.includes(" ")) {
            return text.split(" ")[1].substring(0, 5);
        }

        return text.substring(0, 5);
    }

    function formatPromotionDate(value) {
        if (!value) return "";

        const date = new Date(value);

        if (Number.isNaN(date.getTime())) {
            return String(value);
        }

        return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}/${date.getFullYear()}`;
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

    function escapeHtml(value) {
        return String(value ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }
});