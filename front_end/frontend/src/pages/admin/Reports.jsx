import { useQuery,useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Flag, Check, X, AlertTriangle } from '../../components/AppIcons.jsx';

export default function Reports(){
  const qc=useQueryClient();
  const{data=[]}=useQuery({queryKey:['reports'],queryFn:()=>api.get('/admin/reports').then(unwrap)});
  const resolve=async(id,dismiss=false)=>{await api.patch(`/admin/reports/${id}/resolve`,{actionTaken:dismiss?'Dismissed':'Action reviewed',dismiss});toast.success('Updated');qc.invalidateQueries({queryKey:['reports']});};

  return <DashboardLayout title="Handle Reports">
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {data.map(r=>
        <div className="dash-card" key={r.id}>
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 rounded-lg bg-amber-50 flex items-center justify-center"><Flag className="w-4 h-4 text-amber-600" /></div>
            <span className="px-2 py-1 rounded-lg bg-amber-50 text-amber-700 text-xs font-bold">{r.category}</span>
          </div>
          <p className="text-sm text-slate-700 mb-4 leading-relaxed">{r.reason}</p>
          <div className="flex gap-2">
            <button onClick={()=>resolve(r.id)} className="px-4 py-2 rounded-lg btn-gradient text-sm font-semibold flex items-center gap-1"><Check className="w-4 h-4" /> Resolve</button>
            <button onClick={()=>resolve(r.id,true)} className="px-4 py-2 rounded-lg border border-slate-200 text-slate-600 text-sm font-semibold hover:bg-slate-50 transition-colors flex items-center gap-1"><X className="w-4 h-4" /> Dismiss</button>
          </div>
        </div>
      )}
    </div>
  </DashboardLayout>;
}
