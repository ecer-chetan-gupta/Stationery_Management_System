import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

// Pages
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import CatalogPage from './pages/CatalogPage';
import MyRequestsPage from './pages/MyRequestsPage';
import ManageInventoryPage from './pages/admin/ManageInventoryPage';
import ManageRequestsPage from './pages/admin/ManageRequestsPage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <div className="min-h-screen bg-transparent flex flex-col">
          <Navbar />

          <main className="flex-1 pb-16">
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />

              {/* Shared Authenticated Routes */}
              <Route element={<ProtectedRoute allowedRoles={['STUDENT', 'ADMIN']} />}>
                <Route path="/dashboard" element={<DashboardPage />} />
                <Route path="/catalog" element={<CatalogPage />} />
              </Route>

              {/* Student Only Routes */}
              <Route element={<ProtectedRoute allowedRoles={['STUDENT']} />}>
                <Route path="/my-requests" element={<MyRequestsPage />} />
              </Route>

              {/* Admin Only Routes */}
              <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
                <Route path="/admin/inventory" element={<ManageInventoryPage />} />
                <Route path="/admin/requests" element={<ManageRequestsPage />} />
              </Route>

              {/* Catch-all Redirect */}
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </main>

          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
            theme="light"
          />
        </div>
      </Router>
    </AuthProvider>
  );
}

export default App;
