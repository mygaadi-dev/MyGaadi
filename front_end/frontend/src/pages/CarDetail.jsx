import { useEffect, useMemo, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { toast } from 'react-toastify';
import api, { unwrap } from '../api/api';
import { useAuth } from '../state/AuthContext';
import { MessageCircle, ShoppingBag, Fuel, Calendar, Gauge, Settings, Users, MapPin, CheckCircle } from '../components/AppIcons.jsx';
import { fallbackForCar, getCarImages } from '../utils/carImages';

export default function CarDetail() {
  const { id } = useParams();
  const nav = useNavigate();
  const { user } = useAuth();
  const [selectedImage, setSelectedImage] = useState(0);
  const { data: car, isLoading } = useQuery({
    queryKey: ['car', id],
    queryFn: () => api.get(`/cars/${id}`).then(unwrap),
  });

  const images = useMemo(() => getCarImages(car), [car]);
  useEffect(() => setSelectedImage(0), [id]);

  if (isLoading) return <div className="max-w-7xl mx-auto px-4 py-20 text-center text-slate-400">Loading...</div>;

  const book = async () => {
    if (!user) return nav('/login');
    if (user.role === 'SELLER') return toast.info('Seller accounts cannot book cars');
    await api.post('/bookings', { carId: car.id, notes: 'Interested in this car' });
    toast.success('Booking request sent');
    nav('/buyer/bookings');
  };

  const message = async () => {
    if (!user) return nav('/login');
    await api.post('/messages', { receiverId: car.seller.id, carId: car.id, content: `Hi, I am interested in ${car.title}` });
    toast.success('Message sent');
    nav('/messages');
  };

  const specs = [
    ['Brand', car.brand, null],
    ['Model', car.model, null],
    ['Year', car.year, Calendar],
    ['Fuel', car.fuelType, Fuel],
    ['Transmission', car.transmission, Settings],
    ['Mileage', `${Number(car.mileageKm).toLocaleString('en-IN')} km`, Gauge],
    ['Engine', `${car.engineCc || '-'} cc`, null],
    ['Owners', car.noOfOwners, Users],
    ['Location', `${car.locationCity}, ${car.locationState}`, MapPin],
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 page-fade">
      <div className="flex flex-col lg:flex-row gap-8">
        <div className="lg:w-2/3">
          <div className="dash-card overflow-hidden p-0">
            <div className="bg-slate-100">
              <img
                src={images[selectedImage]}
                className="w-full h-[430px] object-cover"
                alt={`${car.title} view ${selectedImage + 1}`}
                onError={(event) => { event.currentTarget.src = fallbackForCar(car); }}
              />
            </div>
            {images.length > 1 && (
              <div className="flex gap-3 overflow-x-auto p-4 border-b border-slate-100 bg-white">
                {images.map((image, index) => (
                  <button
                    type="button"
                    key={`${image}-${index}`}
                    onClick={() => setSelectedImage(index)}
                    className={`flex-none w-28 h-20 rounded-xl overflow-hidden border-2 transition-all ${selectedImage === index ? 'border-primary shadow-md' : 'border-transparent opacity-75 hover:opacity-100'}`}
                  >
                    <img src={image} alt={`${car.title} thumbnail ${index + 1}`} className="w-full h-full object-cover" onError={(event) => { event.currentTarget.src = fallbackForCar(car); }} />
                  </button>
                ))}
              </div>
            )}
            <div className="p-6 lg:p-8">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-4">
                <h2 className="text-2xl lg:text-3xl font-extrabold text-slate-800">{car.title}</h2>
                <span className="text-2xl font-extrabold text-accent">₹{Number(car.price).toLocaleString('en-IN')}</span>
              </div>
              <p className="text-text-muted mb-6 leading-relaxed">{car.description}</p>
              <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
                {specs.map(([key, value, Icon]) => (
                  <div key={key} className="bg-slate-50 rounded-xl p-4">
                    <div className="text-xs text-slate-400 font-medium mb-1">{key}</div>
                    <div className="text-sm font-semibold text-slate-700 flex items-center gap-1.5">
                      {Icon && <Icon className="w-4 h-4 text-primary" />} {value}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
        <div className="lg:w-1/3">
          <div className="dash-card sticky top-24">
            <div className="flex items-center gap-2 mb-4">
              <CheckCircle className="w-5 h-5 text-accent" />
              <h5 className="font-bold text-slate-800">Seller Info</h5>
            </div>
            <div className="flex items-center gap-3 mb-6 p-4 bg-slate-50 rounded-xl">
              <span className="avatar w-12 h-12 text-lg">{car.seller.name?.[0]}</span>
              <div>
                <b className="text-slate-800">{car.seller.name}</b>
                <div className="text-xs text-text-muted flex items-center gap-1">
                  <CheckCircle className="w-3 h-3 text-accent" /> Verified seller
                </div>
              </div>
            </div>
            <button onClick={message} className="w-full py-3 rounded-xl border-2 border-primary text-primary font-semibold hover:bg-primary hover:text-white transition-all flex items-center justify-center gap-2 mb-3">
              <MessageCircle className="w-5 h-5" /> Contact Seller
            </button>
            {user?.role !== 'SELLER' && (
              <button onClick={book} className="w-full py-3 rounded-xl btn-accent font-semibold flex items-center justify-center gap-2">
                <ShoppingBag className="w-5 h-5" /> Book Car
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
