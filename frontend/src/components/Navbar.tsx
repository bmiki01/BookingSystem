import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Calendar, LogOut, Shield, User, Clock, Settings, LayoutList } from 'lucide-react';

export const Navbar: React.FC = () => {
  const { user, isAuthenticated, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path: string) => location.pathname === path;

  return (
    <header className="sticky top-0 z-50 bg-slate-900/80 backdrop-blur-md border-b border-slate-800">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center space-x-3 text-sky-400 font-bold text-xl hover:text-sky-300 transition-colors">
          <div className="p-2 bg-sky-500/10 rounded-xl border border-sky-500/20">
            <Calendar className="w-6 h-6" />
          </div>
          <span>BookIt</span>
        </Link>

        {isAuthenticated ? (
          <nav className="flex items-center space-x-6">
            <Link
              to="/resources"
              className={`flex items-center space-x-2 text-sm font-medium transition-colors ${
                isActive('/resources') ? 'text-sky-400' : 'text-slate-300 hover:text-white'
              }`}
            >
              <LayoutList className="w-4 h-4" />
              <span>Resources</span>
            </Link>

            <Link
              to="/my-bookings"
              className={`flex items-center space-x-2 text-sm font-medium transition-colors ${
                isActive('/my-bookings') ? 'text-sky-400' : 'text-slate-300 hover:text-white'
              }`}
            >
              <Clock className="w-4 h-4" />
              <span>My Bookings</span>
            </Link>

            {isAdmin && (
              <div className="relative group">
                <div className="flex items-center space-x-4 pl-4 border-l border-slate-800">
                  <Link
                    to="/admin/resources"
                    className={`flex items-center space-x-1.5 text-xs font-semibold px-2.5 py-1 rounded-lg border transition-colors ${
                      isActive('/admin/resources')
                        ? 'bg-amber-500/20 text-amber-300 border-amber-500/30'
                        : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'
                    }`}
                  >
                    <Settings className="w-3.5 h-3.5" />
                    <span>Admin Resources</span>
                  </Link>
                  <Link
                    to="/admin/bookings"
                    className={`flex items-center space-x-1.5 text-xs font-semibold px-2.5 py-1 rounded-lg border transition-colors ${
                      isActive('/admin/bookings')
                        ? 'bg-amber-500/20 text-amber-300 border-amber-500/30'
                        : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'
                    }`}
                  >
                    <Shield className="w-3.5 h-3.5" />
                    <span>Admin Bookings</span>
                  </Link>
                </div>
              </div>
            )}

            <div className="flex items-center space-x-4 pl-4 border-l border-slate-800">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-slate-300 font-semibold text-xs">
                  <User className="w-4 h-4" />
                </div>
                <div className="text-left hidden sm:block">
                  <p className="text-xs font-semibold text-white leading-none">{user?.fullName}</p>
                  <span className={`text-[10px] font-bold px-1.5 py-0.5 rounded mt-0.5 inline-block ${
                    isAdmin ? 'bg-amber-500/20 text-amber-400 border border-amber-500/30' : 'bg-slate-800 text-slate-400'
                  }`}>
                    {user?.role}
                  </span>
                </div>
              </div>

              <button
                onClick={handleLogout}
                className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-xl border border-transparent hover:border-rose-500/20 transition-all"
                title="Logout"
              >
                <LogOut className="w-4 h-4" />
              </button>
            </div>
          </nav>
        ) : (
          <div className="flex items-center space-x-4">
            <Link
              to="/login"
              className="text-sm font-medium text-slate-300 hover:text-white px-3 py-2 transition-colors"
            >
              Sign In
            </Link>
            <Link
              to="/register"
              className="text-sm font-semibold bg-sky-600 hover:bg-sky-500 text-white px-4 py-2 rounded-xl shadow-lg shadow-sky-600/20 transition-all"
            >
              Register
            </Link>
          </div>
        )}
      </div>
    </header>
  );
};
