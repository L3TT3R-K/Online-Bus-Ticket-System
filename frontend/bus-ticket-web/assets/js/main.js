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
        logoutButton.textContent = "\u0110\u0103ng xu\u1ea5t";
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
        ticketLink.textContent = "Xem v\u00e9 c\u1ee7a b\u1ea1n";

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

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        const from = document.getElementById("from").value.trim();
        const to = document.getElementById("to").value.trim();
        const date = document.getElementById("date").value;
        const query = new URLSearchParams({
            from: from || "TP. H\u1ed3 Ch\u00ed Minh",
            to: to || "\u0110\u00e0 L\u1ea1t",
            date: date || "2026-04-25"
        });

        window.location.href = "search-result.html?" + query.toString();
    });
}
