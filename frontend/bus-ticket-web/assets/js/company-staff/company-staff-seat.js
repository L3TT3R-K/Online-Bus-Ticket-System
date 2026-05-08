async function renderSeatMap() {
    const seatMap = document.getElementById("seatMap");

    if (!seatMap) return;

    const tripId = document.getElementById("seatTripSelect")?.value || trips[0]?.id;

    if (!tripId) {
        seatMap.innerHTML = `<div class="alert alert-warning">Chưa có chuyến xe.</div>`;
        return;
    }

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

        seatMap.innerHTML = result.map(seat => {
            let cls = "seat";

            if (seat.trangThai === "DA_DAT") {
                cls += " booked";
            } else if (seat.trangThai === "DANG_GIU") {
                cls += " holding";
            }

            return `
                <button 
                    class="${cls}" 
                    type="button" 
                    disabled 
                    title="${mapSeatStatusText(seat.trangThai)}"
                >
                    ${seat.soGhe}
                </button>
            `;
        }).join("");
    } catch (error) {
        console.error("Lỗi tải sơ đồ ghế:", error);
        seatMap.innerHTML = `<div class="alert alert-danger">Không thể kết nối server.</div>`;
    }
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