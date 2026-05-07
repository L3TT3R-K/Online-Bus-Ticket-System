const API_BASE_URL = "http://localhost:8080";

let currentStaff = null;
let loaiXeList = [];
let tienIchList = [];
let pendingBusStatusId = null;

let addBusImages = [];
let editBusImages = [];

const BUS_STATUS_OPTIONS = ["Hoạt động", "Ngừng hoạt động", "Bảo dưỡng"];

let buses = [];

let trips = [
    { id: "CX001", busId: "XE001", route: "Ninh Bình - Quảng Ninh", date: "2026-04-24", time: "20:35", price: 350000, emptySeats: 8, status: "Đang mở bán" },
    { id: "CX002", busId: "XE002", route: "Ninh Bình - Hà Nội", date: "2026-04-25", time: "07:30", price: 180000, emptySeats: 12, status: "Đang mở bán" },
    { id: "CX003", busId: "XE001", route: "Ninh Bình - Hải Phòng", date: "2026-04-26", time: "13:20", price: 250000, emptySeats: 4, status: "Đã khởi hành" },
    { id: "CX004", busId: "XE003", route: "Ninh Bình - Quảng Ninh", date: "2026-04-27", time: "06:00", price: 220000, emptySeats: 20, status: "Đã hủy" }
];

// Danh sách bến (mẫu tĩnh) — có thể thay bằng API sau
let stations = [
    { id: "ST001", name: "Ninh Bình" },
    { id: "ST002", name: "Hà Nội" },
    { id: "ST003", name: "Hải Phòng" },
    { id: "ST004", name: "Quảng Ninh" },
    { id: "ST005", name: "Nam Định" },
    { id: "ST006", name: "Thanh Hóa" }
];

function getStationById(id) {
    return stations.find(s => s.id === id);
}

function renderStationOptions(selectId, includeEmpty = true) {
    const el = document.getElementById(selectId);
    if (!el) return;

    const options = [];
    if (includeEmpty) options.push(`<option value="">-- Chọn bến --</option>`);

    stations.forEach(s => {
        options.push(`<option value="${s.id}">${s.name}</option>`);
    });

    el.innerHTML = options.join("");
}

let bookings = [
    { id: "VE001", customer: "Nguyễn Văn A", phone: "0901234567", tripId: "CX001", seats: "A1, A2", price: 700000, payment: "Đã thanh toán" },
    { id: "VE002", customer: "Trần Thị B", phone: "0911111111", tripId: "CX001", seats: "B1", price: 350000, payment: "Đã thanh toán" },
    { id: "VE003", customer: "Lê Văn C", phone: "0922222222", tripId: "CX002", seats: "C3", price: 180000, payment: "Chờ thanh toán" },
    { id: "VE004", customer: "Phạm Thị D", phone: "0933333333", tripId: "CX003", seats: "D1, D2", price: 500000, payment: "Đã thanh toán" }
];

const monthlyRevenueDemo = [
    { month: "T1", revenue: 5500000 },
    { month: "T2", revenue: 7200000 },
    { month: "T3", revenue: 6800000 },
    { month: "T4", revenue: 9300000 },
    { month: "T5", revenue: 8400000 },
    { month: "T6", revenue: 10500000 }
];

function getAuthHeaders() {
    const maTK = localStorage.getItem("maTK");
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        "X-MaTK": maTK || "",
        "Authorization": "Bearer " + (token || "")
    };
}

function checkStaffLogin() {
    const maTK = localStorage.getItem("maTK");
    const token = localStorage.getItem("token");

    if (!maTK || !token) {
        alert("Bạn cần đăng nhập bằng tài khoản nhân viên.");
        window.location.href = "login.html";
        return false;
    }

    return true;
}

async function initStaffInfo() {
    if (!checkStaffLogin()) return false;

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/me`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            alert(result.message || "Không thể lấy thông tin nhân viên.");
            window.location.href = "login.html";
            return false;
        }

        currentStaff = result;

        const staffName = document.getElementById("staffName");
        const companyNameBox = document.querySelector(".staff-user span");

        if (staffName) staffName.textContent = result.tenNV || "Nhân viên nhà xe";
        if (companyNameBox) companyNameBox.textContent = result.tenNhaXe || "Nhà xe";

        localStorage.setItem("maNV", result.maNV || "");
        localStorage.setItem("maNhaXe", result.maNhaXe || "");
        localStorage.setItem("tenNhaXe", result.tenNhaXe || "");
        localStorage.setItem("fullname", result.tenNV || "");
        localStorage.setItem("role", result.quyen || "NhanVien");

        return true;
    } catch (error) {
        console.error("Lỗi lấy thông tin nhân viên:", error);
        alert("Không thể kết nối server.");
        return false;
    }
}

function statusBadge(status) {
    let cls = "badge-active";

    if (status === "Bảo dưỡng" || status === "Chờ thanh toán") {
        cls = "badge-pending";
    }

    if (status === "Ngừng hoạt động" || status === "Đã hủy" || status === "Bảo trì") {
        cls = "badge-cancel";
    }

    return `<span class="badge-soft ${cls}">${status}</span>`;
}

function paymentBadge(status) {
    const cls = status === "Đã thanh toán" ? "badge-active" : "badge-pending";
    return `<span class="badge-soft ${cls}">${status}</span>`;
}

function money(value) {
    const number = Number(value || 0);
    return new Intl.NumberFormat("vi-VN").format(number) + "đ";
}

function formatDateTime(value) {
    if (!value) return "";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) return value;

    return date.toLocaleString("vi-VN");
}

function getBusById(id) {
    return buses.find(bus => bus.id === id);
}

function getTripById(id) {
    return trips.find(trip => trip.id === id);
}

function closeModal(id) {
    const element = document.getElementById(id);

    if (!element) return;

    const modal = bootstrap.Modal.getInstance(element);

    if (modal) modal.hide();
}
