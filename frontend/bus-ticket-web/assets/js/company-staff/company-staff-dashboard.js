async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/dashboard`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok || result.success === false) {
            console.error("Không thể tải dashboard:", result.message || result);
            showDashboardError(result.message || "Không thể tải dữ liệu.");
            return;
        }

        // Hỗ trợ cả dạng:
        // 1) { success: true, data: {...} }
        // 2) { totalBus: ..., totalTrip: ... }
        const dashboardData = result.data || result;

        renderDashboardFromApi(dashboardData);
    } catch (error) {
        console.error("Lỗi tải dashboard:", error);
        showDashboardError("Lỗi kết nối server. Vui lòng thử lại sau.");
    }
}

function showDashboardError(message) {
    const totalBus = document.getElementById("totalBus");
    const totalTrip = document.getElementById("totalTrip");
    const totalTicket = document.getElementById("totalTicket");
    const totalRevenue = document.getElementById("totalRevenue");
    const recentTripsBox = document.getElementById("recentTrips");

    if (totalBus) totalBus.textContent = "-";
    if (totalTrip) totalTrip.textContent = "-";
    if (totalTicket) totalTicket.textContent = "-";
    if (totalRevenue) totalRevenue.textContent = "-";

    if (recentTripsBox) {
        recentTripsBox.innerHTML = `<div class="alert alert-danger mb-0"><i class="fa-solid fa-exclamation-triangle"></i> ${message}</div>`;
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

        if (!response.ok || result.success === false) {
            console.error("Lỗi tải doanh thu tháng:", result.message || result);
            renderRevenueChartFromData([]);
            return;
        }

        // Hỗ trợ nhiều kiểu response:
        // 1) [ { monthNumber: 1, revenue: 100000 } ]
        // 2) { success: true, data: [ ... ] }
        // 3) { success: true, monthlyRevenue: [ ... ] }
        const monthlyData = Array.isArray(result)
            ? result
            : Array.isArray(result.data)
                ? result.data
                : Array.isArray(result.monthlyRevenue)
                    ? result.monthlyRevenue
                    : [];

        renderRevenueChartFromData(monthlyData);
    } catch (error) {
        console.error("Lỗi gọi API doanh thu tháng:", error);
        renderRevenueChartFromData([]);
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
            <h6>${trip.tuyen || trip.route || "Không rõ tuyến"}</h6>
            <p>${trip.bienSo || trip.plate || "Không rõ xe"} | ${formatDateTime(trip.thoiGianKhoiHanh)}</p>
            <p>
                Giá vé: <strong>${money(trip.giaVe || trip.price || 0)}</strong> |
                ${trip.soGheTrong ?? trip.emptySeats ?? 0} ghế trống
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

function normalizeMonthlyRevenueData(data) {
    const input = Array.isArray(data) ? data : [];

    const map = new Map();

    input.forEach(item => {
        const monthNumber = Number(
            item.monthNumber ??
            item.thang ??
            item.month ??
            item.MONTH_NUMBER ??
            item.THANG
        );

        if (!monthNumber || monthNumber < 1 || monthNumber > 12) {
            return;
        }

        const revenue = Number(
            item.revenue ??
            item.doanhThu ??
            item.totalRevenue ??
            item.tongDoanhThu ??
            item.REVENUE ??
            item.DOANH_THU ??
            0
        );

        map.set(monthNumber, {
            monthNumber,
            month: item.monthLabel || item.monthName || item.monthText || `T${monthNumber}`,
            revenue
        });
    });

    // Luôn trả đủ 12 tháng
    return Array.from({ length: 12 }, (_, index) => {
        const monthNumber = index + 1;
        return map.get(monthNumber) || {
            monthNumber,
            month: `T${monthNumber}`,
            revenue: 0
        };
    });
}

function compactRevenue(value) {
    const number = Number(value || 0);
    const sign = number < 0 ? "-" : "";
    const absolute = Math.abs(number);
    const format = amount => Number.isInteger(amount)
        ? String(amount)
        : amount.toFixed(1).replace(/\.0$/, "");

    if (absolute >= 1000000) {
        return `${sign}${format(absolute / 1000000)}tr`;
    }

    if (absolute >= 1000) {
        return `${sign}${format(absolute / 1000)}k`;
    }

    return `${sign}${absolute}`;
}

function renderRevenueChartFromData(data) {
    const chart = document.getElementById("revenueChart");

    if (!chart) return;

    const sourceData = Array.isArray(data) && data.length ? data : [];
    const safeData = normalizeMonthlyRevenueData(sourceData);

    const maxRevenue = Math.max(...safeData.map(item => Number(item.revenue || 0)), 1);

    chart.innerHTML = safeData.map(item => {
        const revenue = Number(item.revenue || 0);

        const height = revenue === 0
            ? 28
            : Math.max(45, Math.round((revenue / maxRevenue) * 220));

        const label = item.month || `T${item.monthNumber}`;

        return `
            <div class="bar-item">
                <div class="bar-value">${compactRevenue(revenue)}</div>
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
