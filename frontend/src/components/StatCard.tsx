import type { ReactNode } from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';
import './StatCard.css';

interface StatCardProps {
  title: string;
  value: string;
  icon: ReactNode;
  trend?: number;
  trendLabel?: string;
  delayIndex?: number;
}

const StatCard = ({ title, value, icon, trend, trendLabel, delayIndex = 1 }: StatCardProps) => {
  const isPositive = trend && trend >= 0;

  return (
    <div className={`stat-card glass-panel hover-lift animate-fade-in stagger-${delayIndex}`}>
      <div className="stat-card-header">
        <div className="stat-card-info">
          <h3 className="stat-card-title">{title}</h3>
          <p className="stat-card-value">{value}</p>
        </div>
        <div className="stat-card-icon">
          {icon}
        </div>
      </div>
      
      {trend !== undefined && (
        <div className="stat-card-footer">
          <div className={`trend-badge ${isPositive ? 'positive' : 'negative'}`}>
            {isPositive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
            <span>{Math.abs(trend)}%</span>
          </div>
          {trendLabel && <span className="trend-label">{trendLabel}</span>}
        </div>
      )}
    </div>
  );
};

export default StatCard;
