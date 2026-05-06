async function loadDashboard() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/dashboard`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            console.error("Không thể tải dashboard:", result.message || result);
            renderDemoStats();
            renderRecentTrips();
            return;
        }

        renderDashboardFromApi(result);
    } catch (error) {
        console.error("Lỗi tải dashboard:", error);
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
