async function loadRevenueSummary() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/revenue/summary`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải tổng quan doanh thu:", result.message || result);
            renderRevenueSummaryFallback();
            return;
        }

        renderRevenueSummary(result);
    } catch (error) {
        console.error("Lỗi gọi API revenue summary:", error);
        renderRevenueSummaryFallback();
    }
}

function renderRevenueSummary(summary) {
    const reportRevenue = document.getElementById("reportRevenue");
    const reportPaidTicket = document.getElementById("reportPaidTicket");
    const topTripRevenue = document.getElementById("topTripRevenue");

    if (reportRevenue) {
        reportRevenue.textContent = money(summary.totalRevenue || 0);
    }

    if (reportPaidTicket) {
        reportPaidTicket.textContent = summary.paidTicketCount || 0;
    }

    if (topTripRevenue) {
        topTripRevenue.textContent = summary.topTripId || "---";
    }
}

function renderRevenueSummaryFallback() {
    const reportRevenue = document.getElementById("reportRevenue");
    const reportPaidTicket = document.getElementById("reportPaidTicket");
    const topTripRevenue = document.getElementById("topTripRevenue");

    if (reportRevenue) {
        reportRevenue.textContent = "0đ";
    }

    if (reportPaidTicket) {
        reportPaidTicket.textContent = "0";
    }

    if (topTripRevenue) {
        topTripRevenue.textContent = "---";
    }
}

function renderReport() {
    const report = document.getElementById("tripRevenueReport");

    if (!report) return;

    const tripReports = trips.map(trip => {
        const tripBookings = bookings.filter(item =>
            item.tripId === trip.id &&
            item.payment === "Đã thanh toán"
        );

        const revenue = tripBookings.reduce((sum, item) => sum + Number(item.price || 0), 0);
        const ticketCount = tripBookings.length;
        const bus = getBusById(trip.busId);
        const totalSeats = bus ? Number(bus.seats || bus.seatCount || 1) : 1;

        let fillRate = 0;

        if (totalSeats > 0) {
            fillRate = Math.min(
                100,
                Math.round(((totalSeats - Number(trip.emptySeats || 0)) / totalSeats) * 100)
            );
        }

        return {
            ...trip,
            revenue,
            ticketCount,
            fillRate,
            busPlate: bus ? bus.plate : "Không rõ xe"
        };
    });

    if (!tripReports.length) {
        report.innerHTML = `
            <div class="alert alert-warning mb-0">
                Chưa có dữ liệu chuyến xe để báo cáo.
            </div>
        `;
        return;
    }

    report.innerHTML = tripReports.map(item => `
        <div class="trip-report-card">
            <div class="trip-report-head">
                <h5>${item.id} - ${item.route || "Chưa có tuyến"}</h5>
                <strong>${money(item.revenue)}</strong>
            </div>

            <p class="mb-2">
                Xe: <strong>${item.busPlate}</strong> |
                Ngày đi: <strong>${item.date || "---"}</strong> |
                Số vé bán: <strong>${item.ticketCount}</strong> |
                Lấp đầy: <strong>${item.fillRate}%</strong>
            </p>

            <div class="progress">
                <div class="progress-bar" style="width:${item.fillRate}%"></div>
            </div>
        </div>
    `).join("");
}