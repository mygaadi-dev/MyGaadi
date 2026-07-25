import { useQuery } from '@tanstack/react-query';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { IndianRupee, TrendingUp, Car, CheckCircle } from '../../components/AppIcons.jsx';

export default function Earnings(){
  const{data=[]}=useQuery({queryKey:['sellerBookings'],queryFn:()=>api.get('/bookings/seller/me').then(unwrap)});
  const completed=data.filter(b=>b.bookingStatus==='COMPLETED');
  const total=completed.reduce((s,b)=>s+Number(b.sellerAmount),0);

  return <DashboardLayout title="Earnings">
    <div className="flex flex-col lg:flex-row gap-6">
      <div className="lg:w-1/3">
        <div className="stat-card">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center"><IndianRupee className="w-5 h-5 text-accent" /></div>
            <h2 className="text-3xl font-extrabold text-slate-800">₹{total.toLocaleString('en-IN')}</h2>
          </div>
          <p className="text-text-muted text-sm">Released earnings</p>
          <div className="mt-4 flex items-center gap-1 text-xs text-emerald-600 font-medium">
            <TrendingUp className="w-3 h-3" /> Lifetime earnings
          </div>
        </div>
      </div>
      <div className="lg:w-2/3">
        <div className="dash-card overflow-hidden p-0">
          <table className="data-table">
            <thead><tr><th>Car</th><th>Amount</th><th>Status</th></tr></thead>
            <tbody>{completed.map(b=>
              <tr key={b.id}>
                <td><div className="flex items-center gap-2"><Car className="w-4 h-4 text-primary" /> {b.car.title}</div></td>
                <td className="font-semibold text-accent">₹{Number(b.sellerAmount).toLocaleString('en-IN')}</td>
                <td><span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold"><CheckCircle className="w-3 h-3" /> Completed</span></td>
              </tr>
            )}</tbody>
          </table>
        </div>
      </div>
    </div>
  </DashboardLayout>;
}
