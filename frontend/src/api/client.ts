import type { ImportBatch, Page } from './types'

export async function fetchImports(): Promise<Page<ImportBatch>> {
  const response = await fetch('/api/v1/imports')
  if (!response.ok) {
    throw new Error(`Erreur ${response.status}`)
  }
  return response.json()
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