import React, { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { toast } from 'react-toastify';

const CATEGORIES = ['PAPER', 'PEN', 'PENCIL', 'NOTEBOOK', 'ERASER', 'OTHER'];

const ManageInventoryPage = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(10);

  // Modal States
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [currentItem, setCurrentItem] = useState(null);

  // Form States
  const [formData, setFormData] = useState({
    name: '',
    category: 'PAPER',
    unit: 'Piece',
    availableQuantity: 0,
    minimumQuantity: 0,
  });

  const fetchItems = async () => {
    try {
      setLoading(true);
      if (searchQuery.trim() !== '') {
        const res = await api.get(`/api/inventory/search?q=${searchQuery}`);
        setItems(res.data);
        setTotalPages(1);
        setCurrentPage(0);
      } else {
        const categoryParam = selectedCategory !== 'ALL' ? `&category=${selectedCategory}` : '';
        const res = await api.get(`/api/inventory?page=${currentPage}&size=${pageSize}${categoryParam}`);
        setItems(res.data.content || []);
        setTotalPages(res.data.totalPages || 0);
      }
    } catch (err) {
      console.error(err);
      toast.error('Failed to fetch inventory items');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [currentPage, selectedCategory, searchQuery]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'availableQuantity' || name === 'minimumQuantity' ? parseInt(value) || 0 : value,
    }));
  };

  const handleOpenAddModal = () => {
    setFormData({
      name: '',
      category: 'PAPER',
      unit: 'Piece',
      availableQuantity: 0,
      minimumQuantity: 0,
    });
    setShowAddModal(true);
  };

  const handleOpenEditModal = (item) => {
    setCurrentItem(item);
    setFormData({
      name: item.name,
      category: item.category,
      unit: item.unit || 'Piece',
      availableQuantity: item.availableQuantity,
      minimumQuantity: item.minimumQuantity,
    });
    setShowEditModal(true);
  };

  const handleAddItem = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      toast.error('Item name is required');
      return;
    }
    try {
      await api.post('/api/inventory', formData);
      toast.success('Stationery item added successfully!');
      setShowAddModal(false);
      fetchItems();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to add item');
    }
  };

  const handleEditItem = async (e) => {
    e.preventDefault();
    if (!formData.name.trim()) {
      toast.error('Item name is required');
      return;
    }
    try {
      await api.put(`/api/inventory/${currentItem.id}`, formData);
      toast.success('Stationery item updated successfully!');
      setShowEditModal(false);
      fetchItems();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to update item');
    }
  };

  const handleDeleteItem = async (id) => {
    if (!window.confirm('Are you sure you want to delete this stationery item?')) return;
    try {
      await api.delete(`/api/inventory/${id}`);
      toast.success('Item deleted successfully!');
      fetchItems();
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data?.message || err.response?.data || 'Failed to delete item');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="flex justify-between items-center mb-8 text-left">
        <div>
          <h1 className="text-3xl font-extrabold text-white">Manage Inventory</h1>
          <p className="text-gray-400 mt-1">Add, update, or remove stock items in the system.</p>
        </div>
        <button
          onClick={handleOpenAddModal}
          className="px-4 py-2.5 bg-primary-600 hover:bg-primary-500 text-white font-semibold rounded-lg text-sm transition duration-200 shadow-md"
        >
          Add New Item
        </button>
      </div>

      {/* Controls */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
        <div className="relative flex-1 max-w-md">
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => { setSearchQuery(e.target.value); setCurrentPage(0); }}
            placeholder="Search stock by name..."
            className="w-full px-4 py-2.5 rounded-lg glass-input text-white text-sm"
          />
        </div>

        <div className="flex flex-wrap gap-2">
          <button
            onClick={() => { setSelectedCategory('ALL'); setCurrentPage(0); }}
            className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition duration-200 ${
              selectedCategory === 'ALL'
                ? 'bg-primary-600 text-white'
                : 'bg-gray-950 border border-gray-800 text-gray-400 hover:text-white'
            }`}
          >
            ALL
          </button>
          {CATEGORIES.map((cat) => (
            <button
              key={cat}
              onClick={() => { setSelectedCategory(cat); setCurrentPage(0); }}
              className={`px-3 py-1.5 text-xs font-semibold rounded-lg transition duration-200 ${
                selectedCategory === cat
                  ? 'bg-primary-600 text-white'
                  : 'bg-gray-950 border border-gray-800 text-gray-400 hover:text-white'
              }`}
            >
              {cat}
            </button>
          ))}
        </div>
      </div>

      {/* Grid or Table */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      ) : items.length === 0 ? (
        <div className="glass-card p-12 text-center rounded-2xl">
          <span className="text-4xl block mb-2">📁</span>
          <p className="text-gray-400 font-medium">No stock items found.</p>
        </div>
      ) : (
        <div className="glass-card rounded-2xl overflow-hidden border border-gray-800/80">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-gray-950/65 text-gray-400 text-xs uppercase tracking-wider border-b border-gray-850">
                  <th className="px-6 py-4">ID</th>
                  <th className="px-6 py-4">Item Name</th>
                  <th className="px-6 py-4">Category</th>
                  <th className="px-6 py-4">Unit</th>
                  <th className="px-6 py-4">Stock Level</th>
                  <th className="px-6 py-4 text-center">Status</th>
                  <th className="px-6 py-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-850">
                {items.map((item) => {
                  const isLow = item.lowStock || item.availableQuantity <= item.minimumQuantity;
                  return (
                    <tr key={item.id} className="hover:bg-gray-950/20 text-sm text-gray-200">
                      <td className="px-6 py-4 font-mono text-xs text-gray-500">#{item.id}</td>
                      <td className="px-6 py-4 font-semibold text-white">{item.name}</td>
                      <td className="px-6 py-4">
                        <span className="px-2 py-0.5 text-xs bg-gray-900 border border-gray-800 rounded-md text-gray-400">
                          {item.category}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-gray-400">{item.unit || 'Piece'}</td>
                      <td className="px-6 py-4 font-medium">
                        <span className={isLow ? 'text-red-400' : 'text-green-400'}>
                          {item.availableQuantity}
                        </span>
                        <span className="text-gray-500 text-xs"> / {item.minimumQuantity} threshold</span>
                      </td>
                      <td className="px-6 py-4 text-center">
                        {isLow ? (
                          <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-red-950/40 border border-red-800 text-red-400">
                            Low Stock
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-green-950/40 border border-green-800 text-green-400">
                            Healthy
                          </span>
                        )}
                      </td>
                      <td className="px-6 py-4 text-right space-x-2">
                        <button
                          onClick={() => handleOpenEditModal(item)}
                          className="px-3 py-1 text-xs font-semibold rounded bg-blue-600/20 text-blue-400 border border-blue-800 hover:bg-blue-600/30 transition"
                        >
                          Edit
                        </button>
                        <button
                          onClick={() => handleDeleteItem(item.id)}
                          className="px-3 py-1 text-xs font-semibold rounded bg-red-600/20 text-red-400 border border-red-800 hover:bg-red-600/30 transition"
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          {searchQuery.trim() === '' && totalPages > 1 && (
            <div className="bg-gray-950/30 px-6 py-4 border-t border-gray-850 flex justify-between items-center">
              <span className="text-xs text-gray-400 font-medium">
                Showing page {currentPage + 1} of {totalPages}
              </span>
              <div className="flex space-x-2">
                <button
                  disabled={currentPage === 0}
                  onClick={() => setCurrentPage((p) => p - 1)}
                  className="px-3 py-1 rounded bg-gray-950 border border-gray-800 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition"
                >
                  Prev
                </button>
                <button
                  disabled={currentPage >= totalPages - 1}
                  onClick={() => setCurrentPage((p) => p + 1)}
                  className="px-3 py-1 rounded bg-gray-950 border border-gray-800 text-xs text-gray-400 hover:text-white disabled:opacity-30 transition"
                >
                  Next
                </button>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Add Modal */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md glass-card p-6 rounded-2xl border border-gray-800 text-left">
            <h3 className="text-lg font-bold text-white mb-4">Add New Stationery Item</h3>
            <form onSubmit={handleAddItem} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Item Name</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                  placeholder="e.g. Blue Ballpoint Pen"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Category</label>
                  <select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                  >
                    {CATEGORIES.map((cat) => (
                      <option key={cat} value={cat} className="bg-gray-950">{cat}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Unit Size</label>
                  <input
                    type="text"
                    name="unit"
                    value={formData.unit}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                    placeholder="e.g. Pack, Piece, Box"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Quantity</label>
                  <input
                    type="number"
                    name="availableQuantity"
                    value={formData.availableQuantity}
                    onChange={handleInputChange}
                    min="0"
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Min Threshold</label>
                  <input
                    type="number"
                    name="minimumQuantity"
                    value={formData.minimumQuantity}
                    onChange={handleInputChange}
                    min="0"
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                    required
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-gray-800/60">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 rounded-lg bg-gray-950 border border-gray-800 text-xs font-semibold text-gray-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-primary-600 hover:bg-primary-500 text-xs font-semibold text-white transition duration-200"
                >
                  Save Item
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Edit Modal */}
      {showEditModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm">
          <div className="w-full max-w-md glass-card p-6 rounded-2xl border border-gray-800 text-left">
            <h3 className="text-lg font-bold text-white mb-4">Edit Stationery Item</h3>
            <form onSubmit={handleEditItem} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Item Name</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Category</label>
                  <select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                  >
                    {CATEGORIES.map((cat) => (
                      <option key={cat} value={cat} className="bg-gray-950">{cat}</option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Unit Size</label>
                  <input
                    type="text"
                    name="unit"
                    value={formData.unit}
                    onChange={handleInputChange}
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Quantity</label>
                  <input
                    type="number"
                    name="availableQuantity"
                    value={formData.availableQuantity}
                    onChange={handleInputChange}
                    min="0"
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Min Threshold</label>
                  <input
                    type="number"
                    name="minimumQuantity"
                    value={formData.minimumQuantity}
                    onChange={handleInputChange}
                    min="0"
                    className="w-full px-3 py-2 rounded-lg glass-input text-sm text-white"
                    required
                  />
                </div>
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-gray-800/60">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-4 py-2 rounded-lg bg-gray-950 border border-gray-800 text-xs font-semibold text-gray-400 hover:text-white"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 rounded-lg bg-primary-600 hover:bg-primary-500 text-xs font-semibold text-white transition duration-200"
                >
                  Update Item
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ManageInventoryPage;
