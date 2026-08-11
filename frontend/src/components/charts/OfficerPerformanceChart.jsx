import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis, } from "recharts";
import { inr, inrCompact, PALETTE } from "./chartTheme";
export function OfficerPerformanceChart({ data }) {
    const ranked = [...data].sort((a, b) => b.collectedAmount - a.collectedAmount);
    return (<ResponsiveContainer width="100%" height={Math.max(280, ranked.length * 44)}>
      <BarChart data={ranked} layout="vertical" margin={{ top: 8, right: 24, bottom: 4, left: 8 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={PALETTE.grid} strokeOpacity={0.35} horizontal={false}/>
        <XAxis type="number" stroke={PALETTE.axis} tick={{ fontSize: 12 }} tickFormatter={(value) => inrCompact(Number(value))}/>
        <YAxis type="category" dataKey="officerName" stroke={PALETTE.axis} tick={{ fontSize: 12 }} width={140}/>
        <Tooltip formatter={(value) => [inr(Number(value)), "Collected"]} labelFormatter={(label) => String(label)}/>
        <Bar dataKey="collectedAmount" name="Collected" fill={PALETTE.primary} radius={[0, 4, 4, 0]}/>
      </BarChart>
    </ResponsiveContainer>);
}
