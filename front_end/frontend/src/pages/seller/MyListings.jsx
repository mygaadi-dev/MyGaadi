import { Link } from 'react-router-dom';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Plus, Trash2, Car, Tag } from '../../components/AppIcons.jsx';

export default function MyListings(){
  const qc=useQueryClient();
  const{data}=useQuery({queryKey:['myListings'],queryFn:()=>api.get('/cars/seller/me').then(unwrap)});
  const remove=async(id)=>{await api.delete(`/cars/${id}`);toast.success('Removed');qc.invalidateQueries({queryKey:['myListings']});};

  return <DashboardLayout title="My Listings">
    <Link to="/seller/listings/new" className="inline-flex items-center gap-2 px-5 py-2.5 rounded-xl btn-gradient font-semibold text-sm mb-5">
      <Plus className="w-4 h-4" /> Add Car
    </Link>
    <div className="dash-card overflow-hidden p-0">
      <table className="data-table">
        <thead><tr><th>Car</th><th>Price</th><th>Status</th><th className="text-right">Action</th></tr></thead>
        <tbody>{data?.content?.map(c=>
          <tr key={c.id}>
            <td><div className="flex items-center gap-2"><Car className="w-4 h-4 text-primary" /> {c.title}</div></td>
            <td className="font-semibold">₹{Number(c.price).toLocaleString('en-IN')}</td>
            <td><span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold"><Tag className="w-3 h-3" /> {c.status}</span></td>
            <td className="text-right"><button onClick={()=>remove(c.id)} className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-red-200 text-red-600 text-xs font-semibold hover:bg-red-50 transition-colors"><Trash2 className="w-3 h-3" /> Delete</button></td>
          </tr>
        )}</tbody>
      </table>
    </div>
  </DashboardLayout>;
}
