const API_BASE_URL = "http://localhost:8080";

function showAuthMessage(message, type = "error") {
    const box = document.getElementById("authMessage");

    if (!box) return;

    box.textContent = message;
    box.className = type === "success"
        ? "alert alert-success mt-3"
        : "alert alert-danger mt-3";
}

document.addEventListener("DOMContentLoaded", function () {
    const loginForm = document.getElementById("loginForm");
    const togglePassword = document.getElementById("togglePassword");
    const loginBtn = loginForm ? loginForm.querySelector("button[type='submit']") : null;

    if (togglePassword) {
        togglePassword.addEventListener("click", function () {
            const passwordInput = document.getElementById("password");
            const isPassword = passwordInput.type === "password";

            passwordInput.type = isPassword ? "text" : "password";
            this.innerHTML = isPassword
                ? '<i class="fa-solid fa-eye-slash"></i>'
                : '<i class="fa-solid fa-eye"></i>';
        });
    }

    if (!loginForm) return;

    loginForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const tenDangNhap = document.getElementById("username").value.trim();
        const matKhau = document.getElementById("password").value.trim();

        if (!tenDangNhap || !matKhau) {
            showAuthMessage("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        const data = {
            tenDangNhap: tenDangNhap,
            matKhau: matKhau
        };

        try {
            if (loginBtn) {
                loginBtn.disabled = true;
                loginBtn.textContent = "Đang đăng nhập...";
            }

            const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (result.success) {
                localStorage.setItem("token", result.token || "");
                localStorage.setItem("role", result.quyen || "");
                localStorage.setItem("fullname", result.tenKH || result.tenDangNhap || "");

                showAuthMessage(result.message || "Đăng nhập thành công.", "success");

                setTimeout(function () {
                    window.location.href = "main.html";
                }, 1000);
            } else {
                showAuthMessage(result.message || "Sai tài khoản hoặc mật khẩu.");
            }

        } catch (error) {
            console.error("Lỗi gọi API đăng nhập:", error);
            showAuthMessage("Không thể kết nối đến server. Hãy kiểm tra Spring Boot đã chạy chưa.");
        } finally {
            if (loginBtn) {
                loginBtn.disabled = false;
                loginBtn.textContent = "Đăng nhập";
            }
        }
    });
});