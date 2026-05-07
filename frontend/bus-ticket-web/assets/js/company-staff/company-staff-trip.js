let tripStops = [];
let editTripStops = [];
let editingTripId = null;

function initTripForm() {
    const tripForm = document.getElementById("tripForm");

    if (!tripForm) return;

    const departureSelect = document.getElementById("tripDepartureSelect");
    const arrivalSelect = document.getElementById("tripArrivalSelect");

    try { renderStationOptions("tripDepartureSelect"); } catch (error) {}
    try { renderStationOptions("tripArrivalSelect"); } catch (error) {}
    try { renderStationOptions("tripPickupSelect"); } catch (error) {}
    try { renderStationOptions("tripDropoffSelect"); } catch (error) {}
    try { renderStationOptions("editTripDepartureSelect"); } catch (error) {}
    try { renderStationOptions("editTripArrivalSelect"); } catch (error) {}
    try { renderStationOptions("editTripPickupSelect"); } catch (error) {}
    try { renderStationOptions("editTripDropoffSelect"); } catch (error) {}

    departureSelect?.addEventListener("change", updateTripRouteField);
    arrivalSelect?.addEventListener("change", updateTripRouteField);

    document.getElementById("editTripDepartureSelect")?.addEventListener("change", updateEditTripRouteField);
    document.getElementById("editTripArrivalSelect")?.addEventListener("change", updateEditTripRouteField);

    document.getElementById("addTripPickupBtn")?.addEventListener("click", () => addTripStop("pickup"));
    document.getElementById("addTripDropoffBtn")?.addEventListener("click", () => addTripStop("dropoff"));

    document.getElementById("editAddTripPickupBtn")?.addEventListener("click", () => addEditTripStop("pickup"));
    document.getElementById("editAddTripDropoffBtn")?.addEventListener("click", () => addEditTripStop("dropoff"));

    updateTripRouteField();

    tripForm.addEventListener("submit", async function (event) {
        event.preventDefault();

        const busId = document.getElementById("tripBusSelect").value;

        const departureRawValue = document.getElementById("tripDepartureSelect").value;
        const arrivalRawValue = document.getElementById("tripArrivalSelect").value;

        const departureId = resolveStationId(departureRawValue);
        const arrivalId = resolveStationId(arrivalRawValue);

        const ngayDi = document.getElementById("tripDate").value;
        const gioDi = document.getElementById("tripTime").value;
        const giaVe = Number(document.getElementById("tripPrice").value);
        const khoangCach = Number(document.getElementById("tripDistance")?.value) || 0;
        const thoiGianDuKien = Number(document.getElementById("tripEstimatedTime")?.value) || 0;

        if (!busId) {
            alert("Vui lòng chọn xe.");
            return;
        }

        if (!departureId) {
            alert("Vui lòng chọn bến đi.");
            return;
        }

        if (!arrivalId) {
            alert("Vui lòng chọn bến đến.");
            return;
        }

        if (departureId === arrivalId) {
            alert("Bến đi và bến đến không được trùng nhau.");
            return;
        }

        if (!ngayDi) {
            alert("Vui lòng chọn ngày đi.");
            return;
        }

        if (!gioDi) {
            alert("Vui lòng chọn giờ đi.");
            return;
        }

        if (giaVe < 0) {
            alert("Giá vé không hợp lệ.");
            return;
        }

        if (khoangCach <= 0) {
            alert("Khoảng cách phải lớn hơn 0.");
            return;
        }

        if (thoiGianDuKien <= 0) {
            alert("Thời gian dự kiến phải lớn hơn 0.");
            return;
        }

        const data = {
            maXe: busId,
            maBenDi: departureId,
            maBenDen: arrivalId,
            ngayDi: ngayDi,
            gioDi: gioDi,
            giaVe: giaVe,
            khoangCach: khoangCach,
            thoiGianDuKien: thoiGianDuKien,
            stops: tripStops.map((stop, index) => ({
                stationId: resolveStationId(stop.stationId),
                type: stop.type,
                order: index + 1
            }))
        };

        console.log("DATA GỬI LÊN:", data);

        try {
            const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe`, {
                method: "POST",
                headers: getAuthHeaders(),
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (!response.ok) {
                alert(result.message || "Thêm chuyến xe thất bại.");
                return;
            }

            const newTrip = mapStaffTripResponseToTrip(result);

            trips.unshift(newTrip);

            this.reset();
            tripStops = [];
            renderTripStops();
            updateTripRouteField();
            closeModal("tripModal");

            renderBusOptions();
            renderTrips();
            renderRecentTrips();
            renderSeatMap();
            renderReport();

            alert("Thêm chuyến xe thành công.");
        } catch (error) {
            console.error("Lỗi thêm chuyến xe:", error);
            alert("Không thể kết nối server.");
        }
    });

    const editTripForm = document.getElementById("editTripForm");

    if (editTripForm) {
        editTripForm.addEventListener("submit", function (event) {
            event.preventDefault();
            saveTripChanges();
        });
    }
}

function resolveStationId(value) {
    if (!value) return "";

    const rawValue = String(value).trim();

    if (!rawValue) return "";

    const foundById = getStationById(rawValue);

    if (foundById) {
        return foundById.id || foundById.maBen || rawValue;
    }

    if (Array.isArray(stations)) {
        const foundByName = stations.find(station => {
            const id = String(station.id || station.maBen || "").trim();
            const name = String(station.name || station.tenBen || "").trim();

            return id === rawValue || name === rawValue;
        });

        if (foundByName) {
            return foundByName.id || foundByName.maBen || rawValue;
        }
    }

    return rawValue;
}

function getStationNameByValue(value) {
    const stationId = resolveStationId(value);
    const station = getStationById(stationId);

    if (station) {
        return station.name || station.tenBen || stationId;
    }

    if (Array.isArray(stations)) {
        const found = stations.find(item => {
            const id = String(item.id || item.maBen || "").trim();
            const name = String(item.name || item.tenBen || "").trim();

            return id === String(value).trim() || name === String(value).trim();
        });

        if (found) {
            return found.name || found.tenBen || value;
        }
    }

    return value || "";
}

function mapStaffTripResponseToTrip(item) {
    return {
        id: item.maChuyen,
        busId: item.maXe,
        route: item.tenTuyen,
        date: item.ngayDi,
        time: item.gioDi ? String(item.gioDi).substring(0, 5) : "",
        price: Number(item.giaVe || 0),
        emptySeats: item.gheTrong || 0,
        status: item.trangThai || "Đang mở bán",
        departureId: item.maBenDi || null,
        arrivalId: item.maBenDen || null,
        khoangCach: Number(item.khoangCach || 0),
        thoiGianDuKien: Number(item.thoiGianDuKien || 0),
        stops: Array.isArray(item.stops)
            ? item.stops.map(stop => ({
                stationId: stop.stationId || stop.maBen || stop.maDiem,
                maDiem: stop.maDiem,
                name: stop.tenDiem || stop.name || "",
                type: stop.loai === "Trả" || stop.type === "dropoff" ? "dropoff" : "pickup",
                time: stop.thoiGian || stop.time || null,
                order: stop.thuTu || stop.order || 0
            }))
            : []
    };
}

function updateTripRouteField() {
    const departureSelect = document.getElementById("tripDepartureSelect");
    const arrivalSelect = document.getElementById("tripArrivalSelect");
    const routeInput = document.getElementById("tripRoute");

    if (!departureSelect || !arrivalSelect || !routeInput) return;

    const departureId = resolveStationId(departureSelect.value);
    const arrivalId = resolveStationId(arrivalSelect.value);

    const departureName = getStationNameByValue(departureId);
    const arrivalName = getStationNameByValue(arrivalId);

    if (departureId && arrivalId) {
        routeInput.value = `${departureName} - ${arrivalName}`;
        return;
    }

    routeInput.value = "";
}

function updateEditTripRouteField() {
    const departureSelect = document.getElementById("editTripDepartureSelect");
    const arrivalSelect = document.getElementById("editTripArrivalSelect");
    const routeInput = document.getElementById("editTripRoute");

    if (!departureSelect || !arrivalSelect || !routeInput) return;

    const departureId = resolveStationId(departureSelect.value);
    const arrivalId = resolveStationId(arrivalSelect.value);

    const departureName = getStationNameByValue(departureId);
    const arrivalName = getStationNameByValue(arrivalId);

    if (departureId && arrivalId) {
        routeInput.value = `${departureName} - ${arrivalName}`;
        return;
    }

    routeInput.value = "";
}

function addTripStop(type) {
    const selectId = type === "dropoff" ? "tripDropoffSelect" : "tripPickupSelect";
    const selectEl = document.getElementById(selectId);

    if (!selectEl) return;

    const stationId = resolveStationId(selectEl.value);

    if (!stationId) {
        alert(type === "dropoff" ? "Vui lòng chọn một điểm trả để thêm." : "Vui lòng chọn một điểm đón để thêm.");
        return;
    }

    const existed = tripStops.some(stop =>
        resolveStationId(stop.stationId) === stationId &&
        stop.type === type
    );

    if (existed) {
        alert("Điểm này đã được thêm.");
        return;
    }

    tripStops.push({
        stationId,
        type
    });

    renderTripStops();
}

function addEditTripStop(type) {
    const selectId = type === "dropoff" ? "editTripDropoffSelect" : "editTripPickupSelect";
    const selectEl = document.getElementById(selectId);

    if (!selectEl) return;

    const stationId = resolveStationId(selectEl.value);

    if (!stationId) {
        alert(type === "dropoff" ? "Vui lòng chọn một điểm trả để thêm." : "Vui lòng chọn một điểm đón để thêm.");
        return;
    }

    const existed = editTripStops.some(stop =>
        resolveStationId(stop.stationId) === stationId &&
        stop.type === type
    );

    if (existed) {
        alert("Điểm này đã được thêm.");
        return;
    }

    editTripStops.push({
        stationId,
        type
    });

    renderEditTripStops();
}

function renderTripStops() {
    const pickupList = document.getElementById("tripPickupStopsList");
    const dropoffList = document.getElementById("tripDropoffStopsList");

    if (pickupList) {
        const pickupStops = tripStops.filter(stop => stop.type === "pickup");

        pickupList.innerHTML = pickupStops.length ? pickupStops.map((stop, index) => {
            const name = getStationNameByValue(stop.stationId);

            return `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div>${name}</div>
                    <button class="btn btn-sm btn-outline-danger" onclick="removeTripStopByType('pickup', ${index})">Xoá</button>
                </li>
            `;
        }).join("") : `<li class="list-group-item text-muted">Chưa có điểm đón.</li>`;
    }

    if (dropoffList) {
        const dropoffStops = tripStops.filter(stop => stop.type === "dropoff");

        dropoffList.innerHTML = dropoffStops.length ? dropoffStops.map((stop, index) => {
            const name = getStationNameByValue(stop.stationId);

            return `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div>${name}</div>
                    <button class="btn btn-sm btn-outline-danger" onclick="removeTripStopByType('dropoff', ${index})">Xoá</button>
                </li>
            `;
        }).join("") : `<li class="list-group-item text-muted">Chưa có điểm trả.</li>`;
    }
}

function renderEditTripStops() {
    const pickupList = document.getElementById("editTripPickupStopsList");
    const dropoffList = document.getElementById("editTripDropoffStopsList");

    if (pickupList) {
        const pickupStops = editTripStops.filter(stop => stop.type === "pickup");

        pickupList.innerHTML = pickupStops.length ? pickupStops.map((stop, index) => {
            const name = getStationNameByValue(stop.stationId);

            return `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div>${name}</div>
                    <button class="btn btn-sm btn-outline-danger" onclick="removeEditTripStopByType('pickup', ${index})">Xoá</button>
                </li>
            `;
        }).join("") : `<li class="list-group-item text-muted">Chưa có điểm đón.</li>`;
    }

    if (dropoffList) {
        const dropoffStops = editTripStops.filter(stop => stop.type === "dropoff");

        dropoffList.innerHTML = dropoffStops.length ? dropoffStops.map((stop, index) => {
            const name = getStationNameByValue(stop.stationId);

            return `
                <li class="list-group-item d-flex justify-content-between align-items-center">
                    <div>${name}</div>
                    <button class="btn btn-sm btn-outline-danger" onclick="removeEditTripStopByType('dropoff', ${index})">Xoá</button>
                </li>
            `;
        }).join("") : `<li class="list-group-item text-muted">Chưa có điểm trả.</li>`;
    }
}

function removeTripStopByType(type, index) {
    const filteredStops = tripStops.filter(stop => stop.type === type);
    const targetStop = filteredStops[index];

    if (!targetStop) return;

    const realIndex = tripStops.findIndex(stop =>
        stop.type === type &&
        resolveStationId(stop.stationId) === resolveStationId(targetStop.stationId)
    );

    if (realIndex < 0) return;

    tripStops.splice(realIndex, 1);
    renderTripStops();
}

function removeEditTripStopByType(type, index) {
    const filteredStops = editTripStops.filter(stop => stop.type === type);
    const targetStop = filteredStops[index];

    if (!targetStop) return;

    const realIndex = editTripStops.findIndex(stop =>
        stop.type === type &&
        resolveStationId(stop.stationId) === resolveStationId(targetStop.stationId)
    );

    if (realIndex < 0) return;

    editTripStops.splice(realIndex, 1);
    renderEditTripStops();
}

function removeTripStop(index) {
    if (index < 0 || index >= tripStops.length) return;

    tripStops.splice(index, 1);
    renderTripStops();
}

function openEditTripModal(tripId) {
    const trip = trips.find(item => item.id === tripId);

    if (!trip) return;

    editingTripId = tripId;
    editTripStops = Array.isArray(trip.stops) ? trip.stops.map(stop => ({ ...stop })) : [];

    document.getElementById("editTripId").value = trip.id;
    document.getElementById("editTripBusSelect").value = trip.busId || "";
    document.getElementById("editTripDepartureSelect").value = trip.departureId || "";
    document.getElementById("editTripArrivalSelect").value = trip.arrivalId || "";
    document.getElementById("editTripDate").value = trip.date || "";
    document.getElementById("editTripTime").value = trip.time || "";
    document.getElementById("editTripPrice").value = trip.price ?? 0;
    document.getElementById("editTripDistance").value = trip.khoangCach || 0;
    document.getElementById("editTripEstimatedTime").value = trip.thoiGianDuKien || 0;

    updateEditTripRouteField();
    renderEditTripStops();

    const modalEl = document.getElementById("editTripModal");

    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

function saveTripChanges() {
    const tripIndex = trips.findIndex(item => item.id === editingTripId);

    if (tripIndex < 0) return;

    const busId = document.getElementById("editTripBusSelect").value;

    const departureRawValue = document.getElementById("editTripDepartureSelect").value;
    const arrivalRawValue = document.getElementById("editTripArrivalSelect").value;

    const departureId = resolveStationId(departureRawValue);
    const arrivalId = resolveStationId(arrivalRawValue);

    const departureName = getStationNameByValue(departureId);
    const arrivalName = getStationNameByValue(arrivalId);

    const route = document.getElementById("editTripRoute").value.trim()
        || `${departureName}${departureId && arrivalId ? " - " : ""}${arrivalName}`.trim();

    const khoangCach = Number(document.getElementById("editTripDistance").value) || 0;
    const thoiGianDuKien = Number(document.getElementById("editTripEstimatedTime").value) || 0;

    if (!busId) {
        alert("Vui lòng chọn xe.");
        return;
    }

    if (!departureId) {
        alert("Vui lòng chọn bến đi.");
        return;
    }

    if (!arrivalId) {
        alert("Vui lòng chọn bến đến.");
        return;
    }

    if (departureId === arrivalId) {
        alert("Bến đi và bến đến không được trùng nhau.");
        return;
    }

    if (khoangCach <= 0) {
        alert("Khoảng cách phải lớn hơn 0.");
        return;
    }

    if (thoiGianDuKien <= 0) {
        alert("Thời gian dự kiến phải lớn hơn 0.");
        return;
    }

    trips[tripIndex] = {
        ...trips[tripIndex],
        busId,
        departureId: departureId || null,
        arrivalId: arrivalId || null,
        route,
        date: document.getElementById("editTripDate").value,
        time: document.getElementById("editTripTime").value,
        price: Number(document.getElementById("editTripPrice").value),
        khoangCach,
        thoiGianDuKien,
        stops: [...editTripStops]
    };

    editingTripId = null;
    editTripStops = [];

    const modalEl = document.getElementById("editTripModal");

    if (modalEl) {
        bootstrap.Modal.getInstance(modalEl)?.hide();
    }

    renderBusOptions();
    renderTrips();
    renderRecentTrips();
    renderSeatMap();
    renderReport();

    alert("Đã cập nhật chuyến xe.");
}

function deleteTrip(tripId) {
    const trip = trips.find(item => item.id === tripId);

    if (!trip) return;

    const accepted = confirm(`Bạn có chắc muốn xóa chuyến ${tripId} không?`);

    if (!accepted) return;

    trips = trips.filter(item => item.id !== tripId);

    if (editingTripId === tripId) {
        editingTripId = null;
        editTripStops = [];
    }

    renderBusOptions();
    renderTrips();
    renderRecentTrips();
    renderSeatMap();
    renderReport();
}

function renderTrips() {
    const tbody = document.getElementById("tripTableBody");

    if (!tbody) return;

    const busFilter = document.getElementById("tripBusFilter")?.value || "";
    const statusFilter = document.getElementById("tripStatusFilter")?.value || "";

    let data = [...trips];

    if (busFilter) {
        data = data.filter(trip => trip.busId === busFilter);
    }

    if (statusFilter) {
        data = data.filter(trip => trip.status === statusFilter);
    }

    if (!data.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" class="text-center text-muted py-4">
                    Chưa có chuyến xe nào hoặc API chưa trả dữ liệu.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = data.map(trip => {
        const bus = getBusById(trip.busId);
        const depName = trip.departureId ? getStationNameByValue(trip.departureId) : "";
        const arrName = trip.arrivalId ? getStationNameByValue(trip.arrivalId) : "";

        return `
            <tr>
                <td>${trip.id}</td>
                <td>${bus ? bus.plate : "Không rõ"}</td>
                <td>
                    ${trip.route || "---"}
                    <div class="text-muted small">
                        ${depName}${depName && arrName ? " → " : ""}${arrName}
                    </div>
                    ${
                        trip.khoangCach || trip.thoiGianDuKien
                            ? `<div class="text-muted small">
                                <i class="fa-solid fa-road"></i> ${trip.khoangCach || 0} km
                                |
                                <i class="fa-solid fa-hourglass-end"></i> ${trip.thoiGianDuKien || 0} phút
                            </div>`
                            : ""
                    }
                </td>
                <td>${trip.date || ""}</td>
                <td>${trip.time || ""}</td>
                <td>${money(trip.price)}</td>
                <td>${trip.emptySeats}</td>
                <td>${statusBadge(trip.status)}</td>
                <td>
                    <div class="d-flex gap-2 flex-wrap">
                        <button type="button" class="btn btn-sm btn-outline-primary" onclick="openEditTripModal('${trip.id}')">
                            <i class="fa-solid fa-pen-to-square"></i> Sửa
                        </button>
                        <button type="button" class="btn btn-sm btn-outline-danger" onclick="deleteTrip('${trip.id}')">
                            <i class="fa-solid fa-trash"></i> Xóa
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }).join("");
}

async function loadStaffTrips() {
    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe`, {
            method: "GET",
            headers: getAuthHeaders()
        });

        const result = await response.json();

        if (!response.ok) {
            console.error("Lỗi tải chuyến xe:", result.message || result);
            trips = [];
            renderTrips();
            return;
        }

        trips = Array.isArray(result)
            ? result.map(item => mapStaffTripResponseToTrip(item))
            : [];

        renderBusOptions();
        renderTrips();
        renderRecentTrips();
        renderSeatMap();
        renderReport();
    } catch (error) {
        console.error("Lỗi gọi API chuyến xe:", error);
        trips = [];
        renderTrips();
    }
}