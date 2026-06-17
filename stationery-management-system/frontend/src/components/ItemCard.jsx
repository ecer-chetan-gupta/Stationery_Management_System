import React from 'react';

const ItemCard = ({ item, onAddToRequest, isStudent }) => {
  const isLowStock = item.lowStock || (item.availableQuantity <= item.minimumQuantity);

  return (
    <div className={`glass-card p-6 rounded-2xl flex flex-col justify-between text-left ${
      isLowStock ? 'border-red-500/30 shadow-red-950/20' : ''
    }`}>
      <div>
        <div className="flex justify-between items-start mb-3">
          <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-gray-950 border border-gray-800 text-gray-300">
            {item.category}
          </span>
          {isLowStock && (
            <span className="px-2 py-0.5 text-xs font-semibold rounded-md bg-red-950/40 border border-red-800 text-red-400">
              Low Stock
            </span>
          )}
        </div>
        
        <h3 className="text-lg font-bold text-white mb-1">{item.name}</h3>
        <p className="text-xs text-gray-400 mb-4">
          Unit size: <span className="text-gray-300 font-medium">{item.unit || 'Piece'}</span>
        </p>
      </div>

      <div>
        <div className="flex justify-between items-end mb-4 border-t border-gray-800/40 pt-4">
          <div>
            <p className="text-xs text-gray-500">Min. Threshold</p>
            <p className="text-sm font-semibold text-gray-300">{item.minimumQuantity} units</p>
          </div>
          <div className="text-right">
            <p className="text-xs text-gray-500">Available Stock</p>
            <p className={`text-base font-bold ${isLowStock ? 'text-red-400' : 'text-green-400'}`}>
              {item.availableQuantity} {item.unit || 'pcs'}
            </p>
          </div>
        </div>

        {isStudent && (
          <button
            onClick={() => onAddToRequest(item)}
            disabled={item.availableQuantity <= 0}
            className="w-full py-2.5 px-4 text-xs font-semibold rounded-lg bg-primary-600 hover:bg-primary-500 text-white transition duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {item.availableQuantity <= 0 ? 'Out of Stock' : 'Add to Request'}
          </button>
        )}
      </div>
    </div>
  );
};

export default ItemCard;
