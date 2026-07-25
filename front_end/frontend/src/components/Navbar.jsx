import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { useAuth } from '../state/AuthContext';
import api, { unwrap } from '../api/api';
import { Bell, MessageSquare, LogOut, Car, Menu, X, Search, Heart } from './AppIcons.jsx';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [mobileOpen, setMobileOpen] = useState(false);

  const doLogout = () => {
    logout();
    navigate('/');
  };

  const { data: notificationCount = { count: 0 } } = useQuery({
    queryKey: ['notificationCount'],
    queryFn: () => api.get('/notifications/unread-count').then(unwrap),
    enabled: !!user,
    refetchInterval: 30000
  });

  const { data: messageCount = { count: 0 } } = useQuery({
    queryKey: ['messageCount'],
    queryFn: () => api.get('/messages/unread-count').then(unwrap),
    enabled: !!user,
    refetchInterval: 30000
  });

  const { data: wishlistCount = { count: 0 } } = useQuery({
    queryKey: ['wishlistCount'],
    queryFn: () => api.get('/wishlist/count').then(unwrap),
    enabled: user?.role === 'BUYER' || user?.role === 'ADMIN',
    refetchInterval: 30000
  });

  const navLinkClass = ({ isActive }) => `px-3 py-2 rounded-xl text-sm font-semibold transition-all ${isActive ? 'bg-blue-50 text-blue-700 shadow-sm' : 'text-slate-600 hover:text-blue-700 hover:bg-slate-50'}`;
  const canBuy = !user || user.role === 'BUYER' || user.role === 'ADMIN';
  const showAssistant = !user || user.role !== 'ADMIN';

  const CountBadge = ({ count }) => count > 0 ? <span className="absolute -top-1 -right-1 min-w-5 h-5 px-1 rounded-full bg-red-500 text-white text-[10px] font-extrabold flex items-center justify-center shadow-sm">{count > 99 ? '99+' : count}</span> : null;

  return (
    <nav className="glass-nav sticky top-0 z-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16 gap-4">
          <Link className="brand flex items-center gap-2 whitespace-nowrap" to="/">
            <Car className="w-7 h-7 text-accent" /> MyGaadi<span>.com</span>
          </Link>

          {canBuy && <form onSubmit={(e) => { e.preventDefault(); const q = e.currentTarget.q.value.trim(); navigate(q ? `/cars?q=${encodeURIComponent(q)}` : '/cars'); }} className="hidden lg:flex flex-1 max-w-md relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
            <input name="q" className="w-full pl-9 pr-3 py-2 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-blue-600/20 focus:border-blue-600" placeholder="Search cars, brands, models" />
          </form>}

          <div className="hidden md:flex items-center gap-1">
            {canBuy && <NavLink className={navLinkClass} to="/cars">Buy Cars</NavLink>}
            {showAssistant && <NavLink className={navLinkClass} to="/assistant">AI Assistant</NavLink>}
            {user?.role === 'BUYER' && <>
              <NavLink className={navLinkClass} to="/buyer/dashboard">Dashboard</NavLink>
              <NavLink className={navLinkClass} to="/buyer/bookings">Bookings</NavLink>
              <NavLink className={navLinkClass} to="/buyer/wishlist">Wishlist</NavLink>
            </>}
            {user?.role === 'SELLER' && <>
              <NavLink className={navLinkClass} to="/seller/listings">My Listings</NavLink>
              <NavLink className={navLinkClass} to="/seller/listings/new">Add Car</NavLink>
              <NavLink className={navLinkClass} to="/seller/bookings">Requests</NavLink>
            </>}
            {user?.role === 'ADMIN' && <NavLink className={navLinkClass} to="/admin/dashboard">Admin</NavLink>}
          </div>

          <div className="hidden md:flex items-center gap-3">
            {user ? <>
              {(user.role === 'BUYER' || user.role === 'ADMIN') && <NavLink to="/buyer/wishlist" className="relative p-2 rounded-xl hover:bg-red-50 transition-colors" title="Wishlist">
                <Heart className={`w-5 h-5 ${wishlistCount.count > 0 ? 'text-red-500' : 'text-slate-600'}`} />
                <CountBadge count={wishlistCount.count} />
              </NavLink>}
              <NavLink to="/notifications" className="relative p-2 rounded-xl hover:bg-slate-100 transition-colors"><Bell className="w-5 h-5 text-slate-600" /><CountBadge count={notificationCount.count} /></NavLink>
              <NavLink to="/messages" className="relative p-2 rounded-xl hover:bg-slate-100 transition-colors"><MessageSquare className="w-5 h-5 text-slate-600" /><CountBadge count={messageCount.count} /></NavLink>
              <span className="avatar">{user.name?.charAt(0)}</span>
              <span className="text-sm font-medium text-slate-700 hidden xl:inline">{user.name}</span>
              <button onClick={doLogout} className="flex items-center gap-1 px-3 py-2 rounded-xl border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 hover:text-red-600 transition-all"><LogOut className="w-4 h-4" /> Logout</button>
            </> : <Link to="/login" className="px-5 py-2.5 rounded-xl btn-gradient text-sm font-semibold shadow-lg shadow-blue-500/20">Login</Link>}
          </div>

          <button className="md:hidden p-2 rounded-xl hover:bg-slate-100" onClick={() => setMobileOpen(!mobileOpen)}>{mobileOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}</button>
        </div>
      </div>

      {mobileOpen && <div className="md:hidden border-t border-slate-100 px-4 pb-4 space-y-1 animate-slide-in">
        {canBuy && <NavLink className={navLinkClass} to="/cars" onClick={() => setMobileOpen(false)}>Buy Cars</NavLink>}
        {showAssistant && <NavLink className={navLinkClass} to="/assistant" onClick={() => setMobileOpen(false)}>AI Assistant</NavLink>}
        {user?.role === 'BUYER' && <><NavLink className={navLinkClass} to="/buyer/dashboard" onClick={() => setMobileOpen(false)}>Dashboard</NavLink><NavLink className={navLinkClass} to="/buyer/bookings" onClick={() => setMobileOpen(false)}>Bookings</NavLink><NavLink className={navLinkClass} to="/buyer/wishlist" onClick={() => setMobileOpen(false)}>Wishlist ({wishlistCount.count})</NavLink></>}
        {user?.role === 'SELLER' && <><NavLink className={navLinkClass} to="/seller/listings" onClick={() => setMobileOpen(false)}>My Listings</NavLink><NavLink className={navLinkClass} to="/seller/listings/new" onClick={() => setMobileOpen(false)}>Add Car</NavLink><NavLink className={navLinkClass} to="/seller/bookings" onClick={() => setMobileOpen(false)}>Requests</NavLink></>}
        {user?.role === 'ADMIN' && <NavLink className={navLinkClass} to="/admin/dashboard" onClick={() => setMobileOpen(false)}>Admin</NavLink>}
        {user ? <><NavLink className={navLinkClass} to="/notifications" onClick={() => setMobileOpen(false)}>Notifications ({notificationCount.count})</NavLink><NavLink className={navLinkClass} to="/messages" onClick={() => setMobileOpen(false)}>Messages ({messageCount.count})</NavLink><button onClick={() => { doLogout(); setMobileOpen(false); }} className="w-full text-left px-3 py-2 rounded-xl text-sm font-medium text-red-600 hover:bg-red-50 transition-colors">Logout</button></> : <Link to="/login" className="block px-3 py-2 rounded-xl btn-gradient text-sm font-semibold text-center mt-2" onClick={() => setMobileOpen(false)}>Login</Link>}
      </div>}
    </nav>
  );
}
