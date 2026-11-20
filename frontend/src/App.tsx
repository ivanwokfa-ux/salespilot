import { Navigate, NavLink, Route, Routes } from 'react-router'
import DashboardPage from './pages/DashboardPage'
import ImportPage from './pages/ImportPage'
import ReportsPage from './pages/ReportsPage'
import SalesPage from './pages/SalesPage'

function App() {
  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">
          SalesPilot
          <small>Pilotage des ventes</small>
        </div>
        <nav className="nav">
          <NavLink to="/imports">Importer</NavLink>
          <NavLink to="/dashboard">Tableau de bord</NavLink>
          <NavLink to="/sales">Ventes</NavLink>
          <NavLink to="/reports">Rapports</NavLink>

        </nav>
      </aside>

      <main className="content">
        <Routes>
          <Route path="/imports" element={<ImportPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
          <Route path="/sales" element={<SalesPage />} />
        </Routes>
      </main>
    </div>
  )
}

export default App