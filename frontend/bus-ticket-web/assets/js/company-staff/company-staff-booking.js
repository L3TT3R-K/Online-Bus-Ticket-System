async function loadStaffBookings() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/ve`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải vé:", result.message || result);
            bookings = [];
            renderBookings();
            return;
        }

        bookings = Array.isArray(result)
            ? result.map(item => ({
                id: item.maVe,
                customer: item.tenKhachHang || "Khách hàng",
                phone: item.soDienThoai || "",
                tripId: item.maChuyen,
                route: item.tenTuyen || item.maChuyen,
                seats: item.soGhe || "",
                price: Number(item.giaTien || 0),
                payment: item.trangThaiThanhToan || "Chờ thanh toán",
                ticketStatus: item.trangThaiVe || "",
                time: item.thoiGianDat || null
            }))
            : [];

        renderBookings();
    } catch (error) {
        console.error("Lỗi gọi API vé:", error);
        bookings = [];
        renderBookings();
    }
}

function renderBookings() {
    const tbody = document.getElementById("bookingTableBody");

    if (!tbody) return;

    const keyword = document.getElementById("bookingSearch")?.value.toLowerCase() || "";
    const paymentFilter = document.getElementById("bookingPaymentFilter")?.value || "";

    let data = [...bookings];

    if (keyword) {
        data = data.filter(item =>
            String(item.id || "").toLowerCase().includes(keyword) ||
            String(item.customer || "").toLowerCase().includes(keyword) ||
            String(item.phone || "").includes(keyword)
        );
    }

    if (paymentFilter) {
        data = data.filter(item => item.payment === paymentFilter);
    }

    if (!data.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-muted py-4">
                    Chưa có vé nào.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = data.map(item => `
        <tr>
            <td>
                ${item.id}
                ${
                    item.ticketStatus
                        ? `<div class="text-muted small">${item.ticketStatus}</div>`
                        : ""
                }
            </td>
            <td>${item.customer}</td>
            <td>${item.phone || "---"}</td>
            <td>${item.route || item.tripId}</td>
            <td>${item.seats}</td>
            <td>${money(item.price)}</td>
            <td>${paymentBadge(item.payment)}</td>
        </tr>
    `).join("");
}