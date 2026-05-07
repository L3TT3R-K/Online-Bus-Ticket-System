async function loadLoaiXe() {
    const busTypeSelect = document.getElementById("busType");
    const editBusTypeSelect = document.getElementById("editBusType");

    try {
        const response = await fetch(`${API_BASE_URL}/api/loai-xe`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải loại xe:", result.message || result);

            if (busTypeSelect) busTypeSelect.innerHTML = `<option value="">Không tải được loại xe</option>`;
            if (editBusTypeSelect) editBusTypeSelect.innerHTML = `<option value="">Không tải được loại xe</option>`;

            return;
        }

        loaiXeList = result;

        const options = `
            <option value="">Chọn loại xe</option>
            ${loaiXeList.map(item => `
                <option value="${item.maLoaiXe}">
                    ${item.tenLoaiXe}
                </option>
            `).join("")}
        `;

        if (busTypeSelect) busTypeSelect.innerHTML = options;
        if (editBusTypeSelect) editBusTypeSelect.innerHTML = options;
    } catch (error) {
        console.error("Lỗi gọi API loại xe:", error);

        if (busTypeSelect) busTypeSelect.innerHTML = `<option value="">Không kết nối được server</option>`;
        if (editBusTypeSelect) editBusTypeSelect.innerHTML = `<option value="">Không kết nối được server</option>`;
    }
}

async function loadTienIch() {
    const addAmenityBox = document.querySelector("#busModal .amenity-check-grid");
    const editAmenityBox = document.getElementById("editAmenityBox");

    try {
        const response = await fetch(`${API_BASE_URL}/api/tien-ich`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json"
            }
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải tiện ích:", result.message || result);
            return;
        }

        tienIchList = result;

        if (addAmenityBox) {
            addAmenityBox.innerHTML = renderAmenityCheckboxes("busAmenities", []);
        }

        if (editAmenityBox) {
            editAmenityBox.innerHTML = renderAmenityCheckboxes("editBusAmenities", []);
        }
    } catch (error) {
        console.error("Lỗi gọi API tiện ích:", error);
    }
}

async function loadStaffBuses() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/xe`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải danh sách xe:", result.message || result);
            buses = [];
            renderBuses();
            renderBusOptions();
            return;
        }

        buses = result.map(item => ({
            id: item.maXe,
            plate: item.bienSo,
            maLoaiXe: item.maLoaiXe,
            type: item.tenLoaiXe || item.maLoaiXe || "Không rõ loại xe",
            seats: item.soLuongGhe || 0,
            status: item.trangThai || "Không rõ",
            images: item.images && item.images.length
                ? item.images
                : ["https://placehold.co/500x320?text=Bus"],
            imageDesc: item.imageDesc || "Ảnh minh họa xe",
            amenities: item.amenities || []
        }));
    } catch (error) {
        console.error("Lỗi gọi API xe:", error);
        buses = [];
    }
}

function renderAmenityCheckboxes(inputName, selectedAmenities) {
    const selectedIds = normalizeAmenityIds(selectedAmenities);

    if (!tienIchList.length) {
        return `<div class="text-muted small">Chưa có tiện ích trong database.</div>`;
    }

    return tienIchList.map(item => {
        const checked = selectedIds.includes(item.maTienIch) ? "checked" : "";

        return `
            <label class="amenity-check-item">
                <input 
                    type="checkbox" 
                    name="${inputName}" 
                    value="${item.maTienIch}" 
                    ${checked}
                >
                <span>${getAmenityIcon(item.tenTienIch)} ${item.tenTienIch}</span>
            </label>
        `;
    }).join("");
}
function normalizeAmenityIds(amenities) {
    if (!Array.isArray(amenities)) return [];

    return amenities
        .map(item => {
            if (!item) return null;

            // Backend trả object: { maTienIch, tenTienIch }
            if (typeof item === "object") {
                return item.maTienIch || null;
            }

            // Backend trả mã tiện ích: TI001
            const foundById = tienIchList.find(ti => ti.maTienIch === item);
            if (foundById) return foundById.maTienIch;

            // Backend trả tên tiện ích: Wifi, Điều hòa...
            const foundByName = tienIchList.find(ti => ti.tenTienIch === item);
            if (foundByName) return foundByName.maTienIch;

            return null;
        })
        .filter(Boolean);
}

function normalizeAmenityNames(amenities) {
    if (!Array.isArray(amenities)) return [];

    return amenities
        .map(item => {
            if (!item) return null;

            // Backend trả object
            if (typeof item === "object") {
                return item.tenTienIch || null;
            }

            // Backend trả mã tiện ích
            const foundById = tienIchList.find(ti => ti.maTienIch === item);
            if (foundById) return foundById.tenTienIch;

            // Backend trả tên tiện ích
            return item;
        })
        .filter(Boolean);
}

function getSelectedAmenityIds(inputName) {
    return Array.from(document.querySelectorAll(`input[name="${inputName}"]:checked`))
        .map(input => input.value);
}

function getSelectedAmenityNames(inputName) {
    const selectedIds = getSelectedAmenityIds(inputName);

    return selectedIds
        .map(maTienIch => {
            const found = tienIchList.find(item => item.maTienIch === maTienIch);
            return found ? found.tenTienIch : null;
        })
        .filter(Boolean);
}

function getSelectedBusAmenities() {
    return getSelectedAmenityIds("busAmenities");
}

function getSelectedEditBusAmenities() {
    return getSelectedAmenityIds("editBusAmenities");
}

function getSelectedBusAmenityNames() {
    return getSelectedAmenityNames("busAmenities");
}

function getSelectedEditBusAmenityNames() {
    return getSelectedAmenityNames("editBusAmenities");
}

function getAmenityIcon(name) {
    const icons = {
        "Dây an toàn": '<i class="fa-solid fa-shield-halved"></i>',
        "Điều hòa": '<i class="fa-solid fa-snowflake"></i>',
        "Búa phá kính": '<i class="fa-solid fa-hammer"></i>',
        "Rèm cửa": '<i class="fa-solid fa-window-maximize"></i>',
        "Wifi": '<i class="fa-solid fa-wifi"></i>',
        "Nước uống": '<i class="fa-solid fa-bottle-water"></i>',
        "Cổng sạc USB": '<i class="fa-solid fa-plug"></i>',
        "Ghế massage": '<i class="fa-solid fa-chair"></i>'
    };

    return icons[name] || '<i class="fa-solid fa-circle-check"></i>';
}

function getLoaiXeName(maLoaiXe) {
    const found = loaiXeList.find(item => item.maLoaiXe === maLoaiXe);
    return found ? found.tenLoaiXe : "Không rõ loại xe";
}

function initBusImagesPreview() {
    const busImagesInput = document.getElementById("busImages");

    if (busImagesInput) {
        busImagesInput.addEventListener("change", updateBusImagesPreview);
    }
}

function updateBusImagesPreview() {
    const previewWrap = document.getElementById("busImagePreviewWrap");
    const previewList = document.getElementById("busImagePreviewList");

    if (!previewWrap || !previewList) return;

    const fileImages = Array.from(document.getElementById("busImages")?.files || [])
        .map(file => URL.createObjectURL(file));

    if (!fileImages.length) {
        resetBusImagesPreview();
        return;
    }

    previewWrap.classList.remove("d-none");
    previewList.innerHTML = fileImages.map((image, index) => `
        <div class="preview-image-item">
            <img src="${image}" alt="Preview ${index + 1}">
            <span>Ảnh local ${index + 1}</span>
        </div>
    `).join("");
}

function resetBusImagesPreview() {
    const previewWrap = document.getElementById("busImagePreviewWrap");
    const previewList = document.getElementById("busImagePreviewList");

    if (previewWrap) previewWrap.classList.add("d-none");
    if (previewList) previewList.innerHTML = "";
}

function initBusImageManager() {
    const addBtn = document.getElementById("addBusImageBtn");
    const input = document.getElementById("busImageUrlInput");

    if (addBtn && input) {
        addBtn.addEventListener("click", function () {
            const url = input.value.trim();

            if (!url) {
                alert("Vui lòng nhập URL ảnh.");
                return;
            }

            if (addBusImages.includes(url)) {
                alert("Ảnh này đã được thêm.");
                return;
            }

            addBusImages.push(url);
            input.value = "";
            renderAddBusImages();
        });

        input.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                addBtn.click();
            }
        });
    }

    const editAddBtn = document.getElementById("addEditBusImageBtn");
    const editInput = document.getElementById("editBusImageUrlInput");

    if (editAddBtn && editInput) {
        editAddBtn.addEventListener("click", function () {
            const url = editInput.value.trim();

            if (!url) {
                alert("Vui lòng nhập URL ảnh.");
                return;
            }

            if (editBusImages.includes(url)) {
                alert("Ảnh này đã được thêm.");
                return;
            }

            editBusImages.push(url);
            editInput.value = "";
            renderEditBusImages();
        });

        editInput.addEventListener("keydown", function (event) {
            if (event.key === "Enter") {
                event.preventDefault();
                editAddBtn.click();
            }
        });
    }
}

function renderAddBusImages() {
    const list = document.getElementById("busImageUrlList");

    if (!list) return;

    if (!addBusImages.length) {
        list.innerHTML = `<div class="text-muted small">Chưa có ảnh URL nào.</div>`;
        return;
    }

    list.innerHTML = addBusImages.map((url, index) => `
        <div class="preview-image-item">
            <img src="${url}" alt="Ảnh ${index + 1}">
            <span>Ảnh ${index + 1}</span>
            <button type="button" class="btn btn-sm btn-danger mt-2" onclick="removeAddBusImage(${index})">
                Xóa
            </button>
        </div>
    `).join("");
}

function removeAddBusImage(index) {
    addBusImages.splice(index, 1);
    renderAddBusImages();
}

function renderEditBusImages() {
    const list = document.getElementById("editBusImageUrlList");

    if (!list) return;

    if (!editBusImages.length) {
        list.innerHTML = `<div class="text-muted small">Chưa có ảnh URL nào.</div>`;
        return;
    }

    list.innerHTML = editBusImages.map((url, index) => `
        <div class="preview-image-item">
            <img src="${url}" alt="Ảnh ${index + 1}">
            <span>Ảnh ${index + 1}</span>
            <button type="button" class="btn btn-sm btn-danger mt-2" onclick="removeEditBusImage(${index})">
                Xóa
            </button>
        </div>
    `).join("");
}

function removeEditBusImage(index) {
    editBusImages.splice(index, 1);
    renderEditBusImages();
}

function normalizeBusImages(bus) {
    if (Array.isArray(bus.images) && bus.images.length) return bus.images;
    if (bus.image) return [bus.image];
    return ["https://placehold.co/500x320?text=Bus"];
}

function getEditableBusImages(bus) {
    if (!Array.isArray(bus.images)) return [];

    return bus.images.filter(url =>
        url &&
        !url.includes("placehold.co")
    );
}

function openBusGallery(busId) {
    const bus = getBusById(busId);

    if (!bus) return;

    const images = normalizeBusImages(bus);
    const desc = bus.imageDesc || "Ảnh minh họa xe";
    const amenities = Array.isArray(bus.amenities) ? bus.amenities : [];

    const win = window.open("", "_blank", "width=1100,height=760");

    if (!win) {
        alert("Trình duyệt đang chặn popup. Vui lòng cho phép popup để xem ảnh.");
        return;
    }

    win.document.write(`
        <!DOCTYPE html>
        <html lang="vi">
        <head>
            <meta charset="UTF-8">
            <title>${bus.plate}</title>
            <style>
                body { margin:0; font-family:Arial,sans-serif; background:#111827; color:white; padding:28px; }
                .wrap { max-width:1100px; margin:0 auto; }
                h2 { margin:0 0 8px; }
                p { margin:0 0 14px; color:#d1d5db; }
                .amenities { display:flex; flex-wrap:wrap; gap:8px; margin-bottom:22px; }
                .amenity { background:rgba(59,130,246,.16); border:1px solid rgba(147,197,253,.35); color:#bfdbfe; padding:7px 10px; border-radius:999px; font-weight:700; font-size:13px; }
                .grid { display:grid; grid-template-columns:repeat(auto-fit,minmax(240px,1fr)); gap:16px; }
                .card { background:#1f2937; border-radius:18px; overflow:hidden; border:1px solid rgba(255,255,255,.08); }
                .card img { width:100%; height:220px; object-fit:cover; display:block; background:#000; }
                .caption { padding:12px 14px; color:#e5e7eb; font-size:14px; }
            </style>
        </head>
        <body>
            <div class="wrap">
                <h2>${bus.plate} - ${bus.type}</h2>
                <p>${desc}</p>
                <div class="amenities">
                    ${amenities.length ? amenities.map(item => `<span class="amenity">${item}</span>`).join("") : `<span class="amenity">Chưa cập nhật tiện ích</span>`}
                </div>
                <div class="grid">
                    ${images.map((image, index) => `
                        <div class="card">
                            <img src="${image}" alt="Ảnh ${index + 1}">
                            <div class="caption">Ảnh ${index + 1}</div>
                        </div>
                    `).join("")}
                </div>
            </div>
        </body>
        </html>
    `);

    win.document.close();
}

function openEditBusModal(maXe) {
    const bus = getBusById(maXe);

    if (!bus) {
        alert("Không tìm thấy xe.");
        return;
    }

    document.getElementById("editBusId").value = bus.id;
    document.getElementById("editBusPlate").value = bus.plate;
    document.getElementById("editBusType").value = bus.maLoaiXe || "";
    document.getElementById("editBusSeatCount").value = bus.seats;
    document.getElementById("editBusImageDesc").value = bus.imageDesc || "";

    editBusImages = getEditableBusImages(bus);
    renderEditBusImages();

    const editAmenityBox = document.getElementById("editAmenityBox");

    if (editAmenityBox) {
        editAmenityBox.innerHTML = renderAmenityCheckboxes("editBusAmenities", bus.amenities || []);
    }

    const modalElement = document.getElementById("editBusModal");
    bootstrap.Modal.getOrCreateInstance(modalElement).show();
}

async function changeBusStatus(maXe) {
    const bus = getBusById(maXe);

    if (!bus) {
        alert("Không tìm thấy xe.");
        return;
    }

    pendingBusStatusId = maXe;

    const statusSelect = document.getElementById("busStatusSelect");
    const busInfo = document.getElementById("busStatusBusInfo");
    const currentStatus = document.getElementById("busCurrentStatus");
    const modalElement = document.getElementById("busStatusModal");

    if (statusSelect) {
        const selectedStatus = BUS_STATUS_OPTIONS.includes(bus.status) ? bus.status : "";

        statusSelect.innerHTML = `
            <option value="">Chọn trạng thái</option>
            ${BUS_STATUS_OPTIONS.map(status => `
                <option value="${status}" ${status === selectedStatus ? "selected" : ""}>${status}</option>
            `).join("")}
        `;
    }

    if (busInfo) {
        busInfo.textContent = `${bus.plate} - ${bus.type}`;
    }

    if (currentStatus) {
        currentStatus.textContent = bus.status || "---";
    }

    if (!modalElement) {
        alert("Không tìm thấy hộp thoại đổi trạng thái xe.");
        return;
    }

    bootstrap.Modal.getOrCreateInstance(modalElement).show();
}

function initBusForms() {
    const busForm = document.getElementById("busForm");
    const editBusForm = document.getElementById("editBusForm");

    if (busForm) {
        busForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const selectedAmenityIds = getSelectedBusAmenities();
            const selectedAmenityNames = getSelectedBusAmenityNames();

            const data = {
                bienSo: document.getElementById("busPlate").value.trim(),
                maLoaiXe: document.getElementById("busType").value,
                soLuongGhe: Number(document.getElementById("busSeatCount").value),
                imageUrls: [...addBusImages],
                imageDesc: document.getElementById("busImageDesc")?.value.trim() || "",

                // Backend nên dùng field này để lưu vào bảng trung gian XE_TIENICH
                maTienIchList: selectedAmenityIds,

                // Giữ thêm field này để frontend hiển thị hoặc tương thích code cũ
                amenities: selectedAmenityNames
            };

            if (!data.bienSo) {
                alert("Vui lòng nhập biển số xe.");
                return;
            }

            if (!data.maLoaiXe) {
                alert("Vui lòng chọn loại xe.");
                return;
            }

            if (!data.soLuongGhe || data.soLuongGhe <= 0) {
                alert("Số ghế phải lớn hơn 0.");
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/api/staff/xe`, {
                    method: "POST",
                    headers: getAuthHeaders(),
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (!response.ok) {
                    alert(result.message || "Thêm xe thất bại.");
                    return;
                }

                buses.unshift({
                    id: result.maXe,
                    plate: result.bienSo,
                    maLoaiXe: result.maLoaiXe || data.maLoaiXe,
                    type: result.tenLoaiXe || result.maLoaiXe || getLoaiXeName(data.maLoaiXe),
                    seats: result.soLuongGhe,
                    status: result.trangThai,
                    images: result.images && result.images.length
                        ? result.images
                        : ["https://placehold.co/500x320?text=Bus"],
                    imageDesc: result.imageDesc || "Ảnh minh họa xe",
                    amenities: result.amenities && result.amenities.length
                    ? normalizeAmenityNames(result.amenities)
                    : selectedAmenityNames
                });

                this.reset();
                addBusImages = [];
                renderAddBusImages();
                resetBusImagesPreview();
                closeModal("busModal");

                renderBusOptions();
                renderBuses();
                renderTrips();
                renderSeatMap();
                renderReport();

                alert("Thêm xe thành công.");
            } catch (error) {
                console.error("Lỗi thêm xe:", error);
                alert("Không thể kết nối server.");
            }
        });
    }

    if (editBusForm) {
        editBusForm.addEventListener("submit", async function (event) {
            event.preventDefault();

            const maXe = document.getElementById("editBusId").value;

            const selectedAmenityIds = getSelectedEditBusAmenities();
            const selectedAmenityNames = getSelectedEditBusAmenityNames();

            const data = {
                bienSo: document.getElementById("editBusPlate").value.trim(),
                maLoaiXe: document.getElementById("editBusType").value,
                soLuongGhe: Number(document.getElementById("editBusSeatCount").value),
                imageUrls: [...editBusImages],
                imageDesc: document.getElementById("editBusImageDesc")?.value.trim() || "",

                // Gửi danh sách mã tiện ích khi bấm Lưu thay đổi
                maTienIchList: selectedAmenityIds,

                // Giữ tên tiện ích để frontend không bị lệch nếu backend trả về tên
                amenities: selectedAmenityNames
            };

            if (!data.bienSo) {
                alert("Vui lòng nhập biển số xe.");
                return;
            }

            if (!data.maLoaiXe) {
                alert("Vui lòng chọn loại xe.");
                return;
            }

            if (!data.soLuongGhe || data.soLuongGhe <= 0) {
                alert("Số ghế phải lớn hơn 0.");
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/api/staff/xe/${maXe}`, {
                    method: "PUT",
                    headers: getAuthHeaders(),
                    body: JSON.stringify(data)
                });

                const result = await response.json();

                if (!response.ok) {
                    alert(result.message || "Cập nhật xe thất bại.");
                    return;
                }

                const index = buses.findIndex(item => item.id === maXe);

                const updatedBus = {
                    id: result.maXe,
                    plate: result.bienSo,
                    maLoaiXe: result.maLoaiXe || data.maLoaiXe,
                    type: result.tenLoaiXe || getLoaiXeName(data.maLoaiXe),
                    seats: result.soLuongGhe,
                    status: result.trangThai,
                    images: result.images && result.images.length
                        ? result.images
                        : ["https://placehold.co/500x320?text=Bus"],
                    imageDesc: result.imageDesc || "Ảnh minh họa xe",
                    amenities: result.amenities && result.amenities.length
                    ? normalizeAmenityNames(result.amenities)
                    : selectedAmenityNames
                };

                if (index !== -1) {
                    buses[index] = updatedBus;
                }

                closeModal("editBusModal");

                renderBusOptions();
                renderBuses();
                renderTrips();
                renderSeatMap();
                renderReport();

                alert("Cập nhật xe thành công.");
            } catch (error) {
                console.error("Lỗi cập nhật xe:", error);
                alert("Không thể kết nối server.");
            }
        });
    }
}

function initBusStatusModal() {
    const form = document.getElementById("busStatusForm");

    if (!form) return;

    form.addEventListener("submit", async function (event) {
        event.preventDefault();

        const bus = pendingBusStatusId ? getBusById(pendingBusStatusId) : null;
        const select = document.getElementById("busStatusSelect");
        const nextStatus = select?.value || "";

        if (!bus) {
            alert("Không tìm thấy xe.");
            return;
        }

        if (!BUS_STATUS_OPTIONS.includes(nextStatus)) {
            alert("Vui lòng chọn trạng thái hợp lệ.");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/api/staff/xe/${bus.id}/status`, {
                method: "PUT",
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    trangThai: nextStatus
                })
            });

            const result = await response.json();

            if (!response.ok) {
                alert(result.message || "Cập nhật trạng thái xe thất bại.");
                return;
            }

            bus.status = result.trangThai || nextStatus;

            renderBuses();
            renderBusOptions();
            renderTrips();
            renderSeatMap();
            renderReport();

            closeModal("busStatusModal");
            pendingBusStatusId = null;

            alert("Cập nhật trạng thái xe thành công.");
        } catch (error) {
            console.error("Lỗi cập nhật trạng thái xe:", error);
            alert("Không thể kết nối server.");
        }
    });
}

function renderBusOptions() {
    const tripBusSelect = document.getElementById("tripBusSelect");
    const editTripBusSelect = document.getElementById("editTripBusSelect");
    const tripBusFilter = document.getElementById("tripBusFilter");
    const seatTripSelect = document.getElementById("seatTripSelect");

    if (tripBusSelect) {
        if (!buses.length) {
            tripBusSelect.innerHTML = `<option value="">Chưa có xe</option>`;
        } else {
            tripBusSelect.innerHTML = buses.map(bus => `
                <option value="${bus.id}">${bus.plate} - ${bus.type}</option>
            `).join("");
        }
    }

    if (editTripBusSelect) {
        if (!buses.length) {
            editTripBusSelect.innerHTML = `<option value="">Chưa có xe</option>`;
        } else {
            editTripBusSelect.innerHTML = buses.map(bus => `
                <option value="${bus.id}">${bus.plate} - ${bus.type}</option>
            `).join("");
        }
    }

    if (tripBusFilter) {
        tripBusFilter.innerHTML = `
            <option value="">Tất cả xe</option>
            ${buses.map(bus => `<option value="${bus.id}">${bus.plate}</option>`).join("")}
        `;
    }

    if (seatTripSelect) {
        seatTripSelect.innerHTML = trips.map(trip => {
            const bus = getBusById(trip.busId);
            return `<option value="${trip.id}">${trip.id} - ${bus ? bus.plate : "Không rõ xe"} - ${trip.route}</option>`;
        }).join("");
    }
}

function renderBuses() {
    const tbody = document.getElementById("busTableBody");

    if (!tbody) return;

    if (!buses.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="6" class="text-center text-muted py-4">
                    Chưa có xe nào hoặc API chưa trả dữ liệu.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = buses.map(bus => {
        const images = normalizeBusImages(bus);
        const firstImage = images[0] || "https://placehold.co/500x320?text=Bus";
        const imageDesc = bus.imageDesc || "Chưa có mô tả ảnh";

        return `
            <tr>
                <td>${bus.id}</td>
                <td>
                    <div class="bus-info-cell">
                        <button class="bus-thumb" type="button" onclick="openBusGallery('${bus.id}')">
                            <img src="${firstImage}" alt="${bus.plate}">
                            <span class="image-count-badge">${images.length}</span>
                        </button>

                        <div>
                            <strong>${bus.plate}</strong>
                            <div class="text-muted small">${imageDesc}</div>

                            <div class="bus-amenities">
                                ${renderAmenityTags(bus.amenities)}
                            </div>

                            <button class="mini-link-btn" type="button" onclick="openBusGallery('${bus.id}')">
                                Xem ảnh (${images.length})
                            </button>
                        </div>
                    </div>
                </td>
                <td>${bus.type}</td>
                <td>${bus.seats}</td>
                <td>${statusBadge(bus.status)}</td>
                <td>
                    <button class="action-btn" onclick="openEditBusModal('${bus.id}')">
                        <i class="fa-solid fa-pen"></i>
                    </button>
                    <button class="action-btn danger" type="button" title="Sửa trạng thái xe" onclick="changeBusStatus('${bus.id}')">
                        <i class="fa-solid fa-screwdriver-wrench"></i>
                    </button>
                </td>
            </tr>
        `;
    }).join("");
}

function renderAmenityTags(amenities) {
    const list = normalizeAmenityNames(amenities);

    if (!list.length) {
        return `<span class="amenity-tag muted">Chưa có tiện ích</span>`;
    }

    return list.slice(0, 4).map(item => `
        <span class="amenity-tag">${getAmenityIcon(item)} ${item}</span>
    `).join("") + (list.length > 4 ? `<span class="amenity-tag more">+${list.length - 4}</span>` : "");
}