function initVehicleTypesPage() {
  const state = {
    vehicleTypes: []
  };

  const body = document.getElementById("vehicleTypeBody");
  const form = document.getElementById("vehicleTypeForm");
  const modalEl = document.getElementById("vehicleTypeModal");
  const modalTitle = document.getElementById("vehicleTypeModalTitle");
  const searchInput = document.getElementById("vehicleTypeSearch");
  const nameInput = document.getElementById("vehicleTypeName");
  const descriptionInput = document.getElementById("vehicleTypeDescription");
  const hiddenIdInput = document.getElementById("vehicleTypeId");
  const saveButton = document.getElementById("vehicleTypeSaveBtn");

  const bsModal = new bootstrap.Modal(modalEl);

  bindEvents();
  loadVehicleTypes();

  function bindEvents() {
    document.querySelectorAll('[data-bs-target="#vehicleTypeModal"]').forEach((button) => {
      button.addEventListener("click", openAdd);
    });

    body.addEventListener("click", handleTableClick);
    form.addEventListener("submit", handleSubmit);
    searchInput.addEventListener("input", renderFiltered);
  }

  async function requestJson(path, options = {}) {
    const token = localStorage.getItem("token");
    const headers = {};

    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    if (options.body !== undefined) {
      headers["Content-Type"] = "application/json";
    }

    const response = await fetch(`${API_BASE_URL}${path}`, {
      method: options.method || "GET",
      headers,
      body: options.body !== undefined ? JSON.stringify(options.body) : undefined
    });

    const payload = await readJsonSafely(response);

    if (!response.ok) {
      throw new Error(extractErrorMessage(payload, `Gọi API thất bại: ${path}`));
    }

    return payload;
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

  async function loadVehicleTypes() {
    try {
      const response = await requestJson("/api/admin/loai-xe");
      state.vehicleTypes = Array.isArray(response) ? response : [];
      renderFiltered();
    } catch (error) {
      console.error(error);
      alert(error.message || "Không tải được danh sách loại xe.");
      state.vehicleTypes = [];
      renderFiltered();
    }
  }

  function render(list = state.vehicleTypes) {
    body.innerHTML = "";

    if (!list.length) {
      body.innerHTML = '<tr><td colspan="4" class="text-center">Chưa có loại xe</td></tr>';
      return;
    }

    body.innerHTML = list.map((item) => `
      <tr>
        <td>${escapeHtml(item.maLoaiXe)}</td>
        <td>${escapeHtml(item.tenLoaiXe)}</td>
        <td>${escapeHtml(item.moTa || "")}</td>
        <td>
          <button class="btn btn-sm btn-outline-primary" data-action="edit" data-id="${escapeAttr(item.maLoaiXe)}">
            <i class="fa-solid fa-pen"></i>
          </button>
        </td>
      </tr>
    `).join("");
  }

  function renderFiltered() {
    const keyword = (searchInput.value || "").trim().toLowerCase();

    if (!keyword) {
      render();
      return;
    }

    const filtered = state.vehicleTypes.filter((item) => {
      return [item.maLoaiXe, item.tenLoaiXe, item.moTa]
        .filter(Boolean)
        .some((value) => String(value).toLowerCase().includes(keyword));
    });

    render(filtered);
  }

  function openAdd() {
    form.reset();
    hiddenIdInput.value = "";
    modalTitle.textContent = "Thêm loại xe";
    saveButton.textContent = "Thêm";
    bsModal.show();
  }

  function openEdit(maLoaiXe) {
    const item = state.vehicleTypes.find((vehicleType) => vehicleType.maLoaiXe === maLoaiXe);

    if (!item) return;

    hiddenIdInput.value = item.maLoaiXe || "";
    nameInput.value = item.tenLoaiXe || "";
    descriptionInput.value = item.moTa || "";
    modalTitle.textContent = "Sửa loại xe";
    saveButton.textContent = "Lưu";
    bsModal.show();
  }

  async function handleSubmit(event) {
    event.preventDefault();

    const tenLoaiXe = nameInput.value.trim();
    const moTa = descriptionInput.value.trim();

    if (!tenLoaiXe) {
      alert("Tên loại xe là bắt buộc.");
      return;
    }

    try {
      if (hiddenIdInput.value) {
        await requestJson(`/api/admin/loai-xe/${encodeURIComponent(hiddenIdInput.value)}`, {
          method: "PUT",
          body: {
            tenLoaiXe,
            moTa
          }
        });
      } else {
        await requestJson("/api/admin/loai-xe", {
          method: "POST",
          body: {
            tenLoaiXe,
            moTa
          }
        });
      }

      bsModal.hide();
      await loadVehicleTypes();
    } catch (error) {
      console.error(error);
      alert(error.message || "Không lưu được loại xe.");
    }
  }

  function handleTableClick(event) {
    const button = event.target.closest("button");

    if (!button) return;

    const action = button.getAttribute("data-action");
    const id = button.getAttribute("data-id");

    if (action === "edit") {
      openEdit(id);
    }
  }

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => {
      return ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
      })[character];
    });
  }

  function escapeAttr(value) {
    return escapeHtml(value).replace(/`/g, "&#96;");
  }
}

if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initVehicleTypesPage);
} else {
  initVehicleTypesPage();
}
