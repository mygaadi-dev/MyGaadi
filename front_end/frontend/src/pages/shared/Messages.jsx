import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Send, MessageSquare, User, ArrowRight } from '../../components/AppIcons.jsx';

export default function Messages(){
  const qc=useQueryClient();
  const f=useForm();
  const{data=[]}=useQuery({queryKey:['messages'],queryFn:()=>api.get('/messages').then(unwrap)});
  const submit=async(v)=>{await api.post('/messages',{receiverId:Number(v.receiverId),content:v.content,messageType:'TEXT'});toast.success('Sent');f.reset();qc.invalidateQueries({queryKey:['messages']});};

  return <DashboardLayout title="Messages">
    <div className="flex flex-col lg:flex-row gap-6">
      <div className="lg:w-2/5">
        <form onSubmit={f.handleSubmit(submit)} className="dash-card space-y-4">
          <div className="flex items-center gap-2 mb-2">
            <Send className="w-5 h-5 text-primary" />
            <h5 className="font-bold text-slate-800">New Message</h5>
          </div>
          <div className="relative">
            <User className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Receiver user ID" {...f.register('receiverId')}/>
          </div>
          <div className="relative">
            <MessageSquare className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
            <textarea className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all min-h-[100px] resize-y" placeholder="Message" {...f.register('content')}/>
          </div>
          <button className="px-5 py-2.5 rounded-xl btn-gradient font-semibold text-sm flex items-center gap-2">
            <Send className="w-4 h-4" /> Send
          </button>
        </form>
      </div>
      <div className="lg:w-3/5">
        <div className="dash-card space-y-1 max-h-[500px] overflow-y-auto">
          {data.length===0&&<div className="text-center py-8 text-text-muted text-sm">No messages yet</div>}
          {data.map(m=>
            <div key={m.id} className="flex items-start gap-3 p-3 rounded-xl hover:bg-slate-50 transition-colors">
              <span className="avatar">{m.sender?.name?.[0]}</span>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-1 text-sm font-semibold text-slate-700">
                  {m.sender?.name} <ArrowRight className="w-3 h-3 text-slate-400" /> {m.receiver?.name}
                </div>
                <p className="text-sm text-slate-600 mt-0.5">{m.content}</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  </DashboardLayout>;
}
