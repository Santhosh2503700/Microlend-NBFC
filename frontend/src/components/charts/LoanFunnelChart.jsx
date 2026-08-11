import { Bar, BarChart, Cell, LabelList, ResponsiveContainer, Tooltip, XAxis, YAxis, } from "recharts";
import { PALETTE } from "./chartTheme";
/** Emerald ramp so the funnel reads as a descending pipeline. */
const FUNNEL_RAMP = ["#004D40", "#0B8A75", "#2E7D32", "#EF6C00", "#E65100", "#D32F2F"];
// Application pipeline: count per status as a horizontal stepped funnel.
export function LoanFunnelChart({ data }) {
    return (<ResponsiveContainer width="100%" height={Math.max(280, data.length * 46)}>
      <BarChart data={data} layout="vertical" margin={{ top: 8, right: 40, bottom: 4, left: 8 }}>
        <XAxis type="number" stroke={PALETTE.axis} tick={{ fontSize: 12 }} allowDecimals={false}/>
        <YAxis type="category" dataKey="status" stroke={PALETTE.axis} tick={{ fontSize: 12 }} width={130}/>
        <Tooltip formatter={(value) => [`${Number(value)} application(s)`, "Count"]} labelFormatter={(label) => String(label)}/>
        <Bar dataKey="count" name="Applications" radius={[0, 4, 4, 0]}>
          {data.map((row, index) => (<Cell key={row.status} fill={FUNNEL_RAMP[index % FUNNEL_RAMP.length]}/>))}
          <LabelList dataKey="count" position="right" style={{ fill: PALETTE.text, fontSize: 12 }}/>
        </Bar>
      </BarChart>
    </ResponsiveContainer>);
}
