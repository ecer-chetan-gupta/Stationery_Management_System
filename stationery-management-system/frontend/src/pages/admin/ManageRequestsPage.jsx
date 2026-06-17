import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { toast } from 'react-toastify';

const ManageRequestsPage = () => {
  const [requests, setRequests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('ALL'); // ALL, PENDING, APPROVED, REJECTED

  // Reject Modal State
  const [showRejectModal, setShowRejectModal] = useState(false);
  const [rejectId, setRejectId] = useState(null);
  const [rejectComment, setRejectComment] = useState('');

  const fetchRequests = async () => {
    try {
      setLoading(true);
      const res = await api.get('/api/requests');
      setRequests(res.data || []);
    } catch (err) {
      console.error(err);
      toast.error('Failed to load stationery requests');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  const handleApprove = async (id) => {
    try {
      await api.put(`/api/requests/${id}/approve`);
      toast.success(`Request #${id} approved and stock deducted successfully.`);
      fetchRequests();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to approve request');
    }
  };

  const handleOpenReject = (id) => {
    setRejectId(id);
    setRejectComment('');
    setShowRejectModal(true);
  };

  const handleRejectSubmit = async (e) => {
    e.preventDefault();
    if (!rejectComment.trim()) {
      toast.error('Rejection comment is required');
      return;
    }
    try {
      await api.put(`/api/requests/${rejectId}/reject`, { adminComment: rejectComment });
      toast.success(`Request #${rejectId} rejected.`);
      setShowRejectModal(false);
      fetchRequests();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to reject request');
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const d = new Date(dateStr);
    return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const filteredRequests = requests.filter((req) => {
    if (filterStatus === 'ALL') return true;
    return req.status === filterStatus;
  });

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
        <h1 className="text-3xl font-extrabold text-white">Manage Requests</h1>
        <p className="text-gray-400 mt-1">Review student stationery requests, approve distributions, or reject them.</p>
      </div>

      {/* Tabs */}
      <div className="flex border-b border-gray-800 mb-6 space-x-6">
        {['ALL', 'PENDING', 'APPROVED', 'REJECTED'].map((status) => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`pb-3 text-sm font-semibold transition duration-200 ${
              filterStatus === status
                ? 'text-primary-400 border-b-2 border-primary-500'
                : 'text-gray-500 hover:text-gray-300'
            }`}
          >
            {status}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      ) : filteredRequests.length === 0 ? (
        <div className="glass-card p-12 text-center rounded-2xl">
          <span className="text-4xl block mb-2">📥</span>
          <p className="text-gray-400 font-medium">No requests found matching status "{filterStatus}".</p>
        </div>
      ) : (
        <div className="space-y-4 text-left">
          {filteredRequests.map((req) => (
            <div key={req.id} className="glass-card p-6 rounded-2xl border border-gray-800/80">
              <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-gray-800/60 pb-4 mb-4">
                <div>
                  <div className="flex items-center space-x-2">
                    <h3 className="text-base font-bold text-white">Request #{req.id}</h3>
                    <span className="text-xs text-gray-500">•</span>
                    <span className="text-xs font-medium text-gray-300">Student: {req.studentEmail}</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">Submitted on {formatDate(req.requestDate)}</p>
                </div>
                <div className="flex items-center space-x-3">
                  {getStatusBadge(req.status)}
                  {req.status === 'PENDING' && (
                    <div className="flex items-center space-x-2">
                      <button
                        onClick={() => handleApprove(req.id)}
                        className="px-3 py-1.5 text-xs font-bold rounded-lg bg-green-600/20 text-green-400 border border-green-800 hover:bg-green-600/30 transition duration-200"
                      >
                        Approve
                      </button>
                      <button
                        onClick={() => handleOpenReject(req.id)}
                        className="px-3 py-1.5 text-xs font-bold rounded-lg bg-red-600/20 text-red-400 border border-red-800 hover:bg-red-600/30 transition duration-200"
                      >
                        Reject
                      </button>
                    </div>
                  )}
                </div>
              </div>

              {/* Items List */}
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

              {/* Admin Comment Displays */}
              {req.adminComment && (
                <div className="mt-4 p-3.5 rounded-xl bg-gray-950/20 border border-gray-800/40 text-sm text-gray-300">
                  <span className="font-semibold text-gray-400 text-xs block uppercase tracking-wider mb-1">Feedback/Rejection Comment</span>
                  {req.adminComment}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* Reject Modal */}
      {showRejectModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md glass-card p-6 rounded-2xl border border-gray-800 text-left">
            <h3 className="text-lg font-bold text-white mb-4">Reject Request #{rejectId}</h3>
            <form onSubmit={handleRejectSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-2">Reason for Rejection</label>
                <textarea
                  value={rejectComment}
                  onChange={(e) => setRejectComment(e.target.value)}
                  className="w-full h-28 px-3 py-2 rounded-lg glass-input text-sm text-white focus:outline-none"
                  placeholder="e.g. Requested quantity exceeds departmental allowance / Incorrect details"
                  required
                />
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-gray-800/60">
                <button
                  type="button"
                  onClick={() => setShowRejectModal(false)}
                  className="px-4 py-2 rounded-lg bg-gray-950 border border-gray-800 text-xs font-semibold text-gray-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-red-650 hover:bg-red-600 text-xs font-semibold text-white transition duration-200"
                >
                  Confirm Rejection
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManageRequestsPage;
