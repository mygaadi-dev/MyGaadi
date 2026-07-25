import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import api from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import { uploadMany } from '../../utils/upload';
import { Plus, Image, FileText, Car, Palette, Gauge, Settings, Calendar, Banknote, Hash, MapPin, CheckCircle, Fuel, UploadCloud } from '../../components/AppIcons.jsx';

const imageSlots = [
  ['FRONT', 'Front view'],
  ['REAR', 'Rear view'],
  ['LEFT_SIDE', 'Left side'],
  ['RIGHT_SIDE', 'Right side'],
  ['INTERIOR', 'Interior'],
  ['ODOMETER', 'Odometer'],
  ['ENGINE', 'Engine bay'],
  ['RC', 'RC / document'],
];

export default function CarForm(){
  const f=useForm({defaultValues:{fuelType:'PETROL',transmission:'MANUAL',noOfOwners:1,rcAvailable:true}});
  const [slotFiles, setSlotFiles] = useState({});
  const [galleryFiles, setGalleryFiles] = useState([]);
  const [saving, setSaving] = useState(false);
  const nav=useNavigate();

  const submit=async(v)=>{
    setSaving(true);
    try {
      const images = [];
      for (const [type] of imageSlots) {
        const url = (await uploadMany(slotFiles[type] || []))[0];
        if (url) images.push({ imageUrl: url, thumbnailUrl: url, imageType: type, displayOrder: images.length, primaryImage: images.length === 0 });
      }
      const galleryUrls = await uploadMany(galleryFiles);
      galleryUrls.forEach((url) => images.push({ imageUrl: url, thumbnailUrl: url, imageType: 'GALLERY', displayOrder: images.length, primaryImage: images.length === 0 }));
      const typedUrls = v.imageUrls ? v.imageUrls.split(',').map(x=>x.trim()).filter(Boolean) : [];
      typedUrls.forEach((url) => images.push({ imageUrl: url, thumbnailUrl: url, imageType: 'URL', displayOrder: images.length, primaryImage: images.length === 0 }));

      const payload={
        ...v,
        year:Number(v.year),
        price:Number(v.price),
        mileageKm:Number(v.mileageKm),
        engineCc:Number(v.engineCc||0),
        noOfOwners:Number(v.noOfOwners),
        rcAvailable: String(v.rcAvailable) === 'true' || v.rcAvailable === true,
        imageUrls: images.map(i => i.imageUrl),
        images
      };
      await api.post('/cars',payload);
      toast.success('Listing created with images');
      nav('/seller/listings');
    } finally {
      setSaving(false);
    }
  };

  const input = (icon, placeholder, name, extra={}) => {
    const Icon = icon;
    return (
      <div className="relative">
        <Icon className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
        <input className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all" placeholder={placeholder} {...f.register(name)} {...extra} />
      </div>
    );
  };

  return <DashboardLayout title="Add Car Listing">
    <form onSubmit={f.handleSubmit(submit)} className="dash-card space-y-6">
      <div className="flex items-center gap-2 mb-2">
        <Car className="w-5 h-5 text-primary" />
        <h5 className="font-bold text-slate-800">Basic Info</h5>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div className="md:col-span-2 lg:col-span-3">{input(Car, 'Title', 'title')}</div>
        {input(Car, 'Brand', 'brand')}
        {input(Car, 'Model', 'model')}
        {input(Car, 'Variant', 'variant')}
        {input(Calendar, 'Year', 'year', { type: 'number' })}
        {input(Banknote, 'Price', 'price', { type: 'number' })}
        <div className="relative">
          <Fuel className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <select className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white" {...f.register('fuelType')}>{['PETROL','DIESEL','CNG','ELECTRIC','HYBRID'].map(x=><option key={x}>{x}</option>)}</select>
        </div>
        <div className="relative">
          <Settings className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <select className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white" {...f.register('transmission')}>{['MANUAL','AUTOMATIC','AMT','DCT'].map(x=><option key={x}>{x}</option>)}</select>
        </div>
        {input(Gauge, 'Mileage km', 'mileageKm', { type: 'number' })}
        {input(Car, 'Engine cc', 'engineCc', { type: 'number' })}
        {input(Palette, 'Color', 'color')}
        {input(Hash, 'No. of owners', 'noOfOwners', { type: 'number' })}
        <div className="relative">
          <CheckCircle className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <select className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all bg-white" {...f.register('rcAvailable')}><option value="true">RC Available</option><option value="false">RC Missing</option></select>
        </div>
      </div>

      <div className="flex items-center gap-2 mb-2 pt-2 border-t border-slate-100">
        <MapPin className="w-5 h-5 text-primary" />
        <h5 className="font-bold text-slate-800">Location</h5>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {input(MapPin, 'City', 'locationCity')}
        {input(MapPin, 'State', 'locationState')}
      </div>

      <div className="flex items-center gap-2 mb-2 pt-2 border-t border-slate-100">
        <FileText className="w-5 h-5 text-primary" />
        <h5 className="font-bold text-slate-800">Details</h5>
      </div>
      <div className="relative">
        <FileText className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
        <textarea className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all min-h-[100px] resize-y" placeholder="Description" {...f.register('description')}></textarea>
      </div>

      <div className="pt-2 border-t border-slate-100">
        <div className="flex items-center gap-2 mb-4">
          <Image className="w-5 h-5 text-primary" />
          <h5 className="font-bold text-slate-800">Car Photos</h5>
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {imageSlots.map(([type, label]) => (
            <label key={type} className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-4 hover:border-primary hover:bg-blue-50/40 transition-colors cursor-pointer">
              <div className="flex items-center gap-2 mb-2 text-sm font-bold text-slate-700"><UploadCloud className="w-4 h-4 text-primary" /> {label}</div>
              <input type="file" accept="image/*" className="text-xs w-full" onChange={(e)=>setSlotFiles(prev => ({...prev, [type]: e.target.files}))} />
              <p className="text-[11px] text-slate-400 mt-2">Upload {label.toLowerCase()} image</p>
            </label>
          ))}
        </div>
        <label className="mt-4 block rounded-2xl border border-dashed border-slate-300 bg-white p-4 hover:border-primary transition-colors cursor-pointer">
          <div className="flex items-center gap-2 mb-2 text-sm font-bold text-slate-700"><UploadCloud className="w-4 h-4 text-primary" /> Additional gallery images</div>
          <input type="file" multiple accept="image/*" className="text-xs w-full" onChange={(e)=>setGalleryFiles(e.target.files)} />
        </label>
        <div className="relative mt-4">
          <Image className="absolute left-3 top-3 w-4 h-4 text-slate-400" />
          <textarea className="w-full pl-10 pr-3 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all min-h-[70px] resize-y" placeholder="Optional existing image URLs (comma separated)" {...f.register('imageUrls')}></textarea>
        </div>
      </div>

      <button disabled={saving} className="px-6 py-3 rounded-xl btn-accent font-semibold flex items-center gap-2 disabled:opacity-60">
        <Plus className="w-5 h-5" /> {saving ? 'Uploading...' : 'Create Listing'}
      </button>
    </form>
  </DashboardLayout>;
}
