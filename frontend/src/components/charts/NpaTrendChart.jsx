import { CartesianGrid, Line, LineChart, ReferenceLine, ResponsiveContainer, Tooltip, XAxis, YAxis, } from "recharts";
import { inr, PALETTE } from "./chartTheme";
export function NpaTrendChart({ data }) {
    return (<ResponsiveContainer width="100%" height={280}>
      <LineChart data={data} margin={{ top: 8, right: 16, bottom: 4, left: 8 }}>
        <CartesianGrid strokeDasharray="3 3" stroke={PALETTE.grid} strokeOpacity={0.35} vertical={false}/>
        <XAxis dataKey="month" stroke={PALETTE.axis} tick={{ fontSize: 12 }}/>
        <YAxis stroke={PALETTE.axis} tick={{ fontSize: 12 }} tickFormatter={(value) => `${Number(value)}%`} width={48}/>
        <Tooltip formatter={(value, _name, item) => {
            const row = item.payload;
            const amount = row ? ` · ${inr(row.npaAmount)}` : "";
            return [`${Number(value).toFixed(2)}%${amount}`, "NPA"];
        }} labelFormatter={(label) => `Month: ${String(label)}`}/>
        <ReferenceLine y={5} stroke={PALETTE.danger} strokeDasharray="6 4" label={{ value: "NPA threshold (5%)", position: "insideTopRight", fill: PALETTE.danger, fontSize: 11 }}/>
        <Line type="monotone" dataKey="npaPercent" name="NPA %" stroke={PALETTE.danger} strokeWidth={2.5} dot={{ r: 3 }}/>
      </LineChart>
    </ResponsiveContainer>);
}
