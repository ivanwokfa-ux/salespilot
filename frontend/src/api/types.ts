// Ces types reprennent exactement les records Java renvoyes par l'API.

export type ImportStatus = 'PENDING' | 'COMPLETED' | 'FAILED'

export interface ImportBatch {
  id: number
  fileName: string
  status: ImportStatus
  totalRows: number
  importedRows: number
  rejectedRows: number
  duplicateRows: number
  createdAt: string
}

export interface Page<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}