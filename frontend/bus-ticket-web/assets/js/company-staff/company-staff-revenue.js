let revenueTripReports = [];

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
        if (summary.topTripId) {
            topTripRevenue.textContent = summary.topTripId;
        } else {
            topTripRevenue.textContent = "---";
        }
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

async function loadRevenueTrips() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/revenue/trips`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải doanh thu theo chuyến:", result.message || result);
            revenueTripReports = [];
            renderRevenueTrips();
            return;
        }

        revenueTripReports = Array.isArray(result) ? result : [];

        renderRevenueTrips();
    } catch (error) {
        console.error("Lỗi gọi API revenue trips:", error);
        revenueTripReports = [];
        renderRevenueTrips();
    }
}

function renderRevenueTrips() {
    const report = document.getElementById("tripRevenueReport");

    if (!report) return;

    if (!Array.isArray(revenueTripReports) || !revenueTripReports.length) {
        report.innerHTML = `
            <div class="alert alert-warning mb-0">
                Chưa có dữ liệu doanh thu theo chuyến.
            </div>
        `;
        return;
    }

    report.innerHTML = revenueTripReports.map(item => {
        const ngayDi = item.ngayDi || "---";
        const gioDi = item.gioDi ? String(item.gioDi).substring(0, 5) : "";
        const tyLeLapDay = Number(item.tyLeLapDay || 0);
        const doanhThu = Number(item.doanhThu || 0);
        const soVeDaThanhToan = Number(item.soVeDaThanhToan || 0);

        return `
            <div class="trip-report-card">
                <div class="trip-report-head">
                    <h5>${item.maChuyen || "---"} - ${item.tenTuyen || "Chưa có tuyến"}</h5>
                    <strong>${money(doanhThu)}</strong>
                </div>

                <p class="mb-2">
                    Xe: <strong>${item.bienSo || "Không rõ xe"}</strong> |
                    Ngày đi: <strong>${ngayDi} ${gioDi}</strong> |
                    Số vé thanh toán: <strong>${soVeDaThanhToan}</strong> |
                    Lấp đầy: <strong>${tyLeLapDay}%</strong>
                </p>

                <div class="progress">
                    <div class="progress-bar" style="width:${Math.min(100, tyLeLapDay)}%"></div>
                </div>
            </div>
        `;
    }).join("");
}

function renderReport() {
    renderRevenueTrips();
}