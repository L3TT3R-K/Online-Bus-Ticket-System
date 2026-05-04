const API_BASE_URL = "http://localhost:8080";

let currentStaff = null;

let buses = [
    { id: "XE001", plate: "35B-12345", type: "Limousine 12 chỗ", seats: 12, status: "Đang hoạt động" },
    { id: "XE002", plate: "35B-67890", type: "Giường nằm 34 chỗ", seats: 34, status: "Đang hoạt động" },
    { id: "XE003", plate: "35B-99999", type: "Ghế ngồi 29 chỗ", seats: 29, status: "Bảo trì" }
];

let trips = [
    { id: "CX001", busId: "XE001", route: "Ninh Bình - Quảng Ninh", date: "2026-04-24", time: "20:35", price: 350000, emptySeats: 8, status: "Đang mở bán" },
    { id: "CX002", busId: "XE002", route: "Ninh Bình - Hà Nội", date: "2026-04-25", time: "07:30", price: 180000, emptySeats: 12, status: "Đang mở bán" },
    { id: "CX003", busId: "XE001", route: "Ninh Bình - Hải Phòng", date: "2026-04-26", time: "13:20", price: 250000, emptySeats: 4, status: "Đã khởi hành" },
    { id: "CX004", busId: "XE003", route: "Ninh Bình - Quảng Ninh", date: "2026-04-27", time: "06:00", price: 220000, emptySeats: 20, status: "Đã hủy" }
];

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

document.addEventListener("DOMContentLoaded", async function () {
    initMenu();
    initForms();
    initFilters();

    const ok = await initStaffInfo();

    if (!ok) return;

    await loadDashboard();
    await loadMonthlyRevenue();

    renderBusOptions();
    renderBuses();
    renderTrips();
    renderBookings();
    renderSeatMap();
    renderReport();
});

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

        if (staffName) {
            staffName.textContent = result.tenNV || "Nhân viên nhà xe";
        }

        if (companyNameBox) {
            companyNameBox.textContent = result.tenNhaXe || "Nhà xe";
        }

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

async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/dashboard`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            alert(result.message || "Không thể tải dữ liệu tổng quan.");
            renderDemoStats();
            renderRecentTrips();
            return;
        }

        renderDashboardFromApi(result);

    } catch (error) {
        console.error("Lỗi tải dashboard:", error);
        alert("Không thể kết nối server khi tải tổng quan.");
        renderDemoStats();
        renderRecentTrips();
    }
}

async function loadMonthlyRevenue() {
    const year = new Date().getFullYear();

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/revenue/monthly?year=${year}`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải doanh thu tháng:", result.message || result);
            renderRevenueChartFromData(monthlyRevenueDemo);
            return;
        }

        renderRevenueChartFromData(result);

    } catch (error) {
        console.error("Lỗi gọi API doanh thu tháng:", error);
        renderRevenueChartFromData(monthlyRevenueDemo);
    }
}

function renderDashboardFromApi(data) {
    const totalBus = document.getElementById("totalBus");
    const totalTrip = document.getElementById("totalTrip");
    const totalTicket = document.getElementById("totalTicket");
    const totalRevenue = document.getElementById("totalRevenue");
    const reportRevenue = document.getElementById("reportRevenue");
    const reportPaidTicket = document.getElementById("reportPaidTicket");

    if (totalBus) totalBus.textContent = data.totalBus || 0;
    if (totalTrip) totalTrip.textContent = data.totalTrip || 0;
    if (totalTicket) totalTicket.textContent = data.totalTicket || 0;
    if (totalRevenue) totalRevenue.textContent = money(data.totalRevenue || 0);

    if (reportRevenue) reportRevenue.textContent = money(data.totalRevenue || 0);
    if (reportPaidTicket) reportPaidTicket.textContent = data.totalTicket || 0;

    renderRecentTripsFromApi(data.recentTrips || []);
}

function renderRecentTripsFromApi(recentTrips) {
    const box = document.getElementById("recentTrips");

    if (!box) return;

    if (!recentTrips.length) {
        box.innerHTML = `<div class="alert alert-warning mb-0">Chưa có chuyến xe gần đây.</div>`;
        return;
    }

    box.innerHTML = recentTrips.map(trip => `
        <div class="recent-trip">
            <h6>${trip.tuyen || "Không rõ tuyến"}</h6>
            <p>${trip.bienSo || "Không rõ xe"} | ${formatDateTime(trip.thoiGianKhoiHanh)}</p>
            <p>
                Giá vé: <strong>${money(trip.giaVe || 0)}</strong> |
                ${trip.soGheTrong ?? 0} ghế trống
            </p>
        </div>
    `).join("");
}

function renderDemoStats() {
    const paidBookings = bookings.filter(item => item.payment === "Đã thanh toán");
    const revenue = paidBookings.reduce((sum, item) => sum + item.price, 0);

    const totalBus = document.getElementById("totalBus");
    const totalTrip = document.getElementById("totalTrip");
    const totalTicket = document.getElementById("totalTicket");
    const totalRevenue = document.getElementById("totalRevenue");
    const reportRevenue = document.getElementById("reportRevenue");
    const reportPaidTicket = document.getElementById("reportPaidTicket");

    if (totalBus) totalBus.textContent = buses.length;
    if (totalTrip) totalTrip.textContent = trips.length;
    if (totalTicket) totalTicket.textContent = bookings.length;
    if (totalRevenue) totalRevenue.textContent = money(revenue);

    if (reportRevenue) reportRevenue.textContent = money(revenue);
    if (reportPaidTicket) reportPaidTicket.textContent = paidBookings.length;
}

function initMenu() {
    const links = document.querySelectorAll(".staff-menu a");
    const sections = document.querySelectorAll(".content-section");
    const pageTitle = document.getElementById("pageTitle");

    links.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();

            links.forEach(item => item.classList.remove("active"));
            this.classList.add("active");

            sections.forEach(section => section.classList.remove("active"));

            const sectionId = this.dataset.section;
            const section = document.getElementById(sectionId);

            if (section) {
                section.classList.add("active");
            }

            if (pageTitle) {
                pageTitle.textContent = this.textContent.trim();
            }
        });
    });

    const logoutBtn = document.getElementById("logoutBtn");

    if (logoutBtn) {
        logoutBtn.addEventListener("click", function () {
            localStorage.removeItem("token");
            localStorage.removeItem("maTK");
            localStorage.removeItem("maNV");
            localStorage.removeItem("maNhaXe");
            localStorage.removeItem("tenNhaXe");
            localStorage.removeItem("role");
            localStorage.removeItem("fullname");

            alert("Đã đăng xuất.");
            window.location.href = "main.html";
        });
    }
}

function initForms() {
    const busForm = document.getElementById("busForm");
    const tripForm = document.getElementById("tripForm");

    if (busForm) {
        busForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const bus = {
                id: "XE" + String(buses.length + 1).padStart(3, "0"),
                plate: document.getElementById("busPlate").value.trim(),
                type: document.getElementById("busType").value,
                seats: Number(document.getElementById("busSeatCount").value),
                status: "Đang hoạt động"
            };

            buses.push(bus);
            this.reset();
            closeModal("busModal");

            renderBusOptions();
            renderBuses();
            renderTrips();
            renderSeatMap();
            renderReport();

            alert("Đã thêm xe demo. API POST /api/staff/xe sẽ nối sau.");
        });
    }

    if (tripForm) {
        tripForm.addEventListener("submit", function (event) {
            event.preventDefault();

            const busId = document.getElementById("tripBusSelect").value;
            const bus = getBusById(busId);

            const trip = {
                id: "CX" + String(trips.length + 1).padStart(3, "0"),
                busId: busId,
                route: document.getElementById("tripRoute").value.trim(),
                date: document.getElementById("tripDate").value,
                time: document.getElementById("tripTime").value,
                price: Number(document.getElementById("tripPrice").value),
                emptySeats: bus ? bus.seats : 0,
                status: "Đang mở bán"
            };

            trips.push(trip);
            this.reset();
            closeModal("tripModal");

            renderBusOptions();
            renderTrips();
            renderRecentTrips();
            renderSeatMap();
            renderReport();

            alert("Đã thêm chuyến xe demo. API POST /api/staff/chuyen-xe sẽ nối sau.");
        });
    }
}

function initFilters() {
    const tripBusFilter = document.getElementById("tripBusFilter");
    const tripStatusFilter = document.getElementById("tripStatusFilter");
    const bookingSearch = document.getElementById("bookingSearch");
    const bookingPaymentFilter = document.getElementById("bookingPaymentFilter");
    const seatTripSelect = document.getElementById("seatTripSelect");

    if (tripBusFilter) tripBusFilter.addEventListener("change", renderTrips);
    if (tripStatusFilter) tripStatusFilter.addEventListener("change", renderTrips);

    if (bookingSearch) bookingSearch.addEventListener("input", renderBookings);
    if (bookingPaymentFilter) bookingPaymentFilter.addEventListener("change", renderBookings);

    if (seatTripSelect) seatTripSelect.addEventListener("change", renderSeatMap);
}

function renderBusOptions() {
    const tripBusSelect = document.getElementById("tripBusSelect");
    const tripBusFilter = document.getElementById("tripBusFilter");
    const seatTripSelect = document.getElementById("seatTripSelect");

    if (tripBusSelect) {
        tripBusSelect.innerHTML = buses.map(bus => `
            <option value="${bus.id}">${bus.plate} - ${bus.type}</option>
        `).join("");
    }

    if (tripBusFilter) {
        tripBusFilter.innerHTML = `
            <option value="">Tất cả xe</option>
            ${buses.map(bus => `<option value="${bus.id}">${bus.plate}</option>`).join("")}
        `;
    }

    if (seatTripSelect) {
        seatTripSelect.innerHTML = trips.map(trip => {
            const bus = getBusById(trip.busId);
            return `<option value="${trip.id}">${trip.id} - ${bus ? bus.plate : "Không rõ xe"} - ${trip.route}</option>`;
        }).join("");
    }
}

function renderBuses() {
    const tbody = document.getElementById("busTableBody");

    if (!tbody) return;

    tbody.innerHTML = buses.map(bus => `
        <tr>
            <td>${bus.id}</td>
            <td>${bus.plate}</td>
            <td>${bus.type}</td>
            <td>${bus.seats}</td>
            <td>${statusBadge(bus.status)}</td>
            <td>
                <button class="action-btn" onclick="alert('Chức năng sửa sẽ nối backend sau')">
                    <i class="fa-solid fa-pen"></i>
                </button>
                <button class="action-btn danger" onclick="toggleBusStatus('${bus.id}')">
                    <i class="fa-solid fa-screwdriver-wrench"></i>
                </button>
            </td>
        </tr>
    `).join("");
}

function renderTrips() {
    const tbody = document.getElementById("tripTableBody");

    if (!tbody) return;

    const busFilter = document.getElementById("tripBusFilter")?.value || "";
    const statusFilter = document.getElementById("tripStatusFilter")?.value || "";

    let data = [...trips];

    if (busFilter) {
        data = data.filter(trip => trip.busId === busFilter);
    }

    if (statusFilter) {
        data = data.filter(trip => trip.status === statusFilter);
    }

    tbody.innerHTML = data.map(trip => {
        const bus = getBusById(trip.busId);

        return `
            <tr>
                <td>${trip.id}</td>
                <td>${bus ? bus.plate : "Không rõ"}</td>
                <td>${trip.route}</td>
                <td>${trip.date}</td>
                <td>${trip.time}</td>
                <td>${money(trip.price)}</td>
                <td>${trip.emptySeats}</td>
                <td>${statusBadge(trip.status)}</td>
            </tr>
        `;
    }).join("");
}

function renderBookings() {
    const tbody = document.getElementById("bookingTableBody");

    if (!tbody) return;

    const keyword = document.getElementById("bookingSearch")?.value.toLowerCase() || "";
    const paymentFilter = document.getElementById("bookingPaymentFilter")?.value || "";

    let data = [...bookings];

    if (keyword) {
        data = data.filter(item =>
            item.id.toLowerCase().includes(keyword) ||
            item.customer.toLowerCase().includes(keyword) ||
            item.phone.includes(keyword)
        );
    }

    if (paymentFilter) {
        data = data.filter(item => item.payment === paymentFilter);
    }

    tbody.innerHTML = data.map(item => {
        const trip = getTripById(item.tripId);

        return `
            <tr>
                <td>${item.id}</td>
                <td>${item.customer}</td>
                <td>${item.phone}</td>
                <td>${trip ? trip.route : item.tripId}</td>
                <td>${item.seats}</td>
                <td>${money(item.price)}</td>
                <td>${paymentBadge(item.payment)}</td>
            </tr>
        `;
    }).join("");
}

function renderRevenueChartFromData(data) {
    const chart = document.getElementById("revenueChart");

    if (!chart) return;

    const safeData = Array.isArray(data) && data.length ? data : monthlyRevenueDemo;
    const maxRevenue = Math.max(...safeData.map(item => Number(item.revenue || 0)), 1);

    chart.innerHTML = safeData.map(item => {
        const revenue = Number(item.revenue || 0);
        const height = Math.max(35, Math.round((revenue / maxRevenue) * 220));
        const label = item.month || ("T" + item.monthNumber);

        return `
            <div class="bar-item">
                <div class="bar-value">${money(revenue)}</div>
                <div class="bar" style="height:${height}px" title="${money(revenue)}"></div>
                <div class="bar-label">${label}</div>
            </div>
        `;
    }).join("");
}

function renderRecentTrips() {
    const box = document.getElementById("recentTrips");

    if (!box) return;

    box.innerHTML = trips.slice(0, 4).map(trip => {
        const bus = getBusById(trip.busId);

        return `
            <div class="recent-trip">
                <h6>${trip.route}</h6>
                <p>${bus ? bus.plate : "Không rõ xe"} | ${trip.date} - ${trip.time}</p>
                <p>Giá vé: <strong>${money(trip.price)}</strong> | ${trip.emptySeats} ghế trống</p>
            </div>
        `;
    }).join("");
}

function renderSeatMap() {
    const seatMap = document.getElementById("seatMap");

    if (!seatMap) return;

    const tripId = document.getElementById("seatTripSelect")?.value || trips[0]?.id;
    const trip = getTripById(tripId);

    if (!trip) {
        seatMap.innerHTML = `<div class="alert alert-warning">Chưa có chuyến xe.</div>`;
        return;
    }

    const bus = getBusById(trip.busId);
    const seatCount = bus ? bus.seats : 12;

    const booked = ["A1", "A3", "B2", "C4"];
    const holding = ["D2"];

    const seats = Array.from({ length: seatCount }, (_, index) => {
        const row = String.fromCharCode(65 + Math.floor(index / 4));
        const number = (index % 4) + 1;
        return row + number;
    });

    seatMap.innerHTML = seats.map(seat => {
        let cls = "seat";

        if (booked.includes(seat)) {
            cls += " booked";
        } else if (holding.includes(seat)) {
            cls += " holding";
        }

        return `<button class="${cls}">${seat}</button>`;
    }).join("");
}

function renderReport() {
    const report = document.getElementById("tripRevenueReport");

    if (!report) return;

    const tripReports = trips.map(trip => {
        const tripBookings = bookings.filter(item => item.tripId === trip.id && item.payment === "Đã thanh toán");
        const revenue = tripBookings.reduce((sum, item) => sum + item.price, 0);
        const ticketCount = tripBookings.length;
        const bus = getBusById(trip.busId);
        const totalSeats = bus ? bus.seats : 1;
        const fillRate = Math.min(100, Math.round(((totalSeats - trip.emptySeats) / totalSeats) * 100));

        return {
            ...trip,
            revenue,
            ticketCount,
            fillRate,
            busPlate: bus ? bus.plate : "Không rõ xe"
        };
    });

    const topTrip = [...tripReports].sort((a, b) => b.revenue - a.revenue)[0];

    const topTripRevenue = document.getElementById("topTripRevenue");

    if (topTripRevenue) {
        topTripRevenue.textContent = topTrip ? topTrip.id : "---";
    }

    report.innerHTML = tripReports.map(item => `
        <div class="trip-report-card">
            <div class="trip-report-head">
                <h5>${item.id} - ${item.route}</h5>
                <strong>${money(item.revenue)}</strong>
            </div>

            <p class="mb-2">
                Xe: <strong>${item.busPlate}</strong> |
                Ngày đi: <strong>${item.date}</strong> |
                Số vé bán: <strong>${item.ticketCount}</strong> |
                Lấp đầy: <strong>${item.fillRate}%</strong>
            </p>

            <div class="progress">
                <div class="progress-bar" style="width:${item.fillRate}%"></div>
            </div>
        </div>
    `).join("");
}

function toggleBusStatus(id) {
    const bus = buses.find(item => item.id === id);

    if (!bus) return;

    bus.status = bus.status === "Đang hoạt động" ? "Bảo trì" : "Đang hoạt động";

    renderBuses();
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

    if (modal) {
        modal.hide();
    }
}

function statusBadge(status) {
    let cls = "badge-active";

    if (status === "Chờ thanh toán") cls = "badge-pending";
    if (status === "Đã hủy" || status === "Bảo trì") cls = "badge-cancel";

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

    if (Number.isNaN(date.getTime())) {
        return value;
    }

    return date.toLocaleString("vi-VN");
}