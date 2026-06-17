import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { toast } from 'react-toastify';

const RegisterPage = () => {
  const { register: registerUser } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      role: 'STUDENT'
    }
  });

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await registerUser(data.email, data.password, data.fullName, data.role);
      toast.success('Registration successful! Please sign in.');
      navigate('/login');
    } catch (err) {
      if (typeof err === 'object') {
        Object.keys(err).forEach((key) => {
          toast.error(`${key}: ${err[key]}`);
        });
      } else {
        toast.error(err);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8 mt-4">
      <div className="max-w-md w-full space-y-8 glass-card p-10 rounded-2xl">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-white">
            Create an Account
          </h2>
          <p className="mt-2 text-center text-sm text-gray-400">
            Join the Stationery Management System
          </p>
        </div>
        <form className="mt-8 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-4">
            <div>
              <label htmlFor="fullName" className="sr-only">Full Name</label>
              <input
                id="fullName"
                type="text"
                placeholder="Full Name"
                className={`w-full px-4 py-3 rounded-lg glass-input text-white text-sm ${errors.fullName ? 'border-red-500' : ''}`}
                {...register('fullName', { required: 'Full name is required' })}
              />
              {errors.fullName && <p className="mt-1 text-xs text-red-400">{errors.fullName.message}</p>}
            </div>

            <div>
              <label htmlFor="email-address" className="sr-only">Email address</label>
              <input
                id="email-address"
                type="email"
                placeholder="Email address"
                className={`w-full px-4 py-3 rounded-lg glass-input text-white text-sm ${errors.email ? 'border-red-500' : ''}`}
                {...register('email', { 
                  required: 'Email is required',
                  pattern: { value: /^\S+@\S+$/i, message: 'Invalid email address' }
                })}
              />
              {errors.email && <p className="mt-1 text-xs text-red-400">{errors.email.message}</p>}
            </div>

            <div>
              <label htmlFor="password" className="sr-only">Password</label>
              <input
                id="password"
                type="password"
                placeholder="Password (min 8 characters)"
                className={`w-full px-4 py-3 rounded-lg glass-input text-white text-sm ${errors.password ? 'border-red-500' : ''}`}
                {...register('password', { 
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Password must be at least 8 characters' }
                })}
              />
              {errors.password && <p className="mt-1 text-xs text-red-400">{errors.password.message}</p>}
            </div>

            <div>
              <label htmlFor="role" className="block text-sm font-medium text-gray-300 mb-1 text-left">
                Account Role
              </label>
              <select
                id="role"
                className="w-full px-4 py-3 rounded-lg glass-input text-white text-sm"
                {...register('role', { required: 'Role is required' })}
              >
                <option value="STUDENT" className="bg-gray-900">Student</option>
                <option value="ADMIN" className="bg-gray-900">Administrator</option>
              </select>
              {errors.role && <p className="mt-1 text-xs text-red-400">{errors.role.message}</p>}
            </div>
          </div>

          <div className="pt-2">
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-3 px-4 border border-transparent text-sm font-semibold rounded-lg text-white bg-primary-600 hover:bg-primary-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 transition duration-200 disabled:opacity-50"
            >
              {loading ? 'Creating Account...' : 'Register'}
            </button>
          </div>
        </form>

        <div className="text-center mt-4">
          <p className="text-sm text-gray-400">
            Already have an account?{' '}
            <Link to="/login" className="font-semibold text-primary-400 hover:text-primary-300">
              Sign in here
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
