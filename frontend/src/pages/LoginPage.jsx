import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { Mail, Lock } from "lucide-react";
import { useAuth } from "@/auth/useAuth";
import { Wordmark } from "@/components/brand/Wordmark";
import { Button, Input, Alert } from "@/components/ui";
import { errorMessage } from "@/api/client";
export function LoginPage() {
    const { user, mustReset, login } = useAuth();
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState(null);
    const [busy, setBusy] = useState(false);
    // Already authenticated
    if (user && !mustReset) {
        return <Navigate to="/dashboard" replace/>;
    }
    if (user && mustReset) {
        return <Navigate to="/reset-password" replace/>;
    }
    const onSubmit = async (e) => {
        e.preventDefault();
        setError(null);
        setBusy(true);
        try {
            const res = await login(email.trim(), password);
            navigate(res.forcePasswordReset
                ? "/reset-password"
                : "/dashboard", { replace: true });
        }
        catch (err) {
            setError(errorMessage(err, "Invalid email or password"));
        }
        finally {
            setBusy(false);
        }
    };
    return (<div className="relative min-h-screen overflow-hidden" style={{ backgroundImage: "radial-gradient(circle at bottom right, rgba(102,209,193,0.95) 0%, rgba(102,209,193,0.65) 18%, rgba(102,209,193,0) 42%), linear-gradient(135deg, #00352d 0%, #004d40 35%, #00695c 70%, #23a495 100%)" }}>
      <div className="auth-left"></div>

      <div className="auth-right"></div>

      <div className="absolute left-1/2 top-1/2 z-10 w-[calc(100%-2rem)] max-w-[450px] -translate-x-1/2 -translate-y-1/2 rounded-[20px] bg-white p-6 shadow-[0_20px_60px_rgba(0,0,0,0.2)] sm:p-10">
        <div className="mb-6 flex justify-center text-center">
          <Wordmark height={34}/>
        </div>

        <h1 className="text-center text-2xl">Welcome back</h1>

        <p className="mb-6 mt-2 text-center text-ink-muted">
          Sign in to your MicroLend workspace.
        </p>

        {error && (<Alert tone="error">
            {error}
          </Alert>)}

        <form onSubmit={onSubmit}>
          <Input id="email" label="Email" type="email" autoComplete="username" placeholder="you@microlend.com" icon={<Mail size={18}/>} value={email} onChange={(e) => setEmail(e.target.value)} required/>

          <Input id="password" label="Password" type="password" autoComplete="current-password" placeholder="Your password" icon={<Lock size={18}/>} value={password} onChange={(e) => setPassword(e.target.value)} required/>

          <Button type="submit" block loading={busy}>
            Sign in
          </Button>
        </form>
      </div>
    </div>);
}