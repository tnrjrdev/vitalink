import { BrowserRouter as Router, Routes, Route, Outlet } from 'react-router-dom';
import { Users, FileText, Calendar, Plus, Bell, Search, Activity } from 'lucide-react';
import Login from './pages/Login';
import Sidebar from './components/Sidebar';
import StatCard from './components/StatCard';
import PatientTimeline from './components/PatientTimeline';
import './App.css';

const Dashboard = () => (
  <div className="animate-fade-in">
    <header className="top-header">
      <div className="welcome-text stagger-1 animate-fade-in">
        <h1>Dashboard</h1>
        <p>Visão geral da sua clínica hoje, Dr. Silva.</p>
      </div>
      <div className="top-actions stagger-2 animate-fade-in">
        <div className="search-bar glass-panel" style={{ display: 'flex', alignItems: 'center', padding: '0.5rem 1rem', borderRadius: '99px', gap: '0.5rem' }}>
          <Search size={18} color="var(--color-neutral-400)" />
          <input type="text" placeholder="Buscar paciente..." style={{ border: 'none', background: 'transparent', outline: 'none', color: 'var(--color-text-main)' }} />
        </div>
        <button className="btn btn-primary">
          <Plus size={18} /> Novo Paciente
        </button>
        <button className="btn glass-panel" style={{ width: '42px', height: '42px', padding: 0, borderRadius: '50%' }}>
          <Bell size={18} />
        </button>
      </div>
    </header>

    <div className="dashboard-grid">
      <StatCard title="Total de Pacientes" value="1.248" icon={<Users size={24} />} trend={12.5} trendLabel="vs mês anterior" delayIndex={1} />
      <StatCard title="Consultas Hoje" value="14" icon={<Calendar size={24} />} trend={5.2} trendLabel="vs ontem" delayIndex={2} />
      <StatCard title="Prontuários Abertos" value="342" icon={<FileText size={24} />} trend={-2.4} trendLabel="vs semana passada" delayIndex={3} />
      <StatCard title="Taxa de Retorno" value="84%" icon={<Activity size={24} />} trend={8.1} trendLabel="vs mês anterior" delayIndex={4} />
    </div>

    <div className="dashboard-bento">
      <div className="bento-main">
        <div className="glass-panel animate-fade-in stagger-2" style={{ padding: '1.5rem', flex: 1 }}>
          <h3>Fluxo de Pacientes (Semanal)</h3>
          
          {/* Gráfico CSS puro para efeito estético rápido sem bibliotecas pesadas */}
          <div className="css-chart">
            <div className="bar hover-lift" style={{ height: '40%' }} data-value="Seg"></div>
            <div className="bar hover-lift" style={{ height: '70%' }} data-value="Ter"></div>
            <div className="bar hover-lift" style={{ height: '90%' }} data-value="Qua"></div>
            <div className="bar hover-lift" style={{ height: '50%' }} data-value="Qui"></div>
            <div className="bar hover-lift" style={{ height: '80%' }} data-value="Sex"></div>
            <div className="bar hover-lift" style={{ height: '30%', background: 'var(--color-neutral-300)' }} data-value="Sáb"></div>
            <div className="bar hover-lift" style={{ height: '10%', background: 'var(--color-neutral-300)' }} data-value="Dom"></div>
          </div>
        </div>
      </div>
      
      <div className="bento-sidebar">
        <PatientTimeline />
      </div>
    </div>
  </div>
);

const MainLayout = () => (
  <div className="app-layout">
    <Sidebar />
    <main className="main-content">
      <Outlet />
    </main>
  </div>
);

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route element={<MainLayout />}>
          <Route path="/" element={<Dashboard />} />
          {/* Outras rotas protegidas iriam aqui */}
        </Route>
      </Routes>
    </Router>
  );
}

export default App;
