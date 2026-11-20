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
export interface ImportErrorLine {
  lineNumber: number
  rawLine: string | null
  errorMessage: string
}
export interface CurrencyOverview {
  currency: string
  revenue: number
  invoiceCount: number
  averageBasket: number | null
  unitsSold: number
}

export interface OverviewResponse {
  from: string
  to: string
  totals: CurrencyOverview[]
}

export interface MonthlyPoint {
  month: string
  revenue: number
  invoiceCount: number
}

export interface MonthlyResponse {
  from: string
  to: string
  series: { currency: string; months: MonthlyPoint[] }[]
}

export interface TopProduct {
  currency: string
  rank: number
  productSku: string
  productName: string
  unitsSold: number
  revenue: number
  sharePercent: number | null
}

export interface TopProductsResponse {
  from: string
  to: string
  products: TopProduct[]
}

export interface SaleRow {
  id: number
  importBatchId: number
  orderRef: string
  orderDate: string
  agency: string | null
  customerCode: string | null
  customerName: string | null
  productSku: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
  currency: string
}