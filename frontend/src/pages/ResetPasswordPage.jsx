import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { KeyRound, Lock, Check, X } from "lucide-react";
import { useAuth } from "@/auth/useAuth";
import { Wordmark } from "@/components/brand/Wordmark";
import { Button, Input, Alert } from "@/components/ui";
import { checkPassword } from "@/auth/passwordPolicy";
import { errorMessage } from "@/api/client";

export function ResetPasswordPage() {
    const { user, mustReset, submitReset } = useAuth();
    const navigate = useNavigate();

    const [pw, setPw] = useState("");
    const [confirm, setConfirm] = useState("");
    const [error, setError] = useState(null);
    const [busy, setBusy] = useState(false);

    // Route guard: only a user mid-forced-reset belongs here.
    if (!user)
        return <Navigate to="/login" replace />;

    if (!mustReset)
        return <Navigate to="/dashboard" replace />;

    const check = checkPassword(pw);
    const matches = pw.length > 0 && pw === confirm;
    const canSubmit = check.ok && matches && !busy;

    const onSubmit = async (e) => {
        e.preventDefault();
        if (!canSubmit)
            return;

        setError(null);
        setBusy(true);

        try {
            await submitReset(pw);
            navigate("/dashboard", { replace: true });
        }
        catch (err) {
            setError(errorMessage(err, "Could not reset password"));
        }
        finally {
            setBusy(false);
        }
    };

    return (
        <div className="relative min-h-screen overflow-hidden" style={{ backgroundImage: "radial-gradient(circle at bottom right, rgba(102,209,193,0.95) 0%, rgba(102,209,193,0.65) 18%, rgba(102,209,193,0) 42%), linear-gradient(135deg, #00352d 0%, #004d40 35%, #00695c 70%, #23a495 100%)" }}>
            <section className="auth-main">
                <div className="absolute left-1/2 top-1/2 z-10 w-[calc(100%-2rem)] max-w-[450px] -translate-x-1/2 -translate-y-1/2 rounded-[20px] bg-white p-6 text-center shadow-[0_20px_60px_rgba(0,0,0,0.2)] sm:p-10">
                    {/* MicroLend Logo centered at top of the card */}
                    <div className="mb-5 flex justify-center">
                        <Wordmark height={32} />
                    </div>

                    <h1 className="text-center text-2xl">Set a new password</h1>
                    <p className="mb-6 mt-2 text-center text-ink-muted">
                        Your account uses a temporary password. Choose a new one to continue.
                    </p>

                    {error && <Alert tone="error">{error}</Alert>}

                    <form onSubmit={onSubmit} className="text-left">
                        <Input
                            id="new-password"
                            label="New password"
                            type="password"
                            autoComplete="new-password"
                            icon={<KeyRound size={18} />}
                            value={pw}
                            onChange={(e) => setPw(e.target.value)}
                            required
                        />

                        <Input
                            id="confirm-password"
                            label="Confirm password"
                            type="password"
                            autoComplete="new-password"
                            icon={<Lock size={18} />}
                            value={confirm}
                            onChange={(e) => setConfirm(e.target.value)}
                            error={confirm.length > 0 && !matches ? "Passwords do not match" : undefined}
                            required
                        />

                        <ul className="flex flex-col gap-2 mb-6" style={{ listStyle: "none", padding: 0, margin: "0 0 24px" }}>
                            {check.rules.map((r) => (
                                <li key={r.label} className="flex items-center gap-2 text-sm" style={{ color: r.met ? "var(--color-success)" : "var(--color-text-muted)" }}>
                                    {r.met ? <Check size={15} /> : <X size={15} />}
                                    <span>{r.label}</span>
                                </li>
                            ))}
                        </ul>

                        <Button type="submit" block loading={busy} disabled={!canSubmit}>
                            Update password &amp; continue
                        </Button>
                    </form>
                </div>
            </section>
        </div>
    );
}