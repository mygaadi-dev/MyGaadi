import { useQuery } from '@tanstack/react-query';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Users, Car, ShoppingBag, AlertTriangle, Flag, UserCheck } from '../../components/AppIcons.jsx';

export default function AdminOverview(){
  const{data={}}=useQuery({queryKey:['adminStats'],queryFn:()=>api.get('/admin/stats').then(unwrap)});
  const cards=[
    ['Users',data.users,Users,'bg-blue-50 text-primary'],
    ['Active Cars',data.activeCars,Car,'bg-emerald-50 text-accent'],
    ['Bookings',data.bookings,ShoppingBag,'bg-indigo-50 text-indigo-600'],
    ['Open Disputes',data.openDisputes,AlertTriangle,'bg-red-50 text-red-600'],
    ['Open Reports',data.openReports,Flag,'bg-amber-50 text-amber-600'],
    ['Sellers',data.sellers,UserCheck,'bg-cyan-50 text-cyan-600'],
  ];

  return <DashboardLayout title="Admin Overview">
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      {cards.map(([k,v,Icon,colors])=>
        <div className="stat-card" key={k}>
          <div className="flex items-center gap-3 mb-3">
            <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${colors}`}><Icon className="w-5 h-5" /></div>
            <h2 className="text-3xl font-extrabold text-slate-800">{v ?? 0}</h2>
          </div>
          <p className="text-text-muted text-sm">{k}</p>
        </div>
      )}
    </div>
  </DashboardLayout>;
}
