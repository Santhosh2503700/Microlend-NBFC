import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis, } from "recharts";
import { inr, inrCompact, PALETTE } from "./chartTheme";
/** Amount due (target, dashed) vs amount collected (actual, emerald) per month. */
export function CollectionEfficiencyChart({ data }) {
    return (<ResponsiveContainer width="100%" height={280}>
      <LineChart data={data} margin={{ top: 8, right: 16, bottom: 4, left: 8 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={PALETTE.grid} strokeOpacity={0.35} vertical={false}/>
        <XAxis dataKey="month" stroke={PALETTE.axis} tick={{ fontSize: 12 }}/>
        <YAxis stroke={PALETTE.axis} tick={{ fontSize: 12 }} tickFormatter={(value) => inrCompact(Number(value))} width={72}/>
        <Tooltip formatter={(value, name, item) => {
            const row = item.payload;
            const suffix = row ? ` · efficiency ${row.efficiencyPercent.toFixed(1)}%` : "";
            return [`${inr(Number(value))}${suffix}`, String(name)];
        }} labelFormatter={(label) => `Month: ${String(label)}`}/>
        <Legend />
        <Line type="monotone" dataKey="due" name="Due (target)" stroke={PALETTE.axis} strokeWidth={2} strokeDasharray="6 4" dot={false}/>
        <Line type="monotone" dataKey="collected" name="Collected (actual)" stroke={PALETTE.primary} strokeWidth={2.5} dot={{ r: 3 }}/>
      </LineChart>
    </ResponsiveContainer>);
}
