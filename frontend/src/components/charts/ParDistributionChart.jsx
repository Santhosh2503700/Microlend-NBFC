import { Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip } from "recharts";
import { inr, PALETTE, parColor } from "./chartTheme";
export function ParDistributionChart({ data }) {
    return (<ResponsiveContainer width="100%" height={280}>
      <PieChart>
        <Pie data={data} dataKey="count" nameKey="bucket" cx="50%" cy="50%" innerRadius={62} outerRadius={100} paddingAngle={2} stroke={PALETTE.canvas}>
          {data.map((row) => (<Cell key={row.bucket} fill={parColor(row.bucket)}/>))}
        </Pie>
        <Tooltip formatter={(value, name) => {
            const row = data.find((r) => r.bucket === name);
            const outstanding = row ? inr(row.outstanding) : "";
            return [`${Number(value)} case(s) · ${outstanding} outstanding`, String(name)];
        }}/>
        <Legend />
      </PieChart>
    </ResponsiveContainer>);
}
