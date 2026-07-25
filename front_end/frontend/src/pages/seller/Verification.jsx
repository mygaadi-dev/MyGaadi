import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { uploadFile, uploadMany } from '../../utils/upload';
import { FileText, Upload, CheckCircle, AlertTriangle, Shield, ImagePlus, ExternalLink } from '../../components/AppIcons.jsx';

export default function Verification(){
  const f=useForm({defaultValues:{documentType:'PAN'}});
  const [files, setFiles] = useState({});
  const [saving, setSaving] = useState(false);
  const{data,refetch}=useQuery({queryKey:['verification'],queryFn:()=>api.get('/verifications/me').then(unwrap),retry:false});

  const submit=async(v)=>{
    setSaving(true);
    try {
      const documentUrl = files.documentFront ? await uploadFile(files.documentFront) : v.documentUrl;
      const documentBackUrl = files.documentBack ? await uploadFile(files.documentBack) : v.documentBackUrl;
      const selfieUrl = files.selfie ? await uploadFile(files.selfie) : v.selfieUrl;
      const extraDocumentUrls = await uploadMany(files.extra || []);
      await api.post('/verifications',{...v, documentUrl, documentBackUrl, selfieUrl, extraDocumentUrls});
      toast.success('Submitted for review');
      refetch();
    } finally {
      setSaving(false);
    }
  };

  const statusColor = data?.status === 'APPROVED' ? 'bg-emerald-50 text-emerald-700' : data?.status === 'REJECTED' ? 'bg-red-50 text-red-700' : 'bg-amber-50 text-amber-700';
  const FilePicker = ({name, label, multiple=false}) => (
    <label className="block rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-4 hover:border-primary hover:bg-blue-50/40 transition-colors cursor-pointer">
      <div className="flex items-center gap-2 mb-2 text-sm font-bold text-slate-700"><ImagePlus className="w-4 h-4 text-primary" /> {label}</div>
      <input type="file" multiple={multiple} accept="image/*,.pdf" className="text-xs w-full" onChange={(e)=>setFiles(prev => ({...prev, [name]: multiple ? e.target.files : e.target.files?.[0]}))} />
    </label>
  );

  return <DashboardLayout title="Seller Verification">
    <div className="flex flex-col lg:flex-row gap-6">
      <div className="lg:w-2/5">
        <form onSubmit={f.handleSubmit(submit)} className="dash-card space-y-4">
          <div className="flex items-center gap-2 mb-2">
            <Shield className="w-5 h-5 text-primary" />
            <h5 className="font-bold text-slate-800">Submit KYC Images</h5>
          </div>
          <div className="relative">
            <FileText className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <select className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white" {...f.register('documentType')}>{['PAN','AADHAAR','PASSPORT','DL'].map(x=><option key={x}>{x}</option>)}</select>
          </div>
          <div className="relative">
            <FileText className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Document number" {...f.register('documentNumber')}/>
          </div>
          <FilePicker name="documentFront" label="Document front image / PDF" />
          <FilePicker name="documentBack" label="Document back image / PDF" />
          <FilePicker name="selfie" label="Selfie with document" />
          <FilePicker name="extra" label="Extra supporting images" multiple />
          <div className="grid grid-cols-1 gap-3 pt-2 border-t border-slate-100">
            <div className="relative">
              <Upload className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm" placeholder="Or paste document front URL" {...f.register('documentUrl')}/>
            </div>
            <div className="relative">
              <Upload className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm" placeholder="Or paste document back URL" {...f.register('documentBackUrl')}/>
            </div>
            <div className="relative">
              <Upload className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm" placeholder="Or paste selfie URL" {...f.register('selfieUrl')}/>
            </div>
          </div>
          <button disabled={saving} className="px-5 py-2.5 rounded-xl btn-gradient font-semibold text-sm disabled:opacity-60">{saving ? 'Uploading...' : 'Submit KYC'}</button>
        </form>
      </div>
      <div className="lg:w-3/5">
        <div className="dash-card">
          <div className="flex items-center gap-2 mb-4">
            <CheckCircle className="w-5 h-5 text-primary" />
            <h5 className="font-bold text-slate-800">Verification Status</h5>
          </div>
          <span className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-sm font-bold ${statusColor}`}>
            {data?.status === 'APPROVED' ? <CheckCircle className="w-4 h-4" /> : <AlertTriangle className="w-4 h-4" />}
            {data?.status || 'NOT_SUBMITTED'}
          </span>
          {data?.documentUrl && <a href={data.documentUrl} target="_blank" rel="noreferrer" className="mt-4 flex items-center gap-2 text-primary text-sm font-bold hover:underline"><ExternalLink className="w-4 h-4" /> View submitted front document</a>}
          {data?.rejectionReason&&<div className="mt-4 p-4 bg-red-50 rounded-xl border border-red-100"><p className="text-red-600 text-sm font-medium">{data.rejectionReason}</p></div>}
        </div>
      </div>
    </div>
  </DashboardLayout>;
}
