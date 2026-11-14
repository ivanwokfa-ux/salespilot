interface StatTileProps {
  label: string
  value: string
  hint?: string
}

function StatTile({ label, value, hint }: StatTileProps) {
  return (
    <div className="tile">
      <span className="tile-label">{label}</span>
      <span className="tile-value">{value}</span>
      {hint && <span className="tile-hint">{hint}</span>}
    </div>
  )
}

export default StatTile