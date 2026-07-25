import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../state/AuthContext';
import api, { unwrap } from '../api/api';

const panRegex = /^[A-Z]{5}[0-9]{4}[A-Z]$/;
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function Field({ label, children }) {
  return <div className="space-y-1"><label className="text-xs font-bold text-slate-600">{label}</label>{children}</div>;
}

export default function LoginRegister() {
  const navigate = useNavigate();
  const auth = useAuth();

  const [mode, setMode] = useState('login');
  const [loginRole, setLoginRole] = useState('BUYER');
  const [role, setRole] = useState('BUYER');
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');

  const [panNumber, setPanNumber] = useState('');
  const [panOtp, setPanOtp] = useState('');
  const [panOtpSent, setPanOtpSent] = useState(false);
  const [resendTimer, setResendTimer] = useState(0);
  const [kycVerified, setKycVerified] = useState(false);
  const [kycData, setKycData] = useState(null);

  const [sellerOtpVisible, setSellerOtpVisible] = useState(false);
  const [sellerOtp, setSellerOtp] = useState('');
  const [otpHint, setOtpHint] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (resendTimer <= 0) return;
    const id = setInterval(() => setResendTimer((value) => value > 0 ? value - 1 : 0), 1000);
    return () => clearInterval(id);
  }, [resendTimer]);

  useEffect(() => {
    setSellerOtpVisible(false);
    setSellerOtp('');
    setOtpHint('');
  }, [loginRole, mode]);

  useEffect(() => {
    if (role !== 'SELLER') resetKyc();
  }, [role]);

  const resetKyc = () => {
    setPanOtp('');
    setPanOtpSent(false);
    setResendTimer(0);
    setKycVerified(false);
    setKycData(null);
  };

  const validateCommon = () => {
    if (!emailRegex.test(email)) return toast.error('Enter valid email'), false;
    if (!password || password.length < 6) return toast.error('Password must be minimum 6 characters'), false;
    return true;
  };

  const sendPanOtp = async () => {
    const pan = panNumber.toUpperCase().trim();
    setPanNumber(pan);
    if (!panRegex.test(pan)) return toast.error('Enter valid PAN number like ABCDE1234F');
    try {
      setLoading(true);
      const result = await api.post('/kyc/pan/send-otp', { panNumber: pan }).then(unwrap);
      if (result?.success && result?.code === 'OTP_SENT') {
        setPanOtpSent(true);
        setPanOtp('');
        setKycVerified(false);
        setKycData(null);
        setResendTimer(60);
        toast.success(result.message || 'OTP sent to registered email address');
      } else {
        toast.error(result?.message || 'PAN OTP could not be sent');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Start the MS .NET KYC server and try again');
    } finally {
      setLoading(false);
    }
  };

  const verifyPanOtp = async () => {
    const pan = panNumber.toUpperCase().trim();
    if (!panRegex.test(pan)) return toast.error('Enter valid PAN number');
    if (!/^\d{6}$/.test(panOtp)) return toast.error('Enter 6 digit OTP');
    try {
      setLoading(true);
      const result = await api.post('/kyc/pan/verify-otp', { panNumber: pan, otp: panOtp }).then(unwrap);
      if (result?.success && result?.code === 'PAN_VERIFIED') {
        const verified = result.data || {};
        setKycVerified(true);
        setKycData(verified);
        setPanOtpSent(false);
        setResendTimer(0);
        setPanOtp('');
        if (verified.holderName || verified.HolderName) setName(verified.holderName || verified.HolderName);
        toast.success(result.message || 'PAN verification completed');
      } else {
        toast.error(result?.message || 'PAN OTP verification failed');
      }
    } catch (error) {
      toast.error(error.response?.data?.message || 'Invalid or expired OTP');
    } finally {
      setLoading(false);
    }
  };

  const startSellerLogin = async () => {
    if (!validateCommon()) return;
    try {
      setLoading(true);
      const result = await auth.startSellerLogin(email, password);
      setSellerOtpVisible(true);
      setOtpHint(result?.demoOtp ? `Demo OTP: ${result.demoOtp}` : `OTP sent to ${result?.maskedEmail || 'registered email'}`);
    } catch (error) {
      toast.error(error.response?.data?.message || 'Seller OTP login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleLogin = async () => {
    if (!validateCommon()) return;
    try {
      setLoading(true);
      let user;
      if (loginRole === 'SELLER') {
        if (!sellerOtpVisible) {
          await startSellerLogin();
          return;
        }
        if (!/^\d{6}$/.test(sellerOtp)) return toast.error('Enter 6 digit login OTP');
        user = await auth.verifySellerLogin(email, sellerOtp);
      } else {
        user = await auth.login(email, password);
      }
      navigate(user.role === 'SELLER' ? '/seller/listings' : '/buyer/dashboard');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!name.trim()) return toast.error('Enter full name');
    if (!/^\d{10}$/.test(phone)) return toast.error('Enter valid 10 digit phone');
    if (!validateCommon()) return;
    if (role === 'SELLER' && !kycVerified) return toast.error('Complete PAN OTP verification before seller registration');
    try {
      setLoading(true);
      const user = await auth.register({
        name: name.trim(),
        email,
        phone,
        password,
        role,
        panNumber: panNumber.toUpperCase().trim(),
        kycVerified
      });
      navigate(user.role === 'SELLER' ? '/seller/listings' : '/buyer/dashboard');
    } catch (error) {
      toast.error(error.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (mode === 'login') handleLogin();
    else handleRegister();
  };

  return (
    <div className="min-h-[calc(100vh-64px)] bg-slate-50">
      <div className="max-w-6xl mx-auto px-4 py-10 grid lg:grid-cols-2 gap-8 items-center">
        <div className="space-y-6">
          <div className="inline-flex items-center rounded-full bg-blue-50 border border-blue-100 px-4 py-2 text-sm font-bold text-blue-700">Secure MyGaadi access</div>
          <h1 className="text-4xl lg:text-5xl font-extrabold text-slate-900 leading-tight">Buy and sell cars with verified trust.</h1>
          <p className="text-slate-600 text-lg">Buyer login is simple. Seller/Agent login uses OTP. Seller registration uses PAN OTP verification through the dummy MS .NET KYC helper service.</p>
          <div className="grid sm:grid-cols-2 gap-4">
            {['Verified Sellers', 'PAN KYC', 'Razorpay Payments', 'Escrow Status'].map((item) => <div key={item} className="bg-white rounded-2xl border border-slate-100 shadow-sm p-4 font-bold text-slate-700">✓ {item}</div>)}
          </div>
        </div>

        <div className="bg-white rounded-3xl shadow-xl border border-slate-100 p-6 sm:p-8">
          <div className="grid grid-cols-2 bg-slate-100 rounded-2xl p-1 mb-6">
            <button type="button" onClick={() => setMode('login')} className={`py-3 rounded-xl text-sm font-extrabold ${mode === 'login' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-500'}`}>Login</button>
            <button type="button" onClick={() => setMode('register')} className={`py-3 rounded-xl text-sm font-extrabold ${mode === 'register' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-500'}`}>Register</button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {mode === 'login' && <div className="grid grid-cols-2 gap-2 bg-slate-50 rounded-2xl p-1">
              <button type="button" onClick={() => setLoginRole('BUYER')} className={`py-2.5 rounded-xl text-sm font-bold ${loginRole === 'BUYER' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-500'}`}>Buyer</button>
              <button type="button" onClick={() => setLoginRole('SELLER')} className={`py-2.5 rounded-xl text-sm font-bold ${loginRole === 'SELLER' ? 'bg-white text-blue-700 shadow-sm' : 'text-slate-500'}`}>Seller / Agent</button>
            </div>}

            {mode === 'register' && <>
              <Field label="Full Name"><input value={name} onChange={(e) => setName(e.target.value)} className="input-box" placeholder="Full name" /></Field>
              <Field label="Phone"><input value={phone} onChange={(e) => setPhone(e.target.value.replace(/\D/g, '').slice(0, 10))} className="input-box" placeholder="10 digit phone" /></Field>
              <Field label="Register As"><select value={role} onChange={(e) => setRole(e.target.value)} className="input-box bg-white"><option value="BUYER">Buyer</option><option value="SELLER">Seller / Agent</option></select></Field>

              {role === 'SELLER' && <div className="rounded-3xl border border-blue-100 bg-blue-50/50 p-4 space-y-4">
                <div>
                  <p className="font-extrabold text-slate-900">PAN KYC Verification</p>
                  <p className="text-xs text-slate-500">Verify PAN through dummy MS .NET government-API-style helper service before creating seller account.</p>
                </div>
                <Field label="PAN Number">
                  <div className="flex gap-2">
                    <input value={panNumber} onChange={(e) => { setPanNumber(e.target.value.toUpperCase().slice(0, 10)); if (kycVerified) setKycVerified(false); }} disabled={kycVerified} className="input-box uppercase" placeholder="ABCDE1234F" />
                    {!panOtpSent && !kycVerified && <button type="button" onClick={sendPanOtp} disabled={loading || !panNumber} className="px-5 rounded-xl bg-blue-700 text-white text-sm font-bold disabled:opacity-50">Verify</button>}
                    {kycVerified && <span className="px-4 py-3 rounded-xl bg-emerald-100 text-emerald-700 text-sm font-extrabold">Verified</span>}
                  </div>
                </Field>

                {panOtpSent && !kycVerified && <div className="grid sm:grid-cols-[1fr_auto_auto] gap-2 items-end">
                  <Field label="Enter OTP"><input value={panOtp} onChange={(e) => setPanOtp(e.target.value.replace(/\D/g, '').slice(0, 6))} className="input-box" placeholder="6 digit OTP" /></Field>
                  <button type="button" onClick={verifyPanOtp} disabled={loading} className="px-5 py-3 rounded-xl bg-emerald-600 text-white text-sm font-bold disabled:opacity-50">Submit OTP</button>
                  <button type="button" onClick={sendPanOtp} disabled={loading || resendTimer > 0} className={`px-5 py-3 rounded-xl border text-sm font-bold ${resendTimer > 0 ? 'opacity-40 cursor-not-allowed border-slate-200 text-slate-400' : 'border-blue-200 text-blue-700 hover:bg-blue-50'}`}>{resendTimer > 0 ? `Resend ${resendTimer}s` : 'Resend OTP'}</button>
                </div>}

                {kycVerified && <div className="rounded-2xl bg-emerald-50 border border-emerald-100 p-4 text-sm text-emerald-800">
                  <p className="font-extrabold">PAN Verified Successfully</p>
                  <p>Name: {kycData?.holderName || 'Verified PAN holder'}</p>
                  <p>PAN: {kycData?.panNumber || panNumber} · Status: {kycData?.status || 'ACTIVE'} · KYC: {kycData?.kycStatus || 'COMPLETED'}</p>
                </div>}
              </div>}
            </>}

            <Field label="Email"><input value={email} onChange={(e) => setEmail(e.target.value)} className="input-box" placeholder="Email" /></Field>
            <Field label="Password"><input type="password" value={password} onChange={(e) => setPassword(e.target.value)} className="input-box" placeholder="Password" /></Field>

            {mode === 'login' && loginRole === 'SELLER' && sellerOtpVisible && <div className="rounded-2xl border border-amber-100 bg-amber-50 p-4 space-y-2">
              <Field label="Seller / Agent Login OTP"><input value={sellerOtp} onChange={(e) => setSellerOtp(e.target.value.replace(/\D/g, '').slice(0, 6))} className="input-box bg-white" placeholder="6 digit OTP" /></Field>
              {otpHint && <p className="text-xs font-bold text-amber-700">{otpHint}</p>}
            </div>}

            <button disabled={loading} className="w-full py-3 rounded-xl bg-gradient-to-r from-blue-700 to-blue-500 text-white font-extrabold shadow-lg shadow-blue-500/20 disabled:opacity-60">
              {loading ? 'Please wait...' : mode === 'login' ? (loginRole === 'SELLER' && !sellerOtpVisible ? 'Send Seller OTP' : 'Login') : 'Create Account'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
