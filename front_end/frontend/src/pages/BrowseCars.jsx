import { useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import api, { unwrap } from '../api/api';
import CarCard from '../components/CarCard';
import LoadingSkeleton from '../components/LoadingSkeleton';
import EmptyState from '../components/EmptyState';
import { Search, SlidersHorizontal, Fuel, Settings2, MapPin, Banknote } from '../components/AppIcons.jsx';

export default function BrowseCars() {
  const [params, setParams] = useSearchParams();
  const query = Object.fromEntries(params.entries());
  const { data, isLoading } = useQuery({ queryKey: ['cars', query], queryFn: () => api.get('/cars', { params: query }).then(unwrap) });
  const submit = (e) => { e.preventDefault(); setParams(Object.fromEntries(new FormData(e.currentTarget).entries())); };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 page-fade">
      <div className="flex flex-col lg:flex-row gap-8">
        <aside className="lg:w-72 flex-shrink-0">
          <form onSubmit={submit} className="filter-card sticky top-24">
            <div className="flex items-center gap-2 mb-5">
              <SlidersHorizontal className="w-5 h-5 text-primary" />
              <h5 className="font-bold text-slate-800">Filters</h5>
            </div>
            <div className="space-y-3">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input name="q" defaultValue={query.q || ''} className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Search keyword" />
              </div>
              <div className="relative">
                <Settings2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input name="brand" defaultValue={query.brand || ''} className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Brand" />
              </div>
              <div className="grid grid-cols-2 gap-2">
                <div className="relative">
                  <Banknote className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                  <input name="minPrice" className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Min ₹" />
                </div>
                <input name="maxPrice" className="w-full px-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="Max ₹" />
              </div>
              <div className="relative">
                <Fuel className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <select name="fuelType" className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white">
                  <option value="">Fuel type</option>
                  {['PETROL','DIESEL','CNG','ELECTRIC','HYBRID'].map(x => <option key={x}>{x}</option>)}
                </select>
              </div>
              <div className="relative">
                <Settings2 className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <select name="transmission" className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white">
                  <option value="">Transmission</option>
                  {['MANUAL','AUTOMATIC','AMT','DCT'].map(x => <option key={x}>{x}</option>)}
                </select>
              </div>
              <div className="relative">
                <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                <input name="city" className="w-full pl-9 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder="City" />
              </div>
              <button className="w-full py-2.5 rounded-xl btn-gradient font-semibold text-sm mt-2">Apply Filters</button>
            </div>
          </form>
        </aside>
        <main className="flex-1 min-w-0">
          <div className="mb-5">
            <h2 className="text-2xl font-extrabold text-slate-800">Browse Cars</h2>
            <p className="text-text-muted text-sm mt-1">{data?.content?.length ?? 0} results found</p>
          </div>
          {isLoading ? <LoadingSkeleton/> : data?.content?.length ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">
              {data.content.map(car => <CarCard car={car} key={car.id} />)}
            </div>
          ) : <EmptyState title="No cars found" text="Try changing your filters or search keywords."/>}
        </main>
      </div>
    </div>
  );
}
