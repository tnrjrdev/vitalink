
import './PatientTimeline.css';

interface TimelineEvent {
  id: string;
  time: string;
  patient: string;
  action: string;
  type: 'appointment' | 'record' | 'exam' | 'system';
}

const mockEvents: TimelineEvent[] = [
  { id: '1', time: '09:00', patient: 'Ana Silva', action: 'Consulta de Rotina', type: 'appointment' },
  { id: '2', time: '10:15', patient: 'Carlos Santos', action: 'Atualização de Prontuário', type: 'record' },
  { id: '3', time: '11:30', patient: 'Mariana Costa', action: 'Resultado de Exame de Sangue', type: 'exam' },
  { id: '4', time: '14:00', patient: 'Roberto Alves', action: 'Retorno Agendado', type: 'appointment' },
];

const PatientTimeline = () => {
  return (
    <div className="timeline-container glass-panel animate-fade-in stagger-3">
      <div className="timeline-header">
        <h3 className="timeline-title">Atividades Recentes</h3>
        <button className="btn-link">Ver todas</button>
      </div>
      
      <div className="timeline-list">
        {mockEvents.map((event, index) => (
          <div key={event.id} className="timeline-item" style={{ animationDelay: `${300 + index * 100}ms` }}>
            <div className="timeline-time">
              <span>{event.time}</span>
            </div>
            <div className={`timeline-indicator type-${event.type}`}>
              <div className="timeline-dot"></div>
              {index !== mockEvents.length - 1 && <div className="timeline-line"></div>}
            </div>
            <div className="timeline-content hover-lift">
              <h4>{event.patient}</h4>
              <p>{event.action}</p>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default PatientTimeline;
