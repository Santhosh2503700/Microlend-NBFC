# MicroLend — Microfinance & NBFC Loan Management System

**Domain:** Banking & Financial Services
**Type:** Web-based, REST API platform with a role-based single-page frontend.

MicroLend digitises the full microfinance lending lifecycle for NBFCs and rural lending
cooperatives — borrower onboarding and KYC, joint-liability group formation, loan origination and
credit assessment, sanction and disbursement, repayment collection, delinquency management, and
portfolio analytics — all governed by role-based access control and a complete audit trail.

---

## 1. Technology Stack

| Layer | Technology |
|-------|------------|
| Frontend | React 18 + Vite, React Router, Tailwind CSS (JSX) |
| Backend | Java 21, Spring Boot 3.3.5 (modular monolith), Spring Security, Spring Data JPA |
| Database | MySQL (relational) |
| Auth | Stateless JWT (HS256) with role-based authorities |
| API Docs | OpenAPI / Swagger UI (`springdoc`) |
| Charts | Recharts |

---

## 2. Actors / Roles

| Role | Responsibility |
|------|----------------|
| **Borrower** | Applies for loans, accepts sanction letters, views repayment schedule, approves collection receipts |
| **Field Officer** | Registers borrowers, forms centres & groups, submits applications, records collections |
| **Credit Officer** | Reviews KYC, assesses applications, approves / waitlists / rejects and issues sanctions |
| **Branch Manager** | Oversees branch portfolio, resolves disputes, assigns delinquency cases, views analytics |
| **Collections Officer** | Works assigned overdue cases and recovery actions |
| **NBFC Admin** | Configures loan products, manages users, views audit log and system analytics |

---

## 3. Architecture Overview

```
┌───────────────────────┐        HTTPS / JSON (Bearer JWT)        ┌──────────────────────────────┐
│   React SPA (Vite)     │  ───────────────────────────────────▶  │   Spring Boot Monolith (8090)  │
│   Role-based portal    │   /api/**  (Vite dev proxy → 8090)      │                                │
│   axios + JWT store    │  ◀───────────────────────────────────  │   Controller → Service → Repo  │
└───────────────────────┘                                         │            ↓                   │
                                                                   │        MySQL (JPA)             │
                                                                   └──────────────────────────────┘
```

**Request pipeline (backend):**
`CorrelationIdFilter` → `JwtAuthenticationFilter` (validates JWT, sets role) → `SecurityConfig`
(URL-based role matcher) → **Controller** → **Service** (business rules + ownership checks) →
**Repository** (JPA) → **Entity** (MySQL). Cross-cutting: every state change writes an **audit log**
and, where relevant, a **notification**. Errors are normalised by a global exception handler.

The backend is a **modular monolith** — one package per business capability (`identity`, `borrower`,
`grouporigination`, `loan`, `collection`, `delinquency`, `analytics`, `audit`, `notification`), each
with its own controller / service / repository / entity / DTO layers.

---

## 4. Modules (mapped to requirements)

| # | Module | What it does |
|---|--------|--------------|
| 1 | **Identity & Access Management** | Login, JWT issuance, RBAC, forced first-login password reset, audit logging |
| 2 | **Borrower Onboarding & KYC** | Borrower registration (with portal provisioning), KYC document upload & verification, rule-based credit assessment |
| 3 | **Group & Centre Management** | Meeting centres, joint-liability groups, group summaries |
| 4 | **Loan Origination & Approval** | Applications, automated Green/Amber/Red scoring, approve/waitlist/reject, sanction letters |
| 5 | **Disbursement & Account Management** | Loan account creation on acceptance, EMI amortization, repayment schedule generation |
| 6 | **Repayment Collection & Delinquency** | Collection entry with borrower approval loop, DPD/PAR tracking, case assignment |
| 7 | **Portfolio Analytics & Reporting** | Live PAR, portfolio trend, collection efficiency, officer performance, loan funnel, NPA trend |
| 8 | **Notifications & Alerts** | In-app notifications for KYC, applications, collections, disbursements and delinquency |

---

## 5. Core Lending Workflow (end-to-end)

```
1. Admin        → configures a Loan Product
2. Field Officer→ creates Centre → Group → registers Borrower (auto-creates portal login) → uploads KYC
3. Credit Officer→ verifies KYC
4. Field Officer→ submits Loan Application  →  system auto-runs Credit Assessment (Green/Amber/Red)
5. Credit Officer→ APPROVE  →  Sanction Letter issued (EMI computed)
6. Borrower     → accepts Sanction  →  Loan Account + Repayment Schedule generated (auto-disburse)
7. Field Officer→ records EMI Collection  →  Borrower approves receipt  →  installment marked PAID
8. Nightly scan → flags overdue installments → opens Delinquency Case → Branch Manager assigns officer
9. Analytics    → PAR / NPA / collection dashboards update live
```

---

## 6. Security Model

- **Stateless JWT** — issued on login, sent as `Authorization: Bearer <token>`; no server session.
- **Forced first login** — seeded/new users get a short-lived **RESET-scoped** token that can *only*
  call `reset-password`; a **FULL** role token is issued after the password meets policy
  (≥8 chars, upper, lower, digit, special).
- **RBAC by URL** — enforced centrally in `SecurityConfig` (`/api/admin/**`, `/api/field-officer/**`,
  `/api/credit-officer/**`, `/api/branch-manager/**`, `/api/collections-officer/**`, `/api/borrower/**`).
- **Ownership checks** — each service verifies the caller owns the record (e.g. a Field Officer only
  sees their own borrowers).
- **PII protection** — Aadhaar and bank account numbers are masked (last 4 digits) in responses;
  Aadhaar is validated with the Verhoeff checksum.
- **Audit trail** — every financial/state change is recorded with user, action, module and timestamp.

---

## 7. Data Model (primary entities)

`User`, `AuditLog`, `Borrower`, `BorrowerKYC`, `CreditAssessment`, `Centre`, `CentreMeeting`,
`BorrowerGroup`, `LoanProduct`, `LoanApplication`, `SanctionLetter`, `LoanAccount`,
`RepaymentSchedule`, `CollectionRecord`, `CollectionReceipt`, `DelinquencyCase`, `Notification`,
`PortfolioReport`.

---

## 8. REST API Overview

**Base URL:** `http://localhost:8090`  ·  **Auth:** `Authorization: Bearer <jwt>`  ·  **Docs:** `/swagger-ui.html`

| Area | Base path | Key endpoints |
|------|-----------|---------------|
| Auth (all) | `/api/auth` | `POST /login`, `POST /reset-password`, `GET /me` |
| Profile (all) | `/api/profile` | `GET`, `PUT` |
| Notifications (all) | `/api/notifications` | `GET`, `GET /unread-count`, `PUT /{id}/read` |
| Admin | `/api/admin/loan-products`, `/api/admin/users`, `/api/admin/audit-log`, `/api/admin/delinquency` | product CRUD + `emi-preview`, user CRUD, audit log, `POST /run` |
| Field Officer | `/api/field-officer/**`, `/api/loan-applications` | centres/groups CRUD, borrowers + KYC, collections, submit application |
| Credit Officer | `/api/credit-officer/**` | application queue, `PUT /{id}/decision`, `PUT kyc/{id}/verify` |
| Branch Manager | `/api/branch-manager/**`, `/api/analytics/**` | borrowers, officers, delinquency cases + assign, disputes co-sign, analytics |
| Collections Officer | `/api/collections-officer/cases` | assigned cases |
| Borrower | `/api/borrower/**` | dashboard, loans + schedule, sanction accept/reject, receipts approve/dispute |

> A ready-to-run Postman collection with full token automation is provided in [`/postman`](postman) — see §10.

---

## 9. Running Locally

**Prerequisites:** Java 21, Maven, Node.js 18+, MySQL running on `localhost:3306`.

**1) Backend** (starts on port **8090**, dev profile auto-creates the `microlend` database):
```bash
cd backend
mvn spring-boot:run
```
On first start, `DataSeeder` creates one demo user per role (password `1234`, forced reset on first login):

| Role | Email |
|------|-------|
| NBFC Admin | `admin@microlend.com` |
| Field Officer | `fieldofficer@microlend.com` |
| Credit Officer | `creditofficer@microlend.com` |
| Branch Manager | `branchmanager@microlend.com` |
| Collections Officer | `collectionofficer@microlend.com` |
| Borrower | `borrower@microlend.com` |

**2) Frontend** (starts on port **5173**, proxies `/api` to the backend):
```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`, log in with a demo account, and set a policy-compliant password
----------------------------------------------------------------------------------------------------