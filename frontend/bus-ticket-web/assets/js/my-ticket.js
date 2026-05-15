const API_BASE_URL = "http://localhost:8080";

function buildAuthHeaders() {
	const headers = { "Content-Type": "application/json" };
	const token = localStorage.getItem("token") || sessionStorage.getItem("token");
	if (token) headers.Authorization = `Bearer ${token}`;
	return headers;
}

function isLoggedIn() {
	const token = localStorage.getItem("token") || sessionStorage.getItem("token");
	return Boolean(token && String(token).trim());
}

function extractArray(result) {
	if (Array.isArray(result)) return result;
	if (result && Array.isArray(result.data)) return result.data;
	if (result && Array.isArray(result.result)) return result.result;
	if (result && Array.isArray(result.content)) return result.content;
	return [];
}

function formatDateTime(value) {
	if (!value) return "";
	try {
		const d = new Date(value);
		return d.toLocaleString("vi-VN", { dateStyle: "short", timeStyle: "short" });
	} catch (e) {
		return String(value);
	}
}

function money(value) {
	const number = Number(value || 0);
	return new Intl.NumberFormat("vi-VN").format(number) + "đ";
}

async function resolveCurrentCustomerId() {
	const cachedMaKhachHang =
		localStorage.getItem("maKhachHang") ||
		localStorage.getItem("maKH") ||
		localStorage.getItem("maKh") ||
		sessionStorage.getItem("maKhachHang") ||
		sessionStorage.getItem("maKH") ||
		sessionStorage.getItem("maKh");

	if (cachedMaKhachHang) {
		return cachedMaKhachHang;
	}

	try {
		const profile = await getCurrentAccountProfile();
		if (!profile) return "";
		const src = profile.data || profile || {};
		const maKhachHang = src.maKhachHang || src.maKH || src.maKh || src.khachHang?.maKhachHang || src.khachHang?.maKH || src.khachHang?.maKh || "";
		if (maKhachHang) {
			localStorage.setItem("maKhachHang", maKhachHang);
			localStorage.setItem("maKH", maKhachHang);
			sessionStorage.setItem("maKhachHang", maKhachHang);
			sessionStorage.setItem("maKH", maKhachHang);
		}
		return maKhachHang;
	} catch (err) {
		console.warn("Lỗi khi lấy mã khách hàng:", err);
		return "";
	}
}

function isTicketCompleted(arrivalTime) {
	if (!arrivalTime) return false;
	const now = new Date();
	const arrival = new Date(arrivalTime);
	return now > arrival;
}

async function autoUpdateTicketStatus(ticket) {
	const maVe = ticket.maVe || ticket.id || ticket.code;
	const arrivalTime = ticket.thoiGianDen || ticket.ngayGioDen || ticket.arrivalTime;
	const currentStatus = ticket.trangThaiVe || ticket.status || "";

	if (!maVe || !isTicketCompleted(arrivalTime) || currentStatus === "Đã dùng") {
		return ticket;
	}

	try {
		const res = await fetch(`${API_BASE_URL}/api/ve/${encodeURIComponent(maVe)}/update-status`, {
			method: "POST",
			headers: buildAuthHeaders(),
			body: JSON.stringify({ trangThai: "Đã dùng" })
		});

		if (!res.ok) {
			console.warn(`Không thể cập nhật trạng thái vé ${maVe}`);
			return ticket;
		}

		return await res.json().catch(() => ticket);
	} catch (err) {
		console.warn(`Lỗi khi cập nhật trạng thái vé ${maVe}:`, err);
		return ticket;
	}
}

function renderTickets(list) {
	const box = document.getElementById("ticketList");
	if (!Array.isArray(list) || list.length === 0) {
		box.innerHTML = `<div class="alert alert-info">Bạn chưa tìm thấy vé nào.</div>`;
		return;
	}

	box.innerHTML = list.map(ticket => {
		const title = ticket.tenTuyen || ticket.maChuyen || "Vé";
		const maVe = ticket.maVe || ticket.id || ticket.code || "-";
		const maChuyen = ticket.maChuyen || "-";
		const maNhaXe = ticket.maNhaXe || ticket.nhaXe?.maNhaXe || "-";
		const company = ticket.tenNhaXe || ticket.nhaXe?.tenNhaXe || ticket.maNhaXe || ticket.company || "-";
		const gioDi = formatDateTime(ticket.thoiGianKhoiHanh || ticket.ngayGioDi || ticket.departureTime);
		const gioDen = formatDateTime(ticket.thoiGianDen || ticket.ngayGioDen || ticket.arrivalTime);
		const timeRange = (gioDi || gioDen) ? `${gioDi || "-"} - ${gioDen || "-"}` : "-";
		const seats = (ticket.soGhe || ticket.seats || "").toString();
		const passenger = (ticket.tenKhachHang || ticket.hoTen || ticket.name) ? `${ticket.tenKhachHang || ticket.hoTen || ticket.name}` : "-";
		const phone = ticket.sdt || ticket.phone || "-";
		const price = ticket.giaTien ? money(ticket.giaTien) : (ticket.total ? money(ticket.total) : "-");
		const status = ticket.trangThaiVe || ticket.status || "-";
		
		const isCompleted = isTicketCompleted(ticket.thoiGianDen || ticket.ngayGioDen || ticket.arrivalTime);
		const ratingSection = isCompleted ? `
			<div class="rating-section mt-3 pt-3 border-top">
				<button class="btn btn-warning btn-sm" data-bs-toggle="collapse" data-bs-target="#rating-${maVe}">
					<i class="fa-solid fa-star"></i> Đánh giá chuyến đi
				</button>
				<div class="collapse mt-2" id="rating-${maVe}">
					<div class="rating-form p-3 bg-light rounded">
						<div class="mb-3">
							<label class="form-label">Số sao (1-5):</label>
							<div class="star-rating">
								${[5, 4, 3, 2, 1].map(star => `
									<input type="radio" name="star-${maVe}" value="${star}" id="star-${maVe}-${star}" style="display: none;">
									<label for="star-${maVe}-${star}" class="star-label"><i class="fa-solid fa-star"></i></label>
								`).join('')}
							</div>
						</div>
						<div class="mb-3">
							<label class="form-label">Nội dung đánh giá:</label>
							<textarea class="form-control" id="content-${maVe}" rows="3" placeholder="Chia sẻ trải nghiệm của bạn..."></textarea>
						</div>
						<button class="btn btn-primary btn-sm" onclick="submitRating('${maVe}', '${maChuyen}', '${maNhaXe}')"><i class="fa-solid fa-paper-plane"></i> Gửi đánh giá</button>
					</div>
				</div>
			</div>
		` : '';

		return `
			<div class="ticket-card ticket-item mb-3">
				<div class="row align-items-center g-3">
					<div class="col-md-8">
						<h5 class="fw-bold">${title}</h5>
						<p class="mb-1"><strong>Mã vé:</strong> ${maVe}</p>
						<p class="mb-1"><strong>Tên nhà xe:</strong> ${company}</p>
						<p class="mb-1"><strong>Giờ đi - giờ đến:</strong> ${timeRange}</p>
						<p class="mb-1"><strong>Ghế:</strong> ${seats}</p>
						<p class="mb-1"><strong>Hành khách:</strong> ${passenger} - ${phone}</p>
					</div>
					<div class="col-md-4 text-md-end">
						<div class="trip-price mb-2">${price}</div>
						<span class="badge bg-success">${status}</span>
						<div class="mt-3"><button class="btn btn-outline-primary btn-sm" onclick="window.print()"><i class="fa-solid fa-print"></i> In vé</button></div>
					</div>
				</div>
				${ratingSection}
			</div>`;
	}).join("");
}

async function submitRating(maVe, maChuyen, maNhaXe) {
	const star = document.querySelector(`input[name="star-${maVe}"]:checked`)?.value;
	const content = document.getElementById(`content-${maVe}`)?.value || "";

	if (!star) {
		alert("Vui lòng chọn số sao!");
		return;
	}

	if (!content.trim()) {
		alert("Vui lòng nhập nội dung đánh giá!");
		return;
	}

	const starNum = parseInt(star);
	if (isNaN(starNum) || starNum < 1 || starNum > 5) {
		alert("Số sao phải từ 1 đến 5!");
		return;
	}

	const maKhachHang = localStorage.getItem("maKhachHang") || sessionStorage.getItem("maKhachHang") || "";

	const ratingData = {
		maVe: maVe,
		maChuyen: maChuyen,
		maNhaXe: maNhaXe,
		soSao: starNum,
		noiDung: content.trim(),
		maKhachHang: maKhachHang
	};

	try {
		const res = await fetch(`${API_BASE_URL}/api/danh-gia`, {
			method: "POST",
			headers: buildAuthHeaders(),
			body: JSON.stringify(ratingData)
		});

		if (res.ok) {
			alert("Cảm ơn bạn! Đánh giá của bạn đã được ghi nhận.");
			
			// Reset form
			const starRadios = document.querySelectorAll(`input[name="star-${maVe}"]`);
			starRadios.forEach(radio => radio.checked = false);
			document.getElementById(`content-${maVe}`).value = "";
			
			// Collapse form sau khi thành công
			const collapseEl = document.getElementById(`rating-${maVe}`);
			if (collapseEl) {
				collapseEl.classList.remove('show');
			}
		} else {
			const error = await res.json().catch(() => ({}));
			alert(`Lỗi: ${error.message || 'Không thể gửi đánh giá. Vui lòng thử lại.'}`);
		}
	} catch (err) {
		console.error("Lỗi khi gửi đánh giá:", err);
		alert("Lỗi khi gửi đánh giá. Vui lòng thử lại sau.");
	}
}

document.addEventListener("DOMContentLoaded", async function () {
	const box = document.getElementById("ticketList");

	if (isLoggedIn()) {
		const maKhachHang = await resolveCurrentCustomerId();
		if (!maKhachHang) {
			box.innerHTML = `<div class="alert alert-warning">Không tìm thấy thông tin khách hàng. Vui lòng đăng nhập lại.</div>`;
			return;
		}

		try {
			const res = await fetch(`${API_BASE_URL}/api/ve/khach-hang/${encodeURIComponent(maKhachHang)}`, {
				method: "GET",
				headers: buildAuthHeaders()
			});
			const data = await res.json().catch(() => null);

			if (!res.ok || data?.success === false) {
				box.innerHTML = `<div class="alert alert-danger">${data?.message || "Không thể lấy danh sách vé."}</div>`;
				return;
			}

			const list = extractArray(data);
			const updatedList = await Promise.all(list.map(autoUpdateTicketStatus));
			renderTickets(updatedList);
		} catch (err) {
			console.warn("Lỗi khi gọi API vé khách hàng:", err);
			box.innerHTML = `<div class="alert alert-danger">Lỗi khi lấy vé. Vui lòng thử lại sau.</div>`;
		}

		return;
	}

	box.innerHTML = `<div class="alert alert-warning">Vui lòng <a href="login.html" class="alert-link">đăng nhập</a> để xem vé của bạn.</div>`;
});
