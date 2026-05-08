function initMenu() {
    const links = document.querySelectorAll(".staff-menu a");
    const sections = document.querySelectorAll(".content-section");
    const pageTitle = document.getElementById("pageTitle");

    links.forEach(link => {
        link.addEventListener("click", function (event) {
            event.preventDefault();

            links.forEach(item => item.classList.remove("active"));
            this.classList.add("active");

            sections.forEach(section => section.classList.remove("active"));

            const sectionId = this.dataset.section;
            const section = document.getElementById(sectionId);

            if (section) section.classList.add("active");
            if (pageTitle) pageTitle.textContent = this.textContent.trim();
        });
    });

    const logoutBtn = document.getElementById("logoutBtn");

    if (logoutBtn) {
        logoutBtn.addEventListener("click", function () {
            localStorage.removeItem("token");
            localStorage.removeItem("maTK");
            localStorage.removeItem("maNV");
            localStorage.removeItem("maNhaXe");
            localStorage.removeItem("tenNhaXe");
            localStorage.removeItem("role");
            localStorage.removeItem("fullname");

            alert("Đã đăng xuất.");
            window.location.href = "main.html";
        });
    }
}

function initForms() {
    initBusForms();

    // Quan trọng:
    // initTripForm() phải chạy SAU khi loadStations() xong.
    // Không gọi initTripForm ở đây nữa.
}

function initFilters() {
    const tripBusFilter = document.getElementById("tripBusFilter");
    const tripStatusFilter = document.getElementById("tripStatusFilter");
    const bookingSearch = document.getElementById("bookingSearch");
    const bookingPaymentFilter = document.getElementById("bookingPaymentFilter");
    const seatTripSelect = document.getElementById("seatTripSelect");

    if (tripBusFilter) tripBusFilter.addEventListener("change", renderTrips);
    if (tripStatusFilter) tripStatusFilter.addEventListener("change", renderTrips);

    if (bookingSearch) bookingSearch.addEventListener("input", renderBookings);
    if (bookingPaymentFilter) bookingPaymentFilter.addEventListener("change", renderBookings);

    if (seatTripSelect) seatTripSelect.addEventListener("change", renderSeatMap);
}

document.addEventListener("DOMContentLoaded", async function () {
    initMenu();
    initFilters();
    initBusImagesPreview();
    initBusImageManager();
    initBusStatusModal();

    const ok = await initStaffInfo();

    if (!ok) return;

    await loadLoaiXe();
    await loadTienIch();

    // Bắt buộc gọi trước initTripForm()
    await loadStations();

    await loadDashboard();
    await loadMonthlyRevenue();
    await loadStaffBuses();
    await loadStaffTrips();

    initForms();

    // Gọi initTripForm sau khi stations đã có dữ liệu từ /api/ben-xe
    initTripForm();

    renderAddBusImages();
    renderBusOptions();
    renderBuses();
    renderTrips();
    renderBookings();

    await loadSeatTrips();

    renderReport();
});