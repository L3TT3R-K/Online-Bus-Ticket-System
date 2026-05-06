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
