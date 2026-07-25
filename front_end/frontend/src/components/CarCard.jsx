import { useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import api, { unwrap } from '../api/api';
import { useAuth } from '../state/AuthContext';
import { Heart, Fuel, Gauge, Calendar, MapPin, ShieldCheck } from './AppIcons.jsx';
import { fallbackForCar, primaryCarImage } from '../utils/carImages';

export default function CarCard({ car }) {
  const { user } = useAuth();
  const qc = useQueryClient();
  const [image, setImage] = useState(() => primaryCarImage(car));
  const canWishlist = user?.role === 'BUYER' || user?.role === 'ADMIN';
  const { data: savedIds = [] } = useQuery({
    queryKey: ['wishlistIds'],
    queryFn: () => api.get('/wishlist/ids').then(unwrap),
    enabled: canWishlist,
  });
  const saved = savedIds.includes(car.id);

  const save = async (event) => {
    event.preventDefault();
    if (!user) return toast.info('Login as buyer to save cars');
    if (user.role === 'SELLER') return toast.info('Seller accounts cannot buy or wishlist cars');
    await api.post(`/wishlist/${car.id}`);
    toast.success(saved ? 'Removed from wishlist' : 'Added to wishlist');
    qc.invalidateQueries({ queryKey: ['wishlistIds'] });
    qc.invalidateQueries({ queryKey: ['wishlistCount'] });
  };

  return (
    <Link to={`/cars/${car.id}`} className="block group">
      <div className="car-card h-full flex flex-col">
        <div className="car-img-wrap">
          <img
            src={image}
            className="car-img"
            alt={car.title}
            loading="lazy"
            onError={() => setImage(fallbackForCar(car))}
          />
          <span className="price-badge">₹{Number(car.price).toLocaleString('en-IN')}</span>
          {canWishlist && (
            <button className="heart-btn" onClick={save} title={saved ? 'Saved' : 'Save to wishlist'}>
              <Heart className={`w-5 h-5 transition-colors ${saved ? 'text-red-500 fill-red-500' : 'text-slate-400 hover:text-red-500'}`} />
            </button>
          )}
        </div>
        <div className="p-4 flex-1 flex flex-col">
          <div className="flex items-start justify-between gap-2 mb-1">
            <h5 className="font-bold text-slate-800 text-base group-hover:text-primary transition-colors">{car.title}</h5>
            {car.seller?.kycVerified && (
              <span title="Verified seller" className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-emerald-50 text-emerald-700 text-[10px] font-extrabold">
                <ShieldCheck className="w-3 h-3" />Verified
              </span>
            )}
          </div>
          <div className="text-text-muted text-xs mb-3 flex items-center gap-1">
            <MapPin className="w-3 h-3" /> {car.locationCity}, {car.locationState}
          </div>
          <div className="flex flex-wrap gap-2 mt-auto">
            <span className="chip flex items-center gap-1"><Fuel className="w-3 h-3" /> {car.fuelType}</span>
            <span className="chip">{car.transmission}</span>
            <span className="chip flex items-center gap-1"><Gauge className="w-3 h-3" /> {Number(car.mileageKm).toLocaleString('en-IN')} km</span>
            <span className="chip flex items-center gap-1"><Calendar className="w-3 h-3" /> {car.year}</span>
          </div>
        </div>
      </div>
    </Link>
  );
}
