const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    handleLoginState();
    loadProfileFromApi();
    handleProfileSubmit();
    handlePasswordSubmit();
    handleTogglePassword();
});

function handleLoginState() {
    const token = localStorage.getItem("token");
    const fullname = localStorage.getItem("fullname") || "Người dùng";
    const role = localStorage.getItem("role") || "KhachHang";

    const loginNavItem = document.getElementById("loginNavItem");
    const registerNavItem = document.getElementById("registerNavItem");
    const userNavItem = document.getElementById("userNavItem");
    const userFullName = document.getElementById("userFullName");
    const logoutBtn = document.getElementById("logoutBtn");
    const authWarning = document.getElementById("authWarning");

    if (token) {
        if (loginNavItem) loginNavItem.classList.add("d-none");
        if (registerNavItem) registerNavItem.classList.add("d-none");
        if (userNavItem) userNavItem.classList.remove("d-none");
        if (userFullName) userFullName.textContent = fullname;
        if (authWarning) authWarning.classList.add("d-none");
    } else {
        if (loginNavItem) loginNavItem.classList.remove("d-none");
        if (registerNavItem) registerNavItem.classList.remove("d-none");
        if (userNavItem) userNavItem.classList.add("d-none");
        if (authWarning) authWarning.classList.remove("d-none");
    }

    if (logoutBtn) {
        logoutBtn.addEventListener("click", function () {
            localStorage.removeItem("token");
            localStorage.removeItem("maTK");
            localStorage.removeItem("role");
            localStorage.removeItem("fullname");

            alert("Đăng xuất thành công.");
            window.location.href = "main.html";
        });
    }

    const sidebarName = document.getElementById("sidebarName");
    const sidebarRole = document.getElementById("sidebarRole");

    if (sidebarName) sidebarName.textContent = fullname;
    if (sidebarRole) sidebarRole.textContent = role === "Admin" ? "Quản trị viên" : "Khách hàng";
}

async function loadProfileFromApi() {
    const token = localStorage.getItem("token");
    const maTK = localStorage.getItem("maTK");

    if (!token || !maTK) {
        showMessage("profileMessage", "Bạn cần đăng nhập để xem thông tin tài khoản.", "warning");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/account/${maTK}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const result = await response.json();

        if (!response.ok || !result.success) {
            showMessage("profileMessage", result.message || "Không thể tải thông tin tài khoản.", "danger");
            return;
        }

        document.getElementById("fullName").value = result.tenKH || "";
        document.getElementById("username").value = result.tenDangNhap || "";
        document.getElementById("phone").value = result.sdt || "";
        document.getElementById("email").value = result.email || "";
        document.getElementById("birthday").value = result.ngaySinh || "";
        document.getElementById("gender").value = result.gioiTinh || "";

        // Địa chỉ và ghi chú không dùng ở phiên bản này

        localStorage.setItem("fullname", result.tenKH || "");
        localStorage.setItem("role", result.quyen || "");

        updateUserName(result.tenKH);

        const sidebarRole = document.getElementById("sidebarRole");
        if (sidebarRole) {
            sidebarRole.textContent = result.quyen === "Admin" ? "Quản trị viên" : "Khách hàng";
        }

    } catch (error) {
        console.error("Lỗi gọi API lấy tài khoản:", error);
        showMessage("profileMessage", "Không thể kết nối đến server. Hãy kiểm tra Spring Boot đã chạy chưa.", "danger");
    }
}

function handleProfileSubmit() {
    const profileForm = document.getElementById("profileForm");

    if (!profileForm) return;

    profileForm.addEventListener("submit", function (event) {
        event.preventDefault();

        showMessage(
            "profileMessage",
            "Phần cập nhật thông tin cần API PUT /api/account/{maTK}. Hiện tại mới nối API GET lấy thông tin.",
            "warning"
        );
    });
}

function handlePasswordSubmit() {
    const passwordForm = document.getElementById("passwordForm");

    if (!passwordForm) return;

    passwordForm.addEventListener("submit", function (event) {
        event.preventDefault();

        const currentPassword = document.getElementById("currentPassword").value.trim();
        const newPassword = document.getElementById("newPassword").value.trim();
        const confirmNewPassword = document.getElementById("confirmNewPassword").value.trim();

        if (!currentPassword || !newPassword || !confirmNewPassword) {
            showMessage("passwordMessage", "Vui lòng nhập đầy đủ thông tin mật khẩu.", "danger");
            return;
        }

        if (newPassword.length < 6) {
            showMessage("passwordMessage", "Mật khẩu mới phải có ít nhất 6 ký tự.", "danger");
            return;
        }

        if (newPassword !== confirmNewPassword) {
            showMessage("passwordMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.", "danger");
            return;
        }

        showMessage(
            "passwordMessage",
            "Phần đổi mật khẩu cần API PUT /api/account/{maTK}/password.",
            "warning"
        );
    });
}

function handleTogglePassword() {
    document.querySelectorAll(".toggle-password").forEach(function (button) {
        button.addEventListener("click", function () {
            const targetId = this.getAttribute("data-target");
            const input = document.getElementById(targetId);

            if (!input) return;

            const isPassword = input.type === "password";

            input.type = isPassword ? "text" : "password";

            this.innerHTML = isPassword
                ? '<i class="fa-solid fa-eye-slash"></i>'
                : '<i class="fa-solid fa-eye"></i>';
        });
    });
}

function updateUserName(fullName) {
    const sidebarName = document.getElementById("sidebarName");
    const userFullName = document.getElementById("userFullName");

    if (sidebarName) {
        sidebarName.textContent = fullName || "Người dùng";
    }

    if (userFullName) {
        userFullName.textContent = fullName || "Người dùng";
    }
}

function showMessage(elementId, message, type) {
    const box = document.getElementById(elementId);

    if (!box) return;

    box.innerHTML = `
        <div class="alert alert-${type} mb-0">
            ${message}
        </div>
    `;
}