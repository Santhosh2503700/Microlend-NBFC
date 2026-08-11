
const STOPS = "#00352d 0%, #004d40 35%, #00695c 70%, #23a495 100%";

/** Vertical dark→light sweep (used by the sidebar). */
export const EMERALD_GRADIENT_VERTICAL = {
    backgroundImage:
        `radial-gradient(circle at bottom center, rgba(102,209,193,0.45) 0%, rgba(102,209,193,0.15) 25%, rgba(102,209,193,0) 55%), linear-gradient(180deg, ${STOPS})`,
};

/** Horizontal dark→light sweep (used by the top header and bottom nav). */
export const EMERALD_GRADIENT_HORIZONTAL = {
    backgroundImage: `linear-gradient(90deg, ${STOPS})`,
};
