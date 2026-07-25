import { useQuery,useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Lock, Unlock, Mail, User, Trash2, UserPlus, ShieldCheck } from '../../components/AppIcons.jsx';

export default function ManageUsers(){
  const qc=useQueryClient();
  const f = useForm({ defaultValues: { name:'', email:'', phone:'', password:'', department:'Operations' } });
  const{data}=useQuery({queryKey:['users'],queryFn:()=>api.get('/admin/users').then(unwrap)});
  const action=async(id,type)=>{await api.patch(`/admin/users/${id}/${type}`);toast.success('Updated');qc.invalidateQueries({queryKey:['users']});};
  const softDelete=async(id)=>{await api.patch(`/admin/soft-delete/users/${id}`);toast.success('User soft deleted');qc.invalidateQueries({queryKey:['users']});};
  const createAdmin=async(v)=>{await api.post('/admin/admins', v);toast.success('New admin created and created-by record stored');f.reset({ name:'', email:'', phone:'', password:'', department:'Operations' });qc.invalidateQueries({queryKey:['users']});};

  return <DashboardLayout title="Manage Users & Admins">
    <div className="dash-card mb-6">
      <div className="flex items-center gap-3 mb-4"><div className="w-10 h-10 rounded-xl bg-blue-50 text-primary flex items-center justify-center"><UserPlus className="w-5 h-5" /></div><div><h3 className="font-extrabold text-slate-900">Create New Admin</h3><p className="text-sm text-text-muted">Admin profile stores which admin created this account.</p></div></div>
      <form onSubmit={f.handleSubmit(createAdmin)} className="grid md:grid-cols-5 gap-3">
        <input className="px-4 py-3 rounded-xl border border-slate-200 text-sm" placeholder="Name" {...f.register('name',{required:true})}/>
        <input className="px-4 py-3 rounded-xl border border-slate-200 text-sm" placeholder="Email" {...f.register('email',{required:true})}/>
        <input className="px-4 py-3 rounded-xl border border-slate-200 text-sm" placeholder="Phone" {...f.register('phone',{required:true})}/>
        <input className="px-4 py-3 rounded-xl border border-slate-200 text-sm" placeholder="Password" type="password" {...f.register('password',{required:true})}/>
        <button className="rounded-xl btn-gradient text-sm font-bold flex items-center justify-center gap-2"><ShieldCheck className="w-4 h-4" /> Add Admin</button>
      </form>
    </div>

    <div className="dash-card overflow-hidden p-0">
      <div className="overflow-x-auto">
        <table className="data-table">
          <thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Status</th><th>KYC</th><th className="text-right">Action</th></tr></thead>
          <tbody>{data?.content?.map(u=>
            <tr key={u.id}>
              <td><div className="flex items-center gap-2"><User className="w-4 h-4 text-primary" /> {u.name}</div></td>
              <td><div className="flex items-center gap-2"><Mail className="w-4 h-4 text-slate-400" /> {u.email}</div></td>
              <td><span className="px-2 py-1 rounded-lg bg-slate-50 text-slate-600 text-xs font-bold">{u.role}</span></td>
              <td><span className={`px-2 py-1 rounded-lg text-xs font-bold ${u.status==='BLOCKED'?'bg-red-50 text-red-600':'bg-emerald-50 text-emerald-600'}`}>{u.status}</span></td>
              <td><span className={`px-2 py-1 rounded-lg text-xs font-bold ${u.kycVerified?'bg-emerald-50 text-emerald-600':'bg-slate-50 text-slate-500'}`}>{u.kycVerified?'VERIFIED':'-'}</span></td>
              <td className="text-right"><div className="inline-flex items-center gap-2">
                {u.status==='BLOCKED'? <button onClick={()=>action(u.id,'unblock')} className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg btn-accent text-xs font-semibold"><Unlock className="w-3 h-3" /> Unblock</button> : <button onClick={()=>action(u.id,'block')} className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-amber-200 text-amber-700 text-xs font-semibold hover:bg-amber-50 transition-colors"><Lock className="w-3 h-3" /> Block</button>}
                <button onClick={()=>softDelete(u.id)} className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg border border-red-200 text-red-600 text-xs font-semibold hover:bg-red-50 transition-colors"><Trash2 className="w-3 h-3" /> Soft Delete</button>
              </div></td>
            </tr>
          )}</tbody>
        </table>
      </div>
    </div>
  </DashboardLayout>;
}
