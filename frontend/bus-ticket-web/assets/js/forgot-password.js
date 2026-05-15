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
    const form = document.getElementById("forgotPasswordForm");
    const button = document.getElementById("forgotPasswordBtn");

    if (!form) return;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const email = document.getElementById("email").value.trim();

        if (!email) {
            showAuthMessage("Vui long nhap email.");
            return;
        }

        try {
            if (button) {
                button.disabled = true;
                button.textContent = "Dang gui...";
            }

            const response = await fetch(`${API_BASE_URL}/api/auth/forgot-password`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ email })
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                showAuthMessage(result.message || "Khong the gui email dat lai mat khau.");
                return;
            }

            showAuthMessage(result.message, "success");
            form.reset();
        } catch (error) {
            console.error("Loi gui email dat lai mat khau:", error);
            showAuthMessage("Khong the ket noi den server.");
        } finally {
            if (button) {
                button.disabled = false;
                button.textContent = "Gui link";
            }
        }
    });
});
