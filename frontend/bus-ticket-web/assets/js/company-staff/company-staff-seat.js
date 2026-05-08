let seatTrips = [];

async function loadSeatTrips() {
    const select = document.getElementById("seatTripSelect");
    const seatMap = document.getElementById("seatMap");

    if (!select) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải danh sách chuyến xe:", result.message || result);
            select.innerHTML = `<option value="">Không tải được chuyến xe</option>`;

            if (seatMap) {
                seatMap.innerHTML = `<div class="alert alert-danger">Không tải được danh sách chuyến xe.</div>`;
            }

            return;
        }

        seatTrips = Array.isArray(result)
            ? result.map(item => ({
                id: item.maChuyen,
                maChuyen: item.maChuyen,
                maXe: item.maXe,
                bienSo: item.bienSo,
                route: item.tenTuyen,
                date: item.ngayDi,
                time: item.gioDi ? String(item.gioDi).substring(0, 5) : "",
                status: item.trangThai || "Không rõ"
            }))
            : [];

        renderSeatTripOptions();

        if (seatTrips.length) {
            select.value = seatTrips[0].id;
            await renderSeatMap();
        } else if (seatMap) {
            seatMap.innerHTML = `<div class="alert alert-warning">Chưa có chuyến xe.</div>`;
        }
    } catch (error) {
        console.error("Lỗi gọi API chuyến xe cho sơ đồ ghế:", error);
        select.innerHTML = `<option value="">Không thể kết nối server</option>`;

        if (seatMap) {
            seatMap.innerHTML = `<div class="alert alert-danger">Không thể kết nối server.</div>`;
        }
    }
}

function renderSeatTripOptions() {
    const select = document.getElementById("seatTripSelect");

    if (!select) return;

    if (!Array.isArray(seatTrips) || !seatTrips.length) {
        select.innerHTML = `<option value="">Chưa có chuyến xe</option>`;
        return;
    }

    const currentValue = select.value;

    select.innerHTML = seatTrips.map(trip => `
        <option value="${trip.id}">
            ${trip.id} - ${trip.route || "Chưa có tuyến"} - ${trip.date || ""} ${trip.time || ""}
        </option>
    `).join("");

    if (currentValue && seatTrips.some(trip => trip.id === currentValue)) {
        select.value = currentValue;
    }
}

async function renderSeatMap() {
    const seatMap = document.getElementById("seatMap");
    const select = document.getElementById("seatTripSelect");

    if (!seatMap) return;

    const tripId = select?.value || seatTrips[0]?.id || trips[0]?.id;

    if (!tripId) {
        seatMap.innerHTML = `<div class="alert alert-warning">Chưa có chuyến xe.</div>`;
        return;
    }

    seatMap.innerHTML = `<div class="text-muted">Đang tải sơ đồ ghế...</div>`;

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${tripId}/ghe`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            seatMap.innerHTML = `
                <div class="alert alert-danger">
                    ${result.message || "Không tải được sơ đồ ghế."}
                </div>
            `;
            return;
        }

        if (!Array.isArray(result) || !result.length) {
            seatMap.innerHTML = `<div class="alert alert-warning">Chuyến này chưa có dữ liệu ghế.</div>`;
            return;
        }

        seatMap.innerHTML = result.map(seat => renderSeatButton(seat)).join("");
    } catch (error) {
        console.error("Lỗi tải sơ đồ ghế:", error);
        seatMap.innerHTML = `<div class="alert alert-danger">Không thể kết nối server.</div>`;
    }
}

function renderSeatButton(seat) {
    const status = seat.trangThai || "TRONG";

    let cls = "seat";

    if (status === "DA_DAT") {
        cls += " booked";
    } else if (status === "DANG_GIU") {
        cls += " holding";
    }

    return `
        <button
            class="${cls}"
            type="button"
            disabled
            title="${mapSeatStatusText(status)}"
        >
            ${seat.soGhe}
        </button>
    `;
}

function mapSeatStatusText(status) {
    switch (status) {
        case "TRONG":
            return "Ghế trống";
        case "DANG_GIU":
            return "Đang giữ";
        case "DA_DAT":
            return "Đã đặt";
        default:
            return "Không rõ";
    }
}