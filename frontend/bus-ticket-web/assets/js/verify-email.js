const API_BASE_URL = "http://localhost:8080";

function showAuthMessage(message, type = "error") {
    const box = document.getElementById("authMessage");

    if (!box) return;

    box.textContent = message;
    box.className = type === "success"
        ? "alert alert-success mt-3"
        : "alert alert-danger mt-3";
}

document.addEventListener("DOMContentLoaded", async function () {
    const token = new URLSearchParams(window.location.search).get("token") || "";

    if (!token) {
        showAuthMessage("Link xac thuc khong hop le.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/auth/verify-email?token=${encodeURIComponent(token)}`);
        const result = await response.json();

        if (!response.ok || !result.success) {
            showAuthMessage(result.message || "Khong the xac thuc email.");
            return;
        }

        showAuthMessage(result.message, "success");

        setTimeout(function () {
            window.location.href = "login.html";
        }, 1500);
    } catch (error) {
        console.error("Loi xac thuc email:", error);
        showAuthMessage("Khong the ket noi den server.");
    }
});
