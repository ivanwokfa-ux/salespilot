import type {
  ImportBatch,
  ImportErrorLine,
  Page,
  OverviewResponse,
  MonthlyResponse,
  TopProductsResponse,
} from './types'

// Fonction commune : appelle l'API et renvoie le JSON, ou leve une erreur lisible
async function getJson<T>(url: string): Promise<T> {
  const response = await fetch(url)
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Erreur ${response.status}`)
  }
  return response.json()
}

/* ---------- Imports ---------- */

export function fetchImports() {
  return getJson<Page<ImportBatch>>('/api/v1/imports')
}

export function fetchImportErrors(importId: number) {
  return getJson<Page<ImportErrorLine>>(`/api/v1/imports/${importId}/errors`)
}

export async function uploadImport(file: File, currency: string): Promise<ImportBatch> {
  const body = new FormData()
  body.append('file', file)
  body.append('currency', currency)

  const response = await fetch('/api/v1/imports', { method: 'POST', body })
  if (!response.ok) {
    // Notre API renvoie un ProblemDetail : on affiche son message "detail"
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Erreur ${response.status}`)
  }
  return response.json()
}

/* ---------- Indicateurs ---------- */

export function fetchOverview(from: string, to: string) {
  return getJson<OverviewResponse>(`/api/v1/analytics/overview?${new URLSearchParams({ from, to })}`)
}

export function fetchMonthly(from: string, to: string) {
  return getJson<MonthlyResponse>(`/api/v1/analytics/monthly?${new URLSearchParams({ from, to })}`)
}

export function fetchTopProducts(from: string, to: string, limit = 5) {
  return getJson<TopProductsResponse>(
    `/api/v1/analytics/top-products?${new URLSearchParams({ from, to, limit: String(limit) })}`)
}
export async function deleteImport(importId: number): Promise<void> {
  const response = await fetch(`/api/v1/imports/${importId}`, { method: 'DELETE' })
  if (!response.ok) {
    throw new Error(`Suppression impossible (erreur ${response.status})`)
  }
}
export async function downloadReport(from: string, to: string): Promise<Blob> {
  const response = await fetch(`/api/v1/reports/sales?from=${from}&to=${to}`)
  if (!response.ok) {
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Erreur ${response.status}`)
  }
  return response.blob()
}
export interface SalesQuery {
  page: number
  size?: number
  importId?: number
  agency?: string
  from?: string
  to?: string
  q?: string
}

export function fetchSales(query: SalesQuery) {
  const params = new URLSearchParams()
  // On n'ajoute a l'URL que les parametres renseignes
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== '') params.set(key, String(value))
  })
  return getJson<Page<SaleRow>>(`/api/v1/sales?${params}`)
}