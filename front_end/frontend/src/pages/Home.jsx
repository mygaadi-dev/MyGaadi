import { Link, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import api, { unwrap } from '../api/api';
import CarCard from '../components/CarCard';
import LoadingSkeleton from '../components/LoadingSkeleton';
import { Search, Shield, Users, Car, TrendingUp, ArrowRight } from '../components/AppIcons.jsx';

export default function Home() {
  const nav = useNavigate();
  const { data: cars, isLoading } = useQuery({ queryKey: ['featured'], queryFn: () => api.get('/cars/featured').then(unwrap) });
  const search = (e) => { e.preventDefault(); const q = new FormData(e.currentTarget).get('q'); nav(`/cars?q=${encodeURIComponent(q)}`); };

  return (
    <div className="page-fade">
      <section className="hero-gradient py-12 lg:py-20 rounded-b-[36px] relative overflow-hidden">
        <div className="absolute inset-0 opacity-30">
          <div className="absolute top-20 left-10 w-72 h-72 bg-blue-400/20 rounded-full blur-3xl"></div>
          <div className="absolute bottom-10 right-10 w-96 h-96 bg-emerald-400/10 rounded-full blur-3xl"></div>
        </div>
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
          <div className="flex flex-col lg:flex-row items-center gap-10 lg:gap-16">
            <div className="lg:w-3/5">
              <div className="inline-flex items-center gap-2 bg-white/10 backdrop-blur-sm border border-white/20 rounded-full px-4 py-2 text-sm text-emerald-300 mb-6">
                <Shield className="w-4 h-4" /> Verified sellers • Secure escrow • Real deals
              </div>
              <h1 className="text-4xl lg:text-6xl font-extrabold text-white leading-tight mb-6">
                Find your next <span className="text-emerald-300">pre-owned car</span> without tension.
              </h1>
              <p className="text-lg text-blue-100/80 mb-8 max-w-xl">
                Browse verified listings, chat with sellers, book with escrow, and drive home confidently.
              </p>
              <form onSubmit={search} className="search-glow p-2 flex gap-2 max-w-xl">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                  <input name="q" className="w-full pl-10 pr-4 py-3 rounded-xl border-0 outline-none text-slate-700 placeholder:text-slate-400" placeholder="Search Creta, Baleno, Pune..." />
                </div>
                <button className="px-6 py-3 rounded-xl btn-gradient font-semibold whitespace-nowrap">Search</button>
              </form>
              <div className="flex flex-wrap gap-2 mt-6">
                {['Hyundai','Maruti Suzuki','Honda','Tata','Mahindra'].map(x => (
                  <Link key={x} to={`/cars?brand=${x}`} className="px-4 py-2 rounded-full bg-white/10 backdrop-blur-sm border border-white/20 text-white text-sm font-medium hover:bg-white/20 transition-colors">
                    {x}
                  </Link>
                ))}
              </div>
            </div>
            <div className="lg:w-2/5">
              <div className="glass-card rounded-2xl p-8 text-center animate-float">
                <div className="text-6xl mb-4">🚘</div>
                <h3 className="text-2xl font-bold text-white mb-2">MyGaadi Promise</h3>
                <p className="text-blue-100/70">Clean UI, verified seller flow, role dashboards, and escrow booking logic.</p>
                <div className="mt-6 grid grid-cols-3 gap-3">
                  <div className="bg-white/10 rounded-xl p-3"><div className="text-xl font-bold text-white">2.5k+</div><div className="text-xs text-blue-200">Cars</div></div>
                  <div className="bg-white/10 rounded-xl p-3"><div className="text-xl font-bold text-white">800+</div><div className="text-xs text-blue-200">Sellers</div></div>
                  <div className="bg-white/10 rounded-xl p-3"><div className="text-xl font-bold text-white">10k+</div><div className="text-xs text-blue-200">Happy</div></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
          <div className="stat-card group">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center"><Car className="w-5 h-5 text-primary" /></div>
              <h2 className="text-3xl font-extrabold text-slate-800">2,500+</h2>
            </div>
            <p className="text-text-muted text-sm">Total cars listed on the platform</p>
          </div>
          <div className="stat-card group">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 rounded-xl bg-emerald-50 flex items-center justify-center"><Shield className="w-5 h-5 text-accent" /></div>
              <h2 className="text-3xl font-extrabold text-slate-800">800+</h2>
            </div>
            <p className="text-text-muted text-sm">Verified sellers you can trust</p>
          </div>
          <div className="stat-card group">
            <div className="flex items-center gap-3 mb-3">
              <div className="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center"><Users className="w-5 h-5 text-indigo-600" /></div>
              <h2 className="text-3xl font-extrabold text-slate-800">10k+</h2>
            </div>
            <p className="text-text-muted text-sm">Happy customers across India</p>
          </div>
        </div>

        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-extrabold text-slate-800">Featured Cars</h2>
            <p className="text-text-muted text-sm mt-1">Handpicked deals for you</p>
          </div>
          <Link to="/cars" className="flex items-center gap-1 text-sm font-semibold text-primary hover:text-primary-light transition-colors">
            View all <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
        {isLoading ? <LoadingSkeleton /> : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            {cars?.map(car => <CarCard car={car} key={car.id} />)}
          </div>
        )}
      </section>
    </div>
  );
}
