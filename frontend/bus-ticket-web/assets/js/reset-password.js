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
    const form = document.getElementById("resetPasswordForm");
    const button = document.getElementById("resetPasswordBtn");
    const token = new URLSearchParams(window.location.search).get("token") || "";

    if (!token) {
        showAuthMessage("Link dat lai mat khau khong hop le.");
        if (button) button.disabled = true;
        return;
    }

    if (!form) return;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const matKhauMoi = document.getElementById("newPassword").value.trim();
        const xacNhanMatKhauMoi = document.getElementById("confirmNewPassword").value.trim();

        if (!matKhauMoi || !xacNhanMatKhauMoi) {
            showAuthMessage("Vui long nhap day du mat khau moi.");
            return;
        }

        if (matKhauMoi !== xacNhanMatKhauMoi) {
            showAuthMessage("Mat khau xac nhan khong khop.");
            return;
        }

        try {
            if (button) {
                button.disabled = true;
                button.textContent = "Dang cap nhat...";
            }

            const response = await fetch(`${API_BASE_URL}/api/auth/reset-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    token,
                    matKhauMoi,
                    xacNhanMatKhauMoi
                })
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                showAuthMessage(result.message || "Khong the dat lai mat khau.");
                return;
            }

            showAuthMessage(result.message, "success");
            form.reset();

            setTimeout(function () {
                window.location.href = "login.html";
            }, 1500);
        } catch (error) {
            console.error("Loi dat lai mat khau:", error);
            showAuthMessage("Khong the ket noi den server.");
        } finally {
            if (button) {
                button.disabled = false;
                button.textContent = "Cap nhat mat khau";
            }
        }
    });
});
