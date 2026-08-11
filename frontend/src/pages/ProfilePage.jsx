import { useCallback, useEffect, useState } from "react";
import { User, Mail, Phone, ShieldCheck, CreditCard, Building2, Save, CheckCircle2, Edit, X } from "lucide-react";
import { profileApi } from "@/api/profile";
import { errorMessage } from "@/api/client";
import { useAuth } from "@/auth/useAuth";
import { Alert, Button, Card, Input, LoadingState, StatusBadge, toneForStatus } from "@/components/ui";
import { ROLE_LABELS } from "@/routes/nav";

export function ProfilePage() {
  const { user: authUser } = useAuth();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [bankAccountNumber, setBankAccountNumber] = useState("");
  const [ifscCode, setIfscCode] = useState("");

  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const loadProfile = useCallback(() => {
    setLoading(true);
    setError(null);
    profileApi
      .getProfile()
      .then((data) => {
        setProfile(data);
        setName(data.name || "");
        setPhone(data.phone || "");
        setBankAccountNumber(data.bankAccountNumber || "");
        setIfscCode(data.ifscCode || "");
      })
      .catch((err) => setError(errorMessage(err, "Could not load user profile")))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  const isBorrower = profile?.role === "BORROWER";

  const handleCancel = () => {
    setIsEditing(false);
    setError(null);
    if (profile) {
      setName(profile.name || "");
      setPhone(profile.phone || "");
      setBankAccountNumber(profile.bankAccountNumber || "");
      setIfscCode(profile.ifscCode || "");
    }
  };

  const onSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    if (phone && !/^\d{10}$/.test(phone)) {
      setError("Phone number must be exactly 10 digits");
      return;
    }

    if (isBorrower && bankAccountNumber && !/^\d{9,18}$/.test(bankAccountNumber)) {
      setError("Bank account number must be between 9 and 18 digits");
      return;
    }

    setSaving(true);
    try {
      const payload = {
        name: name.trim(),
        phone: phone.trim(),
        ...(isBorrower
          ? {
              bankAccountNumber: bankAccountNumber.trim(),
              ifscCode: ifscCode.trim().toUpperCase(),
            }
          : {}),
      };

      const updated = await profileApi.updateProfile(payload);
      setProfile(updated);
      setSuccess("Profile updated successfully!");
      setIsEditing(false);
    } catch (err) {
      setError(errorMessage(err, "Failed to update profile"));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingState label="Loading your profile details..." />;
  }

  return (
    <>
      <div className="mb-6">
        <h1 className="text-2xl font-bold">
          <User size={24} style={{ verticalAlign: "-4px", marginRight: 8 }} />
          User Profile
        </h1>
        <p className="mt-2 text-ink-muted">View your account credentials and update allowed contact/banking details.</p>
      </div>

      {error && <Alert tone="error">{error}</Alert>}
      {success && (
        <Alert tone="info">
          <CheckCircle2 size={16} style={{ verticalAlign: "-3px", marginRight: 6 }} />
          {success}
        </Alert>
      )}

      <div className="grid grid-cols-1 items-start gap-5 lg:grid-cols-[1.15fr_1fr]">
        {/* Read-Only Account Summary Card */}
        <Card>
          <div className="flex items-center gap-2 mb-4">
            <ShieldCheck size={20} style={{ color: "var(--color-primary)" }} />
            <h2 className="text-lg font-semibold">
              Account Identity
            </h2>
          </div>

          <dl className="flex flex-col gap-4">
            <div>
              <dt className="text-sm text-ink-muted">Email Address</dt>
              <dd className="flex items-center gap-2 mt-1 font-semibold text-ink">
                <Mail size={16} className="text-ink-muted" />
                {profile?.email}
              </dd>
              <span className="mt-2 block text-xs text-ink-muted">Email cannot be modified.</span>
            </div>

            <div>
              <dt className="text-sm text-ink-muted">Role</dt>
              <dd className="mt-1.5">
                <StatusBadge tone="info">{ROLE_LABELS[profile?.role] || profile?.role}</StatusBadge>
              </dd>
            </div>

            <div>
              <dt className="text-sm text-ink-muted">Account Status</dt>
              <dd className="mt-1.5">
                <StatusBadge tone={toneForStatus(profile?.status || "ACTIVE")}>{profile?.status}</StatusBadge>
              </dd>
            </div>

            {profile?.branchId != null && (
              <div>
                <dt className="text-sm text-ink-muted">Assigned Branch</dt>
                <dd className="flex items-center gap-2 mt-1 font-medium">
                  <Building2 size={16} className="text-ink-muted" />
                  Branch{profile.branchId}
                </dd>
              </div>
            )}
          </dl>
        </Card>

        {/* Profile Card with Toggleable Edit Mode */}
        <Card>
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div className="flex items-center gap-2">
              <User size={20} style={{ color: "var(--color-primary)" }} />
              <h2 className="text-lg font-semibold">
                User Details
              </h2>
            </div>

            {!isEditing && (
              <Button size="sm" variant="secondary" onClick={() => setIsEditing(true)}>
                <Edit size={16} />
                Edit Profile
              </Button>
            )}
          </div>

          <form onSubmit={onSubmit} className="flex flex-col gap-4">
            <Input
              id="prof-name"
              label="Full Name"
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              icon={<User size={18} />}
              disabled={!isEditing}
              required
            />

            <Input
              id="prof-phone"
              label="Phone Number (10 digits)"
              type="tel"
              maxLength={10}
              value={phone}
              onChange={(e) => setPhone(e.target.value.replace(/\D/g, ""))}
              icon={<Phone size={18} />}
              placeholder="9876543210"
              disabled={!isEditing}
              required
            />

            {/* Borrower-Only Fields */}
            {isBorrower && (
              <div
                style={{
                  background: "var(--color-selection-tint)",
                  border: "1px solid var(--color-border)",
                  borderRadius: "var(--radius-md)",
                  padding: "var(--space-4)",
                }}
                className="flex flex-col gap-3 mt-2"
              >
                <div className="flex items-center gap-2 font-semibold" style={{ fontSize: "var(--fs-sm)" }}>
                  <CreditCard size={18} style={{ color: "var(--color-primary)" }} />
                  Bank Disbursal Account Details
                </div>

                <Input
                  id="prof-bank"
                  label="Bank Account Number"
                  type="text"
                  inputMode="numeric"
                  maxLength={18}
                  value={bankAccountNumber}
                  onChange={(e) => setBankAccountNumber(e.target.value.replace(/\D/g, ""))}
                  placeholder="9 to 18 digits"
                  disabled={!isEditing}
                  required
                />

                <Input
                  id="prof-ifsc"
                  label="IFSC Code"
                  type="text"
                  maxLength={11}
                  value={ifscCode}
                  onChange={(e) => setIfscCode(e.target.value.toUpperCase())}
                  placeholder="e.g. SBIN0001234"
                  disabled={!isEditing}
                  required
                />
              </div>
            )}

            {isEditing && (
              <div className="flex flex-wrap items-center gap-2 mt-4">
                <Button type="submit" loading={saving}>
                  <Save size={16} />
                  Save Changes
                </Button>
                <Button type="button" variant="ghost" onClick={handleCancel} disabled={saving}>
                  <X size={16} />
                  Cancel
                </Button>
              </div>
            )}
          </form>
        </Card>
      </div>
    </>
  );
}