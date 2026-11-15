import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import type { MonthlyPoint } from '../api/types'
import { formatMoney } from '../utils/format'

interface MonthlyChartProps {
  currency: string
  months: MonthlyPoint[]
}

// "2026-01" -> "janv. 26"
function formatMonth(month: string): string {
  const [year, m] = month.split('-').map(Number)
  return new Date(year, m - 1, 1).toLocaleDateString('fr-FR', { month: 'short', year: '2-digit' })
}

// 206737 -> "207 k" : des graduations courtes pour l'axe vertical
function formatCompact(value: number): string {
  return new Intl.NumberFormat('fr-FR', { notation: 'compact', maximumFractionDigits: 1 }).format(value)
}

function MonthlyChart({ currency, months }: MonthlyChartProps) {
  const data = months.map((p) => ({ ...p, label: formatMonth(p.month) }))

  return (
    <div className="chart">
      <h3>Chiffre d'affaires par mois</h3>
      <ResponsiveContainer width="100%" height={260}>
        <BarChart data={data} margin={{ top: 8, right: 8, left: 0, bottom: 0 }}>
          <CartesianGrid vertical={false} stroke="#e3e7ee" />
          <XAxis dataKey="label" tickLine={false} axisLine={false} />
          <YAxis tickFormatter={formatCompact} tickLine={false} axisLine={false} width={56} />
          <Tooltip
            formatter={(value) => [formatMoney(Number(value), currency), 'CA']}
            cursor={{ fill: 'rgba(20, 33, 58, 0.05)' }}
          />
          <Bar dataKey="revenue" fill="#b85f08" radius={[4, 4, 0, 0]} maxBarSize={48} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  )
}

export default MonthlyChart