import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "@/auth/AuthContext";
import { ProtectedRoute } from "@/routes/ProtectedRoute";
import { AppShell } from "@/components/shell/AppShell";
import { LoginPage } from "@/pages/LoginPage";
import { ResetPasswordPage } from "@/pages/ResetPasswordPage";
import { DashboardPage } from "@/pages/DashboardPage";
import { NotificationsPage } from "@/pages/NotificationsPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { PlaceholderPage } from "@/pages/PlaceholderPage";

// Borrower (8a)
import { BorrowerLoansPage } from "@/pages/borrower/BorrowerLoansPage";
import { BorrowerSanctionPage } from "@/pages/borrower/BorrowerSanctionPage";
import { BorrowerReceiptsPage } from "@/pages/borrower/BorrowerReceiptsPage";

// Field Officer (8b)
import { FieldOfficerBorrowersPage } from "@/pages/field-officer/FieldOfficerBorrowersPage";
import { FieldOfficerCentresPage } from "@/pages/field-officer/FieldOfficerCentresPage";
import { FieldOfficerApplicationsPage } from "@/pages/field-officer/FieldOfficerApplicationsPage";
import { FieldOfficerCollectionsPage } from "@/pages/field-officer/FieldOfficerCollectionsPage";

// Credit Officer (8c)
import { CreditOfficerQueuePage } from "@/pages/credit-officer/CreditOfficerQueuePage";
import { CreditOfficerWaitlistPage } from "@/pages/credit-officer/CreditOfficerWaitlistPage";
import { CreditOfficerKycPage } from "@/pages/credit-officer/CreditOfficerKycPage";

// Branch Manager (8d)
import { BranchManagerBorrowersPage } from "@/pages/branch-manager/BranchManagerBorrowersPage";
import { BranchManagerDelinquencyPage } from "@/pages/branch-manager/BranchManagerDelinquencyPage";
import { BranchManagerDisputesPage } from "@/pages/branch-manager/BranchManagerDisputesPage";
import { BranchManagerOfficersPage } from "@/pages/branch-manager/BranchManagerOfficersPage";

// Collections Officer (8e)
import { CollectionsOfficerCasesPage } from "@/pages/collections-officer/CollectionsOfficerCasesPage";
import { CollectionsOfficerAgingPage } from "@/pages/collections-officer/CollectionsOfficerAgingPage";

// NBFC Admin (8f)
import { AdminProductsPage } from "@/pages/admin/AdminProductsPage";
import { AdminUsersPage } from "@/pages/admin/AdminUsersPage";
import { AdminAuditPage } from "@/pages/admin/AdminAuditPage";
import { AdminDelinquencyPage } from "@/pages/admin/AdminDelinquencyPage";
import { AdminAnalyticsPage } from "@/pages/admin/AdminAnalyticsPage";

export default function App() {
    return (
      <AuthProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />}/>
            <Route path="/reset-password" element={<ResetPasswordPage />}/>

            <Route element={<ProtectedRoute />}>
              <Route element={<AppShell />}>
                {/* Role-dispatching home */}
                <Route path="/dashboard" element={<DashboardPage />}/>
                <Route path="/notifications" element={<NotificationsPage />}/>
                <Route path="/profile" element={<ProfilePage />}/>

                {/* Borrower */}
                <Route path="/borrower/loans" element={<BorrowerLoansPage />}/>
                <Route path="/borrower/sanctions" element={<BorrowerSanctionPage />}/>
                <Route path="/borrower/receipts" element={<BorrowerReceiptsPage />}/>

                {/* Field Officer */}
                <Route path="/fo/borrowers" element={<FieldOfficerBorrowersPage />}/>
                <Route path="/fo/centres" element={<FieldOfficerCentresPage />}/>
                <Route path="/fo/applications" element={<FieldOfficerApplicationsPage />}/>
                <Route path="/fo/collections" element={<FieldOfficerCollectionsPage />}/>

                {/* Credit Officer */}
                <Route path="/co/applications" element={<CreditOfficerQueuePage />}/>
                <Route path="/co/waitlist" element={<CreditOfficerWaitlistPage />}/>
                <Route path="/co/kyc" element={<CreditOfficerKycPage />}/>

                {/* Branch Manager */}
                <Route path="/bm/borrowers" element={<BranchManagerBorrowersPage />}/>
                <Route path="/bm/delinquency" element={<BranchManagerDelinquencyPage />}/>
                <Route path="/bm/disputes" element={<BranchManagerDisputesPage />}/>
                <Route path="/bm/officers" element={<BranchManagerOfficersPage />}/>

                {/* Collections Officer */}
                <Route path="/co-officer/cases" element={<CollectionsOfficerCasesPage />}/>
                <Route path="/co-officer/aging" element={<CollectionsOfficerAgingPage />}/>

                {/* NBFC Admin */}
                <Route path="/admin/products" element={<AdminProductsPage />}/>
                <Route path="/admin/users" element={<AdminUsersPage />}/>
                <Route path="/admin/audit" element={<AdminAuditPage />}/>
                <Route path="/admin/delinquency" element={<AdminDelinquencyPage />}/>
                <Route path="/admin/analytics" element={<AdminAnalyticsPage />}/>

                <Route path="*" element={<PlaceholderPage />}/>
              </Route>
            </Route>

            <Route path="/" element={<Navigate to="/dashboard" replace/>}/>
          </Routes>
        </BrowserRouter>
      </AuthProvider>
    );
}