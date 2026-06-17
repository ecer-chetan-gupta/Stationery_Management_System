import React, { createContext, useContext, useState, useEffect } from 'react';
import { jwtDecode } from 'jwt-decode';
import api from '../api/axiosConfig';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    if (storedToken) {
      try {
        const decoded = jwtDecode(storedToken);
        const isExpired = decoded.exp * 1000 < Date.now();
        if (isExpired) {
          localStorage.removeItem('token');
          setUser(null);
          setToken(null);
        } else {
          setToken(storedToken);
          setUser({
            id: decoded.userId,
            email: decoded.sub,
            role: decoded.role,
            fullName: decoded.fullName,
          });
        }
      } catch (err) {
        console.error("Token decoding failed", err);
        localStorage.removeItem('token');
      }
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    try {
      const response = await api.post('/api/auth/login', { email, password });
      const { token: jwtToken } = response.data;
      
      localStorage.setItem('token', jwtToken);
      setToken(jwtToken);
      
      const decoded = jwtDecode(jwtToken);
      const loggedUser = {
        id: decoded.userId,
        email: decoded.sub,
        role: decoded.role,
        fullName: decoded.fullName,
      };
      
      setUser(loggedUser);
      return loggedUser;
    } catch (error) {
      throw error.response?.data?.message || error.message || 'Login failed';
    }
  };

  const register = async (email, password, fullName, role) => {
    try {
      const response = await api.post('/api/auth/register', { email, password, fullName, role });
      return response.data;
    } catch (error) {
      // Map error object if validation errors are returned, or single message
      throw error.response?.data || 'Registration failed';
    }
  };

  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, register, logout }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
