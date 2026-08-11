import { Area, CartesianGrid, ComposedChart, Legend, Line, ResponsiveContainer, Tooltip, XAxis, YAxis, } from "recharts";
import { inr, inrCompact, PALETTE } from "./chartTheme";
export function PortfolioTrendChart({ data }) {
    return (<ResponsiveContainer width="100%" height={280}>
      <ComposedChart data={data} margin={{ top: 8, right: 16, bottom: 4, left: 8 }}>
        <defs>
          <linearGradient id="disbursedFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor={PALETTE.secondary} stopOpacity={0.35}/>
            <stop offset="100%" stopColor={PALETTE.secondary} stopOpacity={0.04}/>
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke={PALETTE.grid} strokeOpacity={0.35} vertical={false}/>
        <XAxis dataKey="month" stroke={PALETTE.axis} tick={{ fontSize: 12 }}/>
        <YAxis stroke={PALETTE.axis} tick={{ fontSize: 12 }} tickFormatter={(value) => inrCompact(Number(value))} width={72}/>
        <Tooltip formatter={(value, name) => [inr(Number(value)), String(name)]} labelFormatter={(label) => `Month: ${String(label)}`}/>
        <Legend />
        <Area type="monotone" dataKey="disbursed" name="Disbursed" stroke={PALETTE.secondary} strokeWidth={2} fill="url(#disbursedFill)"/>
        <Line type="monotone" dataKey="cumulativePortfolio" name="Cumulative portfolio" stroke={PALETTE.primary} strokeWidth={2.5} dot={false}/>
      </ComposedChart>
    </ResponsiveContainer>);
}
