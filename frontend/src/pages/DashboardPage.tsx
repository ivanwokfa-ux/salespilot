import { useEffect, useState } from 'react'
import { fetchMonthly, fetchOverview, fetchTopProducts } from '../api/client'
import type { MonthlyResponse, OverviewResponse, TopProductsResponse } from '../api/types'
import StatTile from '../components/StatTile'
import MonthlyChart from '../components/MonthlyChart'
import { formatMoney, formatNumber } from '../utils/format'

function DashboardPage() {
  // Periode par defaut : janvier 2026, le mois de nos donnees de demonstration
  const [from, setFrom] = useState('2026-01-01')
  const [to, setTo] = useState('2026-01-31')

  const [overview, setOverview] = useState<OverviewResponse | null>(null)
  const [monthly, setMonthly] = useState<MonthlyResponse | null>(null)
  const [top, setTop] = useState<TopProductsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    // Les trois appels partent en meme temps
    Promise.all([fetchOverview(from, to), fetchMonthly(from, to), fetchTopProducts(from, to)])
      .then(([o, m, t]) => {
        if (cancelled) return
        setOverview(o)
        setMonthly(m)
        setTop(t)
      })
      .catch((err: Error) => { if (!cancelled) setError(err.message) })
      .finally(() => { if (!cancelled) setLoading(false) })

    return () => { cancelled = true }
  }, [from, to])

  return (
    <section>
      <div className="page-head">
        <div>
          <h1>Tableau de bord</h1>
          <p className="page-subtitle">Indicateurs de ventes sur la période choisie.</p>
        </div>
        <div className="filters">
          <label className="field">Du<input type="date" value={from} onChange={(e) => setFrom(e.target.value)} /></label>
          <label className="field">Au<input type="date" value={to} onChange={(e) => setTo(e.target.value)} /></label>
        </div>
      </div>

      {loading && <p>Chargement…</p>}
      {error && <p className="message error">{error}</p>}
      {!loading && !error && overview?.totals.length === 0 && (
        <p className="message">Aucune vente sur cette période.</p>
      )}

      {!loading && !error && overview?.totals.map((t) => (
        <div key={t.currency} className="currency-block">
          <div className="tiles">
            <StatTile label="Chiffre d'affaires HT" value={formatMoney(t.revenue, t.currency)} hint="avoirs déduits" />
            <StatTile label="Factures" value={formatNumber(t.invoiceCount)} hint="avoirs exclus" />
            <StatTile label="Panier moyen" value={formatMoney(t.averageBasket, t.currency)} />
            <StatTile label="Unités vendues" value={formatNumber(t.unitsSold)} />
          </div>
           {/* AJOUT : le graphique mensuel de cette devise */}
                    {monthly?.series
                      .filter((s) => s.currency === t.currency)
                      .map((s) => (
                        <div key={s.currency} className="card">
                          <MonthlyChart currency={s.currency} months={s.months} />
                        </div>
                      ))}

          <div className="card">
            <h2 className="card-title">Produits les plus vendus</h2>
            <table>
              <thead>
                <tr><th>#</th><th>Produit</th><th className="num">Qté</th><th className="num">CA HT</th><th>Part du CA</th></tr>
              </thead>
              <tbody>
                {top?.products.filter((p) => p.currency === t.currency).map((p) => (
                  <tr key={p.productSku}>
                    <td>{p.rank}</td>
                    <td>{p.productName}<span className="sku">{p.productSku}</span></td>
                    <td className="num">{formatNumber(p.unitsSold)}</td>
                    <td className="num">{formatMoney(p.revenue, p.currency)}</td>
                    <td>
                      <div className="share" title={`${p.sharePercent ?? 0} %`}>
                        <i style={{ width: `${Math.max(0, p.sharePercent ?? 0)}%` }} />
                      </div>
                      <span className="share-label">{p.sharePercent ?? 0} %</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ))}
    </section>
  )
}

export default DashboardPage