const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    updateMainPageUser();
    bindHomeSearch();
});

function updateMainPageUser() {
    const fullname = localStorage.getItem("fullname");
    const token = localStorage.getItem("token");

    if (!token || !fullname) return;

    const navLoginItem = document.getElementById("navLoginItem");
    const navRegisterItem = document.getElementById("navRegisterItem");
    const heroAuthActions = document.getElementById("heroAuthActions");

    if (navLoginItem) {
        navLoginItem.innerHTML = "";

        const welcomeText = document.createElement("span");
        welcomeText.className = "navbar-welcome";
        welcomeText.textContent = "Welcome, " + fullname;

        navLoginItem.appendChild(welcomeText);
    }

    if (navRegisterItem) {
        navRegisterItem.innerHTML = "";

        const logoutButton = document.createElement("button");
        logoutButton.type = "button";
        logoutButton.className = "btn btn-register";
        logoutButton.textContent = "Đăng xuất";
        logoutButton.addEventListener("click", logoutUser);

        navRegisterItem.appendChild(logoutButton);
    }

    if (heroAuthActions) {
        heroAuthActions.innerHTML = "";

        const welcomeTitle = document.createElement("div");
        welcomeTitle.className = "hero-welcome";
        welcomeTitle.textContent = "Welcome, " + fullname;

        const ticketLink = document.createElement("a");
        ticketLink.href = "my-ticket.html";
        ticketLink.className = "btn btn-login mt-3";
        ticketLink.textContent = "Xem vé của bạn";

        heroAuthActions.appendChild(welcomeTitle);
        heroAuthActions.appendChild(ticketLink);
    }
}

function logoutUser() {
    localStorage.removeItem("token");
    localStorage.removeItem("role");
    localStorage.removeItem("fullname");

    window.location.href = "login.html";
}

function bindHomeSearch() {
    const form = document.getElementById("homeSearchForm");

    if (!form) return;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const from = document.getElementById("from").value.trim();
        const to = document.getElementById("to").value.trim();
        const date = document.getElementById("date").value;

        const diemDi = from || "TP. Hồ Chí Minh";
        const diemDen = to || "Đà Lạt";
        const ngayDi = date || "2026-04-25";

        const searchButton = form.querySelector("button[type='submit']");

        try {
            if (searchButton) {
                searchButton.disabled = true;
                searchButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tìm...';
            }

            const params = new URLSearchParams({
                diemDi: diemDi,
                diemDen: diemDen,
                ngayDi: ngayDi
            });

            const response = await fetch(`${API_BASE_URL}/api/chuyen-xe/search?${params.toString()}`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json"
                }
            });

            const result = await response.json();

            if (!response.ok) {
                const message = result.message || "Không thể tìm chuyến xe.";
                alert(message);
                return;
            }

            sessionStorage.setItem("tripSearchResults", JSON.stringify(result));

            sessionStorage.setItem("tripSearchParams", JSON.stringify({
                from: diemDi,
                to: diemDen,
                date: ngayDi
            }));

            const pageParams = new URLSearchParams({
                from: diemDi,
                to: diemDen,
                date: ngayDi
            });

            window.location.href = "search-result.html?" + pageParams.toString();

        } catch (error) {
            console.error("Lỗi gọi API tìm chuyến xe:", error);
            alert("Không thể kết nối đến server. Hãy kiểm tra Spring Boot đã chạy chưa.");
        } finally {
            if (searchButton) {
                searchButton.disabled = false;
                searchButton.innerHTML = '<i class="fa-solid fa-magnifying-glass"></i> Tìm vé';
            }
        }
    });
}