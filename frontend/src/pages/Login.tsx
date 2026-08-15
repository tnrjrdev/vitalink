import { Activity, Mail, Lock, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import api from '../services/api';
import './Login.css';

const Login = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await api.post('/api/v1/auth/login', {
        email,
        password
      });

      const { accessToken, refreshToken } = response.data;
      if (accessToken) {
        localStorage.setItem('token', accessToken);
        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }
        navigate('/');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Erro ao realizar login. Verifique suas credenciais.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <div className="login-form-side">
        <div className="login-header animate-fade-in stagger-1">
          <div className="login-logo">
            <Activity size={32} className="logo-icon" />
            <span>VitaLink</span>
          </div>
        </div>

        <div className="login-content animate-fade-in stagger-2">
          <div className="login-title-wrapper">
            <h1 className="login-title">Bem-vindo de volta</h1>
            <p className="login-subtitle">Acesse sua plataforma de gestão integrada em saúde.</p>
          </div>

          {error && (
            <div style={{ padding: '0.75rem', backgroundColor: '#fee2e2', color: '#ef4444', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.875rem' }}>
              {error}
            </div>
          )}

          <form className="login-form" onSubmit={handleLogin}>
            <div className="input-group">
              <label className="input-label" htmlFor="email">E-mail Profissional</label>
              <div className="input-with-icon">
                <Mail size={18} className="input-icon" />
                <input 
                  type="email" 
                  id="email" 
                  className="input-field pl-icon" 
                  placeholder="dr.nome@clinica.com" 
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required 
                />
              </div>
            </div>

            <div className="input-group">
              <div className="label-row">
                <label className="input-label" htmlFor="password">Senha</label>
                <a href="#" className="forgot-password">Esqueceu a senha?</a>
              </div>
              <div className="input-with-icon">
                <Lock size={18} className="input-icon" />
                <input 
                  type="password" 
                  id="password" 
                  className="input-field pl-icon" 
                  placeholder="••••••••" 
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required 
                />
              </div>
            </div>

            <button type="submit" className="btn btn-primary login-btn" disabled={loading}>
              {loading ? 'Entrando...' : 'Entrar no sistema'} {!loading && <ArrowRight size={18} />}
            </button>

            <div className="divider">
              <span>Ou continue com</span>
            </div>

            <div className="social-login">
              <button type="button" className="btn btn-social">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                Google
              </button>
              <button type="button" className="btn btn-social">
                SSO
              </button>
            </div>
          </form>
        </div>
      </div>

      <div className="login-image-side animate-fade-in">
        <div className="image-overlay">
          <h2>Transformando o cuidado,<br/>um clique por vez.</h2>
        </div>
      </div>
    </div>
  );
};

export default Login;
