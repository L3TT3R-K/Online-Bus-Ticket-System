const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    setupHomeSearchDefaults();
    updateMainPageUser();
    bindHomeSearch();
});

function setupHomeSearchDefaults() {
    const dateInput = document.getElementById("date");
    const today = formatDateForInput(new Date());

    if (dateInput && !dateInput.value) {
        dateInput.value = today;
    }

    if (dateInput) {
        dateInput.min = today;
    }

    populateStationSuggestions();
}

async function populateStationSuggestions() {
    const datalist = document.getElementById("stationSuggestions");

    if (!datalist) return;

    const stationNames = new Set();
    const stationOptions = [];

    try {
        const response = await fetch(`${API_BASE_URL}/api/ben-xe`);
        if (!response.ok) {
            throw new Error("Khong the tai danh sach ben xe.");
        }

        const result = await response.json();
        const stations = Array.isArray(result) ? result : [];

        stations.forEach(station => {
            const name = station.tenBen?.trim();
            if (!name || stationNames.has(name)) return;

            stationNames.add(name);
            stationOptions.push({
                value: name,
                label: station.diaChi?.trim() || station.maBen || ""
            });
        });
    } catch (error) {
        console.error("Không thể tải danh sách bến xe:", error);
        populateStationSuggestionsFromTrips(stationNames, stationOptions);
    }

    datalist.innerHTML = stationOptions
        .sort((left, right) => left.value.localeCompare(right.value, "vi"))
        .map(station => {
            const label = station.label ? ` label="${escapeHtml(station.label)}"` : "";
            return `<option value="${escapeHtml(station.value)}"${label}></option>`;
        })
        .join("");
}

function populateStationSuggestionsFromTrips(stationNames, stationOptions) {
    if (typeof BUS_TRIPS === "undefined" || !Array.isArray(BUS_TRIPS)) return;

    BUS_TRIPS.forEach(trip => {
        [trip.from, trip.to, trip.startStation, trip.endStation].forEach(name => {
            const stationName = name?.trim();
            if (stationName && !stationNames.has(stationName)) {
                stationNames.add(stationName);
                stationOptions.push({ value: stationName, label: "" });
            }
        });
    });
}

function formatDateForInput(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

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
        welcomeText.textContent = "Xin chào, " + fullname;

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
        welcomeTitle.textContent = "Xin chào, " + fullname;

        const ticketLink = document.createElement("a");
        ticketLink.href = "my-ticket.html";
        ticketLink.className = "btn btn-login mt-3";
        ticketLink.textContent = "Xem vé của bạn";

        heroAuthActions.appendChild(welcomeTitle);
        heroAuthActions.appendChild(ticketLink);
    }
}

function logoutUser() {
    if (typeof clearAuthState === "function") {
        clearAuthState();
    } else {
        localStorage.removeItem("token");
        localStorage.removeItem("maTK");
        localStorage.removeItem("maKH");
        localStorage.removeItem("role");
        localStorage.removeItem("fullname");
    }

    window.location.href = "login.html";
}

function bindHomeSearch() {
    const form = document.getElementById("homeSearchForm");

    if (!form) return;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const fromInput = document.getElementById("from");
        const toInput = document.getElementById("to");
        const dateInput = document.getElementById("date");

        const diemDi = fromInput?.value?.trim() || "";
        const diemDen = toInput?.value?.trim() || "";
        const ngayDi = dateInput?.value || "";

        if (!diemDi) {
            alert("Vui lòng nhập điểm đi.");
            fromInput?.focus();
            return;
        }

        if (!diemDen) {
            alert("Vui lòng nhập điểm đến.");
            toInput?.focus();
            return;
        }

        if (!ngayDi) {
            alert("Vui lòng chọn ngày đi.");
            dateInput?.focus();
            return;
        }

        const today = formatDateForInput(new Date());

        if (ngayDi < today) {
            alert("Vui lòng chọn ngày đi từ hôm nay trở đi.");
            dateInput?.focus();
            return;
        }

        const searchButton = form.querySelector("button[type='submit']");

        try {
            setSearchButtonLoading(searchButton, true);

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
                alert(result.message || "Không thể tìm chuyến xe.");
                return;
            }

            sessionStorage.setItem("tripSearchResults", JSON.stringify(Array.isArray(result) ? result : []));
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

            window.location.href = `search-result.html?${pageParams.toString()}`;
        } catch (error) {
            console.error("Lỗi gọi API tìm chuyến xe:", error);
            alert("Không thể kết nối server. Hãy kiểm tra Spring Boot đã chạy chưa.");
        } finally {
            setSearchButtonLoading(searchButton, false);
        }
    });
}

function setSearchButtonLoading(button, isLoading) {
    if (!button) return;

    if (isLoading) {
        button.disabled = true;
        button.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Đang tìm...`;
        return;
    }

    button.disabled = false;
    button.innerHTML = `<i class="fa-solid fa-magnifying-glass"></i> Tìm vé`;
}
