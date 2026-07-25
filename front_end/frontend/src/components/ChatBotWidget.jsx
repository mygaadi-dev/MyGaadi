import { useState } from 'react';
import { toast } from 'react-toastify';

const CHATBOT_URL = 'http://localhost:8000/api/chatbot/chat';

function CarMiniCard({ car }) {
  return <div className="rounded-2xl border border-slate-200 p-3 bg-white">
    <div className="flex justify-between gap-3">
      <div>
        <p className="font-extrabold text-slate-900 text-sm">{car.title || `${car.brand || ''} ${car.model || ''}`}</p>
        <p className="text-xs text-slate-500">{car.year} · {car.fuelType || car.fuel_type} · {car.transmission}</p>
        <p className="text-xs text-slate-500">{car.locationCity || car.location_city} · {car.mileageKm || car.mileage_km || 0} km</p>
      </div>
      <p className="font-extrabold text-blue-700 text-sm">₹{Number(car.price || 0).toLocaleString('en-IN')}</p>
    </div>
  </div>;
}

export default function ChatBotWidget({ fullPage = false }) {
  const [open, setOpen] = useState(fullPage);
  const [message, setMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [chat, setChat] = useState([
    { role: 'bot', text: 'Hi, I am MyGaadi AI Assistant. Tell me your budget, city, fuel type and usage. I will suggest the best available cars.' }
  ]);

  const askBot = async () => {
    const text = message.trim();
    if (!text) return;
    setChat((list) => [...list, { role: 'user', text }]);
    setMessage('');
    try {
      setLoading(true);
      const res = await fetch(CHATBOT_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: text })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.detail || 'Chatbot service failed');
      setChat((list) => [...list, { role: 'bot', text: data.answer, cars: data.cars || [] }]);
    } catch (error) {
      toast.error('Start FastAPI GenAI chatbot service on port 8000');
      setChat((list) => [...list, { role: 'bot', text: 'I could not connect to GenAI chatbot service. Please start the FastAPI service first.' }]);
    } finally {
      setLoading(false);
    }
  };

  const box = <div className={`${fullPage ? 'w-full' : 'w-[92vw] sm:w-96'} bg-white rounded-3xl shadow-2xl border border-slate-200 overflow-hidden`}>
    <div className="bg-gradient-to-r from-blue-700 to-blue-500 text-white p-4">
      <p className="font-extrabold">MyGaadi AI Assistant</p>
      <p className="text-xs text-blue-100">FastAPI + LangChain car recommendation bot</p>
    </div>
    <div className={`${fullPage ? 'h-[60vh]' : 'h-96'} overflow-y-auto p-4 bg-slate-50 space-y-3`}>
      {chat.map((item, index) => <div key={index} className={`${item.role === 'user' ? 'ml-10 bg-blue-700 text-white' : 'mr-4 bg-white text-slate-700 border border-slate-200'} rounded-2xl p-3 text-sm shadow-sm`}>
        <p className="whitespace-pre-line">{item.text}</p>
        {item.cars?.length > 0 && <div className="mt-3 space-y-2">{item.cars.map((car) => <CarMiniCard key={car.carId || car.car_id || car.id} car={car} />)}</div>}
      </div>)}
      {loading && <div className="mr-10 bg-white border border-slate-200 rounded-2xl p-3 text-sm text-slate-500">Finding best cars...</div>}
    </div>
    <div className="p-3 border-t border-slate-100 flex gap-2">
      <input value={message} onChange={(e) => setMessage(e.target.value)} onKeyDown={(e) => { if (e.key === 'Enter') askBot(); }} className="input-box" placeholder="Example: automatic petrol car under 8 lakh in Pune" />
      <button onClick={askBot} disabled={loading} className="px-4 rounded-xl bg-blue-700 text-white font-bold disabled:opacity-50">Send</button>
    </div>
  </div>;

  if (fullPage) return box;

  return <div className="fixed right-4 bottom-4 z-50">
    {open && <div className="mb-3">{box}</div>}
    <button onClick={() => setOpen(!open)} className="ml-auto flex items-center gap-2 rounded-full bg-blue-700 hover:bg-blue-800 text-white px-5 py-3 shadow-2xl font-extrabold">
      <span>💬</span> {open ? 'Close' : 'AI Assistant'}
    </button>
  </div>;
}
