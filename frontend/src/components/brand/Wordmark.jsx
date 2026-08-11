import { HandCoins } from "lucide-react";
export function Wordmark({ height = 30, variant = "dark", showText = true, }) {
    const textColor = variant === "light" ? "#FFFFFF" : "#004D40";
    return (<div style={{
            display: "flex",
            alignItems: "center",
            gap: "10px",
        }}>
      <HandCoins size={height} color={variant === "light" ? "#7FD8C6" : "#004D40"} strokeWidth={2.2}/>

      {showText && (<span style={{
                fontFamily: "Plus Jakarta Sans, Inter, sans-serif",
                fontSize: "22px",
                fontWeight: 700,
                letterSpacing: "-0.5px",
                color: textColor,
                lineHeight: 1,
            }}>
          Micro
          <span style={{
                color: variant === "light"
                    ? "#7FD8C6"
                    : "#0B8A75",
            }}>
            Lend
          </span>
        </span>)}
    </div>);
}
