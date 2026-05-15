let tripStops = [];
let editTripStops = [];
let originalEditTripStops = [];
let editingTripId = null;
let editingTripStatusId = null;

async function loadDiemDonTraForBen(maBen, loai) {
    if (!maBen) return [];

    try {
        const res = await fetch(`${API_BASE_URL}/api/ben-xe/${encodeURIComponent(maBen)}/diem-don-tra?loai=${encodeURIComponent(loai)}`, {
            method: "GET",
            headers: { "Content-Type": "application/json" }
        });

        const body = await res.json();

        if (!res.ok) return [];

        const arr = Array.isArray(body) ? body : (Array.isArray(body.data) ? body.data : []);

        return arr.map(item => ({
            maDiemBen: item.maDiemBen || item.maDiem || item.id || "",
            tenDiem: item.tenDiem || item.name || item.tenBen || "",
            thoiGian: item.thoiGian || item.time || "",
            diaChi: item.diaChi || item.address || ""
        }));
    } catch (error) {
        console.warn("Không tải được điểm đón/trả:", error);
        return [];
    }
}

function renderPointOptions(selectId, points, placeholder) {
    const el = document.getElementById(selectId);
    if (!el) return;

    const finalPoints = Array.isArray(points) ? points : [];

    if (!finalPoints.length) {
        el.innerHTML = `<option value="">-- ${placeholder} --</option>`;
        return;
    }

    el.innerHTML = `
        <option value="">-- ${placeholder} --</option>
        ${finalPoints.map(point => `
            <option value="${point.maDiemBen || point.tenDiem}" data-ma-diem="${point.maDiemBen || ""}" data-name="${(point.tenDiem || "")}" data-time="${(point.thoiGian || "")}">
                ${point.tenDiem || "Không rõ điểm"}${point.thoiGian ? ` - ${point.thoiGian}` : ""}
            </option>
        `).join("")}
    `;

    if (finalPoints.length === 1) {
        el.selectedIndex = 1;
    }

    el.onchange = selectId && selectId.includes("edit") ? renderEditTripStops : renderTripStops;
}

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

    departureSelect?.addEventListener("change", async () => {
        updateTripRouteField();
        const benMa = resolveStationId(departureSelect.value);
        if (benMa) {
            const points = await loadDiemDonTraForBen(benMa, "don");
            renderPointOptions("tripPickupSelect", points, "Chọn điểm đón");
        } else {
            try { renderStationOptions("tripPickupSelect"); } catch (e) {}
        }
        tripStops = [];
        renderTripStops();
    });

    arrivalSelect?.addEventListener("change", async () => {
        updateTripRouteField();
        const benMa = resolveStationId(arrivalSelect.value);
        if (benMa) {
            const points = await loadDiemDonTraForBen(benMa, "tra");
            renderPointOptions("tripDropoffSelect", points, "Chọn điểm trả");
        } else {
            try { renderStationOptions("tripDropoffSelect"); } catch (e) {}
        }
        tripStops = [];
        renderTripStops();
    });

    const editDep = document.getElementById("editTripDepartureSelect");
    const editArr = document.getElementById("editTripArrivalSelect");

    editDep?.addEventListener("change", async () => {
        updateEditTripRouteField();
        const benMa = resolveStationId(editDep.value);
        if (benMa) {
            const points = await loadDiemDonTraForBen(benMa, "don");
            renderPointOptions("editTripPickupSelect", points, "Chọn điểm đón");
        } else {
            try { renderStationOptions("editTripPickupSelect"); } catch (e) {}
        }
        editTripStops = [];
        renderEditTripStops();
    });

    editArr?.addEventListener("change", async () => {
        updateEditTripRouteField();
        const benMa = resolveStationId(editArr.value);
        if (benMa) {
            const points = await loadDiemDonTraForBen(benMa, "tra");
            renderPointOptions("editTripDropoffSelect", points, "Chọn điểm trả");
        } else {
            try { renderStationOptions("editTripDropoffSelect"); } catch (e) {}
        }
        editTripStops = [];
        renderEditTripStops();
    });

    document.getElementById("addTripPickupBtn")?.addEventListener("click", () => addTripStop("pickup"));
    document.getElementById("addTripDropoffBtn")?.addEventListener("click", () => addTripStop("dropoff"));

    document.getElementById("editAddTripPickupBtn")?.addEventListener("click", () => addEditTripStop("pickup"));
    document.getElementById("editAddTripDropoffBtn")?.addEventListener("click", () => addEditTripStop("dropoff"));

    updateTripRouteField();

    (async () => {
        const benDep = resolveStationId(departureSelect?.value);
        const benArr = resolveStationId(arrivalSelect?.value);

        if (benDep) {
            const p = await loadDiemDonTraForBen(benDep, "don");
            renderPointOptions("tripPickupSelect", p, "Chọn điểm đón");
        }

        if (benArr) {
            const p2 = await loadDiemDonTraForBen(benArr, "tra");
            renderPointOptions("tripDropoffSelect", p2, "Chọn điểm trả");
        }
    })();

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
            stops: mapTripStopsForRequest(tripStops)
        };

        console.log("DATA GỬI LÊN:", data);

        try {
            const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe`, {
                method: "POST",
                headers: Object.assign({ "Content-Type": "application/json" }, getAuthHeaders()),
                body: JSON.stringify(data)
            });

            const result = await readApiBody(response);

            if (!response.ok) {
                alert(getApiErrorMessage(result, "Them chuyen xe that bai."));
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

            await refreshSeatSection();

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

    const tripStatusForm = document.getElementById("tripStatusForm");

    if (tripStatusForm) {
        tripStatusForm.addEventListener("submit", function (event) {
            event.preventDefault();
            saveTripStatus();
        });
    }
}

async function refreshSeatSection() {
    if (typeof loadSeatTrips === "function") {
        await loadSeatTrips();
        return;
    }

    if (typeof renderSeatTripOptions === "function") {
        renderSeatTripOptions();
    }

    if (typeof renderSeatMap === "function") {
        await renderSeatMap();
    }
}

async function readApiBody(response) {
    const text = await response.text();

    if (!text) return {};

    try {
        return JSON.parse(text);
    } catch (error) {
        return { message: text };
    }
}

function getApiErrorMessage(result, fallback) {
    if (!result) return fallback;

    return result.message || result.error || result.detail || fallback;
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

function normalizeStopType(type) {
    const value = String(type || "").trim().toLowerCase();

    if (
        value === "dropoff" ||
        value === "tra" ||
        value === "trả" ||
        value === "diemtra" ||
        value === "điểm trả"
    ) {
        return "dropoff";
    }

    return "pickup";
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
                stationId: stop.stationId || stop.maDiemBen || stop.maDiem || stop.maBen || "",
                maDiem: stop.maDiem,
                maDiemBen: stop.maDiemBen,
                tenDiem: stop.tenDiem || stop.name || "",
                name: stop.tenDiem || stop.name || "",
                type: normalizeStopType(stop.type || stop.loai),
                time: stop.thoiGian || stop.time || null,
                order: stop.thuTu || stop.order || 0
            }))
            : []
    };
}

function mapTripStopsForRequest(stops) {
    const counters = {
        pickup: 0,
        dropoff: 0
    };

    return (Array.isArray(stops) ? stops : [])
        .map(stop => {
            const type = normalizeStopType(stop.type || stop.loai);
            const maDiemBen = getTripStopCode(stop);

            if (!maDiemBen) {
                return null;
            }

            counters[type] += 1;

            return {
                maDiemBen,
                name: stop.name || stop.tenDiem || "",
                type,
                order: counters[type]
            };
        })
        .filter(Boolean);
}

function getTripStopCode(stop) {
    if (!stop) return "";

    const code = stop.maDiemBen || stop.stationId || stop.maBen || stop.maDiem || "";

    return String(code).trim();
}

function isPointCode(value) {
    return /^(DB_|DD|DT)/i.test(String(value || "").trim());
}

function getOptionDisplayName(selectId, value) {
    const selectEl = document.getElementById(selectId);
    const code = String(value || "").trim();

    if (!selectEl || !code) return "";

    const option = Array.from(selectEl.options).find(item =>
        String(item.value || "").trim() === code ||
        String(item.dataset?.maDiem || "").trim() === code
    );

    if (!option) return "";

    return String(option.dataset?.name || option.textContent || "").trim();
}

function getTripStopDisplayName(stop, selectId) {
    const savedName = String(stop?.tenDiem || stop?.name || "").trim();

    if (savedName && !isPointCode(savedName)) {
        return savedName;
    }

    const optionName = getOptionDisplayName(selectId, getTripStopCode(stop));

    if (optionName && !isPointCode(optionName)) {
        return optionName;
    }

    return "Diem don tra";
}

function hasTripStop(stops, code) {
    const value = String(code || "").trim();

    if (!value) return false;

    return (Array.isArray(stops) ? stops : []).some(stop => getTripStopCode(stop) === value);
}

function getOptionPointCode(option) {
    return String(option?.dataset?.maDiem || option?.value || "").trim();
}

function updateTripStopSelectAvailability(stops, pickupSelectId, dropoffSelectId) {
    const usedCodes = new Set((Array.isArray(stops) ? stops : [])
        .map(stop => getTripStopCode(stop))
        .filter(Boolean));

    [pickupSelectId, dropoffSelectId].forEach(selectId => {
        const selectEl = document.getElementById(selectId);

        if (!selectEl) return;

        Array.from(selectEl.options).forEach(option => {
            const code = getOptionPointCode(option);

            if (!code) {
                option.disabled = false;
                return;
            }

            option.disabled = usedCodes.has(code);
        });

        const selectedOption = selectEl.options[selectEl.selectedIndex];

        if (selectedOption?.disabled) {
            selectEl.value = "";
        }
    });
}

function updateEditTripStopSelectAvailability() {
    updateTripStopSelectAvailability(editTripStops, "editTripPickupSelect", "editTripDropoffSelect");
}

function updateAddTripStopSelectAvailability() {
    updateTripStopSelectAvailability(tripStops, "tripPickupSelect", "tripDropoffSelect");
}

function uniqueTripStops(stops) {
    const seen = new Set();

    return (Array.isArray(stops) ? stops : []).filter(stop => {
        const code = getTripStopCode(stop);
        const key = code || `${normalizeStopType(stop.type || stop.loai)}:${normalizeTextValue(stop.tenDiem || stop.name)}`;

        if (!key || seen.has(key)) {
            return false;
        }

        seen.add(key);
        return true;
    });
}

function normalizeTextValue(value) {
    return String(value || "").trim().toLowerCase();
}

function isTripStopInstanceCode(code) {
    return /^dd/i.test(String(code || "").trim());
}

function resolveTripStopCodeFromSelect(stop, selectEl) {
    const directCode = getTripStopCode(stop);

    if (directCode && !isTripStopInstanceCode(directCode)) {
        return directCode;
    }

    if (!selectEl) return directCode;

    const stopName = normalizeTextValue(stop.tenDiem || stop.name);
    const matchedOption = Array.from(selectEl.options).find(option => {
        const optionName = normalizeTextValue(option.dataset?.name || option.textContent || option.value);

        return stopName && (optionName === stopName || optionName.includes(stopName) || stopName.includes(optionName));
    });

    const optionCode = matchedOption?.dataset?.maDiem || matchedOption?.value || "";

    return String(optionCode || directCode).trim();
}

function normalizeEditTripStopCodesFromSelects() {
    const pickupSelect = document.getElementById("editTripPickupSelect");
    const dropoffSelect = document.getElementById("editTripDropoffSelect");

    editTripStops = editTripStops.map(stop => {
        const type = normalizeStopType(stop.type || stop.loai);
        const selectEl = type === "dropoff" ? dropoffSelect : pickupSelect;
        const code = resolveTripStopCodeFromSelect(stop, selectEl);

        return {
            ...stop,
            type,
            stationId: code,
            maDiemBen: code
        };
    });
}

function getComparableTripStops(stops) {
    return mapTripStopsForRequest(stops)
        .map(stop => ({
            maDiemBen: stop.maDiemBen,
            type: stop.type,
            order: stop.order
        }));
}

function areTripStopsEqual(left, right) {
    return JSON.stringify(left || []) === JSON.stringify(right || []);
}

function tripHasBookings(tripId) {
    return Array.isArray(bookings) && bookings.some(item => String(item.tripId) === String(tripId));
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

    const selectedOption = selectEl.options[selectEl.selectedIndex];
    const dataMaDiem = selectedOption?.dataset?.maDiem;
    const stationId = dataMaDiem && String(dataMaDiem).trim() ? String(dataMaDiem).trim() : resolveStationId(selectEl.value);

    if (!stationId) {
        alert(type === "dropoff" ? "Vui lòng chọn một điểm trả để thêm." : "Vui lòng chọn một điểm đón để thêm.");
        return;
    }

    tripStops = uniqueTripStops(tripStops);

    const existed = hasTripStop(tripStops, stationId);

    if (existed) {
        alert("Điểm này đã được thêm.");
        return;
    }

    const tenDiem = selectedOption?.dataset?.name || String(selectedOption?.textContent || "").trim() || "Diem don tra";

    tripStops.push({
        stationId,
        maDiemBen: stationId,
        tenDiem,
        type
    });

    renderTripStops();
}

function addEditTripStop(type) {
    const selectId = type === "dropoff" ? "editTripDropoffSelect" : "editTripPickupSelect";
    const selectEl = document.getElementById(selectId);

    if (!selectEl) return;

    const selectedOption = selectEl.options[selectEl.selectedIndex];
    const dataMaDiem = selectedOption?.dataset?.maDiem;
    const stationId = dataMaDiem && String(dataMaDiem).trim() ? String(dataMaDiem).trim() : resolveStationId(selectEl.value);

    if (!stationId) {
        alert(type === "dropoff" ? "Vui lòng chọn một điểm trả để thêm." : "Vui lòng chọn một điểm đón để thêm.");
        return;
    }

    normalizeEditTripStopCodesFromSelects();
    editTripStops = uniqueTripStops(editTripStops);

    const existed = hasTripStop(editTripStops, stationId);

    if (existed) {
        alert("Điểm này đã được thêm.");
        return;
    }

    const tenDiem = selectedOption?.dataset?.name || String(selectedOption?.textContent || "").trim() || "Diem don tra";

    editTripStops.push({
        stationId,
        maDiemBen: stationId,
        tenDiem,
        type
    });

    renderEditTripStops();
}

function renderTripStops() {
    tripStops = uniqueTripStops(tripStops);

    const pickupList = document.getElementById("tripPickupStopsList");
    const dropoffList = document.getElementById("tripDropoffStopsList");

    if (pickupList) {
        const pickupStops = tripStops.filter(stop => stop.type === "pickup");

        pickupList.innerHTML = pickupStops.length ? pickupStops.map((stop, index) => {
            const name = getTripStopDisplayName(stop, "tripPickupSelect");

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
            const name = getTripStopDisplayName(stop, "tripDropoffSelect");

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
    editTripStops = uniqueTripStops(editTripStops);

    const pickupList = document.getElementById("editTripPickupStopsList");
    const dropoffList = document.getElementById("editTripDropoffStopsList");

    if (pickupList) {
        const pickupStops = editTripStops.filter(stop => stop.type === "pickup");

        pickupList.innerHTML = pickupStops.length ? pickupStops.map((stop, index) => {
            const name = getTripStopDisplayName(stop, "editTripPickupSelect");

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
            const name = getTripStopDisplayName(stop, "editTripDropoffSelect");

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
        getTripStopCode(stop) === getTripStopCode(targetStop)
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
        getTripStopCode(stop) === getTripStopCode(targetStop)
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

    (async () => {
        const benDep = resolveStationId(document.getElementById("editTripDepartureSelect")?.value);
        const benArr = resolveStationId(document.getElementById("editTripArrivalSelect")?.value);

        if (benDep) {
            const p = await loadDiemDonTraForBen(benDep, "don");
            renderPointOptions("editTripPickupSelect", p, "Chọn điểm đón");
        } else {
            try { renderStationOptions("editTripPickupSelect"); } catch (e) {}
        }

        if (benArr) {
            const p2 = await loadDiemDonTraForBen(benArr, "tra");
            renderPointOptions("editTripDropoffSelect", p2, "Chọn điểm trả");
        } else {
            try { renderStationOptions("editTripDropoffSelect"); } catch (e) {}
        }

        try {
            const pickupSelect = document.getElementById("editTripPickupSelect");
            const dropoffSelect = document.getElementById("editTripDropoffSelect");

            const pickupStops = editTripStops.filter(s => s.type === "pickup");
            const dropoffStops = editTripStops.filter(s => s.type === "dropoff");

            if (pickupSelect && pickupStops.length) {
                const existing = Array.from(pickupSelect.options).map(o => String(o.value).trim());
                pickupStops.forEach(stop => {
                    const val = getTripStopCode(stop);
                    if (!val) return;
                    if (!existing.includes(val)) {
                        const opt = document.createElement("option");
                        opt.value = val;
                        opt.dataset.maDiem = val;
                        opt.text = stop.name || stop.tenDiem || "Diem don tra";
                        pickupSelect.appendChild(opt);
                    }
                });
            }

            if (dropoffSelect && dropoffStops.length) {
                const existing2 = Array.from(dropoffSelect.options).map(o => String(o.value).trim());
                dropoffStops.forEach(stop => {
                    const val = getTripStopCode(stop);
                    if (!val) return;
                    if (!existing2.includes(val)) {
                        const opt = document.createElement("option");
                        opt.value = val;
                        opt.dataset.maDiem = val;
                        opt.text = stop.name || stop.tenDiem || "Diem don tra";
                        dropoffSelect.appendChild(opt);
                    }
                });
            }

            normalizeEditTripStopCodesFromSelects();
            editTripStops = uniqueTripStops(editTripStops);
            originalEditTripStops = getComparableTripStops(editTripStops);
            renderEditTripStops();
        } catch (e) {
            console.warn("Không thể đảm bảo các option cho edit modal:", e);
        }
    })();

    const modalEl = document.getElementById("editTripModal");

    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

function openTripStatusModal(tripId) {
    const trip = trips.find(item => item.id === tripId);

    if (!trip) return;

    editingTripStatusId = tripId;

    const modalEl = document.getElementById("tripStatusModal");
    const tripInfoEl = document.getElementById("tripStatusTripInfo");
    const currentStatusEl = document.getElementById("tripCurrentStatus");
    const statusSelect = document.getElementById("tripStatusSelect");

    if (tripInfoEl) {
        tripInfoEl.textContent = `${trip.id} - ${trip.route || "Chưa có tuyến"}`;
    }

    if (currentStatusEl) {
        currentStatusEl.textContent = trip.status || "Không rõ";
    }

    if (statusSelect) {
        statusSelect.value = trip.status || "";
    }

    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

async function saveTripStatus() {
    const tripIndex = trips.findIndex(item => item.id === editingTripStatusId);

    if (tripIndex < 0) return;

    const trangThai = document.getElementById("tripStatusSelect")?.value || "";

    if (!trangThai) {
        alert("Vui lòng chọn trạng thái.");
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${editingTripStatusId}/status`, {
            method: "PUT",
            headers: Object.assign({ "Content-Type": "application/json" }, getAuthHeaders()),
            body: JSON.stringify({ trangThai })
        });

        const result = await response.json();

        if (!response.ok) {
            alert(result.message || "Cập nhật trạng thái chuyến xe thất bại.");
            return;
        }

        const updatedTrip = mapStaffTripResponseToTrip(result);
        trips[tripIndex] = updatedTrip;

        editingTripStatusId = null;

        const modalEl = document.getElementById("tripStatusModal");
        if (modalEl) {
            bootstrap.Modal.getInstance(modalEl)?.hide();
        }

        renderTrips();
        renderRecentTrips();

        await refreshSeatSection();

        renderReport();

        alert("Đã cập nhật trạng thái chuyến xe.");
    } catch (error) {
        console.error("Lỗi cập nhật trạng thái chuyến xe:", error);
        alert("Không thể kết nối server.");
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

    const khoangCach = Number(document.getElementById("editTripDistance").value) || 0;
    const thoiGianDuKien = Number(document.getElementById("editTripEstimatedTime").value) || 0;
    const stops = mapTripStopsForRequest(editTripStops);
    const comparableStops = getComparableTripStops(editTripStops);
    const stopsChanged = !areTripStopsEqual(comparableStops, originalEditTripStops);

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

    if (!stops.some(stop => stop.type === "pickup")) {
        alert("Vui lÃ²ng chá»n Ã­t nháº¥t má»™t Ä‘iá»ƒm Ä‘Ã³n.");
        return;
    }

    if (!stops.some(stop => stop.type === "dropoff")) {
        alert("Vui lÃ²ng chá»n Ã­t nháº¥t má»™t Ä‘iá»ƒm tráº£.");
        return;
    }

    const payload = {
        maXe: busId,
        maBenDi: departureId,
        maBenDen: arrivalId,
        ngayDi: document.getElementById("editTripDate").value,
        gioDi: document.getElementById("editTripTime").value,
        giaVe: Number(document.getElementById("editTripPrice").value),
        khoangCach,
        thoiGianDuKien,
        stops
    };

    (async () => {
        let res;

        try {
            res = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${editingTripId}`, {
                method: "PUT",
                headers: Object.assign({ "Content-Type": "application/json" }, getAuthHeaders()),
                body: JSON.stringify(payload)
            });
        } catch (error) {
            console.error("Network error when updating trip:", error);
            alert("Khong the ket noi server.");
            return;
        }

        const result = await readApiBody(res);

            if (!res.ok) {
                alert(result.message || "Cập nhật chuyến xe thất bại.");
                return;
            }

        try {
            const updated = mapStaffTripResponseToTrip(result);

            const idx = trips.findIndex(t => t.id === updated.id);
            if (idx >= 0) {
                trips[idx] = updated;
            } else {
                trips.unshift(updated);
            }

            editingTripId = null;
            editTripStops = [];

            const modalEl = document.getElementById("editTripModal");
            if (modalEl) bootstrap.Modal.getInstance(modalEl)?.hide();

            renderBusOptions();
            renderTrips();
            renderRecentTrips();

            await refreshSeatSection();

            renderReport();

            alert("Đã cập nhật chuyến xe.");
        } catch (error) {
            console.error("Lỗi cập nhật chuyến xe:", error);
            alert("Không thể kết nối server.");
        }
    })();
}

function saveTripChanges() {
    const tripIndex = trips.findIndex(item => item.id === editingTripId);

    if (tripIndex < 0) return;

    const busId = document.getElementById("editTripBusSelect").value;
    const departureId = resolveStationId(document.getElementById("editTripDepartureSelect").value);
    const arrivalId = resolveStationId(document.getElementById("editTripArrivalSelect").value);
    const khoangCach = Number(document.getElementById("editTripDistance").value) || 0;
    const thoiGianDuKien = Number(document.getElementById("editTripEstimatedTime").value) || 0;

    normalizeEditTripStopCodesFromSelects();
    editTripStops = uniqueTripStops(editTripStops);

    const stops = mapTripStopsForRequest(editTripStops);
    const comparableStops = getComparableTripStops(editTripStops);
    const stopsChanged = !areTripStopsEqual(comparableStops, originalEditTripStops);

    if (!busId) {
        alert("Vui long chon xe.");
        return;
    }

    if (!departureId) {
        alert("Vui long chon ben di.");
        return;
    }

    if (!arrivalId) {
        alert("Vui long chon ben den.");
        return;
    }

    if (departureId === arrivalId) {
        alert("Ben di va ben den khong duoc trung nhau.");
        return;
    }

    if (khoangCach <= 0) {
        alert("Khoang cach phai lon hon 0.");
        return;
    }

    if (thoiGianDuKien <= 0) {
        alert("Thoi gian du kien phai lon hon 0.");
        return;
    }

    if (!stops.some(stop => stop.type === "pickup")) {
        alert("Vui long chon it nhat mot diem don.");
        return;
    }

    if (!stops.some(stop => stop.type === "dropoff")) {
        alert("Vui long chon it nhat mot diem tra.");
        return;
    }

    if (stops.some(stop => isTripStopInstanceCode(stop.maDiemBen))) {
        alert("Danh sach diem don tra chua tai xong. Vui long mo lai man hinh sua chuyen va thu lai.");
        return;
    }

    if (stopsChanged && tripHasBookings(editingTripId)) {
        alert("Chuyen xe da co ve, khong the doi diem don tra. Ban van co the sua xe, ngay gio, gia ve, khoang cach.");
        return;
    }

    const payload = {
        maXe: busId,
        maBenDi: departureId,
        maBenDen: arrivalId,
        ngayDi: document.getElementById("editTripDate").value,
        gioDi: document.getElementById("editTripTime").value,
        giaVe: Number(document.getElementById("editTripPrice").value),
        khoangCach: Math.round(khoangCach),
        thoiGianDuKien: Math.round(thoiGianDuKien)
    };

    if (stopsChanged) {
        payload.stops = stops;
    }

    (async () => {
        let response;

        try {
            response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${editingTripId}`, {
                method: "PUT",
                headers: Object.assign({ "Content-Type": "application/json" }, getAuthHeaders()),
                body: JSON.stringify(payload)
            });
        } catch (error) {
            console.error("Network error when updating trip:", error);
            alert("Khong the ket noi server.");
            return;
        }

        const result = await readApiBody(response);

        if (!response.ok) {
            console.error("Update trip failed:", result);
            alert(getApiErrorMessage(result, "Cap nhat chuyen xe that bai."));
            return;
        }

        const updated = mapStaffTripResponseToTrip(result);
        const idx = trips.findIndex(t => t.id === updated.id);

        if (idx >= 0) {
            trips[idx] = updated;
        } else {
            trips.unshift(updated);
        }

        editingTripId = null;
        editTripStops = [];

        const modalEl = document.getElementById("editTripModal");
        if (modalEl) bootstrap.Modal.getInstance(modalEl)?.hide();

        try {
            renderBusOptions();
            renderTrips();
            renderRecentTrips();
            await refreshSeatSection();
            renderReport();
        } catch (error) {
            console.warn("Trip was updated, but UI refresh failed:", error);
        }

        alert("Da cap nhat chuyen xe.");
    })();
}

function deleteTrip(tripId) {
    const trip = trips.find(item => item.id === tripId);

    if (!trip) return;

    const accepted = confirm(`Bạn có chắc muốn xóa chuyến ${tripId} không?`);

    if (!accepted) return;

    (async () => {
        try {
            const res = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${tripId}`, {
                method: "DELETE",
                headers: getAuthHeaders()
            });

            if (!res.ok) {
                let body = {};
                try { body = await res.json(); } catch (e) {}
                alert(body.message || "Xoá chuyến thất bại.");
                return;
            }

            trips = trips.filter(item => item.id !== tripId);

            if (editingTripId === tripId) {
                editingTripId = null;
                editTripStops = [];
            }

            renderBusOptions();
            renderTrips();
            renderRecentTrips();

            await refreshSeatSection();

            renderReport();

            alert("Đã xóa chuyến xe.");
        } catch (error) {
            console.error("Lỗi xoá chuyến:", error);
            alert("Không thể kết nối server.");
        }
    })();
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
                        <button type="button" class="btn btn-sm btn-outline-warning" onclick="openTripStatusModal('${trip.id}')">
                            <i class="fa-solid fa-rotate"></i> Sửa trạng thái
                        </button>
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
            await refreshSeatSection();
            return;
        }

        trips = Array.isArray(result)
            ? result.map(item => mapStaffTripResponseToTrip(item))
            : [];

        renderBusOptions();
        renderTrips();
        renderRecentTrips();

        await refreshSeatSection();

        renderReport();
    } catch (error) {
        console.error("Lỗi gọi API chuyến xe:", error);
        trips = [];
        renderTrips();
        await refreshSeatSection();
    }
}

async function updateTripStatus(tripId, newStatus) {
    const accepted = confirm(`Bạn có chắc muốn đổi trạng thái chuyến ${tripId} thành "${newStatus}" không?`);

    if (!accepted) return;

    try {
        const response = await fetch(`${API_BASE_URL}/api/staff/chuyen-xe/${tripId}/status`, {
            method: "PUT",
            headers: Object.assign({ "Content-Type": "application/json" }, getAuthHeaders()),
            body: JSON.stringify({
                trangThai: newStatus
            })
        });

        const result = await response.json();

        if (!response.ok) {
            alert(result.message || "Cập nhật trạng thái chuyến xe thất bại.");
            return;
        }

        const updatedTrip = mapStaffTripResponseToTrip(result);

        const index = trips.findIndex(item => item.id === tripId);

        if (index >= 0) {
            trips[index] = updatedTrip;
        }

        renderTrips();
        renderRecentTrips();

        await refreshSeatSection();

        renderReport();

        alert("Đã cập nhật trạng thái chuyến xe.");
    } catch (error) {
        console.error("Lỗi cập nhật trạng thái chuyến:", error);
        alert("Không thể kết nối server.");
    }
}

