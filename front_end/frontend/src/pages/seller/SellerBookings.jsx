import { useQuery,useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Car, Check, X, Handshake, Clock } from '../../components/AppIcons.jsx';

export default function SellerBookings(){
  const qc=useQueryClient();
  const{data=[]}=useQuery({queryKey:['sellerBookings'],queryFn:()=>api.get('/bookings/seller/me').then(unwrap)});
  const act=async(id,path)=>{await api.post(`/bookings/${id}/${path}`);toast.success('Updated');qc.invalidateQueries({queryKey:['sellerBookings']});};

  return <DashboardLayout title="Booking Requests">
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {data.map(b=>
        <div className="dash-card" key={b.id}>
          <div className="flex items-center gap-2 mb-2">
            <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center"><Car className="w-4 h-4 text-primary" /></div>
            <h5 className="font-bold text-slate-800">{b.car.title}</h5>
          </div>
          <p className="text-sm text-text-muted mb-3">{b.buyer.name} • ₹{Number(b.amount).toLocaleString('en-IN')}</p>
          <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-lg bg-blue-50 text-blue-700 text-xs font-bold mb-4"><Clock className="w-3 h-3" /> {b.bookingStatus}</span>
          <div className="flex gap-2">
            {b.bookingStatus==='INITIATED'&&<>
              <button onClick={()=>act(b.id,'accept')} className="px-4 py-2 rounded-lg btn-accent text-sm font-semibold flex items-center gap-1"><Check className="w-4 h-4" /> Accept</button>
              <button onClick={()=>act(b.id,'reject')} className="px-4 py-2 rounded-lg border border-red-200 text-red-600 text-sm font-semibold hover:bg-red-50 transition-colors flex items-center gap-1"><X className="w-4 h-4" /> Reject</button>
            </>}
            {b.bookingStatus==='CONFIRMED'&&!b.sellerConfirmed&&<button onClick={()=>act(b.id,'seller-confirm')} className="px-4 py-2 rounded-lg btn-gradient text-sm font-semibold flex items-center gap-1"><Handshake className="w-4 h-4" /> Confirm Handover</button>}
          </div>
        </div>
      )}
    </div>
  </DashboardLayout>;
}
