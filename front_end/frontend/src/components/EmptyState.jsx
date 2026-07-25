import { Road } from './AppIcons.jsx';

export default function EmptyState({ title = 'Nothing here yet', text = 'Your data will appear here when available.' }) {
  return (
    <div className="empty-state">
      <div className="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-blue-50 to-indigo-50 mb-4">
        <Road className="w-10 h-10 text-primary" />
      </div>
      <h4 className="text-lg font-bold text-slate-700 mb-1">{title}</h4>
      <p className="text-text-muted text-sm">{text}</p>
    </div>
  );
}
