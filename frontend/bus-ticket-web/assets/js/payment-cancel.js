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

document.addEventListener("DOMContentLoaded", function () {
  const orderCode = getParam("orderCode") || "--";
  const orderCodeEl = document.getElementById("orderCode");

  orderCodeEl.textContent = orderCode;

  if (!orderCode || orderCode === "--") {
    return;
  }

  syncPayment(orderCode)
    .then((status) => {
      if (status) {
        orderCodeEl.textContent = `${orderCode} (${status})`;
      }
    })
    .catch(() => {
      orderCodeEl.textContent = `${orderCode} (không đồng bộ được)`;
    });
});