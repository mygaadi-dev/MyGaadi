import { useForm } from 'react-hook-form';
import { toast } from 'react-toastify';
import api from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { Star, Send, MessageSquare } from '../../components/AppIcons.jsx';

export default function ReviewPage(){
  const f=useForm();
  const submit=async(v)=>{await api.post('/reviews',{bookingId:Number(v.bookingId),rating:Number(v.rating),comment:v.comment});toast.success('Review submitted');f.reset();};
  return <DashboardLayout title="Write Review">
    <div className="max-w-xl">
      <form onSubmit={f.handleSubmit(submit)} className="dash-card space-y-4">
        <div className="flex items-center gap-2 mb-2">
          <MessageSquare className="w-5 h-5 text-primary" />
          <h5 className="font-bold text-slate-800">Share your experience</h5>
        </div>
        <div>
          <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 block">Booking ID</label>
          <input className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Completed booking ID" {...f.register('bookingId')}/>
        </div>
        <div>
          <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 block">Rating</label>
          <div className="relative">
            <Star className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <select className="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white" {...f.register('rating')}>
              <option value="5">5 - Excellent</option>
              <option value="4">4 - Good</option>
              <option value="3">3 - Average</option>
              <option value="2">2 - Poor</option>
              <option value="1">1 - Bad</option>
            </select>
          </div>
        </div>
        <div>
          <label className="text-xs font-semibold text-slate-500 uppercase tracking-wider mb-1.5 block">Comment</label>
          <textarea className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all min-h-[120px] resize-y" placeholder="Tell us about your experience..." {...f.register('comment')}/>
        </div>
        <button className="px-6 py-2.5 rounded-xl btn-gradient font-semibold text-sm flex items-center gap-2">
          <Send className="w-4 h-4" /> Submit Review
        </button>
      </form>
    </div>
  </DashboardLayout>;
}
