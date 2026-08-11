/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx,ts,tsx}"],
  theme: {
    extend: {
      colors: {
        // Sovereign Emerald design tokens. Values mirror src/styles/theme.css so JSX utilities
        // (e.g. bg-primary, text-muted, border-border) stay the single source of truth.
        primary: {
          DEFAULT: "#004d40",
          hover: "#0b8a75",
        },
        canvas: "#f8f9fa",
        card: "#e2f3ed",
        ink: {
          DEFAULT: "#1c2826",
          muted: "#5b6b68",
        },
        border: "#9fcdbf",
        selection: "#e0f2f1",
        success: { DEFAULT: "#2e7d32", tint: "#e8f5e9" },
        warning: { DEFAULT: "#ef6c00", tint: "#fff3e0" },
        danger: { DEFAULT: "#d32f2f", tint: "#fdecea" },
        info: { tint: "#e0f2f1" },
      },
      fontFamily: {
        sans: ['"Plus Jakarta Sans"', '"Inter"', "system-ui", "sans-serif"],
      },
      fontSize: {
        xs: "0.75rem",
        sm: "0.875rem",
        base: "0.95rem",
        md: "1rem",
        lg: "1.125rem",
        xl: "1.5rem",
        "2xl": "2rem",
      },
      borderRadius: {
        sm: "6px",
        md: "10px",
        lg: "14px",
        full: "999px",
      },
      boxShadow: {
        card: "0 1px 2px rgba(16,40,34,0.04), 0 4px 16px rgba(16,40,34,0.06)",
        pop: "0 8px 28px rgba(16,40,34,0.16)",
      },
      spacing: {
        // Layout rails referenced by the shell.
        sidebar: "256px",
        "sidebar-collapsed": "80px",
        header: "64px",
        bottomnav: "64px",
      },
      backdropBlur: {
        glass: "12px",
      },
      screens: {
        // Mobile-first; adds an ultrawide breakpoint on top of the Tailwind defaults.
        "3xl": "1920px",
      },
    },
  },
  plugins: [],
};
