import { useEffect, useState, type FormEvent } from 'react'
import { fetchImports, fetchSales } from '../api/client'
import type { ImportBatch, Page, SaleRow } from '../api/types'
import { formatDate, formatMoney, formatNumber } from '../utils/format'

const PAGE_SIZE = 20

interface Filters {
  importId: string
  agency: string
  from: string
  to: string
  q: string
}

const EMPTY_FILTERS: Filters = { importId: '', agency: '', from: '', to: '', q: '' }

function SalesPage() {
  const [draft, setDraft] = useState<Filters>(EMPTY_FILTERS)     // ce qui est tape
  const [applied, setApplied] = useState<Filters>(EMPTY_FILTERS) // ce qui est applique
  const [page, setPage] = useState(0)

  const [imports, setImports] = useState<ImportBatch[]>([])
  const [result, setResult] = useState<Page<SaleRow> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // La liste des fichiers, pour le menu deroulant : chargee une seule fois
  useEffect(() => {
    fetchImports().then((p) => setImports(p.content)).catch(() => setImports([]))
  }, [])

  // Les ventes : rechargees quand la page ou les filtres appliques changent
  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setError(null)

    fetchSales({
      page,
      size: PAGE_SIZE,
      importId: applied.importId ? Number(applied.importId) : undefined,
      agency: applied.agency,
      from: applied.from,
      to: applied.to,
      q: applied.q,
    })
      .then((data) => { if (!cancelled) setResult(data) })
      .catch((err: Error) => { if (!cancelled) setError(err.message) })
      .finally(() => { if (!cancelled) setLoading(false) })

    return () => { cancelled = true }
  }, [page, applied])

  // Met a jour un seul champ du brouillon
  function updateDraft(field: keyof Filters, value: string) {
    setDraft((d) => ({ ...d, [field]: value }))
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()      // empeche le rechargement de la page par le navigateur
    setApplied(draft)
    setPage(0)
  }

  function handleReset() {
    setDraft(EMPTY_FILTERS)
    setApplied(EMPTY_FILTERS)
    setPage(0)
  }

  const totalPages = result?.totalPages ?? 0

  return (
    <section>
      <h1>Ventes</h1>
      <p className="page-subtitle">Recherchez et filtrez les lignes de vente importées.</p>

      <form className="card sales-filters" onSubmit={handleSubmit}>
        <label className="field">
          Fichier
          <select value={draft.importId} onChange={(e) => updateDraft('importId', e.target.value)}>
            <option value="">Tous les imports</option>
            {imports.map((imp) => (
              <option key={imp.id} value={imp.id}>
                {imp.fileName} ({new Date(imp.createdAt).toLocaleDateString('fr-FR')})
              </option>
            ))}
          </select>
        </label>
        <label className="field">
          Agence
          <input value={draft.agency} placeholder="ex. DOUALA"
                 onChange={(e) => updateDraft('agency', e.target.value)} />
        </label>
        <label className="field">
          Du
          <input type="date" value={draft.from} onChange={(e) => updateDraft('from', e.target.value)} />
        </label>
        <label className="field">
          Au
          <input type="date" value={draft.to} onChange={(e) => updateDraft('to', e.target.value)} />
        </label>
        <label className="field grow">
          Recherche
          <input value={draft.q} placeholder="Produit, référence, n° de pièce, client…"
                 onChange={(e) => updateDraft('q', e.target.value)} />
        </label>
        <div className="filter-actions">
          <button type="submit" className="btn">Filtrer</button>
          <button type="button" className="btn-link" onClick={handleReset}>Réinitialiser</button>
        </div>
      </form>

      <div className="card">
        {error && <p className="message error">{error}</p>}
        {!error && !loading && result?.content.length === 0 && (
          <p>Aucune vente ne correspond à ces filtres.</p>
        )}

        {result && result.content.length > 0 && (
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Pièce</th>
                <th>Agence</th>
                <th>Client</th>
                <th>Produit</th>
                <th className="num">Qté</th>
                <th className="num">Prix unitaire</th>
                <th className="num">Montant HT</th>
              </tr>
            </thead>
            <tbody>
              {result.content.map((s) => (
                <tr key={s.id} className={s.quantity < 0 ? 'credit-note' : ''}>
                  <td>{formatDate(s.orderDate)}</td>
                  <td>{s.orderRef}</td>
                  <td>{s.agency ?? '—'}</td>
                  <td>{s.customerName ?? s.customerCode ?? '—'}</td>
                  <td>{s.productName}<span className="sku">{s.productSku}</span></td>
                  <td className="num">{formatNumber(s.quantity)}</td>
                  <td className="num">{formatMoney(s.unitPrice, s.currency)}</td>
                  <td className="num">{formatMoney(s.lineTotal, s.currency)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        {result && totalPages > 0 && (
          <div className="pagination">
            <button type="button" className="btn" disabled={page === 0 || loading}
                    onClick={() => setPage((p) => p - 1)}>
              ← Précédent
            </button>
            <span>
              Page {page + 1} / {totalPages} · {formatNumber(result.totalElements)} ventes
            </span>
            <button type="button" className="btn" disabled={page + 1 >= totalPages || loading}
                    onClick={() => setPage((p) => p + 1)}>
              Suivant →
            </button>
          </div>
        )}
      </div>
    </section>
  )
}

export default SalesPage