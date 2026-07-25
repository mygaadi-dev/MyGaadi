import { useQuery, useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Car, User, Tag, Trash2 } from '../../components/AppIcons.jsx';

export default function ManageListings(){
  const qc = useQueryClient();
  const{data}=useQuery({queryKey:['adminListings'],queryFn:()=>api.get('/cars/admin/all',{params:{status:'ACTIVE'}}).then(unwrap)});
  const softDelete=async(id)=>{await api.patch(`/admin/soft-delete/cars/${id}`);toast.success('Listing soft deleted');qc.invalidateQueries({queryKey:['adminListings']});};

  return <DashboardLayout title="Manage Listings">
    <div className="dash-card overflow-hidden p-0">
      <table className="data-table">
        <thead><tr><th>Title</th><th>Seller</th><th>Price</th><th>Status</th><th className="text-right">Action</th></tr></thead>
        <tbody>{data?.content?.map(c=>
          <tr key={c.id}>
            <td><div className="flex items-center gap-2"><Car className="w-4 h-4 text-primary" /> {c.title}</div></td>
            <td><div className="flex items-center gap-2"><User className="w-4 h-4 text-slate-400" /> {c.seller.name}</div></td>
            <td className="font-semibold text-accent">₹{Number(c.price).toLocaleString('en-IN')}</td>
            <td><span className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold"><Tag className="w-3 h-3" /> {c.status}</span></td>
            <td className="text-right"><button onClick={()=>softDelete(c.id)} className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-red-200 text-red-600 text-xs font-semibold hover:bg-red-50"><Trash2 className="w-3 h-3" /> Soft Delete</button></td>
          </tr>
        )}</tbody>
      </table>
    </div>
  </DashboardLayout>;
}
