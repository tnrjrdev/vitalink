import { Activity, Mail, Lock, User, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import api from '../services/api';
import './Login.css'; // Vamos reutilizar o visual do login

const Register = () => {
    const navigate = useNavigate();
    // Nossos estados para os dados que o backend espera
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    // Para simplificar, vou deixar o perfil fixo como PROFISSIONAL, 
    // mas depois você pode criar um "Select" (dropdown) para escolher!
    const [role, setRole] = useState('ROLE_PROFESSIONAL');

    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleRegister = async (e: React.FormEvent) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        try {
            // Repare que o endpoint da API no backend é /api/v1/auth/register
            const response = await api.post('/api/v1/auth/register', {
                fullName,
                email,
                password,
                roles: [role] // O backend espera um array (lista) de roles
            });

            // Se o cadastro for um sucesso, direcionamos ele para o login
            navigate('/auth/login');
        } catch (err: any) {
            // Pegamos a mensagem de erro que o backend Java enviar (ex: "E-mail já existe")
            setError(err.response?.data?.message || 'Erro ao realizar cadastro.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <div className="login-form-side">
                <div className="login-header">
                    <div className="login-logo">
                        <Activity size={32} className="logo-icon" />
                        <span>VitaLink</span>
                    </div>
                </div>

                <div className="login-content">
                    <div className="login-title-wrapper">
                        <h1 className="login-title">Criar Conta</h1>
                        <p className="login-subtitle">Junte-se a nós e transforme sua gestão em saúde.</p>
                    </div>

                    {error && (
                        <div style={{ padding: '0.75rem', backgroundColor: '#fee2e2', color: '#ef4444', borderRadius: '8px', marginBottom: '1rem', fontSize: '0.875rem' }}>
                            {error}
                        </div>
                    )}

                    <form className="login-form" onSubmit={handleRegister}>
                        {/* Campo NOME */}
                        <div className="input-group">
                            <label className="input-label" htmlFor="fullName">Nome Completo</label>
                            <div className="input-with-icon">
                                <User size={18} className="input-icon" />
                                <input
                                    type="text"
                                    id="fullName"
                                    className="input-field pl-icon"
                                    placeholder="Seu nome completo"
                                    value={fullName}
                                    onChange={(e) => setFullName(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        {/* Campo E-MAIL */}
                        <div className="input-group">
                            <label className="input-label" htmlFor="email">E-mail</label>
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

                        {/* Campo SENHA */}
                        <div className="input-group">
                            <label className="input-label" htmlFor="password">Senha</label>
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
                            {loading ? 'Cadastrando...' : 'Criar Conta'} {!loading && <ArrowRight size={18} />}
                        </button>

                        <div className="divider" style={{ marginTop: '1.5rem', marginBottom: '1rem', textAlign: 'center' }}>
                            <span style={{ fontSize: '0.875rem', color: 'var(--color-neutral-400)' }}>
                                Já tem uma conta? <a href="/auth/login" style={{ color: 'var(--color-primary-main)', fontWeight: 600 }}>Faça Login</a>
                            </span>
                        </div>
                    </form>
                </div>
            </div>

            {/* Mesmo visual lateral */}
            <div className="login-image-side animate-fade-in">
                <div className="image-overlay">
                    <h2>Transformando o cuidado,<br />um clique por vez.</h2>
                </div>
            </div>
        </div>
    );
};

export default Register;
