import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import DashboardLayout from '../../components/DashboardLayout';
import api, { unwrap } from '../../api/api';
import { CalendarCheck, Heart, Bell, MessageSquare, Car, ArrowRight, ShieldCheck, CreditCard } from '../../components/AppIcons.jsx';

export default function BuyerDashboard(){
  const { data: bookings = [] } = useQuery({queryKey:['buyerBookings'],queryFn:()=>api.get('/bookings/buyer/me').then(unwrap)});
  const { data: wishlistCount = { count: 0 } } = useQuery({queryKey:['wishlistCount'],queryFn:()=>api.get('/wishlist/count').then(unwrap)});
  const { data: notifications = { count: 0 } } = useQuery({queryKey:['notificationCount'],queryFn:()=>api.get('/notifications/unread-count').then(unwrap)});
  const { data: messages = { count: 0 } } = useQuery({queryKey:['messageCount'],queryFn:()=>api.get('/messages/unread-count').then(unwrap)});
  const recent = bookings.slice(0, 3);
  const confirmed = bookings.filter(b=>['CONFIRMED','COMPLETED'].includes(b.bookingStatus)).length;

  const cards = [
    ['Bookings', bookings.length, CalendarCheck, 'bg-blue-50 text-primary', '/buyer/bookings'],
    ['Wishlist', wishlistCount.count, Heart, 'bg-red-50 text-red-600', '/buyer/wishlist'],
    ['Unread alerts', notifications.count, Bell, 'bg-amber-50 text-amber-600', '/notifications'],
    ['Messages', messages.count, MessageSquare, 'bg-emerald-50 text-accent', '/messages'],
  ];

  return <DashboardLayout title="Buyer Dashboard">
    <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5 mb-6">
      {cards.map(([label,value,Icon,cls,to]) => <Link to={to} key={label} className="stat-card group">
        <div className="flex items-center justify-between mb-4"><div className={`w-11 h-11 rounded-2xl flex items-center justify-center ${cls}`}><Icon className="w-5 h-5" /></div><ArrowRight className="w-4 h-4 text-slate-300 group-hover:text-primary transition-colors" /></div>
        <h3 className="text-3xl font-extrabold text-slate-900">{value ?? 0}</h3><p className="text-sm text-text-muted font-semibold">{label}</p>
      </Link>)}
    </div>

    <div className="grid lg:grid-cols-3 gap-6">
      <div className="lg:col-span-2 dash-card">
        <div className="flex items-center justify-between mb-4"><h3 className="font-extrabold text-slate-900">Recent Bookings</h3><Link to="/buyer/bookings" className="text-sm font-bold text-primary">View all</Link></div>
        {recent.length === 0 ? <div className="rounded-2xl border border-dashed border-slate-200 p-8 text-center"><Car className="w-10 h-10 text-slate-300 mx-auto mb-2" /><p className="text-sm text-slate-500">No bookings yet. Browse cars and book your first vehicle.</p><Link to="/cars" className="inline-flex mt-4 px-4 py-2 rounded-xl btn-gradient text-sm font-bold">Browse Cars</Link></div> : <div className="space-y-3">{recent.map(b => <div key={b.id} className="flex items-center gap-4 p-4 rounded-2xl bg-slate-50 border border-slate-100"><div className="w-12 h-12 rounded-xl bg-white flex items-center justify-center"><Car className="w-5 h-5 text-primary" /></div><div className="flex-1"><p className="font-bold text-slate-800">{b.car.title}</p><p className="text-xs text-text-muted">₹{Number(b.amount).toLocaleString('en-IN')} • {b.escrowStatus}</p></div><span className="px-3 py-1 rounded-full bg-blue-50 text-primary text-xs font-extrabold">{b.bookingStatus}</span></div>)}</div>}
      </div>

      <div className="space-y-5">
        <div className="dash-card bg-gradient-to-br from-slate-900 to-blue-900 text-white">
          <ShieldCheck className="w-8 h-8 text-emerald-300 mb-4" />
          <h3 className="text-xl font-extrabold mb-2">Safe buying checklist</h3>
          <p className="text-sm text-blue-100 mb-4">Pay only through Razorpay and release escrow after both buyer and seller confirm.</p>
          <div className="text-sm space-y-2"><p>✓ Verified seller badge</p><p>✓ Chat before booking</p><p>✓ Review car images carefully</p></div>
        </div>
        <div className="dash-card">
          <div className="flex items-center gap-3"><div className="w-10 h-10 rounded-xl bg-emerald-50 text-accent flex items-center justify-center"><CreditCard className="w-5 h-5" /></div><div><h3 className="font-extrabold text-slate-900">Confirmed deals</h3><p className="text-sm text-text-muted">{confirmed} bookings confirmed/completed</p></div></div>
        </div>
      </div>
    </div>
  </DashboardLayout>;
}
