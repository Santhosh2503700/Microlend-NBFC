import { useAuth } from "@/auth/useAuth";
import { BorrowerDashboardPage } from "@/pages/borrower/BorrowerDashboardPage";
import { FieldOfficerDashboardPage } from "@/pages/field-officer/FieldOfficerDashboardPage";
import { CreditOfficerDashboardPage } from "@/pages/credit-officer/CreditOfficerDashboardPage";
import { BranchManagerDashboardPage } from "@/pages/branch-manager/BranchManagerDashboardPage";
import { CollectionsOfficerDashboardPage } from "@/pages/collections-officer/CollectionsOfficerDashboardPage";
import { AdminDashboardPage } from "@/pages/admin/AdminDashboardPage";
export function DashboardPage() {
    const { user } = useAuth();
    if (!user)
        return null;
    switch (user.role) {
        case "BORROWER":
            return <BorrowerDashboardPage />;
        case "FIELD_OFFICER":
            return <FieldOfficerDashboardPage />;
        case "CREDIT_OFFICER":
            return <CreditOfficerDashboardPage />;
        case "BRANCH_MANAGER":
            return <BranchManagerDashboardPage />;
        case "COLLECTIONS_OFFICER":
            return <CollectionsOfficerDashboardPage />;
        case "NBFC_ADMIN":
            return <AdminDashboardPage />;
        default:
            return null;
    }
}
