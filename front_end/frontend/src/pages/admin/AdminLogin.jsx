import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { ShieldCheck, Mail, Lock, KeyRound, ArrowRight, Car } from '../../components/AppIcons.jsx';
import { useAuth } from '../../state/AuthContext';

export default function AdminLogin() {
  const auth = useAuth();
  const nav = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');
  const [otpVisible, setOtpVisible] = useState(false);
  const [hint, setHint] = useState('');
  const [loading, setLoading] = useState(false);

  const start = async () => {
    if (!email || !password) return toast.error('Enter admin email and password');
    try {
      setLoading(true);
      const res = await auth.startAdminLogin(email, password);
      setOtpVisible(true);
      setHint(res?.demoOtp ? `Demo OTP: ${res.demoOtp}` : `OTP sent to ${res?.maskedEmail || 'registered email'}`);
    } catch (e) {
      toast.error(e.response?.data?.message || 'Admin OTP login failed');
    } finally { setLoading(false); }
  };

  const verify = async (e) => {
    e.preventDefault();
    try {
      setLoading(true);
      if (!otpVisible) { await start(); return; }
      if (!/^\d{6}$/.test(otp)) return toast.error('Enter 6 digit OTP');
      const user = await auth.verifyAdminLogin(email, otp);
      if (user.role !== 'ADMIN') return toast.error('Invalid admin account');
      nav('/admin/dashboard');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Admin login failed');
    } finally { setLoading(false); }
  };

  return (
    <div className="min-h-[calc(100vh-64px)] bg-slate-950 relative overflow-hidden flex items-center justify-center px-4 py-10">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_20%_15%,rgba(59,130,246,.25),transparent_35%),radial-gradient(circle_at_80%_70%,rgba(39,174,96,.15),transparent_35%)]" />
      <div className="relative max-w-5xl w-full grid lg:grid-cols-2 gap-8 items-center">
        <div className="text-white">
          <div className="inline-flex items-center gap-2 rounded-full bg-white/10 border border-white/10 px-4 py-2 text-sm mb-6"><Car className="w-4 h-4 text-emerald-300" /> MyGaadi Admin Portal</div>
          <h1 className="text-4xl lg:text-5xl font-extrabold leading-tight mb-4">Separate secure login for administrators.</h1>
          <p className="text-slate-300 text-lg">Admin accounts cannot use the public login page. Password verification is followed by OTP verification before entering the admin dashboard.</p>
          <div className="mt-8 grid sm:grid-cols-3 gap-3">
            {['OTP protected','Role checked','Audit ready'].map(x => <div key={x} className="rounded-2xl bg-white/10 border border-white/10 p-4 text-sm font-bold">{x}</div>)}
          </div>
        </div>
        <form onSubmit={verify} className="bg-white rounded-3xl shadow-2xl p-8 space-y-4">
          <div className="w-14 h-14 rounded-2xl bg-blue-50 text-primary flex items-center justify-center mb-2"><ShieldCheck className="w-7 h-7" /></div>
          <h2 className="text-2xl font-extrabold text-slate-900">Admin Login</h2>
          <p className="text-sm text-slate-500">Enter admin credentials. OTP field appears after verification.</p>
          <div className="relative"><Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" /><input value={email} onChange={e=>setEmail(e.target.value)} className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="Admin email" /></div>
          <div className="relative"><Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" /><input type="password" value={password} onChange={e=>setPassword(e.target.value)} className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary" placeholder="Password" /></div>
          {otpVisible && <div className="rounded-2xl border border-blue-100 bg-blue-50/50 p-4 space-y-2"><div className="relative"><KeyRound className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" /><input value={otp} onChange={e=>setOtp(e.target.value.replace(/\D/g,'').slice(0,6))} className="w-full pl-10 pr-4 py-3 rounded-xl border border-slate-200 text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary bg-white" placeholder="Enter admin OTP" /></div>{hint && <p className="text-xs text-primary font-semibold">{hint}</p>}</div>}
          <button disabled={loading} className="w-full py-3 rounded-xl btn-gradient font-bold flex items-center justify-center gap-2 disabled:opacity-60">{otpVisible ? 'Verify & Login' : 'Send OTP'} <ArrowRight className="w-4 h-4" /></button>
          <p className="text-xs text-slate-500">First admin is inserted directly in MySQL. Later admins are created from the admin panel and stored with created-by-admin tracking.</p>
        </form>
      </div>
    </div>
  );
}
