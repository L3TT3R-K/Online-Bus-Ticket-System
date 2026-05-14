const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    handleLoginState();
    loadProfileFromApi();
    handleProfileSubmit();
    handlePasswordSubmit();
    handleTogglePassword();
});

function handleLoginState() {
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");
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
            if (typeof clearAuthState === "function") {
                clearAuthState();
            } else {
                localStorage.removeItem("token");
                localStorage.removeItem("maTK");
                localStorage.removeItem("role");
                localStorage.removeItem("fullname");
            }

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
    const profile = await getCurrentAccountProfile().catch(function (error) {
        console.error("Lỗi gọi API lấy hồ sơ:", error);
        return null;
    });

    if (!profile) {
        showMessage("profileMessage", "Bạn cần đăng nhập để xem thông tin tài khoản.", "warning");
        return;
    }

    document.getElementById("fullName").value = profile.tenKH || "";
    document.getElementById("username").value = profile.tenDangNhap || "";
    document.getElementById("username").readOnly = true;
    document.getElementById("phone").value = profile.sdt || "";
    document.getElementById("email").value = profile.email || "";
    document.getElementById("birthday").value = profile.ngaySinh || "";
    document.getElementById("gender").value = profile.gioiTinh || "";

    localStorage.setItem("fullname", profile.tenKH || profile.tenDangNhap || "");
    localStorage.setItem("role", profile.quyen || "");
    cacheCustomerId(profile);

    updateUserName(profile.tenKH || profile.tenDangNhap);

    const sidebarRole = document.getElementById("sidebarRole");
    if (sidebarRole) {
        sidebarRole.textContent = profile.quyen === "Admin" ? "Quản trị viên" : "Khách hàng";
    }
}

function handleProfileSubmit() {
    const profileForm = document.getElementById("profileForm");

    if (!profileForm) return;

    profileForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const profile = await getCurrentAccountProfile().catch(function (error) {
            console.error("Lỗi lấy hồ sơ hiện tại:", error);
            return null;
        });

        const maTK = profile?.maTK || getCurrentAccountIdFromToken();

        if (!maTK) {
            showMessage("profileMessage", "Bạn cần đăng nhập để cập nhật thông tin.", "warning");
            return;
        }

        const tenKH = document.getElementById("fullName").value.trim();
        const sdt = document.getElementById("phone").value.trim();
        const email = document.getElementById("email").value.trim();
        const ngaySinh = document.getElementById("birthday").value;
        const gioiTinh = document.getElementById("gender").value;

        if (!tenKH || !sdt || !email) {
            showMessage("profileMessage", "Vui lòng nhập đầy đủ họ tên, số điện thoại và email.", "danger");
            return;
        }

        const data = {
            tenKH: tenKH,
            sdt: sdt,
            email: email,
            ngaySinh: ngaySinh || null,
            gioiTinh: gioiTinh || null
        };

        try {
            const response = await fetch(`${API_BASE_URL}/api/account/${maTK}`, {
                method: "PUT",
                headers: getAccountAuthHeaders(),
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                showMessage("profileMessage", result.message || "Cập nhật thông tin thất bại.", "danger");
                return;
            }

            localStorage.setItem("fullname", result.tenKH || tenKH);
            localStorage.setItem("role", result.quyen || localStorage.getItem("role") || "");
            cacheCustomerId(result);
            await getCurrentAccountProfile(true).catch(() => null);

            updateUserName(result.tenKH || tenKH);

            showMessage("profileMessage", result.message || "Cập nhật thông tin thành công.", "success");

        } catch (error) {
            console.error("Lỗi cập nhật thông tin:", error);
            showMessage("profileMessage", "Không thể kết nối đến server.", "danger");
        }
    });
}

function handlePasswordSubmit() {
    const passwordForm = document.getElementById("passwordForm");

    if (!passwordForm) return;

    passwordForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const profile = await getCurrentAccountProfile().catch(function (error) {
            console.error("Lỗi lấy hồ sơ hiện tại:", error);
            return null;
        });

        const maTK = profile?.maTK || getCurrentAccountIdFromToken();

        if (!maTK) {
            showMessage("passwordMessage", "Bạn cần đăng nhập để đổi mật khẩu.", "warning");
            return;
        }

        const matKhauCu = document.getElementById("currentPassword").value.trim();
        const matKhauMoi = document.getElementById("newPassword").value.trim();
        const xacNhanMatKhauMoi = document.getElementById("confirmNewPassword").value.trim();

        if (!matKhauCu || !matKhauMoi || !xacNhanMatKhauMoi) {
            showMessage("passwordMessage", "Vui lòng nhập đầy đủ thông tin mật khẩu.", "danger");
            return;
        }

        if (matKhauMoi.length < 6) {
            showMessage("passwordMessage", "Mật khẩu mới phải có ít nhất 6 ký tự.", "danger");
            return;
        }

        if (matKhauMoi !== xacNhanMatKhauMoi) {
            showMessage("passwordMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.", "danger");
            return;
        }

        const data = {
            matKhauCu: matKhauCu,
            matKhauMoi: matKhauMoi,
            xacNhanMatKhauMoi: xacNhanMatKhauMoi
        };

        try {
            const response = await fetch(`${API_BASE_URL}/api/account/${maTK}/password`, {
                method: "PUT",
                headers: getAccountAuthHeaders(),
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (!response.ok || !result.success) {
                showMessage("passwordMessage", result.message || "Đổi mật khẩu thất bại.", "danger");
                return;
            }

            showMessage("passwordMessage", result.message || "Đổi mật khẩu thành công.", "success");
            passwordForm.reset();

        } catch (error) {
            console.error("Lỗi đổi mật khẩu:", error);
            showMessage("passwordMessage", "Không thể kết nối đến server.", "danger");
        }
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

function getAccountAuthHeaders() {
    if (typeof buildAuthHeaders === "function") {
        return buildAuthHeaders();
    }

    const headers = { "Content-Type": "application/json" };
    const token = localStorage.getItem("token") || sessionStorage.getItem("token");

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

function cacheCustomerId(profile) {
    if (!profile) return;

    const maKhachHang = profile.maKhachHang || profile.maKH || profile.maKh || "";

    if (!maKhachHang) return;

    localStorage.setItem("maKhachHang", maKhachHang);
    localStorage.setItem("maKH", maKhachHang);
    sessionStorage.setItem("maKhachHang", maKhachHang);
    sessionStorage.setItem("maKH", maKhachHang);
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
