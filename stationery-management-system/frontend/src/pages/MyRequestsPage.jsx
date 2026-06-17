import React, { useEffect, useState } from 'react';
import api from '../api/axiosConfig';
import { toast } from 'react-toastify';

const MyRequestsPage = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/requests/my');
      setRequests(res.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load request history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const getStatusBadge = (status) => {
    switch (status) {
      case 'APPROVED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-green-950/40 text-green-400 border border-green-800">Approved</span>;
      case 'REJECTED':
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-red-950/40 text-red-400 border border-red-800">Rejected</span>;
      default:
        return <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-amber-950/40 text-amber-400 border border-amber-800">Pending</span>;
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8 text-left">
        <h1 className="text-3xl font-extrabold text-white">My Requests</h1>
        <p className="text-gray-400 mt-1">Track and manage your submitted stationery requests.</p>
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      ) : requests.length === 0 ? (
        <div className="glass-card p-12 text-center rounded-2xl">
          <span className="text-4xl block mb-2">📋</span>
          <p className="text-gray-400 font-medium">You haven't submitted any requests yet.</p>
        </div>
      ) : (
        <div className="space-y-4 text-left">
          {requests.map((req) => (
            <div key={req.id} className="glass-card p-6 rounded-2xl border border-gray-800/80">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-800/60 pb-4 mb-4">
                <div>
                  <h3 className="text-base font-bold text-white">Request #{req.id}</h3>
                  <p className="text-xs text-gray-500 mt-1">Submitted on {formatDate(req.requestDate)}</p>
                </div>
                <div className="flex items-center space-x-3">
                  {getStatusBadge(req.status)}
                </div>
              </div>

              <div className="mb-4">
                <p className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2">Requested Items</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
                  {req.items?.map((item, index) => (
                    <div key={index} className="flex justify-between items-center p-3 bg-gray-950/35 border border-gray-900 rounded-xl">
                      <span className="text-sm text-gray-200 font-medium">{item.itemName || `Item ID: ${item.itemId}`}</span>
                      <span className="text-xs font-bold text-primary-400">Qty: {item.quantity}</span>
                    </div>
                  ))}
                </div>
              </div>

              {req.adminComment && (
                <div className="mt-4 p-3.5 rounded-xl bg-gray-950/20 border border-gray-800/40 text-sm text-gray-300">
                  <span className="font-semibold text-gray-400 text-xs block uppercase tracking-wider mb-1">Admin Comment</span>
                  {req.adminComment}
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default MyRequestsPage;
