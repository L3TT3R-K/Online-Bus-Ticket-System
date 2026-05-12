function getParam(name) {
  return new URLSearchParams(window.location.search).get(name);
}

document.addEventListener("DOMContentLoaded", function () {
  document.getElementById("orderCode").textContent = getParam("orderCode") || "--";
});