// Montant avec sa devise : "206 737 FCFA", "1 248,90 €"
export function formatMoney(value: number | null, currency: string): string {
  if (value === null) return '—'
  return new Intl.NumberFormat('fr-FR', { style: 'currency', currency }).format(value)
}

export function formatNumber(value: number): string {
  return new Intl.NumberFormat('fr-FR').format(value)
}
// "2026-06-27" -> "27/06/2026", sans passer par Date (pas de decalage de fuseau horaire)
export function formatDate(iso: string): string {
  const [year, month, day] = iso.split('-')
  return `${day}/${month}/${year}`
}