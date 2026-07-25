import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import Navbar from './components/Navbar.jsx';
import Home from './pages/Home.jsx';
import BrowseCars from './pages/BrowseCars.jsx';
import CarDetail from './pages/CarDetail.jsx';
import LoginRegister from './pages/LoginRegister.jsx';
import ProtectedRoute from './routes/ProtectedRoute.jsx';
import BuyerDashboard from './pages/buyer/BuyerDashboard.jsx';
import BuyerBookings from './pages/buyer/BuyerBookings.jsx';
import Wishlist from './pages/buyer/Wishlist.jsx';
import Messages from './pages/shared/Messages.jsx';
import Notifications from './pages/shared/Notifications.jsx';
import ReviewPage from './pages/buyer/ReviewPage.jsx';
import MyListings from './pages/seller/MyListings.jsx';
import CarForm from './pages/seller/CarForm.jsx';
import SellerBookings from './pages/seller/SellerBookings.jsx';
import Verification from './pages/seller/Verification.jsx';
import Earnings from './pages/seller/Earnings.jsx';
import AdminLogin from './pages/admin/AdminLogin.jsx';
import AdminOverview from './pages/admin/AdminOverview.jsx';
import ManageUsers from './pages/admin/ManageUsers.jsx';
import ManageListings from './pages/admin/ManageListings.jsx';
import VerifySellers from './pages/admin/VerifySellers.jsx';
import Reports from './pages/admin/Reports.jsx';
import Disputes from './pages/admin/Disputes.jsx';
import AssistantPage from './pages/shared/AssistantPage.jsx';
import ChatBotWidget from './components/ChatBotWidget.jsx';

export default function App() {
  const location = useLocation();
  const hideNavbar = location.pathname === '/admin/login';
  return (
    <div className="min-h-screen flex flex-col">
      {!hideNavbar && <Navbar />}
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/cars" element={<BrowseCars />} />
          <Route path="/cars/:id" element={<CarDetail />} />
          <Route path="/login" element={<LoginRegister />} />
          <Route path="/assistant" element={<AssistantPage />} />
          <Route path="/admin/login" element={<AdminLogin />} />

          <Route element={<ProtectedRoute roles={['BUYER', 'ADMIN']} />}>
            <Route path="/buyer/dashboard" element={<BuyerDashboard />} />
            <Route path="/buyer/bookings" element={<BuyerBookings />} />
            <Route path="/buyer/wishlist" element={<Wishlist />} />
            <Route path="/buyer/review" element={<ReviewPage />} />
          </Route>

          <Route element={<ProtectedRoute roles={['SELLER', 'ADMIN']} />}>
            <Route path="/seller/listings" element={<MyListings />} />
            <Route path="/seller/listings/new" element={<CarForm />} />
            <Route path="/seller/bookings" element={<SellerBookings />} />
            <Route path="/seller/verification" element={<Verification />} />
            <Route path="/seller/earnings" element={<Earnings />} />
          </Route>

          <Route element={<ProtectedRoute roles={['ADMIN']} />}>
            <Route path="/admin" element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="/admin/dashboard" element={<AdminOverview />} />
            <Route path="/admin/users" element={<ManageUsers />} />
            <Route path="/admin/listings" element={<ManageListings />} />
            <Route path="/admin/verify-sellers" element={<VerifySellers />} />
            <Route path="/admin/reports" element={<Reports />} />
            <Route path="/admin/disputes" element={<Disputes />} />
          </Route>

          <Route element={<ProtectedRoute />}>
            <Route path="/messages" element={<Messages />} />
            <Route path="/notifications" element={<Notifications />} />
          </Route>
        </Routes>
      </main>
      {!location.pathname.startsWith('/admin') && location.pathname !== '/assistant' && <ChatBotWidget />}
    </div>
  );
}
