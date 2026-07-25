import { useQuery,useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { AlertTriangle, RotateCcw, CheckCircle, X } from '../../components/AppIcons.jsx';

export default function Disputes(){
  const qc=useQueryClient();
  const{data=[]}=useQuery({queryKey:['disputes'],queryFn:()=>api.get('/admin/disputes').then(unwrap)});
  const resolve=async(id,resolution)=>{await api.patch(`/admin/disputes/${id}/resolve`,{resolution,adminRemarks:`Resolved with ${resolution}`});toast.success('Resolved');qc.invalidateQueries({queryKey:['disputes']});};

  return <DashboardLayout title="Resolve Disputes">
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {data.map(d=>
        <div className="dash-card" key={d.id}>
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 rounded-lg bg-red-50 flex items-center justify-center"><AlertTriangle className="w-4 h-4 text-red-600" /></div>
            <span className="px-2 py-1 rounded-lg bg-red-50 text-red-700 text-xs font-bold">{d.status}</span>
          </div>
          <p className="text-sm text-slate-700 mb-4 leading-relaxed">{d.reason}</p>
          <div className="flex gap-2">
            <button onClick={()=>resolve(d.id,'REFUND_BUYER')} className="px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-600 text-white text-sm font-semibold transition-colors flex items-center gap-1"><RotateCcw className="w-4 h-4" /> Refund Buyer</button>
            <button onClick={()=>resolve(d.id,'RELEASE_TO_SELLER')} className="px-4 py-2 rounded-lg btn-accent text-sm font-semibold flex items-center gap-1"><CheckCircle className="w-4 h-4" /> Release Seller</button>
          </div>
        </div>
      )}
    </div>
  </DashboardLayout>;
}
