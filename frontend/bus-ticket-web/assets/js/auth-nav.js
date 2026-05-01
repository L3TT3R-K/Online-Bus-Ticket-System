document.addEventListener("DOMContentLoaded", function () {
    if (window.location.pathname.endsWith("/main.html") || window.location.pathname.endsWith("main.html")) {
        return;
    }

    const token = localStorage.getItem("token");

    if (!token) return;

    const nav = document.querySelector(".main-nav .navbar-nav");

    if (!nav) return;

    const loginButton = nav.querySelector("a.btn-login[href='login.html']");
    const registerButton = nav.querySelector("a.btn-register[href='register.html']");

    if (loginButton) {
        loginButton.textContent = "Tài khoản";
        loginButton.href = "account.html";
        loginButton.classList.remove("btn-login");
        loginButton.classList.add("btn-register");
    }

    if (registerButton && registerButton.parentElement) {
        registerButton.parentElement.remove();
    }
});