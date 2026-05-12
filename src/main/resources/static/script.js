/* ============================================================
 * Expense Tracker - Frontend logic
 * Talks to the Spring Boot REST API at /api/expenses
 * ============================================================ */

const API = "/api/expenses";
const currentUser = localStorage.getItem("userEmail");

if (!currentUser) {
    window.location.href = "/auth.html";
}
// Local in-memory copy of the current list (for client-side sorting)
let expenses = [];
let sortKey = "date";
let sortDir = "desc"; // "asc" | "desc"

// Chart.js instances (kept so we can destroy/recreate on refresh)
let categoryChart = null;
let dailyChart = null;

// Bootstrap modal handle
let expenseModal = null;

// ===== Helpers =====
const $ = (sel) => document.querySelector(sel);

function formatMoney(n) {
  const num = Number(n || 0);
  return "₹" + num.toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
}

function formatDate(iso) {
  if (!iso) return "";
  const d = new Date(iso + "T00:00:00");
  return d.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "2-digit",
  });
}

function escapeHtml(str) {
  if (str == null) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function showToast(message, type = "success") {
  const id = "t" + Date.now();
  const bg =
    type === "success"
      ? "text-bg-success"
      : type === "error"
      ? "text-bg-danger"
      : "text-bg-secondary";
  const html = `
    <div id="${id}" class="toast align-items-center ${bg} border-0" role="alert">
      <div class="d-flex">
        <div class="toast-body">${escapeHtml(message)}</div>
        <button type="button" class="btn-close btn-close-white me-2 m-auto"
                data-bs-dismiss="toast"></button>
      </div>
    </div>`;
  $("#toastContainer").insertAdjacentHTML("beforeend", html);
  const el = document.getElementById(id);
  const t = new bootstrap.Toast(el, { delay: 2500 });
  t.show();
  el.addEventListener("hidden.bs.toast", () => el.remove());
}

// ===== API calls =====
async function apiFetch(url, options = {}) {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });
  if (!res.ok) {
    let msg = `Request failed (${res.status})`;
    try {
      const body = await res.json();
      if (body.message) msg = body.message;
      if (body.details) {
        msg +=
          " — " +
          Object.entries(body.details)
            .map(([k, v]) => `${k}: ${v}`)
            .join(", ");
      }
    } catch (_) {}
    throw new Error(msg);
  }
  if (res.status === 204) return null;
  return res.json();
}

async function loadExpenses() {
  const params = new URLSearchParams();

  const cat = $("#filterCategory").value;
  const from = $("#filterFrom").value;
  const to = $("#filterTo").value;

  if (cat) params.set("category", cat);
  if (from) params.set("from", from);
  if (to) params.set("to", to);

  params.set("email", currentUser);
  const url = `${API}?${params.toString()}`;
  expenses = await apiFetch(url);

  renderTable();
}

async function loadDashboard() {
  const data = await apiFetch(`${API}/dashboard?email=${currentUser}`);
  $("#statMonth").textContent = formatMoney(data.totalMonth);
  $("#statTotal").textContent = formatMoney(data.totalAll);
  renderCategoryChart(data.byCategory || {});
  renderDailyChart(data.last7Days || []);
}

// ===== Rendering =====
function renderTable() {
  const tbody = $("#expensesBody");
  const empty = $("#emptyState");

  if (!expenses || expenses.length === 0) {
    tbody.innerHTML = "";
    empty.classList.remove("d-none");
    return;
  }
  empty.classList.add("d-none");

  // Sort
  const sorted = [...expenses].sort((a, b) => {
    let av = a[sortKey];
    let bv = b[sortKey];
    if (sortKey === "amount") {
      av = Number(av);
      bv = Number(bv);
    }
    if (av < bv) return sortDir === "asc" ? -1 : 1;
    if (av > bv) return sortDir === "asc" ? 1 : -1;
    return 0;
  });

  tbody.innerHTML = sorted
    .map(
      (e) => `
      <tr>
        <td>${formatDate(e.date)}</td>
        <td class="fw-medium">${escapeHtml(e.title)}</td>
        <td><span class="cat-badge cat-${escapeHtml(
          e.category
        )}">${escapeHtml(e.category)}</span></td>
        <td class="text-end fw-semibold">${formatMoney(e.amount)}</td>
        <td class="text-muted small" style="max-width:240px; white-space:normal;">
          ${escapeHtml(e.notes || "")}
        </td>
        <td class="text-end">
          <button class="btn-icon" title="Edit" data-edit="${e.id}">
            <i class="bi bi-pencil-square"></i>
          </button>
          <button class="btn-icon danger" title="Delete" data-delete="${e.id}">
            <i class="bi bi-trash3"></i>
          </button>
        </td>
      </tr>`
    )
    .join("");

  // Highlight active sort header
  document.querySelectorAll(".sortable").forEach((th) => {
    th.classList.toggle("active", th.dataset.sort === sortKey);
  });
}

const PALETTE = [
  "#4f46e5", "#10b981", "#f59e0b", "#ef4444", "#8b5cf6", "#6b7280",
];

function renderCategoryChart(byCat) {
  const labels = Object.keys(byCat);
  const data = labels.map((k) => Number(byCat[k]));
  if (categoryChart) categoryChart.destroy();
  if (labels.length === 0) return;

  categoryChart = new Chart($("#categoryChart"), {
    type: "doughnut",
    data: {
      labels,
      datasets: [
        {
          data,
          backgroundColor: labels.map((_, i) => PALETTE[i % PALETTE.length]),
          borderWidth: 0,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => `${ctx.label}: ${formatMoney(ctx.parsed)}`,
          },
        },
      },
    },
  });
}

function renderDailyChart(days) {
  const labels = days.map((d) =>
    new Date(d.date + "T00:00:00").toLocaleDateString(undefined, {
      weekday: "short",
    })
  );
  const data = days.map((d) => Number(d.total));
  if (dailyChart) dailyChart.destroy();

  dailyChart = new Chart($("#dailyChart"), {
    type: "bar",
    data: {
      labels,
      datasets: [
        {
          data,
          backgroundColor: "#4f46e5",
          borderRadius: 6,
        },
      ],
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          callbacks: {
            label: (ctx) => formatMoney(ctx.parsed.y),
          },
        },
      },
      scales: {
        x: { grid: { display: false } },
        y: { display: false, beginAtZero: true },
      },
    },
  });
}

// ===== Form / CRUD =====
function openAddModal() {
  $("#modalTitle").textContent = "Add Expense";
  $("#expenseForm").reset();
  $("#expenseForm").classList.remove("was-validated");
  $("#expenseId").value = "";
  $("#date").value = new Date().toISOString().slice(0, 10);
}

function openEditModal(id) {
  const e = expenses.find((x) => x.id === id);
  if (!e) return;
  $("#modalTitle").textContent = "Edit Expense";
  $("#expenseForm").classList.remove("was-validated");
  $("#expenseId").value = e.id;
  $("#title").value = e.title;
  $("#amount").value = e.amount;
  $("#category").value = e.category;
  $("#date").value = e.date;
  $("#notes").value = e.notes || "";
  expenseModal.show();
}

async function handleFormSubmit(ev) {
  ev.preventDefault();
  const form = $("#expenseForm");

  // Extra client-side check: date not in far future (more than 1 year)
  const dateVal = new Date($("#date").value);
  const oneYearAhead = new Date();
  oneYearAhead.setFullYear(oneYearAhead.getFullYear() + 1);
  const dateOk = !isNaN(dateVal) && dateVal <= oneYearAhead;
  if (!dateOk) $("#date").setCustomValidity("Date is too far in the future");
  else $("#date").setCustomValidity("");

  if (!form.checkValidity()) {
    form.classList.add("was-validated");
    return;
  }

  const email = localStorage.getItem("userEmail");
  
  const payload = {
    title: $("#title").value.trim(),
    amount: parseFloat($("#amount").value),
    category: $("#category").value,
    date: $("#date").value,
    notes: $("#notes").value.trim() || null,

    user: {
    email:email
    }
  };

  const id = $("#expenseId").value;
  try {
    if (id) {
      await apiFetch(`${API}/${id}`, {
        method: "PUT",
        body: JSON.stringify(payload),
      });
      showToast("Expense updated");
    } else {
      await apiFetch(API, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      showToast("Expense added");
    }
    expenseModal.hide();
    await Promise.all([loadExpenses(), loadDashboard()]);
  } catch (err) {
    showToast(err.message, "error");
  }
}

async function handleDelete(id) {
  if (!confirm("Delete this expense? This cannot be undone.")) return;
  try {
    await apiFetch(`${API}/${id}`, { method: "DELETE" });
    showToast("Expense deleted");
    await Promise.all([loadExpenses(), loadDashboard()]);
  } catch (err) {
    showToast(err.message, "error");
  }
}

function logout() {

    // clear logged in user
    localStorage.removeItem("userEmail");

    // redirect to login page
    window.location.href = "/auth.html";
}

// ===== Wiring =====
document.addEventListener("DOMContentLoaded", () => {
  expenseModal = new bootstrap.Modal($("#expenseModal"));

  $("#btnAdd").addEventListener("click", openAddModal);
  $("#expenseForm").addEventListener("submit", handleFormSubmit);

  // Filters
  ["filterCategory", "filterFrom", "filterTo"].forEach((id) => {
    $("#" + id).addEventListener("change", () => loadExpenses());
  });
  $("#btnClearFilters").addEventListener("click", () => {
    $("#filterCategory").value = "";
    $("#filterFrom").value = "";
    $("#filterTo").value = "";
    loadExpenses();
  });

  // Sort headers
  document.querySelectorAll(".sortable").forEach((th) => {
    th.addEventListener("click", () => {
      const key = th.dataset.sort;
      if (sortKey === key) {
        sortDir = sortDir === "asc" ? "desc" : "asc";
      } else {
        sortKey = key;
        sortDir = "desc";
      }
      renderTable();
    });
  });

  // Edit / Delete (event delegation)
  $("#expensesBody").addEventListener("click", (ev) => {
    const editBtn = ev.target.closest("[data-edit]");
    const delBtn = ev.target.closest("[data-delete]");
    if (editBtn) openEditModal(Number(editBtn.dataset.edit));
    else if (delBtn) handleDelete(Number(delBtn.dataset.delete));
  });

  // Initial load
  Promise.all([loadExpenses(), loadDashboard()]).catch((err) =>
    showToast(err.message, "error")
  );
});
