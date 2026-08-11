import {
    LayoutDashboard,
    Users,
    Building2,
    FileText,
    HandCoins,
    ShieldCheck,
    ClipboardList,
    Clock,
    AlertTriangle,
    Receipt,
    UserCog,
    Package,
    BarChart3,
    Bell,
    FolderOpen,
    User,
} from "lucide-react";


const NAV_BY_ROLE = {
    BORROWER: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "My Loans", path: "/borrower/loans", icon: HandCoins },
        { label: "Applications & Sanctions", path: "/borrower/sanctions", icon: FileText },
        { label: "Receipts", path: "/borrower/receipts", icon: Receipt },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
    FIELD_OFFICER: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "Borrowers", path: "/fo/borrowers", icon: Users },
        { label: "Centres & Groups", path: "/fo/centres", icon: Building2 },
        { label: "Applications", path: "/fo/applications", icon: ClipboardList },
        { label: "Collections", path: "/fo/collections", icon: HandCoins },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
    CREDIT_OFFICER: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "Application Queue", path: "/co/applications", icon: ClipboardList },
        { label: "Waitlist", path: "/co/waitlist", icon: Clock },
        { label: "KYC Review", path: "/co/kyc", icon: ShieldCheck },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
    BRANCH_MANAGER: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "All Borrowers", path: "/bm/borrowers", icon: Users },
        { label: "Delinquency Cases", path: "/bm/delinquency", icon: AlertTriangle },
        { label: "Receipt Disputes", path: "/bm/disputes", icon: Receipt },
        { label: "Officer Roster", path: "/bm/officers", icon: UserCog },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
    COLLECTIONS_OFFICER: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "My Cases", path: "/co-officer/cases", icon: FolderOpen },
        { label: "DPD Aging", path: "/co-officer/aging", icon: AlertTriangle },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
    NBFC_ADMIN: [
        { label: "Dashboard", path: "/dashboard", icon: LayoutDashboard },
        { label: "Loan Products", path: "/admin/products", icon: Package },
        { label: "Users", path: "/admin/users", icon: UserCog },
        { label: "Audit Log", path: "/admin/audit", icon: FileText },
        { label: "Delinquency", path: "/admin/delinquency", icon: AlertTriangle },
        { label: "Analytics", path: "/admin/analytics", icon: BarChart3 },
        { label: "Notifications", path: "/notifications", icon: Bell },
        { label: "My Profile", path: "/profile", icon: User },
    ],
};

export function navForRole(role) {
    return NAV_BY_ROLE[role] ?? [];
}

export const ROLE_LABELS = {
    BORROWER: "Borrower",
    FIELD_OFFICER: "Field Officer",
    CREDIT_OFFICER: "Credit Officer",
    BRANCH_MANAGER: "Branch Manager",
    COLLECTIONS_OFFICER: "Collections Officer",
    NBFC_ADMIN: "NBFC Admin",
};