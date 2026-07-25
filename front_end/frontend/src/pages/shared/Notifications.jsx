import { useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import EmptyState from '../../components/EmptyState';
import { Bell, CheckCheck } from '../../components/AppIcons.jsx';

export default function Notifications(){
  const qc = useQueryClient();
  const{data=[]}=useQuery({queryKey:['notifications'],queryFn:()=>api.get('/notifications').then(unwrap)});
  const markRead = async (id) => { await api.patch(`/notifications/${id}/read`); qc.invalidateQueries({queryKey:['notifications']}); qc.invalidateQueries({queryKey:['notificationCount']}); };
  const markAll = async () => { await api.patch('/notifications/read-all'); toast.success('Notifications marked read'); qc.invalidateQueries({queryKey:['notifications']}); qc.invalidateQueries({queryKey:['notificationCount']}); };

  return <DashboardLayout title="Notifications">
    {data.length? (
      <div className="space-y-4">
        <div className="flex justify-end">
          <button onClick={markAll} className="inline-flex items-center gap-2 px-4 py-2 rounded-xl border border-slate-200 text-sm font-bold text-slate-600 hover:bg-slate-50"><CheckCheck className="w-4 h-4" /> Mark all read</button>
        </div>
        <div className="dash-card space-y-1">
          {data.map(n=>
            <button key={n.id} onClick={()=>markRead(n.id)} className={`w-full text-left flex items-start gap-3 p-4 rounded-xl hover:bg-slate-50 transition-colors border-b border-slate-50 last:border-0 ${!n.read ? 'bg-blue-50/40' : ''}`}>
              <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center flex-shrink-0 mt-0.5">
                <Bell className="w-4 h-4 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex flex-wrap items-center gap-2 mb-1">
                  <span className="px-2 py-0.5 rounded-full bg-blue-50 text-primary text-xs font-bold">{n.notificationType}</span>
                  {!n.read && <span className="px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-700 text-[10px] font-extrabold">NEW</span>}
                  <b className="text-sm text-slate-800">{n.title}</b>
                </div>
                <p className="text-sm text-text-muted">{n.body}</p>
              </div>
            </button>
          )}
        </div>
      </div>
    ) : <EmptyState title="No notifications" />}
  </DashboardLayout>;
}
