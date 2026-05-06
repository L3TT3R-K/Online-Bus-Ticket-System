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
