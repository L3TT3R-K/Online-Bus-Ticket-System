const API_BASE_URL = "http://localhost:8080";

let currentStaff = null;
let loaiXeList = [];
let tienIchList = [];
let pendingBusStatusId = null;

let addBusImages = [];
let editBusImages = [];

const BUS_STATUS_OPTIONS = ["Hoạt động", "Ngừng hoạt động", "Bảo dưỡng"];

let buses = [];

let trips = [];

let stations = [];

async function loadStations() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/ben-xe`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải bến xe:", result.message || result);
            stations = [];
            return;
        }

        stations = result.map(item => ({
            id: item.maBen,
            maBen: item.maBen,
            name: item.tenBen,
            tenBen: item.tenBen,
            address: item.diaChi || ""
        }));

        console.log("DANH SÁCH BẾN XE:", stations);
    } catch (error) {
        console.error("Lỗi gọi API bến xe:", error);
        stations = [];
    }
}

function getStationById(id) {
    if (!id) return null;

    const value = String(id).trim();

    return stations.find(station =>
        station.id === value ||
        station.maBen === value ||
        station.name === value ||
        station.tenBen === value
    ) || null;
}

function renderStationOptions(selectId, includeEmpty = true) {
    const el = document.getElementById(selectId);

    if (!el) return;

    const options = [];

    if (includeEmpty) {
        options.push(`<option value="">-- Chọn bến --</option>`);
    }

    if (!Array.isArray(stations) || !stations.length) {
        options.push(`<option value="">Chưa có dữ liệu bến</option>`);
        el.innerHTML = options.join("");
        return;
    }

    stations.forEach(station => {
        options.push(`
            <option value="${station.maBen}">
                ${station.tenBen}
            </option>
        `);
    });

    el.innerHTML = options.join("");
}

let bookings = [];



function getAuthHeaders() {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");

    const headers = {
        "Content-Type": "application/json"
    };

    if (token) {
        headers.Authorization = "Bearer " + token;
    }

    return headers;
}

function checkStaffLogin() {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");

    if (!token) {
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
