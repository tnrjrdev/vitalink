import { NavLink } from 'react-router-dom';
import { Activity, LayoutDashboard, Users, FileText, Calendar, Settings, LogOut } from 'lucide-react';
import './Sidebar.css';

const Sidebar = () => {
  return (
    <aside className="sidebar glass-panel">
      <div className="sidebar-header">
        <Activity className="sidebar-logo-icon" size={32} />
        <span className="sidebar-logo-text">VitaLink</span>
      </div>

      <div className="sidebar-nav-group">
        <p className="sidebar-nav-title">Menu Principal</p>
        <nav className="sidebar-nav">
          <NavLink to="/" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <LayoutDashboard size={20} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink to="/pacientes" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Users size={20} />
            <span>Pacientes</span>
          </NavLink>
          <NavLink to="/prontuarios" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <FileText size={20} />
            <span>Prontuários</span>
          </NavLink>
          <NavLink to="/agenda" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
            <Calendar size={20} />
            <span>Agenda</span>
          </NavLink>
        </nav>
      </div>

      <div className="sidebar-footer">
        <NavLink to="/configuracoes" className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}>
          <Settings size={20} />
          <span>Ajustes</span>
        </NavLink>
        <button className="sidebar-link logout-btn">
          <LogOut size={20} />
          <span>Sair</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
