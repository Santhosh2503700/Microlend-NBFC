export function checkPassword(pw) {
    const rules = [
        { label: "At least 8 characters", met: pw.length >= 8 },
        { label: "An uppercase letter", met: /[A-Z]/.test(pw) },
        { label: "A lowercase letter", met: /[a-z]/.test(pw) },
        { label: "A number", met: /\d/.test(pw) },
        { label: "A special character", met: /[^A-Za-z0-9]/.test(pw) },
    ];
    return { ok: rules.every((r) => r.met), rules };
}
