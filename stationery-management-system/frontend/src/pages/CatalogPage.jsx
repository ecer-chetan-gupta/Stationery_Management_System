import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import ItemCard from '../components/ItemCard';
import { toast } from 'react-toastify';

const CATEGORIES = ['ALL', 'PAPER', 'PEN', 'PENCIL', 'NOTEBOOK', 'ERASER', 'OTHER'];

const CatalogPage = () => {
  const { user } = useAuth();
  const isStudent = user?.role === 'STUDENT';

  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(8);

  // Request basket state (for Student)
  const [basket, setBasket] = useState([]);

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
      toast.error('Failed to load stationery catalog');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [currentPage, selectedCategory, searchQuery]);

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handleCategorySelect = (category) => {
    setSelectedCategory(category);
    setCurrentPage(0);
  };

  // Basket management
  const addToBasket = (item) => {
    setBasket((prevBasket) => {
      const existing = prevBasket.find((i) => i.id === item.id);
      if (existing) {
        if (existing.reqQty >= item.availableQuantity) {
          toast.warning(`Cannot request more than available stock (${item.availableQuantity})`);
          return prevBasket;
        }
        return prevBasket.map((i) =>
          i.id === item.id ? { ...i, reqQty: i.reqQty + 1 } : i
        );
      } else {
        return [...prevBasket, { ...item, reqQty: 1 }];
      }
    });
    toast.info(`Added ${item.name} to request list`);
  };

  const updateBasketQty = (itemId, qty, maxStock) => {
    if (qty > maxStock) {
      toast.warning(`Quantity cannot exceed available stock (${maxStock})`);
      qty = maxStock;
    }
    if (qty < 1) qty = 1;
    setBasket((prev) =>
      prev.map((item) => (item.id === itemId ? { ...item, reqQty: qty } : item))
    );
  };

  const removeFromBasket = (itemId) => {
    setBasket((prev) => prev.filter((item) => item.id !== itemId));
  };

  const submitRequest = async () => {
    if (basket.length === 0) return;
    
    try {
      const payload = {
        items: basket.map((item) => ({
          itemId: item.id,
          quantity: item.reqQty,
        })),
      };

      await api.post('/api/requests', payload);
      toast.success('Stationery request submitted successfully!');
      setBasket([]);
    } catch (err) {
      console.error(err);
      toast.error(err.response?.data || 'Failed to submit request');
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div className="mb-8 text-left">
        <h1 className="text-3xl font-extrabold text-white">Stationery Catalog</h1>
        <p className="text-gray-400 mt-1">Browse available stationery, filter by categories, and request items.</p>
      </div>

      <div className="flex flex-col lg:flex-row gap-8">
        {/* Left: Search, Filter, Grid */}
        <div className="flex-1">
          {/* Controls */}
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
            {/* Search */}
            <div className="relative flex-1 max-w-md">
              <input
                type="text"
                value={searchQuery}
                onChange={handleSearchChange}
                placeholder="Search by name..."
                className="w-full px-4 py-2.5 rounded-lg glass-input text-white text-sm"
              />
              {searchQuery && (
                <button
                  onClick={() => setSearchQuery('')}
                  className="absolute right-3 top-3.5 text-xs text-gray-400 hover:text-white"
                >
                  Clear
                </button>
              )}
            </div>

            {/* Category Pills */}
            <div className="flex flex-wrap gap-2">
              {CATEGORIES.map((cat) => (
                <button
                  key={cat}
                  onClick={() => handleCategorySelect(cat)}
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

          {/* Loader or Grid */}
          {loading ? (
            <div className="flex justify-center items-center py-20">
              <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
            </div>
          ) : items.length === 0 ? (
            <div className="glass-card p-12 text-center rounded-2xl">
              <span className="text-4xl block mb-2">📁</span>
              <p className="text-gray-400 font-medium">No stationery items found.</p>
            </div>
          ) : (
            <div>
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {items.map((item) => (
                  <ItemCard
                    key={item.id}
                    item={item}
                    onAddToRequest={addToBasket}
                    isStudent={isStudent}
                  />
                ))}
              </div>

              {/* Pagination (only when not searching) */}
              {searchQuery.trim() === '' && totalPages > 1 && (
                <div className="flex justify-center space-x-2 mt-8">
                  <button
                    disabled={currentPage === 0}
                    onClick={() => setCurrentPage((p) => p - 1)}
                    className="px-3.5 py-1.5 rounded-lg bg-gray-950 border border-gray-800 text-gray-400 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition"
                  >
                    Prev
                  </button>
                  <span className="px-4 py-1.5 text-sm text-gray-400 font-semibold self-center">
                    Page {currentPage + 1} of {totalPages}
                  </span>
                  <button
                    disabled={currentPage >= totalPages - 1}
                    onClick={() => setCurrentPage((p) => p + 1)}
                    className="px-3.5 py-1.5 rounded-lg bg-gray-950 border border-gray-800 text-gray-400 hover:text-white disabled:opacity-30 disabled:cursor-not-allowed transition"
                  >
                    Next
                  </button>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Right: Request Basket (only for Students) */}
        {isStudent && (
          <div className="w-full lg:w-80 shrink-0">
            <div className="glass-card p-6 rounded-2xl sticky top-24 text-left">
              <h2 className="text-lg font-bold text-white mb-4 flex items-center justify-between">
                <span>Request Basket</span>
                {basket.length > 0 && (
                  <span className="px-2 py-0.5 text-xs bg-primary-600 text-white rounded-full">
                    {basket.length}
                  </span>
                )}
              </h2>

              {basket.length === 0 ? (
                <div className="py-8 text-center border-2 border-dashed border-gray-800 rounded-xl">
                  <p className="text-sm text-gray-500">Your request list is empty.</p>
                  <p className="text-xs text-gray-600 mt-1">Add items from the catalog.</p>
                </div>
              ) : (
                <div className="space-y-4">
                  <div className="max-h-[300px] overflow-y-auto pr-1 space-y-3">
                    {basket.map((item) => (
                      <div key={item.id} className="p-3 bg-gray-950/40 rounded-xl border border-gray-800/80 flex flex-col justify-between">
                        <div className="flex justify-between items-start mb-2">
                          <span className="text-xs font-semibold text-white truncate max-w-[140px]">{item.name}</span>
                          <button
                            onClick={() => removeFromBasket(item.id)}
                            className="text-xs text-red-500 hover:text-red-400"
                          >
                            Remove
                          </button>
                        </div>
                        <div className="flex items-center justify-between">
                          <span className="text-xs text-gray-500">Qty:</span>
                          <input
                            type="number"
                            min="1"
                            max={item.availableQuantity}
                            value={item.reqQty}
                            onChange={(e) => updateBasketQty(item.id, parseInt(e.target.value) || 1, item.availableQuantity)}
                            className="w-16 px-2 py-1 rounded bg-black border border-gray-800 text-white text-xs focus:outline-none focus:border-primary-500"
                          />
                        </div>
                      </div>
                    ))}
                  </div>

                  <button
                    onClick={submitRequest}
                    className="w-full py-3 px-4 text-xs font-bold rounded-lg bg-primary-600 hover:bg-primary-500 text-white transition duration-200 shadow-md"
                  >
                    Submit Request
                  </button>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CatalogPage;
