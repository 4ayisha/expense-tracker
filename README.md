<<<<<<< HEAD
# 💰 Personal Expense Tracker

A clean, full-stack **Personal Expense Tracker** built as a Java mini project.
Track your daily spending, visualize where your money goes, and stay on top of your monthly budget — all from a single, polished web app.

> Submitted as a college Java mini project. Single-command run, file-based H2 DB, ready to deploy on Render's free tier.

---

## 🛠 Tech Stack

**Backend**
- Java 17
- Spring Boot 3.3 (Web, Data JPA, Validation)
- Maven
- H2 Database (file-based — data persists between restarts)
- REST API with full CRUD + global exception handling
- Jakarta Bean Validation

**Frontend** (served from `src/main/resources/static`)
- Plain HTML / CSS / vanilla JavaScript
- Bootstrap 5 + Bootstrap Icons (CDN)
- Chart.js (CDN)

The whole project deploys as **a single JAR** — frontend and backend together.

---

## ✨ Features

- ➕ **Add expenses** with title, amount, category, date and optional notes
- 📋 **Sortable table** — sort by date or amount, ascending/descending
- ✏️ **Edit** any expense in a modal
- 🗑️ **Delete** with confirmation prompt
- 🔍 **Filter** by category and/or date range
- 📊 **Dashboard**
  - Total spent this month
  - Total spent overall
  - Doughnut chart of spending **by category**
  - Bar chart of spending over the **last 7 days**
- 📭 Friendly **empty state** when there are no expenses
- 📱 **Responsive** layout (mobile + desktop)
- ✅ Server-side **input validation** with proper HTTP status codes

---

## 📂 Project Structure

```
expense-tracker/
├── pom.xml
├── Dockerfile
├── render.yaml
├── README.md
├── .gitignore
└── src/main/
    ├── java/com/example/expensetracker/
    │   ├── ExpenseTrackerApplication.java
    │   ├── model/Expense.java
    │   ├── repository/ExpenseRepository.java
    │   ├── service/ExpenseService.java
    │   ├── controller/ExpenseController.java
    │   ├── controller/GlobalExceptionHandler.java
    │   └── config/CorsConfig.java
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── style.css
            └── script.js
```

---

## 🚀 Run Locally

**Prerequisites:** Java 17+ and Maven (or use the Maven wrapper).

```bash
# clone and enter the project
git clone <your-repo-url>
cd expense-tracker

# run it (Maven wrapper)
./mvnw spring-boot:run

# …or with Maven
mvn spring-boot:run
```

Open: **http://localhost:8080**

H2 web console (handy for demos): **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:file:./data/expensesdb`
- User: `sa`  •  Password: *(empty)*

The database file is created at `./data/expensesdb.mv.db` and persists across restarts.

---

## 🔌 REST API

Base URL: `/api/expenses`

| Method | Endpoint                      | Description                                        |
|--------|-------------------------------|----------------------------------------------------|
| GET    | `/api/expenses`               | List all expenses. Optional query params: `category`, `from`, `to` (ISO date) |
| GET    | `/api/expenses/{id}`          | Get a single expense                               |
| POST   | `/api/expenses`               | Create a new expense                               |
| PUT    | `/api/expenses/{id}`          | Update an existing expense                         |
| DELETE | `/api/expenses/{id}`          | Delete an expense                                  |
| GET    | `/api/expenses/dashboard`     | Aggregated dashboard data (totals + chart series)  |

**Sample expense JSON**
```json
{
  "title": "Lunch with friends",
  "amount": 480.00,
  "category": "Food",
  "date": "2026-05-03",
  "notes": "Cafe XYZ"
}
```

**Allowed categories:** `Food`, `Transport`, `Bills`, `Entertainment`, `Shopping`, `Other`.

**HTTP status codes**
- `200 OK` — success
- `201 Created` — expense created
- `204 No Content` — expense deleted
- `400 Bad Request` — validation failed (response body lists per-field errors)
- `404 Not Found` — expense id does not exist
- `500 Internal Server Error` — unexpected error

---

## 📸 Screenshots

> Drop screenshots here before submitting.

- `docs/screenshot-dashboard.png` — Dashboard cards + charts
- `docs/screenshot-table.png` — Sortable expense table
- `docs/screenshot-add.png` — Add/Edit modal
- `docs/screenshot-mobile.png` — Mobile view

---

## ☁️ Deploy to Render (Free Tier)

This repo includes a `Dockerfile` and a `render.yaml` blueprint.

1. Push the project to a **public GitHub repo**.
2. Go to [https://render.com](https://render.com) → **New** → **Blueprint**.
3. Select your repo. Render reads `render.yaml`, builds with Docker, and deploys.
4. Once deployed, open the public URL Render provides — the app is live.

> Note: Render's free tier has an ephemeral disk, so the H2 file will reset whenever the container restarts (e.g. after long idle). That's fine for a demo. For permanent storage, switch the JDBC URL in `application.properties` to a managed Postgres add-on.

---

## 🧪 Quick Smoke Test

```bash
# Add an expense
curl -X POST http://localhost:8080/api/expenses \
  -H "Content-Type: application/json" \
  -d '{"title":"Coffee","amount":120,"category":"Food","date":"2026-05-03"}'

# List
curl http://localhost:8080/api/expenses

# Dashboard
curl http://localhost:8080/api/expenses/dashboard
```

---

## 📝 Notes for the Reviewer

- The codebase is intentionally small and well-commented for academic review.
- Validation is enforced **on the server** with `@Valid` + Bean Validation annotations.
- A `GlobalExceptionHandler` returns clean JSON errors instead of stack traces.
- Charts and the UI are built without any frontend framework — just Bootstrap + Chart.js — to keep the project approachable.
=======
# expense-tracker
>>>>>>> 80de600d6971326976babe11e8ecb1b825000f0a
