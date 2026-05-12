const API_BASE_URL = "http://localhost:8080";

function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

async function syncPayment(orderCode) {
  const response = await fetch(`${API_BASE_URL}/api/payment/payos/sync/${encodeURIComponent(orderCode)}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" }
  });

  const text = await response.text();

  if (!response.ok) {
    throw new Error(text || "Không đồng bộ được trạng thái thanh toán.");
  }

  return text;
}

document.addEventListener("DOMContentLoaded", async function () {
  const orderCode = getParam("orderCode") || "--";
  const maDatVe = getParam("maDatVe") || sessionStorage.getItem("maDatVe") || localStorage.getItem("maDatVe") || "--";

  document.getElementById("orderCode").textContent = orderCode;
  document.getElementById("maDatVe").textContent = maDatVe;

  const paymentStatusEl = document.getElementById("paymentStatus");
  const syncNoteEl = document.getElementById("syncNote");

  if (!orderCode || orderCode === "--") {
    paymentStatusEl.textContent = "Thiếu orderCode";
    syncNoteEl.textContent = "Không thể đồng bộ vì thiếu mã giao dịch trên URL.";
    return;
  }

  try {
    const status = await syncPayment(orderCode);
    paymentStatusEl.textContent = status || "Đã đồng bộ";
    syncNoteEl.textContent = "Trạng thái đã được cập nhật từ backend.";
  } catch (error) {
    paymentStatusEl.textContent = "Đã thanh toán";
    syncNoteEl.textContent = error.message || "Đồng bộ thất bại, bạn vẫn có thể kiểm tra lại ở trang vé.";
  }
});