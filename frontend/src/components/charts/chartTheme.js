export const PALETTE = {
    primary: "#004D40",
    secondary: "#0B8A75",
    success: "#2E7D32",
    warning: "#EF6C00",
    danger: "#D32F2F",
    axis: "#94A3B8",
    grid: "#94A3B8",
    canvas: "#F8F9FA",
    text: "#1C2826",
};
/** PAR bucket → colour, amber→red ramp with green for the current book. */
export const PAR_COLORS = {
    CURRENT: "#2E7D32",
    PAR30: "#0B8A75",
    PAR60: "#EF6C00",
    PAR90: "#E65100",
    PAR180: "#D32F2F",
};
/** Fallback colour for any bucket not in the PAR map. */
export const parColor = (bucket) => PAR_COLORS[bucket] ?? PALETTE.axis;
/** ₹ formatter — whole rupees, Indian grouping. */
export const inr = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
}).format(v);
/** Compact ₹ for dense axes (e.g. ₹1.2L). */
export const inrCompact = (v) => new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    notation: "compact",
    maximumFractionDigits: 1,
}).format(v);
