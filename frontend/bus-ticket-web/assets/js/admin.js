const API_BASE_URL = "http://localhost:8080";

const state = {
  summary: null,
  monthlyRevenue: [],
  topCompanies: [],
  accounts: [],
  customers: [],
  companies: [],
  busStations: [],
  promotions: [],
  staff: [],
  trips: [],
  bookings: [],
  reportSummary: null,
  companyRevenue: []
};

document.addEventListener("DOMContentLoaded", () => {
  initMenu();
  initFilters();
  initPromotionForm();
  initAccountForm();
  initCompanyForm();
  initStationForm();
  initStaffForm();
  initBookingForm();
  bindLogout();
  setAdminName();
  loadAdminData();
});

function bindLogout() {
  const logoutBtn = document.getElementById("logoutBtn");
  if (!logoutBtn) return;

  logoutBtn.onclick = () => {
    localStorage.clear();
    location.href = "main.html";
  };
}

function setAdminName() {
  const adminName = document.getElementById("adminName");
  if (adminName) {
    adminName.textContent = localStorage.getItem("fullname") || "Quản trị viên";
  }
}

function initMenu() {
  document.querySelectorAll(".menu a").forEach((item) => {
    item.onclick = (event) => {
      event.preventDefault();

      document.querySelectorAll(".menu a").forEach((link) => link.classList.remove("active"));
      item.classList.add("active");

      document.querySelectorAll(".page").forEach((section) => section.classList.remove("active"));
      const page = document.getElementById(item.dataset.page);
      if (page) page.classList.add("active");

      const pageTitle = document.getElementById("pageTitle");
      if (pageTitle) pageTitle.textContent = item.textContent.trim();
    };
  });
}

async function loadAdminData() {
  const year = new Date().getFullYear();

  const requests = [
    ["summary", () => requestJson("/api/admin/dashboard/summary")],
    ["monthlyRevenue", () => requestJson(`/api/admin/dashboard/revenue-monthly?year=${year}`)],
    ["topCompanies", () => requestJson("/api/admin/dashboard/top-companies?limit=5")],
    ["accounts", () => requestJson("/api/admin/accounts")],
    ["customers", () => requestJson("/api/admin/customers")],
    ["companies", () => requestJson("/api/admin/companies")],
    ["busStations", () => requestJson(buildStationRequestPath())],
    ["promotions", () => requestJson("/api/admin/khuyen-mai")],
    ["staff", () => requestJson("/api/admin/staff")],
    ["trips", () => requestJson(buildTripRequestPath())],
    ["bookings", () => requestJson("/api/admin/bookings")],
    ["reportSummary", () => requestJson("/api/admin/reports/summary")],
    ["companyRevenue", () => requestJson("/api/admin/reports/company-revenue")]
  ];

  const results = await Promise.allSettled(requests.map((item) => item[1]()));

  results.forEach((result, index) => {
    const key = requests[index][0];

    if (result.status === "fulfilled") {
      state[key] = normalizeResult(result.value);
    } else {
      console.error(`Không tải được ${key}:`, result.reason);
    }
  });

  renderAll();
}

async function loadTrips() {
  try {
    state.trips = normalizeResult(await requestJson(buildTripRequestPath()));
    renderTrips(state.trips);
  } catch (error) {
    console.error("Không tải được trips:", error);
    alert(error.message || "Không thể tải danh sách chuyến xe.");
  }
}

function buildTripRequestPath() {
  const filter = document.getElementById("tripCompanyFilter");
  const tenNhaXe = filter ? filter.value.trim() : "";

  if (!tenNhaXe) {
    return "/api/staff/chuyen-xe";
  }

  return `/api/staff/chuyen-xe?tenNhaXe=${encodeURIComponent(tenNhaXe)}`;
}

async function requestJson(path, options = {}) {
  const token = localStorage.getItem("token");
  const headers = {};

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const method = options.method || "GET";
  let body;

  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
    body = JSON.stringify(options.body);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body
  });

  const raw = await readJsonSafely(response);

  if (!response.ok) {
    throw new Error(extractErrorMessage(raw, `Gọi API thất bại: ${path}`));
  }

  return raw;
}

async function readJsonSafely(response) {
  try {
    return await response.json();
  } catch (error) {
    return null;
  }
}

function extractErrorMessage(payload, fallback) {
  if (!payload) return fallback;
  if (typeof payload === "string") return payload;
  if (payload.message) return payload.message;
  if (payload.error) return payload.error;
  if (payload.detail) return payload.detail;
  return fallback;
}

function normalizeResult(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }

  if (!payload || typeof payload !== "object") {
    return payload;
  }

  if (Array.isArray(payload.data)) return payload.data;
  if (Array.isArray(payload.content)) return payload.content;
  if (Array.isArray(payload.items)) return payload.items;
  if (Array.isArray(payload.result)) return payload.result;
  if (Array.isArray(payload.monthlyRevenue)) return payload.monthlyRevenue;

  if (payload.data && typeof payload.data === "object") return payload.data;
  if (payload.result && typeof payload.result === "object") return payload.result;

  return payload;
}

function renderAll() {
  renderDashboardStats();
  renderAccounts(filterAccounts());
  renderCustomers(state.customers);
  renderCompanies(state.companies);
  renderBusStations(filterBusStations());
  renderPromotions(filterPromotions());
  renderStaff(state.staff);
  renderTrips(state.trips);
  renderBookings(state.bookings);
  renderMonthlyRevenue(state.monthlyRevenue);
  renderTopCompanies(state.topCompanies);
  renderCompanyRevenue(state.companyRevenue);
}

function renderDashboardStats() {
  const summary = state.summary || {};
  const reportSummary = state.reportSummary || summary;

  const paidTickets = (state.bookings || [])
    .filter((booking) => normalizeText(booking.trangThaiThanhToan) === "Thành công")
    .reduce((total, booking) => total + Number(booking.soLuongVe || 0), 0);

  setText("totalAccounts", summary.totalAccounts ?? 0);
  setText("totalTickets", summary.totalTickets ?? 0);
  setText("totalRevenue", formatMoney(summary.totalRevenue ?? 0));
  setText("totalTrips", summary.totalTrips ?? 0);
  setText("reportRevenue", formatMoney(reportSummary.totalRevenue ?? 0));
  setText("paidTickets", paidTickets);

  const topCompany = state.topCompanies[0] || state.companyRevenue[0];
  setText("topCompany", topCompany ? getCompanyName(topCompany) : "-");
}

function renderAccounts(data) {
  setHtml(
    "accountBody",
    (data || []).map((item) => {
      const accountId = item.maTK ?? "";
      const role = getAccountRoleLabel(item.quyen);

      return `
        <tr>
          <td>${escapeHtml(accountId)}</td>
          <td>${escapeHtml(item.tenDangNhap ?? "-")}</td>
          <td>${escapeHtml(role)}</td>
          <td>${escapeHtml(item.tenNguoiDung ?? "-")}</td>
          <td>${escapeHtml(item.email ?? "-")}</td>
          <td>${escapeHtml(item.sdt ?? "-")}</td>
          <td>${statusBadge(item.trangThaiTK, "account")}</td>
          <td>
            <button class="action-btn" type="button" onclick="openAccountEditor('${escapeAttr(accountId)}')">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button class="action-btn danger" type="button" onclick="toggleAccountStatus('${escapeAttr(accountId)}')">
              <i class="fa-solid fa-lock"></i>
            </button>
          </td>
        </tr>
      `;
    })
  );
}

function renderCustomers(data) {
  setHtml(
    "customerBody",
    (data || []).map((item) => `
      <tr>
        <td>${escapeHtml(item.maKH ?? "-")}</td>
        <td>${escapeHtml(item.tenKH ?? "-")}</td>
        <td>${escapeHtml(formatDate(item.ngaySinh))}</td>
        <td>${escapeHtml(item.gioiTinh ?? "-")}</td>
        <td>${escapeHtml(item.sdt ?? "-")}</td>
        <td>${escapeHtml(item.email ?? "-")}</td>
        <td>${statusBadge(item.trangThai, "status")}</td>
        <td>${escapeHtml(item.maTK ?? "-")}</td>
        <td>${escapeHtml(item.tenDangNhap ?? "-")}</td>
      </tr>
    `)
  );
}

function renderCompanies(data) {
  setHtml(
    "companyBody",
    (data || []).map((item) => {
      const maNhaXe = item.maNhaXe ?? "";
      const trangThai = normalizeText(item.trangThai) || "Hoạt động";

      return `
        <tr>
          <td>${escapeHtml(maNhaXe || "-")}</td>
          <td>${escapeHtml(item.tenNhaXe ?? "-")}</td>
          <td>${escapeHtml(item.sdt ?? "-")}</td>
          <td>${escapeHtml(item.email ?? "-")}</td>
          <td>${escapeHtml(item.diaChi ?? "-")}</td>
          <td>${escapeHtml(item.moTa ?? "-")}</td>
          <td>${statusBadge(trangThai, "status")}</td>
          <td>
            <button class="action-btn" type="button" onclick="openCompanyEditor('${escapeAttr(maNhaXe)}')">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button class="action-btn danger" type="button" onclick="toggleCompanyStatus('${escapeAttr(maNhaXe)}')">
              <i class="fa-solid fa-circle-half-stroke"></i>
            </button>
          </td>
        </tr>
      `;
    })
  );
}

function renderBusStations(data) {
  setHtml(
    "stationBody",
    (data || []).map((item) => {
      const stationId = item.maBen ?? "";
      const points = Array.isArray(item.diemBen) ? item.diemBen : [];
      const pointSummary = points.length
        ? points.map((point) => `${point.tenDiem ?? "-"} (${point.loai ?? "-"})`).join(", ")
        : "-";

      return `
        <tr>
          <td>${escapeHtml(stationId || "-")}</td>
          <td>${escapeHtml(item.tenBen ?? "-")}</td>
          <td>${escapeHtml(item.diaChi ?? "-")}</td>
          <td>${escapeHtml(pointSummary)}</td>
          <td>
            <button class="action-btn" type="button" onclick="openStationEditor('${escapeAttr(stationId)}')">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button class="action-btn danger" type="button" onclick="deleteStation('${escapeAttr(stationId)}')">
              <i class="fa-solid fa-trash"></i>
            </button>
          </td>
        </tr>
      `;
    })
  );
}

function renderPromotions(data) {
  setHtml(
    "promotionBody",
    (data || []).map((item) => {
      const maKhuyenMai = item.maKhuyenMai ?? "";
      const discountText = formatPromotionDiscount(item);
      const timeText = `${formatDateTime(item.ngayBatDau)} - ${formatDateTime(item.ngayKetThuc)}`;

      return `
        <tr>
          <td>${escapeHtml(maKhuyenMai || "-")}</td>
          <td>${escapeHtml(item.tenKhuyenMai ?? "-")}</td>
          <td>${escapeHtml(discountText)}</td>
          <td>${escapeHtml(timeText)}</td>
          <td>${statusBadge(item.trangThai, "promotion")}</td>
          <td>
            <button class="action-btn" type="button" onclick="openPromotionEditor('${escapeAttr(maKhuyenMai)}')">
              <i class="fa-solid fa-pen"></i>
            </button>
            <button class="action-btn danger" type="button" onclick="togglePromotionStatus('${escapeAttr(maKhuyenMai)}')">
              <i class="fa-solid fa-rotate"></i>
            </button>
            <button class="action-btn danger" type="button" onclick="deletePromotion('${escapeAttr(maKhuyenMai)}')">
              <i class="fa-solid fa-trash"></i>
            </button>
          </td>
        </tr>
      `;
    })
  );
}

function renderStaff(data) {
  setHtml(
    "staffBody",
    (data || []).map((item) => `
      <tr>
        <td>${escapeHtml(item.maNV ?? "-")}</td>
        <td>${escapeHtml(item.tenNV ?? "-")}</td>
        <td>${escapeHtml(item.tenNhaXe ?? "-")}</td>
        <td>${escapeHtml(item.sdt ?? "-")}</td>
        <td>${escapeHtml(item.email ?? "-")}</td>
        <td>${statusBadge(item.trangThai, "status")}</td>
        <td>${escapeHtml(item.maTK ?? "-")}</td>
        <td>${escapeHtml(item.tenDangNhap ?? "-")}</td>
      </tr>
    `)
  );
}

function renderTrips(data) {
  setHtml(
    "tripBody",
    (data || []).map((item) => {
      const status = getTripDisplayStatus(item.trangThai);
      const tripDate = item.thoiGianKhoiHanh || item.ngayDi;
      const tripTime = item.thoiGianKhoiHanh || item.gioDi;
      const availableSeats = item.gheTrong ?? item.soLuongGhe ?? "-";
      const canCancel = status !== "Đã hủy";
      const nextStatus = canCancel ? "Đã hủy" : "Đang mở bán";

      return `
        <tr>
          <td>${escapeHtml(item.maChuyen ?? "-")}</td>
          <td>${escapeHtml(item.tenNhaXe ?? "-")}</td>
          <td>${escapeHtml(formatTripRoute(item))}</td>
          <td>${escapeHtml(formatTripDate(tripDate))}</td>
          <td>${escapeHtml(formatTripTime(tripTime))}</td>
          <td>${escapeHtml(formatMoney(item.giaVe ?? 0))}</td>
          <td>${escapeHtml(availableSeats)}</td>
          <td>${statusBadge(status, "trip")}</td>
          <td>
            <button class="action-btn ${canCancel ? "danger" : ""}" type="button" onclick="updateTripStatus('${escapeAttr(item.maChuyen ?? "")}','${escapeAttr(nextStatus)}')">
              <i class="fa-solid fa-rotate"></i>
            </button>
          </td>
        </tr>
      `;
    })
  );
}

function renderBookings(data) {
  setHtml(
    "bookingBody",
    (data || []).map((item) => {
      const bookingStatus = normalizeText(item.trangThaiDatVe);
      const paymentStatus = normalizeText(item.trangThaiThanhToan);

      return `
        <tr>
          <td>${escapeHtml(item.maDatVe ?? "-")}</td>
          <td>${escapeHtml(item.tenKH ?? "-")}</td>
          <td>${escapeHtml(item.tenNhaXe ?? "-")}</td>
          <td>${escapeHtml(formatBookingRoute(item))}</td>
          <td>${escapeHtml((Array.isArray(item.soGhe) ? item.soGhe : []).join(", ") || "-")}</td>
          <td>${escapeHtml(formatMoney(item.tongTien ?? 0))}</td>
            <td>${statusBadge(bookingStatus, "booking")}</td>
            <td>${statusBadge(paymentStatus, "payment")}</td>
            <td>
              <button class="action-btn" type="button" onclick="openBookingEditor('${escapeAttr(item.maDatVe ?? "")}')">
                <i class="fa-solid fa-pen"></i>
              </button>
              <button class="action-btn danger" type="button" onclick="deleteBooking('${escapeAttr(item.maDatVe ?? "")}')">
                <i class="fa-solid fa-trash"></i>
              </button>
            </td>
        </tr>
      `;
    })
  );
}

function shortMoney(value) {
  const number = Number(value || 0);

  if (number >= 1000000000) {
    return (number / 1000000000).toFixed(1).replace(".0", "") + " tỷ";
  }

  if (number >= 1000000) {
    return (number / 1000000).toFixed(1).replace(".0", "") + "tr";
  }

  if (number >= 1000) {
    return Math.round(number / 1000) + "k";
  }

  return number + "đ";
}

function normalizeMonthlyRevenueData(data) {
  const input = Array.isArray(data) ? data : [];
  const map = new Map();

  input.forEach((item) => {
    const monthNumber = Number(
      item.monthNumber ??
      item.thang ??
      item.month ??
      item.MONTH_NUMBER ??
      item.THANG
    );

    if (!monthNumber || monthNumber < 1 || monthNumber > 12) {
      return;
    }

    const revenue = Number(
      item.revenue ??
      item.doanhThu ??
      item.totalRevenue ??
      item.tongDoanhThu ??
      item.REVENUE ??
      item.DOANH_THU ??
      0
    );

    map.set(monthNumber, {
      monthNumber,
      month: item.monthLabel || item.monthName || item.monthText || `T${monthNumber}`,
      revenue
    });
  });

  return Array.from({ length: 12 }, (_, index) => {
    const monthNumber = index + 1;

    return map.get(monthNumber) || {
      monthNumber,
      month: `T${monthNumber}`,
      revenue: 0
    };
  });
}

function renderMonthlyRevenue(data) {
  const items = normalizeMonthlyRevenueData(data);
  const maxRevenue = Math.max(...items.map((item) => Number(item.revenue || 0)), 1);

  setHtml(
    "monthChart",
    items.map((item) => {
      const revenue = Number(item.revenue || 0);

      const height = revenue === 0
        ? 28
        : Math.max(45, Math.round((revenue / maxRevenue) * 220));

      return `
        <div class="bar-item">
          <div class="bar-value" title="${escapeAttr(formatMoney(revenue))}">
            ${escapeHtml(shortMoney(revenue))}
          </div>
          <div class="bar" style="height:${height}px" title="${escapeAttr(formatMoney(revenue))}"></div>
          <div class="bar-label">${escapeHtml(item.month || `T${item.monthNumber}`)}</div>
        </div>
      `;
    })
  );
}

function renderTopCompanies(data) {
  const items = data || [];
  const maxRevenue = Math.max(...items.map((item) => Number(item.revenue || 0)), 1);

  setHtml(
    "companyMini",
    items.map((item) => {
      const revenue = Number(item.revenue || 0);
      const percent = Math.round((revenue / maxRevenue) * 100);

      return `
        <div class="mb-3">
          <div class="rev-row">
            <span class="rev-name">${escapeHtml(getCompanyName(item))}</span>
            <span class="rev-value">${escapeHtml(formatMoney(revenue))}</span>
          </div>
          <div class="progress">
            <div class="progress-bar" style="width:${percent}%"></div>
          </div>
        </div>
      `;
    })
  );
}

function renderCompanyRevenue(data) {
  const items = data || [];
  const maxRevenue = Math.max(...items.map((item) => Number(item.revenue || 0)), 1);

  setHtml(
    "companyReport",
    items.map((item) => {
      const revenue = Number(item.revenue || 0);
      const percent = Math.round((revenue / maxRevenue) * 100);

      return `
        <div class="report-card">
          <div class="report-head">
            <h5>${escapeHtml(getCompanyName(item))}</h5>
            <strong>${escapeHtml(formatMoney(revenue))}</strong>
          </div>
          <p>
            Số chuyến: <b>${escapeHtml(item.tripCount ?? 0)}</b> |
            Đơn đã thanh toán: <b>${escapeHtml(item.paidOrderCount ?? 0)}</b> |
            Tỷ lệ: <b>${percent}%</b>
          </p>
          <div class="progress">
            <div class="progress-bar" style="width:${percent}%"></div>
          </div>
        </div>
      `;
    })
  );
}

function initFilters() {
  const searchInput = document.getElementById("accountSearch");
  const roleFilter = document.getElementById("roleFilter");
  const tripCompanyFilter = document.getElementById("tripCompanyFilter");
  const tripFilterClear = document.getElementById("tripFilterClear");

  if (searchInput) searchInput.oninput = applyAccountFilters;
  if (roleFilter) roleFilter.onchange = applyAccountFilters;
  if (tripCompanyFilter) tripCompanyFilter.oninput = debounce(loadTrips, 300);
  if (tripFilterClear) {
    tripFilterClear.onclick = () => {
      if (tripCompanyFilter) tripCompanyFilter.value = "";
      loadTrips();
    };
  }
}

function initPromotionForm() {
  const form = document.getElementById("promotionForm");
  const modal = document.getElementById("promotionModal");
  const searchInput = document.getElementById("promotionSearch");
  const codeInput = document.getElementById("promotionCode");

  if (!form || !modal) {
    return;
  }

  if (codeInput) {
    codeInput.required = false;
    codeInput.placeholder = "Mã sẽ được tạo tự động khi thêm mới";
  }

  modal.addEventListener("show.bs.modal", () => {
    if (!document.getElementById("promotionId").value) {
      resetPromotionForm();
    }
  });

  modal.addEventListener("hidden.bs.modal", () => {
    resetPromotionForm();
  });

  if (searchInput) {
    searchInput.oninput = () => renderPromotions(filterPromotions());
  }

  form.onsubmit = async (event) => {
    event.preventDefault();

    const promotionId = document.getElementById("promotionId").value.trim();
    const payload = buildPromotionPayload();

    if (!payload) {
      return;
    }

    const isEdit = Boolean(promotionId);

    try {
      await requestJson(
        isEdit
          ? `/api/admin/khuyen-mai/${encodeURIComponent(promotionId)}`
          : "/api/admin/khuyen-mai",
        {
          method: isEdit ? "PUT" : "POST",
          body: payload
        }
      );

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      resetPromotionForm();
      await loadAdminData();
    } catch (error) {
      alert(error.message || "Không thể lưu khuyến mãi.");
    }
  };
}

function filterPromotions() {
  const searchInput = document.getElementById("promotionSearch");
  const keyword = normalizeText(searchInput ? searchInput.value : "").toLowerCase();

  if (!keyword) {
    return state.promotions || [];
  }

  return (state.promotions || []).filter((item) => {
    return [item.maKhuyenMai, item.tenKhuyenMai, item.trangThai]
      .filter(Boolean)
      .some((value) => String(value).toLowerCase().includes(keyword));
  });
}

function resetPromotionForm(promotion) {
  setValue("promotionId", promotion ? String(promotion.maKhuyenMai ?? "") : "");
  setValue("promotionCode", promotion?.maKhuyenMai ?? "");
  setValue("promotionName", promotion?.tenKhuyenMai ?? "");
  setValue("promotionPercent", promotion?.phanTramGiam ?? "");
  setValue("promotionAmount", promotion?.soTienGiam ?? "");
  setValue("promotionStart", toDateTimeLocalValue(promotion?.ngayBatDau));
  setValue("promotionEnd", toDateTimeLocalValue(promotion?.ngayKetThuc));
  setValue("promotionStatus", promotion?.trangThai ?? "Đang áp dụng");

  const modalTitle = document.getElementById("promotionModalTitle");
  const saveBtn = document.getElementById("promotionSaveBtn");
  const codeInput = document.getElementById("promotionCode");

  if (modalTitle) modalTitle.textContent = promotion ? "Cập nhật khuyến mãi" : "Thêm khuyến mãi";
  if (saveBtn) saveBtn.textContent = promotion ? "Lưu thay đổi" : "Thêm";
  if (codeInput) {
    codeInput.readOnly = Boolean(promotion);
    codeInput.required = false;
    codeInput.placeholder = promotion
      ? "Mã khuyến mãi"
      : "Mã sẽ được tạo tự động khi thêm mới";
  }
}

async function openPromotionEditor(maKhuyenMai) {
  if (!maKhuyenMai) return;

  try {
    const payload = await requestJson(`/api/admin/khuyen-mai/${encodeURIComponent(maKhuyenMai)}`);
    const promotion = normalizePromotionPayload(payload);

    if (!promotion || !promotion.maKhuyenMai) {
      alert("Không tìm thấy khuyến mãi.");
      return;
    }

    resetPromotionForm(promotion);
    bootstrap.Modal.getOrCreateInstance(document.getElementById("promotionModal")).show();
  } catch (error) {
    alert(error.message || "Không thể tải chi tiết khuyến mãi.");
  }
}

async function togglePromotionStatus(maKhuyenMai) {
  const promotion = (state.promotions || []).find((item) => String(item.maKhuyenMai) === String(maKhuyenMai));

  if (!promotion) {
    alert("Không tìm thấy khuyến mãi.");
    return;
  }

  const currentStatus = normalizeText(promotion.trangThai) || "Đang áp dụng";
  const nextStatus = currentStatus === "Đang áp dụng" ? "Tạm dừng" : "Đang áp dụng";

  try {
    await requestJson(`/api/admin/khuyen-mai/${encodeURIComponent(maKhuyenMai)}/status`, {
      method: "PUT",
      body: {
        trangThai: nextStatus
      }
    });

    await loadAdminData();
  } catch (error) {
    alert(error.message || "Không thể cập nhật trạng thái khuyến mãi.");
  }
}

function buildStationRequestPath() {
  const searchInput = document.getElementById("stationSearch");
  const keyword = searchInput ? searchInput.value.trim() : "";

  if (!keyword) {
    return "/api/admin/ben-xe";
  }

  return `/api/admin/ben-xe?keyword=${encodeURIComponent(keyword)}`;
}

async function deletePromotion(maKhuyenMai) {
  const promotion = (state.promotions || []).find((item) => String(item.maKhuyenMai) === String(maKhuyenMai));
  const promotionName = promotion?.tenKhuyenMai || maKhuyenMai;

  if (!maKhuyenMai) return;

  const confirmed = confirm(`Xóa khuyến mãi ${promotionName}?`);

  if (!confirmed) return;

  try {
    await requestJson(`/api/admin/khuyen-mai/${encodeURIComponent(maKhuyenMai)}`, {
      method: "DELETE"
    });

    await loadAdminData();
  } catch (error) {
    alert(error.message || "Không thể xóa khuyến mãi.");
  }
}

function normalizePromotionPayload(payload) {
  const item = normalizeResult(payload);
  return Array.isArray(item) ? item[0] : item;
}

function buildPromotionPayload() {
  const tenKhuyenMai = normalizeText(document.getElementById("promotionName")?.value);
  const phanTramGiam = parseOptionalNumber(document.getElementById("promotionPercent")?.value);
  const soTienGiam = parseOptionalNumber(document.getElementById("promotionAmount")?.value);
  const ngayBatDau = document.getElementById("promotionStart")?.value;
  const ngayKetThuc = document.getElementById("promotionEnd")?.value;
  const trangThai = document.getElementById("promotionStatus")?.value || "Đang áp dụng";

  if (!tenKhuyenMai) {
    alert("Tên khuyến mãi là bắt buộc.");
    return null;
  }

  if (!ngayBatDau || !ngayKetThuc) {
    alert("Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.");
    return null;
  }

  if (new Date(ngayKetThuc).getTime() <= new Date(ngayBatDau).getTime()) {
    alert("Ngày kết thúc phải lớn hơn ngày bắt đầu.");
    return null;
  }

  if ((phanTramGiam > 0 ? 1 : 0) + (soTienGiam > 0 ? 1 : 0) !== 1) {
    alert("Khuyến mãi phải có đúng một trong hai giá trị: giảm theo % hoặc giảm trực tiếp.");
    return null;
  }

  return {
    tenKhuyenMai,
    phanTramGiam: phanTramGiam > 0 ? phanTramGiam : 0,
    soTienGiam: soTienGiam > 0 ? soTienGiam : 0,
    ngayBatDau,
    ngayKetThuc,
    trangThai
  };
}

function parseOptionalNumber(value) {
  const text = normalizeText(value);

  if (!text) {
    return 0;
  }

  const number = Number(text);
  return Number.isNaN(number) ? 0 : number;
}

function toDateTimeLocalValue(value) {
  if (!value) {
    return "";
  }

  const date = new Date(String(value));
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");

  return `${year}-${month}-${day}T${hour}:${minute}`;
}

function formatPromotionDiscount(item) {
  const percent = Number(item?.phanTramGiam || 0);
  const amount = Number(item?.soTienGiam || 0);

  if (percent > 0) {
    return `-${percent}%`;
  }

  if (amount > 0) {
    return `-${formatMoney(amount)}`;
  }

  return "-";
}

function applyAccountFilters() {
  renderAccounts(filterAccounts());
}

function filterAccounts() {
  const searchInput = document.getElementById("accountSearch");
  const roleFilter = document.getElementById("roleFilter");
  const keyword = normalizeText(searchInput ? searchInput.value : "");
  const role = roleFilter ? roleFilter.value : "";

  return (state.accounts || []).filter((item) => {
    const matchesKeyword =
      normalizeText(item.tenDangNhap).includes(keyword) ||
      normalizeText(item.tenNguoiDung).includes(keyword) ||
      normalizeText(item.email).includes(keyword) ||
      normalizeText(item.sdt).includes(keyword) ||
      normalizeText(String(item.maTK ?? "")).includes(keyword);

    const matchesRole = !role || normalizeText(item.quyen) === role;

    return matchesKeyword && matchesRole;
  });
}

function initAccountForm() {
  const form = document.getElementById("accountForm");
  const modal = document.getElementById("accountModal");

  if (!form || !modal) return;

  modal.addEventListener("show.bs.modal", () => {
    if (!document.getElementById("accountId").value) {
      resetAccountForm();
    }
  });

  form.onsubmit = async (event) => {
    event.preventDefault();

    const accountId = document.getElementById("accountId").value.trim();

    const payload = {
      tenDangNhap: document.getElementById("accountUsername").value.trim(),
      quyen: document.getElementById("accountRole").value,
      trangThaiTK: document.getElementById("accountStatus").value,
      tenNguoiDung: document.getElementById("accountFullName").value.trim(),
      email: document.getElementById("accountEmail").value.trim(),
      sdt: document.getElementById("accountPhone").value.trim()
    };

    const password = document.getElementById("accountPassword").value.trim();

    if (!accountId || password) {
      payload.matKhau = password;
    }

    if (!accountId && !payload.matKhau) {
      alert("Vui lòng nhập mật khẩu cho tài khoản mới.");
      return;
    }

    try {
      await requestJson(
        accountId
          ? `/api/admin/accounts/${encodeURIComponent(accountId)}`
          : "/api/admin/accounts",
        {
          method: accountId ? "PUT" : "POST",
          body: payload
        }
      );

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      resetAccountForm();
      await loadAdminData();
    } catch (error) {
      alert(error.message || "Không thể lưu tài khoản.");
    }
  };
}

function initCompanyForm() {
  const form = document.getElementById("companyForm");
  const modal = document.getElementById("companyModal");

  if (!form || !modal) return;

  modal.addEventListener("show.bs.modal", () => {
    if (!document.getElementById("companyId").value) {
      resetCompanyForm();
    }
  });

  form.onsubmit = async (event) => {
    event.preventDefault();

    const companyId = document.getElementById("companyId").value.trim();
    const companyCode = document.getElementById("companyCode").value.trim();
    const isEdit = Boolean(companyId);

    const payload = {
      tenNhaXe: document.getElementById("companyName").value.trim(),
      sdt: document.getElementById("companyPhone").value.trim(),
      email: document.getElementById("companyEmail").value.trim(),
      diaChi: document.getElementById("companyAddress").value.trim(),
      moTa: document.getElementById("companyDescription").value.trim(),
      trangThai: document.getElementById("companyStatus").value
    };

    try {
      await requestJson(
        isEdit
          ? `/api/admin/companies/${encodeURIComponent(companyId)}`
          : "/api/admin/companies",
        {
          method: isEdit ? "PUT" : "POST",
          body: isEdit ? payload : { maNhaXe: companyCode, ...payload }
        }
      );

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      resetCompanyForm();
      await loadAdminData();
    } catch (error) {
      alert(error.message || "Không thể lưu nhà xe.");
    }
  };
}

function initStationForm() {
  const form = document.getElementById("stationForm");
  const modal = document.getElementById("stationModal");
  const searchInput = document.getElementById("stationSearch");
  const addPointButton = document.getElementById("addStationPoint");

  if (!form || !modal) return;

  modal.addEventListener("show.bs.modal", () => {
    if (!document.getElementById("stationId").value) {
      resetStationForm();
    }
  });

  modal.addEventListener("hidden.bs.modal", () => {
    resetStationForm();
  });

  if (searchInput) searchInput.oninput = () => loadBusStations();
  if (addPointButton) addPointButton.onclick = () => addStationPointRow();

  form.onsubmit = async (event) => {
    event.preventDefault();

    const stationId = document.getElementById("stationId").value.trim();
    const code = document.getElementById("stationCode").value.trim();
    const isEdit = Boolean(stationId);

    const payload = {
      maBen: isEdit ? stationId : code,
      tenBen: document.getElementById("stationName").value.trim(),
      diaChi: document.getElementById("stationAddress").value.trim(),
      diemBen: collectStationPoints()
    };

    if (!payload.maBen || !payload.tenBen) {
      alert("Vui lòng nhập đầy đủ mã bến và tên bến xe.");
      return;
    }

    if (!payload.diemBen.length) {
      alert("Vui lòng thêm ít nhất một điểm bến.");
      return;
    }

    try {
      await requestJson(isEdit ? `/api/admin/ben-xe/${encodeURIComponent(stationId)}` : "/api/admin/ben-xe", {
        method: isEdit ? "PUT" : "POST",
        body: payload
      });

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      await loadBusStations();
    } catch (error) {
      alert(error.message || "Không thể lưu bến xe.");
    }
  };
}

function initStaffForm() {
  const form = document.getElementById("staffForm");
  const modal = document.getElementById("staffModal");

  if (!form || !modal) return;

  form.onsubmit = async (event) => {
    event.preventDefault();

    const payload = {
      tenDangNhap: document.getElementById("staffUsername").value.trim(),
      matKhau: document.getElementById("staffPassword").value.trim(),
      trangThaiTK: document.getElementById("staffAccountStatus").value,
      tenNV: document.getElementById("staffName").value.trim(),
      gioiTinh: document.getElementById("staffGender").value,
      sdt: document.getElementById("staffPhone").value.trim(),
      email: document.getElementById("staffEmail").value.trim(),
      ngayVaoLam: document.getElementById("staffStartDate").value || null,
      trangThai: document.getElementById("staffStatus").value,
      maNhaXe: document.getElementById("staffCompany").value.trim()
    };

    try {
      await requestJson("/api/admin/staff", {
        method: "POST",
        body: payload
      });

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      form.reset();
      await loadAdminData();
    } catch (error) {
      alert(error.message || "Không thể tạo nhân viên.");
    }
  };
}

function initBookingForm() {
  const form = document.getElementById("bookingForm");
  const modal = document.getElementById("bookingModal");

  if (!form || !modal) return;

  form.onsubmit = async (event) => {
    event.preventDefault();

    const bookingId = document.getElementById("bookingId").value.trim();

    if (!bookingId) {
      alert("Không tìm thấy mã đặt vé.");
      return;
    }

    try {
      await requestJson(`/api/admin/bookings/${encodeURIComponent(bookingId)}`, {
        method: "PUT",
        body: {
          trangThaiDatVe: document.getElementById("bookingStatus").value,
          trangThaiHoaDon: document.getElementById("invoiceStatus").value || null,
          trangThaiThanhToan: document.getElementById("paymentStatus").value || null
        }
      });

      bootstrap.Modal.getOrCreateInstance(modal).hide();
      await loadAdminData();
    } catch (error) {
      alert(error.message || "Không thể cập nhật đặt vé.");
    }
  };
}

function resetAccountForm(account) {
  const accountId = document.getElementById("accountId");
  const accountModalTitle = document.getElementById("accountModalTitle");
  const accountSaveBtn = document.getElementById("accountSaveBtn");

  if (accountId) accountId.value = account ? String(account.maTK ?? "") : "";
  if (accountModalTitle) accountModalTitle.textContent = account ? "Cập nhật tài khoản" : "Thêm tài khoản";
  if (accountSaveBtn) accountSaveBtn.textContent = account ? "Lưu thay đổi" : "Thêm";

  setValue("accountUsername", account?.tenDangNhap ?? "");
  setValue("accountPassword", "");
  setValue("accountRole", account?.quyen ?? "KhachHang");
  setValue("accountStatus", account?.trangThaiTK ?? "Hoạt động");
  setValue("accountFullName", account?.tenNguoiDung ?? "");
  setValue("accountEmail", account?.email ?? "");
  setValue("accountPhone", account?.sdt ?? "");
}

function openAccountEditor(maTK) {
  const account = (state.accounts || []).find((item) => String(item.maTK) === String(maTK));

  if (!account) {
    alert("Không tìm thấy tài khoản.");
    return;
  }

  resetAccountForm(account);
  bootstrap.Modal.getOrCreateInstance(document.getElementById("accountModal")).show();
}

function resetCompanyForm(company) {
  const companyId = document.getElementById("companyId");
  const companyModalTitle = document.getElementById("companyModalTitle");
  const companySaveBtn = document.getElementById("companySaveBtn");
  const companyCode = document.getElementById("companyCode");

  if (companyId) companyId.value = company ? String(company.maNhaXe ?? "") : "";
  if (companyModalTitle) companyModalTitle.textContent = company ? "Cập nhật nhà xe" : "Thêm nhà xe";
  if (companySaveBtn) companySaveBtn.textContent = company ? "Lưu thay đổi" : "Thêm";

  setValue("companyCode", company?.maNhaXe ?? "");
  setValue("companyName", company?.tenNhaXe ?? "");
  setValue("companyPhone", company?.sdt ?? "");
  setValue("companyEmail", company?.email ?? "");
  setValue("companyAddress", company?.diaChi ?? "");
  setValue("companyDescription", company?.moTa ?? "");
  setValue("companyStatus", company?.trangThai ?? "Hoạt động");

  if (companyCode) {
    companyCode.readOnly = Boolean(company);
  }
}

function openCompanyEditor(maNhaXe) {
  const company = (state.companies || []).find((item) => String(item.maNhaXe) === String(maNhaXe));

  if (!company) {
    alert("Không tìm thấy nhà xe.");
    return;
  }

  resetCompanyForm(company);
  bootstrap.Modal.getOrCreateInstance(document.getElementById("companyModal")).show();
}

async function toggleCompanyStatus(maNhaXe) {
  const company = (state.companies || []).find((item) => String(item.maNhaXe) === String(maNhaXe));
  if (!company) return;

  const currentStatus = normalizeText(company.trangThai) || "Hoạt động";
  const nextStatus = currentStatus === "Hoạt động" ? "Ngừng hoạt động" : "Hoạt động";

  try {
    await requestJson(`/api/admin/companies/${encodeURIComponent(maNhaXe)}/status`, {
      method: "PUT",
      body: {
        trangThai: nextStatus
      }
    });

    await loadAdminData();
  } catch (error) {
    alert(error.message || "Không thể cập nhật trạng thái nhà xe.");
  }
}

function resetStationForm(station) {
  const stationId = document.getElementById("stationId");
  const stationModalTitle = document.getElementById("stationModalTitle");
  const stationSaveBtn = document.getElementById("stationSaveBtn");
  const stationCode = document.getElementById("stationCode");
  const stationPointList = document.getElementById("stationPointList");

  if (stationId) stationId.value = station ? String(station.maBen ?? "") : "";
  if (stationModalTitle) stationModalTitle.textContent = station ? "Cập nhật bến xe" : "Thêm bến xe";
  if (stationSaveBtn) stationSaveBtn.textContent = station ? "Lưu thay đổi" : "Thêm";

  setValue("stationCode", station?.maBen ?? "");
  setValue("stationName", station?.tenBen ?? "");
  setValue("stationAddress", station?.diaChi ?? "");

  if (stationPointList) {
    stationPointList.innerHTML = "";
    const points = Array.isArray(station?.diemBen) ? station.diemBen : [];
    if (points.length) {
      points.forEach((point) => addStationPointRow(point));
    } else {
      addStationPointRow();
    }
  }

  if (stationCode) {
    stationCode.readOnly = Boolean(station);
  }
}

function openStationEditor(maBen) {
  const station = (state.busStations || []).find((item) => String(item.maBen) === String(maBen));

  if (!station) {
    alert("Không tìm thấy bến xe.");
    return;
  }

  resetStationForm(station);
  bootstrap.Modal.getOrCreateInstance(document.getElementById("stationModal")).show();
}

async function deleteStation(maBen) {
  if (!maBen) return;

  const confirmed = confirm(`Xóa bến xe ${maBen}?`);
  if (!confirmed) return;

  try {
    await requestJson(`/api/admin/ben-xe/${encodeURIComponent(maBen)}`, {
      method: "DELETE"
    });

    await loadBusStations();
  } catch (error) {
    alert(error.message || "Không thể xóa bến xe.");
  }
}

function filterBusStations() {
  const searchInput = document.getElementById("stationSearch");
  const keyword = normalizeSearchText(searchInput ? searchInput.value : "");

  return (state.busStations || []).filter((item) => {
    const points = Array.isArray(item.diemBen) ? item.diemBen : [];
    return !keyword || [item.maBen, item.tenBen, item.diaChi, ...points.flatMap((point) => [
      point.maDiemBen,
      point.tenDiem,
      point.diaChi,
      point.loai,
      point.trangThai
    ])]
      .filter(Boolean)
      .some((value) => normalizeSearchText(value).includes(keyword));
  });
}

async function loadBusStations() {
  try {
    state.busStations = normalizeResult(await requestJson(buildStationRequestPath()));
    renderBusStations(filterBusStations());
  } catch (error) {
    console.error("Không tải được danh sách bến xe:", error);
    alert(error.message || "Không thể tải danh sách bến xe.");
  }
}

function addStationPointRow(point = {}) {
  const list = document.getElementById("stationPointList");
  if (!list) return;

  const row = document.createElement("div");
  row.className = "row g-2 align-items-end mb-2 station-point-row";
  row.innerHTML = `
    <input class="station-point-id" type="hidden" value="${escapeAttr(point.maDiemBen ?? "")}">
    <div class="col-md-3">
      <label class="form-label">Tên điểm</label>
      <input class="form-control station-point-name" value="${escapeAttr(point.tenDiem ?? "")}" required>
    </div>
    <div class="col-md-3">
      <label class="form-label">Địa chỉ</label>
      <input class="form-control station-point-address" value="${escapeAttr(point.diaChi ?? "")}">
    </div>
    <div class="col-md-2">
      <label class="form-label">Loại</label>
      <select class="form-select station-point-type">
        <option value="Đón">Đón</option>
        <option value="Trả">Trả</option>
        <option value="Cả hai">Cả hai</option>
      </select>
    </div>
    <div class="col-md-1">
      <label class="form-label">Thứ tự</label>
      <input class="form-control station-point-order" type="number" min="1" value="${escapeAttr(point.thuTu ?? (list.children.length + 1))}">
    </div>
    <div class="col-md-2">
      <label class="form-label">Trạng thái</label>
      <select class="form-select station-point-status">
        <option value="Hoạt động">Hoạt động</option>
        <option value="Tạm ngưng">Tạm ngưng</option>
      </select>
    </div>
    <div class="col-md-1">
      <button class="action-btn danger" type="button" onclick="removeStationPointRow(this)">
        <i class="fa-solid fa-trash"></i>
      </button>
    </div>
  `;

  list.appendChild(row);
  row.querySelector(".station-point-type").value = point.loai || "Cả hai";
  row.querySelector(".station-point-status").value = point.trangThai || "Hoạt động";
}

function removeStationPointRow(button) {
  const row = button?.closest(".station-point-row");
  if (row) row.remove();
}

function collectStationPoints() {
  return Array.from(document.querySelectorAll(".station-point-row")).map((row, index) => ({
    maDiemBen: row.querySelector(".station-point-id")?.value.trim() || null,
    tenDiem: row.querySelector(".station-point-name")?.value.trim() || "",
    diaChi: row.querySelector(".station-point-address")?.value.trim() || null,
    loai: row.querySelector(".station-point-type")?.value || "Cả hai",
    thuTu: Number(row.querySelector(".station-point-order")?.value || index + 1),
    trangThai: row.querySelector(".station-point-status")?.value || "Hoạt động"
  })).filter((point) => point.tenDiem);
}

async function toggleAccountStatus(maTK) {
  const account = (state.accounts || []).find((item) => String(item.maTK) === String(maTK));
  if (!account) return;

  const nextStatus = normalizeText(account.trangThaiTK) === "Bị khóa" ? "Hoạt động" : "Bị khóa";

  try {
    await requestJson(`/api/admin/accounts/${encodeURIComponent(maTK)}/status`, {
      method: "PUT",
      body: {
        trangThaiTK: nextStatus
      }
    });

    await loadAdminData();
  } catch (error) {
    alert(error.message || "Không thể cập nhật trạng thái tài khoản.");
  }
}

async function updateTripStatus(maChuyen, nextStatus) {
  if (!maChuyen) return;

  try {
    await requestJson(`/api/staff/chuyen-xe/${encodeURIComponent(maChuyen)}/status`, {
      method: "PUT",
      body: {
        trangThai: nextStatus
      }
    });

    await loadTrips();
  } catch (error) {
    alert(error.message || "Không thể cập nhật trạng thái chuyến xe.");
  }
}

function openBookingEditor(maDatVe) {
  const booking = (state.bookings || []).find((item) => String(item.maDatVe) === String(maDatVe));

  if (!booking) {
    alert("Không tìm thấy đặt vé.");
    return;
  }

  setValue("bookingId", booking.maDatVe ?? "");
  setValue("bookingCode", booking.maDatVe ?? "");
  setValue("bookingStatus", normalizeText(booking.trangThaiDatVe) || "Chờ thanh toán");
  setValue("invoiceStatus", normalizeText(booking.trangThaiHoaDon));
  setValue("paymentStatus", normalizeText(booking.trangThaiThanhToan));

  bootstrap.Modal.getOrCreateInstance(document.getElementById("bookingModal")).show();
}

async function deleteBooking(maDatVe) {
  if (!maDatVe) return;

  const confirmed = confirm(`Xóa đơn đặt vé ${maDatVe}?`);
  if (!confirmed) return;

  try {
    await requestJson(`/api/admin/bookings/${encodeURIComponent(maDatVe)}`, {
      method: "DELETE"
    });

    await loadAdminData();
  } catch (error) {
    alert(error.message || "Không thể xóa đặt vé.");
  }
}

function getAccountRoleLabel(role) {
  const normalized = normalizeText(role);

  if (normalized === "KhachHang") return "Khách hàng";
  if (normalized === "NhanVien") return "Nhân viên";
  if (normalized === "Admin") return "Admin";

  return role || "-";
}

function getCompanyName(item) {
  return item.tenNhaXe || item.name || item.tenCongTy || item.maNhaXe || "-";
}

function getTripDisplayStatus(status) {
  const normalized = normalizeText(status);

  if (normalized === "Sắp chạy") return "Đang mở bán";

  return normalized || "-";
}

function formatTripRoute(item) {
  const from = item.tenBenDi || item.benDi || item.tenBenDiBenXe;
  const to = item.tenBenDen || item.benDen || item.tenBenDenBenXe;

  if (from && to) return `${from} - ${to}`;

  return item.maTuyen || item.tenTuyen || "-";
}

function formatBookingRoute(item) {
  const from = item.tenBenDi || item.benDi || "";
  const to = item.tenBenDen || item.benDen || "";

  if (from && to) return `${from} - ${to}`;

  return item.maChuyen || "-";
}

function formatTripDate(value) {
  if (!value) return "-";

  const date = new Date(String(value));

  if (Number.isNaN(date.getTime())) return String(value);

  return date.toLocaleDateString("vi-VN");
}

function formatTripTime(value) {
  if (!value) return "-";

  const text = String(value);
  if (/^\d{2}:\d{2}(:\d{2})?$/.test(text)) {
    return text.slice(0, 5);
  }

  const date = new Date(text);

  if (Number.isNaN(date.getTime())) return String(value);

  return date.toLocaleTimeString("vi-VN", {
    hour: "2-digit",
    minute: "2-digit",
    hour12: false
  });
}

function formatDate(value) {
  if (!value) return "-";

  const text = String(value);

  if (/^\d{4}-\d{2}-\d{2}$/.test(text)) {
    const [year, month, day] = text.split("-");
    return `${day}/${month}/${year}`;
  }

  const date = new Date(text);

  if (Number.isNaN(date.getTime())) return text;

  return date.toLocaleDateString("vi-VN");
}

function formatDateTime(value) {
  if (!value) return "-";

  const text = String(value);
  const date = new Date(text);

  if (Number.isNaN(date.getTime())) return text;

  return date.toLocaleString("vi-VN", {
    hour12: false
  });
}

function formatMoney(value) {
  const number = Number(value || 0);
  return `${new Intl.NumberFormat("vi-VN").format(Number.isNaN(number) ? 0 : number)}đ`;
}

function statusBadge(status, type) {
  const text = normalizeText(status) || "-";
  let className = "badge-soft";

  if (type === "payment") {
    className += text === "Thành công"
      ? " badge-paid"
      : text.includes("Hủy") || text === "Không thành công"
        ? " badge-locked"
        : " badge-pending";
  } else if (type === "booking") {
    className += text === "Đã thanh toán"
      ? " badge-paid"
      : text.includes("Hủy")
        ? " badge-locked"
        : " badge-pending";
  } else if (type === "trip") {
    className += text === "Đã hủy" ? " badge-locked" : " badge-active";
  } else if (type === "promotion") {
    className += text === "Đang áp dụng"
      ? " badge-active"
      : text === "Tạm dừng"
        ? " badge-pending"
        : " badge-locked";
  } else if (type === "account" || type === "status") {
    className += text === "Hoạt động" ? " badge-active" : " badge-locked";
  }

  return `<span class="${className}">${escapeHtml(text)}</span>`;
}

function setHtml(id, html) {
  const element = document.getElementById(id);

  if (element) {
    element.innerHTML = Array.isArray(html) ? html.join("") : html;
  }
}

function setText(id, value) {
  const element = document.getElementById(id);

  if (element) {
    element.textContent = value;
  }
}

function setValue(id, value) {
  const element = document.getElementById(id);

  if (element) {
    element.value = value;
  }
}

function normalizeText(value) {
  return value == null ? "" : String(value).trim();
}

function normalizeSearchText(value) {
  return normalizeText(value)
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/đ/g, "d")
    .replace(/Đ/g, "D")
    .toLowerCase();
}

function debounce(callback, delay = 250) {
  let timerId;

  return (...args) => {
    window.clearTimeout(timerId);
    timerId = window.setTimeout(() => callback(...args), delay);
  };
}

function escapeHtml(value) {
  return normalizeText(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/\"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function escapeAttr(value) {
  return escapeHtml(value).replace(/`/g, "&#96;");
}
