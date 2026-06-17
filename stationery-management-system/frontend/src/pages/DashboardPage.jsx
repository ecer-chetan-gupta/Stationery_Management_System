import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';

const DashboardPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalItems: 0,
    pendingRequests: 0,
    lowStockCount: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        setLoading(true);
        const itemsRes = await api.get('/api/inventory?page=0&size=1');
        const totalItems = itemsRes.data.totalElements || 0;

        let pendingRequests = 0;
        if (user.role === 'ADMIN') {
          const reqsRes = await api.get('/api/requests');
          pendingRequests = reqsRes.data.filter(r => r.status === 'PENDING').length;
        } else {
          const reqsRes = await api.get('/api/requests/my');
          pendingRequests = reqsRes.data.filter(r => r.status === 'PENDING').length;
        }

        let lowStockCount = 0;
        if (user.role === 'ADMIN') {
          const lowStockRes = await api.get('/api/inventory/low-stock');
          lowStockCount = lowStockRes.data.length || 0;
        }

        setStats({
          totalItems,
          pendingRequests,
          lowStockCount,
        });
      } catch (err) {
        console.error('Failed to load dashboard statistics', err);
        toast.error('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [user]);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-24">
        <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8">
        <h1 className="text-3xl font-extrabold text-white text-left tracking-tight">
          Welcome back, {user.fullName || user.email}!
        </h1>
        <p className="text-gray-400 text-left mt-1">
          Here is what's happening today in the Stationery Management System.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8 text-left">
        <div className="glass-card p-6 rounded-2xl">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-400 uppercase tracking-wider">Total Stationery Items</p>
              <p className="mt-2 text-4xl font-bold text-white">{stats.totalItems}</p>
            </div>
            <div className="p-3 bg-blue-600/20 rounded-xl border border-blue-500/20 text-blue-400">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4"></path>
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <Link to="/catalog" className="text-xs text-primary-400 hover:text-primary-300 font-semibold flex items-center">
              Browse Catalog
              <svg className="w-3.5 h-3.5 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </Link>
          </div>
        </div>

        <div className="glass-card p-6 rounded-2xl">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-400 uppercase tracking-wider">Pending Requests</p>
              <p className="mt-2 text-4xl font-bold text-amber-400">{stats.pendingRequests}</p>
            </div>
            <div className="p-3 bg-amber-600/20 rounded-xl border border-amber-500/20 text-amber-400">
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
          <div className="mt-4">
            <Link to={user.role === 'ADMIN' ? "/admin/requests" : "/my-requests"} className="text-xs text-amber-400 hover:text-amber-300 font-semibold flex items-center">
              View Requests
              <svg className="w-3.5 h-3.5 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path>
              </svg>
            </Link>
          </div>
        </div>

        {user.role === 'ADMIN' ? (
          <div className="glass-card p-6 rounded-2xl">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-medium text-gray-400 uppercase tracking-wider">Low Stock Items</p>
                <p className="mt-2 text-4xl font-bold text-red-400">{stats.lowStockCount}</p>
              </div>
              <div className="p-3 bg-red-600/20 rounded-xl border border-red-500/20 text-red-400">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>
              </div>
            </div>
            <div className="mt-4">
              <Link to="/admin/inventory" className="text-xs text-red-400 hover:text-red-300 font-semibold flex items-center">
                Manage Inventory
                <svg className="w-3.5 h-3.5 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </Link>
            </div>
          </div>
        ) : (
          <div className="glass-card p-6 rounded-2xl flex flex-col justify-between">
            <div>
              <p className="text-sm font-medium text-gray-400 uppercase tracking-wider">Need Supplies?</p>
              <p className="mt-2 text-sm text-gray-300">Submit requests for stationery and keep track of updates.</p>
            </div>
            <div className="mt-4">
              <Link to="/catalog" className="inline-flex items-center px-4 py-2 text-xs font-semibold rounded-lg bg-primary-600 hover:bg-primary-500 text-white transition duration-200">
                Submit Request
              </Link>
            </div>
          </div>
        )}
      </div>

      <div className="glass-card p-8 rounded-2xl text-left">
        <h2 className="text-xl font-bold text-white mb-4">Quick Access Navigation</h2>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <Link to="/dashboard" className="p-4 rounded-xl bg-gray-900/40 hover:bg-gray-900/80 border border-gray-800 text-center transition duration-200">
            <span className="block text-xl mb-1">🏠</span>
            <span className="text-xs text-gray-300 font-semibold">Home</span>
          </Link>
          <Link to="/catalog" className="p-4 rounded-xl bg-gray-900/40 hover:bg-gray-900/80 border border-gray-800 text-center transition duration-200">
            <span className="block text-xl mb-1">📖</span>
            <span className="text-xs text-gray-300 font-semibold">Browse Catalog</span>
          </Link>
          {user.role === 'STUDENT' && (
            <Link to="/my-requests" className="p-4 rounded-xl bg-gray-900/40 hover:bg-gray-900/80 border border-gray-800 text-center transition duration-200">
              <span className="block text-xl mb-1">📝</span>
              <span className="text-xs text-gray-300 font-semibold">My Requests</span>
            </Link>
          )}
          {user.role === 'ADMIN' && (
            <>
              <Link to="/admin/inventory" className="p-4 rounded-xl bg-gray-900/40 hover:bg-gray-900/80 border border-gray-800 text-center transition duration-200">
                <span className="block text-xl mb-1">📦</span>
                <span className="text-xs text-gray-300 font-semibold">Manage Stock</span>
              </Link>
              <Link to="/admin/requests" className="p-4 rounded-xl bg-gray-900/40 hover:bg-gray-900/80 border border-gray-800 text-center transition duration-200">
                <span className="block text-xl mb-1">📥</span>
                <span className="text-xs text-gray-300 font-semibold">Manage Requests</span>
              </Link>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default DashboardPage;
