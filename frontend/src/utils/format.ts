// Montant avec sa devise : "206 737 FCFA", "1 248,90 €"
export function formatMoney(value: number | null, currency: string): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency }).format(value)
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat('fr-FR').format(value)
}