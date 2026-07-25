import { NavLink } from 'react-router-dom';
import { useAuth } from '../state/AuthContext';
import { Bell, BookOpenCheck, Car, ChartNoAxesColumn, Heart, LayoutDashboard, MessageSquare, ShieldCheck, Star, Users, Flag, AlertTriangle, UserCheck, PlusCircle } from './AppIcons.jsx';

export default function DashboardLayout({ title, children }) {
  const { user } = useAuth();
  const buyer = [
    ['/buyer/dashboard', 'Dashboard', LayoutDashboard], ['/buyer/bookings', 'Bookings', BookOpenCheck], ['/buyer/wishlist', 'Wishlist', Heart], ['/messages', 'Messages', MessageSquare], ['/notifications', 'Notifications', Bell], ['/buyer/review', 'Write Review', Star]
  ];
  const seller = [
    ['/seller/listings', 'My Listings', Car], ['/seller/listings/new', 'Add Car', PlusCircle], ['/seller/bookings', 'Booking Requests', BookOpenCheck], ['/seller/verification', 'Verification', ShieldCheck], ['/seller/earnings', 'Earnings', ChartNoAxesColumn], ['/messages', 'Messages', MessageSquare], ['/notifications', 'Notifications', Bell]
  ];
  const admin = [
    ['/admin/dashboard', 'Overview', LayoutDashboard], ['/admin/users', 'Users & Admins', Users], ['/admin/listings', 'Listings', Car], ['/admin/verify-sellers', 'Verify Sellers', UserCheck], ['/admin/reports', 'Reports', Flag], ['/admin/disputes', 'Disputes', AlertTriangle], ['/notifications', 'Notifications', Bell]
  ];
  const links = user?.role === 'ADMIN' ? admin : user?.role === 'SELLER' ? seller : buyer;
  return (
    <div className="dashboard-bg py-6">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex flex-col lg:flex-row gap-6">
          <aside className="lg:w-64 flex-shrink-0">
            <div className="sidebar">
              <div className="px-3 pb-4 mb-3 border-b border-slate-100">
                <p className="text-xs font-bold uppercase tracking-wider text-slate-400">{user?.role || 'User'} Dashboard</p>
                <p className="text-sm font-extrabold text-slate-800 mt-1 truncate">{user?.name}</p>
              </div>
              {links.map(([to, label, Icon]) => <NavLink key={to} to={to} className={({ isActive }) => `side-link ${isActive ? 'active' : ''}`}><span className="inline-flex items-center gap-2"><Icon className="w-4 h-4" />{label}</span></NavLink>)}
            </div>
          </aside>
          <main className="flex-1 min-w-0">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 mb-5">
              <div><p className="text-xs font-bold uppercase tracking-wider text-slate-400">MyGaadi.com</p><h2 className="text-2xl lg:text-3xl font-extrabold text-slate-800">{title}</h2></div>
            </div>
            {children}
          </main>
        </div>
      </div>
    </div>
  );
}
