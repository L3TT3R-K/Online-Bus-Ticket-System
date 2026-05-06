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
