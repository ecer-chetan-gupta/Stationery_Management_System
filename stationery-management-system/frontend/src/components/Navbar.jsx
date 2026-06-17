import React from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!user) return null;

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="glass-panel sticky top-0 z-50 w-full px-6 py-4 flex items-center justify-between shadow-md mb-8">
      <div className="flex items-center space-x-8">
        <Link to="/dashboard" className="text-xl font-bold bg-gradient-to-r from-primary-400 to-purple-400 bg-clip-text text-transparent">
          SMS Stationery
        </Link>
        <div className="hidden md:flex space-x-6">
          <Link
            to="/dashboard"
            className={`transition duration-200 text-sm font-medium ${
              isActive('/dashboard') ? 'text-primary-400 border-b-2 border-primary-500 pb-1' : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            Dashboard
          </Link>
          <Link
            to="/catalog"
            className={`transition duration-200 text-sm font-medium ${
              isActive('/catalog') ? 'text-primary-400 border-b-2 border-primary-500 pb-1' : 'text-gray-400 hover:text-gray-200'
            }`}
          >
            Catalog
          </Link>
          
          {user.role === 'STUDENT' && (
            <Link
              to="/my-requests"
              className={`transition duration-200 text-sm font-medium ${
                isActive('/my-requests') ? 'text-primary-400 border-b-2 border-primary-500 pb-1' : 'text-gray-400 hover:text-gray-200'
              }`}
            >
              My Requests
            </Link>
          )}

          {user.role === 'ADMIN' && (
            <>
              <Link
                to="/admin/inventory"
                className={`transition duration-200 text-sm font-medium ${
                  isActive('/admin/inventory') ? 'text-primary-400 border-b-2 border-primary-500 pb-1' : 'text-gray-400 hover:text-gray-200'
                }`}
              >
                Manage Inventory
              </Link>
              <Link
                to="/admin/requests"
                className={`transition duration-200 text-sm font-medium ${
                  isActive('/admin/requests') ? 'text-primary-400 border-b-2 border-primary-500 pb-1' : 'text-gray-400 hover:text-gray-200'
                }`}
              >
                Manage Requests
              </Link>
            </>
          )}
        </div>
      </div>

      <div className="flex items-center space-x-4">
        <div className="text-right hidden sm:block">
          <p className="text-xs text-gray-400 font-light">Logged in as</p>
          <p className="text-sm font-semibold text-gray-200">{user.fullName || user.email}</p>
        </div>
        <span className={`px-2.5 py-0.5 text-xs font-semibold rounded-full ${
          user.role === 'ADMIN' ? 'bg-purple-900/40 text-purple-300 border border-purple-800' : 'bg-blue-900/40 text-blue-300 border border-blue-800'
        }`}>
          {user.role}
        </span>
        <button
          onClick={handleLogout}
          className="px-4 py-1.5 text-xs font-semibold rounded-lg bg-red-600/20 text-red-400 border border-red-800 hover:bg-red-600/30 hover:text-white transition duration-200"
        >
          Logout
        </button>
      </div>
    </nav>
  );
};

export default Navbar;
