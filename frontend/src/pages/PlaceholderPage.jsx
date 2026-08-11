import { useLocation } from "react-router-dom";
import { Hammer } from "lucide-react";
import { useAuth } from "@/auth/useAuth";
import { navForRole } from "@/routes/nav";
import { EmptyState } from "@/components/ui";

export function PlaceholderPage() {
    const { user } = useAuth();
    const { pathname } = useLocation();
    const item = user ? navForRole(user.role).find((i) => i.path === pathname) : undefined;
    const title = item?.label ?? "Workspace";
    return (<>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">{title}</h1>
        <p className="mt-2 text-ink-muted">This workspace screen is scaffolded in the shell and will be wired to live APIs in Phase 8.</p>
      </div>
      <div className="min-w-0 rounded-lg border border-border bg-card shadow-card">
        <EmptyState icon={Hammer} title={`${title} — coming in Phase 8`} description="The navigation, layout, and design system are ready; the data-driven screen lands next phase."/>
      </div>
    </>);
}
