function initTripForm() {
    const tripForm = document.getElementById("tripForm");

    if (!tripForm) return;

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

function renderTrips() {
    const tbody = document.getElementById("tripTableBody");

    if (!tbody) return;

    const busFilter = document.getElementById("tripBusFilter")?.value || "";
    const statusFilter = document.getElementById("tripStatusFilter")?.value || "";

    let data = [...trips];

    if (busFilter) data = data.filter(trip => trip.busId === busFilter);
    if (statusFilter) data = data.filter(trip => trip.status === statusFilter);

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
