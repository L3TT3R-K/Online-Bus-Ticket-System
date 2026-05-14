window.__BUS_TICKET_API_BASE_URL__ = window.__BUS_TICKET_API_BASE_URL__ || "http://localhost:8080";

let authProfilePromise = null;

function getAuthToken() {
    return localStorage.getItem("token") || sessionStorage.getItem("token") || "";
}

function buildAuthHeaders() {
    const headers = {
        "Content-Type": "application/json"
    };

    const token = getAuthToken();

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return headers;
}

function clearAuthState() {
    authProfilePromise = null;

    [
        "token",
        "maTK",
        "maKH",
        "maKhachHang",
        "maKh",
        "role",
        "fullname",
        "tenDangNhap",
        "authContext"
    ].forEach(function (key) {
        localStorage.removeItem(key);
        sessionStorage.removeItem(key);
    });
}

function getCurrentAccountIdFromToken() {
    const token = getAuthToken();

    if (!token) return "";

    const parts = token.split(".");

    if (parts.length < 2) return "";

    try {
        const payload = JSON.parse(atob(parts[1].replace(/-/g, "+").replace(/_/g, "/")));
        return String(payload.maTK || payload.sub || "").trim();
    } catch (error) {
        return "";
    }
}

async function getCurrentAccountProfile(forceReload = false) {
    if (forceReload) {
        authProfilePromise = null;
    }

    if (authProfilePromise) {
        return authProfilePromise;
    }

    const token = getAuthToken();

    if (!token) {
        return null;
    }

    authProfilePromise = fetch(`${window.__BUS_TICKET_API_BASE_URL__}/api/auth/me`, {
        method: "GET",
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
        .then(async function (response) {
            const result = await response.json().catch(() => null);

            if (!response.ok || !result || result.success === false) {
                throw new Error(result?.message || "Không thể tải thông tin tài khoản.");
            }

            const profile = result.data || result;

            if (profile.tenKH) {
                localStorage.setItem("fullname", profile.tenKH);
            } else if (profile.tenDangNhap) {
                localStorage.setItem("fullname", profile.tenDangNhap);
            }

            if (profile.quyen) {
                localStorage.setItem("role", profile.quyen);
            }

            const maKhachHang = profile.maKhachHang || profile.maKH || profile.maKh || "";
            if (maKhachHang) {
                localStorage.setItem("maKhachHang", maKhachHang);
                localStorage.setItem("maKH", maKhachHang);
                sessionStorage.setItem("maKhachHang", maKhachHang);
                sessionStorage.setItem("maKH", maKhachHang);
            }

            localStorage.setItem("authContext", JSON.stringify(profile));

            return profile;
        })
        .catch(function (error) {
            authProfilePromise = null;
            throw error;
        });

    return authProfilePromise;
}
