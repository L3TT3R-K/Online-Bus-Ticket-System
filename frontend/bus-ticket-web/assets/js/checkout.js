const API_BASE_URL = "http://localhost:8080";

const state = {
    tripId: "",
    maDatVe: "",
    maKhachHang: "",
    maKhuyenMai: "",
    seatNos: [],
    seatIds: [],
    selectedSeatDetails: [],
    tickets: [],
    pickupId: "",
    dropoffId: "",
    pickup: null,
    dropoff: null,
    trip: null,
    promotions: [],
    selectedPromotion: null,
    ticketTypes: [],
    selectedTicketType: null,
    maLoaiVe: "",
    paymentMethod: "QR chuyển khoản / Ví điện tử",
    ticketPrice: 0,
    discount: 0,
    totalPrice: 0
};

document.addEventListener("DOMContentLoaded", async function () {
    hydrateStateFromStorageAndQuery();
    bindStaticEvents();
    await loadContactInfoForCheckout();

    try {
        await loadCheckoutData();
        await loadTicketTypesForCheckout();
        await loadPromotionsForCheckout();
        renderCheckout();
    } catch (error) {
        console.error("Lỗi tải checkout:", error);
        alert("Không thể tải dữ liệu checkout. Vui lòng quay lại chọn chuyến.");
    }
});

function hydrateStateFromStorageAndQuery() {
    const bookingDataRaw = sessionStorage.getItem("bookingData");
    const bookingPickupRaw = sessionStorage.getItem("bookingPickup");
    const bookingDropoffRaw = sessionStorage.getItem("bookingDropoff");
    const bookingSeatsRaw = sessionStorage.getItem("bookingSeats");
    const bookingTicketsRaw = sessionStorage.getItem("bookingTickets");

    const query = new URLSearchParams(window.location.search);

    state.tripId = query.get("tripId") || "";

    if (bookingDataRaw) {
        try {
            const bookingData = JSON.parse(bookingDataRaw);

            state.maDatVe = String(
                query.get("maDatVe") ||
                bookingData?.maDatVe ||
                bookingData?.datVe?.maDatVe ||
                bookingData?.booking?.maDatVe ||
                sessionStorage.getItem("maDatVe") ||
                localStorage.getItem("maDatVe") ||
                ""
            ).trim();

            state.maKhachHang = String(
                bookingData?.maKhachHang ||
                bookingData?.maKH ||
                bookingData?.maKh ||
                sessionStorage.getItem("maKhachHang") ||
                sessionStorage.getItem("maKH") ||
                sessionStorage.getItem("maKh") ||
                localStorage.getItem("maKhachHang") ||
                localStorage.getItem("maKH") ||
                localStorage.getItem("maKh") ||
                ""
            ).trim();

            state.maKhuyenMai = String(
                query.get("maKhuyenMai") ||
                bookingData?.maKhuyenMai ||
                bookingData?.khuyenMai?.maKhuyenMai ||
                sessionStorage.getItem("maKhuyenMai") ||
                localStorage.getItem("maKhuyenMai") ||
                ""
            ).trim();

            state.maLoaiVe = String(
                query.get("maLoaiVe") ||
                bookingData?.maLoaiVe ||
                bookingData?.loaiVe?.maLoaiVe ||
                sessionStorage.getItem("maLoaiVe") ||
                localStorage.getItem("maLoaiVe") ||
                ""
            ).trim();

            if (!state.tripId && bookingData?.trip?.id) {
                state.tripId = String(bookingData.trip.id);
            }

            if (Array.isArray(bookingData?.seats)) {
                state.selectedSeatDetails = bookingData.seats;
                state.seatNos = bookingData.seats.map(item => String(item.soGhe || "")).filter(Boolean);
                state.seatIds = bookingData.seats.map(item => String(item.maGhe || "")).filter(Boolean);
            }

            if (Array.isArray(bookingData?.tickets)) {
                state.tickets = bookingData.tickets;
            }

            if (bookingData?.totalPrice) {
                state.totalPrice = Number(bookingData.totalPrice || 0);
            }
        } catch (error) {
            console.warn("Không đọc được bookingData:", error);
        }
    }

    if (!state.selectedSeatDetails.length && bookingSeatsRaw) {
        try {
            const seats = JSON.parse(bookingSeatsRaw);

            if (Array.isArray(seats)) {
                state.selectedSeatDetails = seats;
                state.seatNos = seats.map(item => String(item.soGhe || "")).filter(Boolean);
                state.seatIds = seats.map(item => String(item.maGhe || "")).filter(Boolean);
            }
        } catch (error) {
            console.warn("Không đọc được bookingSeats:", error);
        }
    }

    if (!state.tickets.length && bookingTicketsRaw) {
        try {
            const tickets = JSON.parse(bookingTicketsRaw);

            if (Array.isArray(tickets)) {
                state.tickets = tickets;
            }
        } catch (error) {
            console.warn("Không đọc được bookingTickets:", error);
        }
    }

    if (!state.maDatVe) {
        state.maDatVe = String(
            query.get("maDatVe") ||
            sessionStorage.getItem("maDatVe") ||
            localStorage.getItem("maDatVe") ||
            ""
        ).trim();
    }

    if (!state.maKhachHang) {
        state.maKhachHang = String(
            sessionStorage.getItem("maKhachHang") ||
            sessionStorage.getItem("maKH") ||
            sessionStorage.getItem("maKh") ||
            localStorage.getItem("maKhachHang") ||
            localStorage.getItem("maKH") ||
            localStorage.getItem("maKh") ||
            ""
        ).trim();
    }

    if (!state.maKhuyenMai) {
        state.maKhuyenMai = String(
            query.get("maKhuyenMai") ||
            sessionStorage.getItem("maKhuyenMai") ||
            localStorage.getItem("maKhuyenMai") ||
            ""
        ).trim();
    }

    if (!state.maLoaiVe) {
        state.maLoaiVe = String(
            query.get("maLoaiVe") ||
            sessionStorage.getItem("maLoaiVe") ||
            localStorage.getItem("maLoaiVe") ||
            ""
        ).trim();
    }

    if (!state.seatNos.length) {
        state.seatNos = splitCsv(query.get("seats"));
    }

    if (!state.seatIds.length) {
        state.seatIds = splitCsv(query.get("maGhe"));
    }

    state.pickupId = query.get("pickupId") || "";
    state.dropoffId = query.get("dropoffId") || "";

    if (bookingPickupRaw) {
        try {
            state.pickup = JSON.parse(bookingPickupRaw);
        } catch (error) {
            console.warn("Không đọc được bookingPickup:", error);
        }
    }

    if (bookingDropoffRaw) {
        try {
            state.dropoff = JSON.parse(bookingDropoffRaw);
        } catch (error) {
            console.warn("Không đọc được bookingDropoff:", error);
        }
    }
}

function bindStaticEvents() {
    document.querySelectorAll(".payment-option").forEach(option => {
        option.addEventListener("click", function () {
            document.querySelectorAll(".payment-option").forEach(item => item.classList.remove("active"));
            this.classList.add("active");
            state.paymentMethod = this.dataset.payment || state.paymentMethod;
        });
    });

    const toggleSummaryBtn = document.getElementById("toggleSummaryBtn");
    const summaryDetail = document.getElementById("summaryDetail");

    if (toggleSummaryBtn && summaryDetail) {
        toggleSummaryBtn.addEventListener("click", function () {
            summaryDetail.classList.toggle("d-none");
            const icon = this.querySelector("i");

            if (icon) {
                icon.classList.toggle("fa-chevron-up");
                icon.classList.toggle("fa-chevron-down");
            }
        });
    }

    const payBtn = document.getElementById("payBtn");

    if (payBtn) {
        payBtn.addEventListener("click", onClickPay);
    }

    const applyPromotionBtn = document.getElementById("applyPromotionBtn");

    if (applyPromotionBtn) {
        applyPromotionBtn.addEventListener("click", applySelectedPromotion);
    }

    const ticketTypeSelect = document.getElementById("ticketTypeSelect");

    if (ticketTypeSelect) {
        ticketTypeSelect.addEventListener("change", applySelectedTicketType);
    }
}

async function loadContactInfoForCheckout() {
    const customerNameEl = document.getElementById("customerName");
    const customerPhoneEl = document.getElementById("customerPhone");
    const customerEmailEl = document.getElementById("customerEmail");
    const loginReminderEl = document.querySelector(".login-reminder");

    if (customerNameEl) customerNameEl.value = "";
    if (customerPhoneEl) customerPhoneEl.value = "";
    if (customerEmailEl) customerEmailEl.value = "";

    const profile = await getCurrentAccountProfile().catch(function (error) {
        console.warn("Không thể tải hồ sơ hiện tại:", error);
        return null;
    });

    if (!profile) {
        if (loginReminderEl) {
            loginReminderEl.classList.remove("d-none");
        }
        return;
    }

    try {
        const account = await loadAccountByMaTK();

        if (customerNameEl) {
            customerNameEl.value = account.tenKH || account.hoTen || account.tenKhachHang || "";
        }

        if (customerPhoneEl) {
            customerPhoneEl.value = account.sdt || account.soDienThoai || "";
        }

        if (customerEmailEl) {
            customerEmailEl.value = account.email || "";
        }

        const maKhachHang =
            account.maKhachHang ||
            account.maKH ||
            account.maKh ||
            account.khachHang?.maKhachHang ||
            account.khachHang?.maKH ||
            account.khachHang?.maKh ||
            "";

        if (maKhachHang) {
            state.maKhachHang = maKhachHang;
            localStorage.setItem("maKhachHang", maKhachHang);
            localStorage.setItem("maKH", maKhachHang);
            sessionStorage.setItem("maKhachHang", maKhachHang);
            sessionStorage.setItem("maKH", maKhachHang);
        }

        if (loginReminderEl) {
            loginReminderEl.classList.add("d-none");
        }
    } catch (error) {
        console.warn("Không thể tự điền thông tin tài khoản:", error);

        if (loginReminderEl) {
            loginReminderEl.classList.remove("d-none");
        }
    }
}

async function loadCheckoutData() {
    if (!state.tripId) {
        throw new Error("Thiếu mã chuyến xe.");
    }

    const [trip, stopData, seats] = await Promise.all([
        loadTripDetail(state.tripId),
        loadDiemDonTra(state.tripId),
        loadSeatMap(state.tripId)
    ]);

    state.trip = trip;

    if (stopData?.diemDon?.length) {
        const matchedPickup = stopData.diemDon.find(item => String(item.maDiem || "") === String(state.pickupId || ""));
        state.pickup = matchedPickup || state.pickup || stopData.diemDon[0];
    }

    if (stopData?.diemTra?.length) {
        const matchedDropoff = stopData.diemTra.find(item => String(item.maDiem || "") === String(state.dropoffId || ""));
        state.dropoff = matchedDropoff || state.dropoff || stopData.diemTra[stopData.diemTra.length - 1];
    }

    const seatMap = new Map((seats || []).map(item => [String(item.maGhe || item.soGhe || ""), item]));

    if (!state.maDatVe) {
        const unavailableSeats = [];

        state.seatIds.forEach(id => {
            const matched = seatMap.get(String(id));

            if (!matched) return;

            const status = normalizeSeatStatus(matched.trangThai || matched.status);

            if (status !== "TRONG") {
                unavailableSeats.push(matched.soGhe || id);
            }
        });

        if (unavailableSeats.length) {
            alert(`Các ghế đã không còn trống: ${unavailableSeats.join(", ")}. Vui lòng chọn lại.`);
            window.location.href = `trip-detail.html?id=${encodeURIComponent(state.tripId)}`;
            return;
        }
    }

    if (!state.seatNos.length && Array.isArray(state.selectedSeatDetails) && state.selectedSeatDetails.length) {
        state.seatNos = state.selectedSeatDetails.map(item => String(item.soGhe || "")).filter(Boolean);
    }

    updateTicketPrice();
    recalculateTotals();
}

async function loadTicketTypesForCheckout() {
    const ticketTypeSelect = document.getElementById("ticketTypeSelect");

    if (!ticketTypeSelect) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/loai-ve/active`, {
            method: "GET",
            headers: buildAuthHeaders()
        });

        const result = await response.json().catch(() => null);

        if (!response.ok) {
            state.ticketTypes = [];
            renderTicketTypeOptions([]);
            setTicketTypeMessage(result?.message || "Không thể tải danh sách loại vé.", "error");
            return;
        }

        state.ticketTypes = extractArray(result);
        renderTicketTypeOptions(state.ticketTypes);

        if (state.maLoaiVe) {
            ticketTypeSelect.value = state.maLoaiVe;
        }

        applySelectedTicketType();
    } catch (error) {
        console.warn("Không thể tải loại vé:", error);
        state.ticketTypes = [];
        renderTicketTypeOptions([]);
        setTicketTypeMessage("Không thể kết nối server để tải loại vé.", "error");
    }
}

async function loadPromotionsForCheckout() {
    const promotionSelect = document.getElementById("promotionSelect");

    if (!promotionSelect) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/khuyen-mai/active`, {
            method: "GET",
            headers: buildAuthHeaders()
        });

        const result = await response.json().catch(() => null);

        if (!response.ok) {
            state.promotions = [];
            renderPromotionOptions([]);
            setPromotionMessage(result?.message || "Không thể tải danh sách khuyến mãi.", "error");
            return;
        }

        const activePromotions = extractArray(result);
        const tripPromotions = Array.isArray(state.trip?.khuyenMai) ? state.trip.khuyenMai : [];

        state.promotions = mergePromotions(tripPromotions, activePromotions);
        renderPromotionOptions(state.promotions);

        if (state.maKhuyenMai) {
            promotionSelect.value = state.maKhuyenMai;
            applySelectedPromotion();
        }
    } catch (error) {
        console.warn("Không thể tải khuyến mãi:", error);
        state.promotions = [];
        renderPromotionOptions([]);
        setPromotionMessage("Không thể kết nối server để tải khuyến mãi.", "error");
    }
}

function renderCheckout() {
    const trip = state.trip || {};

    setText("summaryTripDate", formatTripDateLabel(trip.date));
    setText("summaryCompany", trip.company || "Đang cập nhật");
    setText("summaryBusType", trip.busType || "Đang cập nhật");
    setText("summarySeatCount", `${state.seatNos.length}`);
    setText("summarySeatList", state.seatNos.length ? state.seatNos.join(", ") : "Chưa chọn");

    setText("summaryPickupTime", trip.time || "--:--");
    setText("summaryPickupDate", `(${formatShortDate(trip.date)})`);
    setText("summaryPickupName", state.pickup?.tenDiem || state.pickup?.name || trip.startStation || trip.from || "Đang cập nhật");
    setText("summaryPickupAddress", state.pickup?.diaChi || state.pickup?.address || "Đang cập nhật");

    setText("summaryDropoffTime", trip.arrivalTime || "--:--");
    setText("summaryDropoffDate", `(${formatShortDate(trip.arrivalDate || trip.date)})`);
    setText("summaryDropoffName", state.dropoff?.tenDiem || state.dropoff?.name || trip.endStation || trip.to || "Đang cập nhật");
    setText("summaryDropoffAddress", state.dropoff?.diaChi || state.dropoff?.address || "Đang cập nhật");

    setText("ticketPrice", money(state.ticketPrice));
    setText("discountValue", `-${money(state.discount)}`);
    setText("finalTotal", money(state.totalPrice));
    setText("summaryTotalTop", money(state.totalPrice));

    const tripImage = document.getElementById("summaryTripImage");

    if (tripImage && Array.isArray(trip.images) && trip.images[0]) {
        tripImage.src = trip.images[0];
    }

    const tripDetailLink = `trip-detail.html?id=${encodeURIComponent(state.tripId)}`;
    const summaryTripLink = document.getElementById("summaryTripLink");
    const summaryPickupChange = document.getElementById("summaryPickupChange");
    const summaryDropoffChange = document.getElementById("summaryDropoffChange");

    if (summaryTripLink) summaryTripLink.href = tripDetailLink;
    if (summaryPickupChange) summaryPickupChange.href = tripDetailLink;
    if (summaryDropoffChange) summaryDropoffChange.href = tripDetailLink;
}

function renderPromotionOptions(promotions) {
    const promotionSelect = document.getElementById("promotionSelect");

    if (!promotionSelect) return;

    const currentValue = state.maKhuyenMai || promotionSelect.value || "";
    const options = [
        `<option value="">Không áp dụng</option>`,
        ...promotions.map(promotion => {
            const code = promotion.maKhuyenMai || "";
            const label = `${code} - ${promotion.tenKhuyenMai || "Khuyến mãi"} (${promotionDiscountText(promotion)})`;
            return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
        })
    ];

    promotionSelect.innerHTML = options.join("");
    promotionSelect.value = currentValue;
}

function renderTicketTypeOptions(ticketTypes) {
    const ticketTypeSelect = document.getElementById("ticketTypeSelect");

    if (!ticketTypeSelect) return;

    if (!Array.isArray(ticketTypes) || !ticketTypes.length) {
        ticketTypeSelect.innerHTML = `<option value="">Không có loại vé hoạt động</option>`;
        ticketTypeSelect.value = "";
        return;
    }

    const currentValue = state.maLoaiVe || ticketTypeSelect.value || "";
    ticketTypeSelect.innerHTML = ticketTypes.map(ticketType => {
        const code = ticketType.maLoaiVe || "";
        const heSoGia = Number(ticketType.heSoGia || 1);
        const label = `${ticketType.tenLoaiVe || code} x${formatMultiplier(heSoGia)}`;
        return `<option value="${escapeHtml(code)}">${escapeHtml(label)}</option>`;
    }).join("");

    if (currentValue && ticketTypes.some(item => String(item.maLoaiVe || "") === currentValue)) {
        ticketTypeSelect.value = currentValue;
    } else {
        ticketTypeSelect.selectedIndex = 0;
    }
}

function applySelectedTicketType() {
    const ticketTypeSelect = document.getElementById("ticketTypeSelect");
    const selectedCode = ticketTypeSelect?.value || "";
    const ticketType = state.ticketTypes.find(item => String(item.maLoaiVe || "") === selectedCode) || null;

    state.maLoaiVe = selectedCode;
    state.selectedTicketType = ticketType;

    if (selectedCode) {
        sessionStorage.setItem("maLoaiVe", selectedCode);
        localStorage.setItem("maLoaiVe", selectedCode);
    } else {
        sessionStorage.removeItem("maLoaiVe");
        localStorage.removeItem("maLoaiVe");
    }

    updateTicketPrice();
    recalculateTotals();
    renderCheckout();

    if (ticketType) {
        setTicketTypeMessage(`${ticketType.tenLoaiVe || selectedCode}: hệ số giá x${formatMultiplier(ticketType.heSoGia || 1)}.`, "success");
    } else {
        setTicketTypeMessage("Vui lòng chọn loại vé đang hoạt động.", "error");
    }
}

function applySelectedPromotion() {
    const promotionSelect = document.getElementById("promotionSelect");
    const selectedCode = promotionSelect?.value || "";

    if (!selectedCode) {
        state.maKhuyenMai = "";
        state.selectedPromotion = null;
        sessionStorage.removeItem("maKhuyenMai");
        localStorage.removeItem("maKhuyenMai");
        recalculateTotals();
        renderCheckout();
        setPromotionMessage("Chưa áp dụng khuyến mãi.", "muted");
        return;
    }

    const promotion = state.promotions.find(item => String(item.maKhuyenMai || "") === selectedCode);

    if (!promotion) {
        setPromotionMessage("Mã khuyến mãi không hợp lệ.", "error");
        return;
    }

    state.maKhuyenMai = selectedCode;
    state.selectedPromotion = promotion;
    sessionStorage.setItem("maKhuyenMai", selectedCode);
    localStorage.setItem("maKhuyenMai", selectedCode);
    recalculateTotals();
    renderCheckout();
    setPromotionMessage(`Đã áp dụng ${selectedCode}: giảm ${money(state.discount)}.`, "success");
}

async function onClickPay() {
    const form = document.getElementById("checkoutForm");
    const payBtn = document.getElementById("payBtn");

    if (!form || !form.checkValidity()) {
        form?.reportValidity();
        return;
    }

    setPayButtonLoading(payBtn, true);

    try {
        if (!state.maKhachHang) {
            state.maKhachHang = await resolveCurrentCustomerId();
        }

        if (!state.maKhachHang) {
            throw new Error("Bạn cần đăng nhập tài khoản khách hàng trước khi thanh toán.");
        }

        if (!state.maDatVe) {
            if (!state.seatIds.length) {
                throw new Error("Thiếu danh sách ghế để tạo đặt vé.");
            }

            if (!state.maLoaiVe) {
                throw new Error("Vui lòng chọn loại vé.");
            }

            const generatedMaDatVe = generateMaDatVe();

            if (payBtn) {
                payBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang tạo mã đặt vé...';
            }

            const createRes = await fetch(`${API_BASE_URL}/api/dat-ve/create`, {
                method: "POST",
                headers: buildAuthHeaders(),
                body: JSON.stringify({
                    maDatVe: generatedMaDatVe,
                    maChuyen: state.tripId,
                    maKhachHang: state.maKhachHang,
                    maDiemDon: state.pickup?.maDiem || state.pickupId,
                    maDiemTra: state.dropoff?.maDiem || state.dropoffId,
                    maLoaiVe: state.maLoaiVe,
                    maGhes: state.seatIds
                })
            });

            const resJson = await createRes.json().catch(() => null);

            console.log("Kết quả từ API DatVe:", resJson);

            if (!createRes.ok || resJson?.success === false) {
                throw new Error(resJson?.message || "Không lấy được mã đặt vé từ server.");
            }

            const data = resJson?.data || resJson || {};

            state.maDatVe = data.maDatVe || generatedMaDatVe;

            if (Array.isArray(data.veList)) {
                state.tickets = data.veList;
                sessionStorage.setItem("bookingTickets", JSON.stringify(data.veList));
            }

            if (data.tongTien) {
                state.totalPrice = Number(data.tongTien);
            }

            sessionStorage.setItem("maDatVe", state.maDatVe);
            sessionStorage.setItem("maKhachHang", state.maKhachHang);
            sessionStorage.setItem("maKH", state.maKhachHang);

            localStorage.setItem("maKhachHang", state.maKhachHang);
            localStorage.setItem("maKH", state.maKhachHang);

            sessionStorage.setItem("bookingData", JSON.stringify({
                trip: state.trip,
                seats: state.selectedSeatDetails,
                tickets: state.tickets,
                pickup: state.pickup,
                dropoff: state.dropoff,
                totalPrice: state.totalPrice,
                maDatVe: state.maDatVe,
                maKhachHang: state.maKhachHang,
                maLoaiVe: state.maLoaiVe,
                maKhuyenMai: state.maKhuyenMai || null,
                trangThai: data.trangThai || "Chờ thanh toán"
            }));
        }

        if (!state.maDatVe) {
            throw new Error("Mã đặt vé vẫn bị trống sau khi tạo.");
        }

        if (payBtn) {
            payBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang kết nối PayOS...';
        }

        const payRes = await fetch(`${API_BASE_URL}/api/payment/payos/create`, {
            method: "POST",
            headers: buildAuthHeaders(),
            body: JSON.stringify({
                maDatVe: state.maDatVe,
                maKhuyenMai: state.maKhuyenMai || null
            })
        });

        const payJson = await payRes.json().catch(() => null);

        console.log("Kết quả từ API PayOS:", payJson);

        if (!payRes.ok) {
            throw new Error(payJson?.message || "Lỗi tạo link thanh toán.");
        }

        if (payJson?.checkoutUrl) {
            window.location.href = payJson.checkoutUrl;
            return;
        }

        throw new Error(payJson?.message || "Không nhận được checkoutUrl từ PayOS.");
    } catch (error) {
        console.error("Lỗi chi tiết:", error);
        alert("Lỗi thanh toán: " + error.message);
    } finally {
        setPayButtonLoading(payBtn, false);
    }
}

function setPayButtonLoading(button, isLoading) {
    if (!button) return;

    button.disabled = isLoading;
    button.dataset.originalText = button.dataset.originalText || button.textContent || "Thanh toán";

    if (isLoading) {
        button.textContent = "Đang tạo link thanh toán...";
    } else {
        button.textContent = button.dataset.originalText;
    }
}

async function loadTripDetail(maChuyen) {
    const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}`, {
        method: "GET",
        headers: buildAuthHeaders()
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Không tải được chi tiết chuyến.");
    }

    const data = result && result.data ? result.data : result;

    return mapTripToView(data);
}

async function loadAccountByMaTK() {
    const account = await getCurrentAccountProfile();

    if (!account) {
        throw new Error("Không tải được thông tin tài khoản.");
    }

    const maKhachHang =
        account.maKhachHang ||
        account.maKH ||
        account.maKh ||
        account.khachHang?.maKhachHang ||
        account.khachHang?.maKH ||
        account.khachHang?.maKh ||
        "";

    if (maKhachHang) {
        localStorage.setItem("maKhachHang", maKhachHang);
        localStorage.setItem("maKH", maKhachHang);
        sessionStorage.setItem("maKhachHang", maKhachHang);
        sessionStorage.setItem("maKH", maKhachHang);
    }

    return account;
}

async function loadDiemDonTra(maChuyen) {
    const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}/diem-don-tra`, {
        method: "GET",
        headers: buildAuthHeaders()
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Không tải được điểm đón/trả.");
    }

    const data = result && result.data ? result.data : result;

    return {
        diemDon: normalizePointList(data?.diemDon),
        diemTra: normalizePointList(data?.diemTra)
    };
}

async function loadSeatMap(maChuyen) {
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
        trangThai: item.trangThai || item.status || "TRONG"
    }));
}

function mapTripToView(item) {
    const departure = item.thoiGianKhoiHanh || item.departureTime || "";
    const arrival = item.thoiGianDen || item.arrivalTimeFull || "";

    return {
        id: item.maChuyen || item.id || state.tripId,
        company: item.tenNhaXe || item.nhaXe || item.company || "Đang cập nhật",
        busType: item.tenLoaiXe || item.loaiXe || item.busType || "Đang cập nhật",
        from: item.diemDi || item.tenBenDi || item.benDi || item.from || "Đang cập nhật",
        to: item.diemDen || item.tenBenDen || item.benDen || item.to || "Đang cập nhật",
        startStation: item.diemDi || item.tenBenDi || item.benDi || "Đang cập nhật",
        endStation: item.diemDen || item.tenBenDen || item.benDen || "Đang cập nhật",
        date: item.ngayDi || item.date || getDateFromApi(departure),
        time: item.gioDi || item.time || getTimeFromApi(departure),
        arrivalDate: item.ngayDen || item.arrivalDate || getDateFromApi(arrival),
        arrivalTime: item.gioDen || item.arrivalTime || getTimeFromApi(arrival),
        price: Number(item.giaVe || item.price || 0),
        images: normalizeStringList(item.images || item.imageUrls || item.image),
        khuyenMai: Array.isArray(item.khuyenMai) ? item.khuyenMai : []
    };
}

function normalizePointList(value) {
    if (!Array.isArray(value)) return [];

    return value.map((item, index) => ({
        maDiem: item.maDiem || item.id || "",
        tenDiem: item.tenDiem || item.name || item.tenBen || `Điểm ${index + 1}`,
        thoiGian: item.thoiGian || item.time || "",
        diaChi: item.diaChi || item.address || "",
        thuTu: Number(item.thuTu || index + 1)
    }));
}

function normalizeStringList(value) {
    if (Array.isArray(value)) return value.filter(Boolean);

    if (typeof value === "string") {
        return value.split("||").map(item => item.trim()).filter(Boolean);
    }

    return [];
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

function buildAuthHeaders() {
    const headers = {
        "Content-Type": "application/json"
    };

    const token = localStorage.getItem("token") || sessionStorage.getItem("token");

    if (token) {
        headers.Authorization = `Bearer ${token}`;
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

    try {
        const account = await loadAccountByMaTK();

        const maKhachHang =
            account.maKhachHang ||
            account.maKH ||
            account.maKh ||
            account.khachHang?.maKhachHang ||
            account.khachHang?.maKH ||
            account.khachHang?.maKh ||
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

function setText(id, value) {
    const el = document.getElementById(id);

    if (!el) return;

    const icon = el.querySelector("i");

    if (icon) {
        el.innerHTML = `${icon.outerHTML} ${escapeHtml(String(value ?? ""))}`;
        return;
    }

    el.textContent = String(value ?? "");
}

function splitCsv(value) {
    if (!value) return [];

    return String(value)
        .split(",")
        .map(item => item.trim())
        .filter(Boolean);
}

function getTimeFromApi(value) {
    if (!value) return "";

    const text = String(value);

    if (text.includes("T")) return text.split("T")[1].substring(0, 5);
    if (text.includes(" ")) return text.split(" ")[1].substring(0, 5);

    return text.substring(0, 5);
}

function getDateFromApi(value) {
    if (!value) return "";

    const text = String(value);

    if (text.includes("T")) return text.split("T")[0];
    if (text.includes(" ")) return text.split(" ")[0];

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

function formatDisplayDate(value) {
    if (!value) return "--/--/----";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}/${date.getFullYear()}`;
}

function formatTripDateLabel(value) {
    if (!value) return "--/--/----";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    const weekdays = ["CN", "T2", "T3", "T4", "T5", "T6", "T7"];

    return `${weekdays[date.getDay()]}, ${formatDisplayDate(date.toISOString())}`;
}

function money(value) {
    return new Intl.NumberFormat("vi-VN").format(Number(value || 0)) + "đ";
}

function recalculateTotals() {
    state.discount = calculatePromotionDiscount(state.ticketPrice, state.selectedPromotion);
    state.totalPrice = Math.max(Number(state.ticketPrice || 0) - Number(state.discount || 0), 0);
}

function updateTicketPrice() {
    const multiplier = Number(state.selectedTicketType?.heSoGia || 1);
    state.ticketPrice = Number(state.trip?.price || 0) * state.seatNos.length * multiplier;
}

function calculatePromotionDiscount(amount, promotion) {
    if (!promotion || !amount || amount <= 0) {
        return 0;
    }

    const percent = Number(promotion.phanTramGiam || 0);
    const fixedAmount = Number(promotion.soTienGiam || 0);
    let discount = 0;

    if (percent > 0) {
        discount += Math.round(amount * percent / 100);
    }

    if (fixedAmount > 0) {
        discount += fixedAmount;
    }

    return Math.min(discount, amount);
}

function mergePromotions(...promotionGroups) {
    const map = new Map();

    promotionGroups.flat().forEach(promotion => {
        if (!promotion || !promotion.maKhuyenMai) return;
        map.set(String(promotion.maKhuyenMai), promotion);
    });

    return [...map.values()];
}

function promotionDiscountText(promotion) {
    const parts = [];
    const percent = Number(promotion.phanTramGiam || 0);
    const fixedAmount = Number(promotion.soTienGiam || 0);

    if (percent > 0) {
        parts.push(`${percent}%`);
    }

    if (fixedAmount > 0) {
        parts.push(money(fixedAmount));
    }

    return parts.length ? `Giảm ${parts.join(" + ")}` : "Khuyến mãi";
}

function setPromotionMessage(message, type = "muted") {
    const box = document.getElementById("promotionMessage");

    if (!box) return;

    box.textContent = message;
    box.className = `promotion-message ${type}`;
}

function setTicketTypeMessage(message, type = "muted") {
    const box = document.getElementById("ticketTypeMessage");

    if (!box) return;

    box.textContent = message;
    box.className = `promotion-message ${type}`;
}

function formatMultiplier(value) {
    return Number(value || 1).toLocaleString("vi-VN", {
        minimumFractionDigits: 0,
        maximumFractionDigits: 2
    });
}

function generateMaDatVe() {
    return "DV" + Date.now();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
