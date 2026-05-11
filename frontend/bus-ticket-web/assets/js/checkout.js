const API_BASE_URL = "http://localhost:8080";

const state = {
    tripId: "",
    maDatVe: "",
    maKhuyenMai: "",
    seatNos: [],
    seatIds: [],
    selectedSeatDetails: [],
    pickupId: "",
    dropoffId: "",
    pickup: null,
    dropoff: null,
    trip: null,
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

            state.maKhuyenMai = String(
                query.get("maKhuyenMai") ||
                bookingData?.maKhuyenMai ||
                bookingData?.khuyenMai?.maKhuyenMai ||
                sessionStorage.getItem("maKhuyenMai") ||
                localStorage.getItem("maKhuyenMai") ||
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
        } catch (error) {
            console.warn("Không đọc được bookingData:", error);
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

    if (!state.maKhuyenMai) {
        state.maKhuyenMai = String(
            query.get("maKhuyenMai") ||
            sessionStorage.getItem("maKhuyenMai") ||
            localStorage.getItem("maKhuyenMai") ||
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
}

async function loadContactInfoForCheckout() {
    const customerNameEl = document.getElementById("customerName");
    const customerPhoneEl = document.getElementById("customerPhone");
    const customerEmailEl = document.getElementById("customerEmail");
    const loginReminderEl = document.querySelector(".login-reminder");

    if (customerNameEl) customerNameEl.value = "";
    if (customerPhoneEl) customerPhoneEl.value = "";
    if (customerEmailEl) customerEmailEl.value = "";

    const token = localStorage.getItem("token");
    const maTK = localStorage.getItem("maTK");

    if (!token || !maTK) {
        if (loginReminderEl) {
            loginReminderEl.classList.remove("d-none");
        }
        return;
    }

    try {
        const account = await loadAccountByMaTK(maTK);

        if (customerNameEl) customerNameEl.value = account.tenKH || "";
        if (customerPhoneEl) customerPhoneEl.value = account.sdt || "";
        if (customerEmailEl) customerEmailEl.value = account.email || "";

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

    if (!state.seatNos.length && Array.isArray(state.selectedSeatDetails) && state.selectedSeatDetails.length) {
        state.seatNos = state.selectedSeatDetails.map(item => String(item.soGhe || "")).filter(Boolean);
    }

    state.ticketPrice = Number(state.trip?.price || 0) * state.seatNos.length;
    state.discount = 0;
    state.totalPrice = Math.max(state.ticketPrice - state.discount, 0);
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
    setText("summaryPickupName", state.pickup?.tenDiem || trip.startStation || trip.from || "Đang cập nhật");
    setText("summaryPickupAddress", state.pickup?.diaChi || "Đang cập nhật");

    setText("summaryDropoffTime", trip.arrivalTime || "--:--");
    setText("summaryDropoffDate", `(${formatShortDate(trip.arrivalDate || trip.date)})`);
    setText("summaryDropoffName", state.dropoff?.tenDiem || trip.endStation || trip.to || "Đang cập nhật");
    setText("summaryDropoffAddress", state.dropoff?.diaChi || "Đang cập nhật");

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

async function onClickPay() {
    const form = document.getElementById("checkoutForm");
    const payBtn = document.getElementById("payBtn");

    if (!form || !form.checkValidity()) {
        form?.reportValidity();
        return;
    }

    if (!state.tripId || !state.seatNos.length) {
        alert("Không có dữ liệu đặt vé. Vui lòng chọn lại chuyến và ghế.");
        return;
    }

    if (!state.maDatVe) {
        alert("Thiếu mã đặt vé. Vui lòng quay lại bước đặt vé để tạo đơn trước khi thanh toán.");
        return;
    }

    setPayButtonLoading(payBtn, true);

    try {
        const latestSeats = await loadSeatMap(state.tripId);
        const seatByNo = new Map(latestSeats.map(item => [String(item.soGhe || ""), normalizeSeatStatus(item.trangThai || item.status)]));
        const conflict = state.seatNos.filter(seat => seatByNo.get(String(seat)) !== "TRONG");

        if (conflict.length) {
            alert(`Ghế ${conflict.join(", ")} vừa có người đặt. Vui lòng chọn lại ghế.`);
            window.location.href = `trip-detail.html?id=${encodeURIComponent(state.tripId)}`;
            return;
        }

        const response = await fetch(`${API_BASE_URL}/api/payment/payos/create`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                maDatVe: state.maDatVe,
                maKhuyenMai: state.maKhuyenMai || null
            })
        });

        let result = null;

        try {
            result = await response.json();
        } catch (error) {
            result = null;
        }

        if (!response.ok) {
            throw new Error(result?.message || result?.error || "Không tạo được link thanh toán.");
        }

        const checkoutUrl = result?.checkoutUrl || result?.data?.checkoutUrl;

        if (!checkoutUrl) {
            throw new Error("Backend chưa trả về checkoutUrl.");
        }

        window.location.href = checkoutUrl;
        return;
    } catch (error) {
        console.error("Không tạo được link thanh toán PayOS:", error);
        alert(error.message || "Không tạo được link thanh toán. Vui lòng thử lại.");
    } finally {
        setPayButtonLoading(payBtn, false);
    }
}

function setPayButtonLoading(button, isLoading) {
    if (!button) return;

    button.disabled = isLoading;
    button.dataset.originalText = button.dataset.originalText || button.textContent || "Thanh toán";
    button.textContent = isLoading ? "Đang tạo link thanh toán..." : button.dataset.originalText;
}

async function loadTripDetail(maChuyen) {
    const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Không tải được chi tiết chuyến.");
    }

    const data = result && result.data ? result.data : result;

    return mapTripToView(data);
}

async function loadAccountByMaTK(maTK) {
    const response = await fetch(`${API_BASE_URL}/api/account/${encodeURIComponent(maTK)}`, {
        method: "GET",
        headers: { "Content-Type": "application/json" }
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.message || "Không tải được thông tin tài khoản.");
    }

    return result;
}

async function loadDiemDonTra(maChuyen) {
    const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/${encodeURIComponent(maChuyen)}/diem-don-tra`, {
        method: "GET",
        headers: { "Content-Type": "application/json" }
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
        headers: { "Content-Type": "application/json" }
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
        images: normalizeStringList(item.images || item.imageUrls || item.image)
    };
}

function normalizePointList(value) {
    if (!Array.isArray(value)) return [];

    return value.map((item, index) => ({
        maDiem: item.maDiem || item.id || "",
        tenDiem: item.tenDiem || item.name || `Điểm ${index + 1}`,
        thoiGian: item.thoiGian || item.time || "",
        diaChi: item.diaChi || item.address || "",
        thuTu: Number(item.thuTu || index + 1)
    }));
}

function normalizeStringList(value) {
    if (Array.isArray(value)) return value.filter(Boolean);
    if (typeof value === "string") return value.split("||").map(item => item.trim()).filter(Boolean);
    return [];
}

function normalizeSeatStatus(status) {
    const text = String(status || "").trim().toUpperCase();

    if (text === "TRONG" || text === "EMPTY" || text === "AVAILABLE") return "TRONG";
    if (text === "DANG_GIU" || text === "GIU_CHO" || text === "HOLDING") return "DANG_GIU";
    if (text === "DA_DAT" || text === "BOOKED" || text === "DA_THANH_TOAN") return "DA_DAT";

    return text || "TRONG";
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
    return String(value).split(",").map(item => item.trim()).filter(Boolean);
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

function generateTicketId() {
    return "VE" + Date.now();
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}
