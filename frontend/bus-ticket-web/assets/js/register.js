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
    const registerForm = document.getElementById("registerForm");
    const registerBtn = document.getElementById("registerBtn");

    if (!registerForm) return;

    registerForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const tenDangNhap = document.getElementById("tenDangNhap").value.trim();
        const tenKH = document.getElementById("tenKH").value.trim();
        const sdt = document.getElementById("sdt").value.trim();
        const email = document.getElementById("email").value.trim();
        const matKhau = document.getElementById("password").value.trim();
        const xacNhanMatKhau = document.getElementById("confirmPassword").value.trim();

        if (!tenDangNhap || !tenKH || !sdt || !email || !matKhau || !xacNhanMatKhau) {
            showAuthMessage("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        if (matKhau !== xacNhanMatKhau) {
            showAuthMessage("Mật khẩu xác nhận không khớp.");
            return;
        }

        const data = {
            tenDangNhap: tenDangNhap,
            matKhau: matKhau,
            xacNhanMatKhau: xacNhanMatKhau,
            tenKH: tenKH,
            sdt: sdt,
            email: email
        };

        try {
            if (registerBtn) {
                registerBtn.disabled = true;
                registerBtn.textContent = "Đang đăng ký...";
            }

            const response = await fetch(`${API_BASE_URL}/api/auth/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (result.success) {
                showAuthMessage(result.message, "success");

                registerForm.reset();
            } else {
                showAuthMessage(result.message);
            }

        } catch (error) {
            console.error("Lỗi gọi API đăng ký:", error);
            showAuthMessage("Không thể kết nối đến server. Hãy kiểm tra Spring Boot đã chạy chưa.");
        } finally {
            if (registerBtn) {
                registerBtn.disabled = false;
                registerBtn.textContent = "Đăng ký";
            }
        }
    });
});
