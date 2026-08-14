import { Activity, User, Bell, Menu } from 'lucide-react';
import './Header.css';

const Header = () => {
  return (
    <header className="header glass-panel">
      <div className="container header-content">
        <div className="header-logo hover-lift">
          <Activity className="logo-icon" size={28} />
          <span className="logo-text">VitaLink</span>
        </div>
        
        <nav className="header-nav">
          <a href="#" className="nav-link active">Dashboard</a>
          <a href="#" className="nav-link">Pacientes</a>
          <a href="#" className="nav-link">Prontuários</a>
          <a href="#" className="nav-link">Configurações</a>
        </nav>
        
        <div className="header-actions">
          <button className="action-btn hover-lift" aria-label="Notificações">
            <Bell size={20} />
            <span className="notification-badge"></span>
          </button>
          <button className="action-btn hover-lift profile-btn">
            <User size={20} />
          </button>
          <button className="action-btn mobile-menu">
            <Menu size={24} />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Header;
