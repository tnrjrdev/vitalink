import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Users, FileText, Calendar, ArrowRight } from 'lucide-react';
import Header from './components/Header';
import './App.css';

const Dashboard = () => (
  <div className="container animate-fade-in">
    <section className="hero-section glass-panel hover-lift">
      <h1 className="hero-title">Bem-vindo ao VitaLink</h1>
      <p className="hero-subtitle">
        Gestão integrada de saúde. Acompanhe seus pacientes, gerencie prontuários e otimize seus atendimentos em uma única plataforma moderna e segura.
      </p>
      <div className="flex-center" style={{ gap: '1rem' }}>
        <button className="btn btn-primary hover-lift">
          Novo Atendimento <ArrowRight size={18} />
        </button>
        <button className="btn btn-secondary hover-lift">
          Ver Agenda
        </button>
      </div>
    </section>

    <h2 style={{ marginBottom: '1rem' }}>Visão Geral</h2>
    
    <div className="dashboard-grid">
      <div className="stat-card glass-panel hover-lift">
        <div className="stat-icon">
          <Users size={24} />
        </div>
        <div className="stat-info">
          <h3>Total de Pacientes</h3>
          <p>1,248</p>
        </div>
      </div>
      
      <div className="stat-card glass-panel hover-lift">
        <div className="stat-icon">
          <FileText size={24} />
        </div>
        <div className="stat-info">
          <h3>Prontuários Ativos</h3>
          <p>342</p>
        </div>
      </div>
      
      <div className="stat-card glass-panel hover-lift">
        <div className="stat-icon">
          <Calendar size={24} />
        </div>
        <div className="stat-info">
          <h3>Consultas Hoje</h3>
          <p>12</p>
        </div>
      </div>
    </div>
  </div>
);

function App() {
  return (
    <Router>
      <div className="app-layout">
        <Header />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            {/* Outras rotas serão adicionadas aqui */}
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
