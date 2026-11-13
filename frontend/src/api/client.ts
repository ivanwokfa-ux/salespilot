import type { ImportBatch, Page } from './types'

export async function fetchImports(): Promise<Page<ImportBatch>> {
  const response = await fetch('/api/v1/imports')
  if (!response.ok) {
    throw new Error(`Erreur ${response.status}`)
  }
  return response.json()
}