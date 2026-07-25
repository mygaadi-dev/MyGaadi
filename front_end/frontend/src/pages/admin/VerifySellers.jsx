import { useQuery,useQueryClient } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import EmptyState from '../../components/EmptyState';
import { Check, X, FileText, ExternalLink, Shield, Image } from '../../components/AppIcons.jsx';

function parseExtra(value) {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  try { return JSON.parse(value); } catch { return []; }
}

export default function VerifySellers(){
  const qc=useQueryClient();
  const{data=[]}=useQuery({queryKey:['pendingVerifications'],queryFn:()=>api.get('/admin/verifications/pending').then(unwrap)});
  const approve=async(id)=>{await api.patch(`/admin/verifications/${id}/approve`);toast.success('Approved');qc.invalidateQueries({queryKey:['pendingVerifications']});};
  const reject=async(id)=>{const reason=prompt('Reject reason')||'Documents not clear';await api.patch(`/admin/verifications/${id}/reject`,{reason});toast.info('Rejected');qc.invalidateQueries({queryKey:['pendingVerifications']});};

  const DocLink = ({ href, label }) => href ? (
    <a href={href} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-sm text-primary font-medium hover:underline mr-3 mb-2">
      <ExternalLink className="w-3 h-3" /> {label}
    </a>
  ) : null;

  return <DashboardLayout title="Verify Sellers">
    {data.length? <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
      {data.map(v=>
        <div className="dash-card" key={v.id}>
          <div className="flex items-center gap-2 mb-3">
            <div className="w-8 h-8 rounded-lg bg-blue-50 flex items-center justify-center"><Shield className="w-4 h-4 text-primary" /></div>
            <h5 className="font-bold text-slate-800">{v.user?.name}</h5>
          </div>
          <p className="text-sm text-text-muted mb-3 flex items-center gap-1"><FileText className="w-3 h-3" /> {v.documentType}: {v.documentNumber}</p>
          <div className="mb-4 p-3 bg-slate-50 rounded-xl border border-slate-100">
            <div className="text-xs font-bold text-slate-400 uppercase tracking-wide mb-2 flex items-center gap-1"><Image className="w-3 h-3" /> Uploaded KYC files</div>
            <DocLink href={v.documentUrl} label="Front document" />
            <DocLink href={v.documentBackUrl} label="Back document" />
            <DocLink href={v.selfieUrl} label="Selfie" />
            {parseExtra(v.extraDocumentUrls).map((url, idx) => <DocLink key={url} href={url} label={`Extra ${idx + 1}`} />)}
          </div>
          <div className="flex gap-2">
            <button onClick={()=>approve(v.id)} className="px-4 py-2 rounded-lg btn-accent text-sm font-semibold flex items-center gap-1"><Check className="w-4 h-4" /> Approve</button>
            <button onClick={()=>reject(v.id)} className="px-4 py-2 rounded-lg border border-red-200 text-red-600 text-sm font-semibold hover:bg-red-50 transition-colors flex items-center gap-1"><X className="w-4 h-4" /> Reject</button>
          </div>
        </div>
      )}
    </div> : <EmptyState title="No pending KYC" />}
  </DashboardLayout>;
}
