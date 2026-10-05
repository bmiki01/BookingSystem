import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { ApiError } from '../types';
import { Calendar, Lock, Mail, AlertCircle, Loader2 } from 'lucide-react';

const loginSchema = z.object({
  email: z.string().email('Please enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
});

type LoginFormInputs = z.infer<typeof loginSchema>;

export const LoginPage: React.FC = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormInputs>({
    resolver: zodResolver(loginSchema),
  });

  const onSubmit = async (data: LoginFormInputs) => {
    setIsSubmitting(true);
    setErrorMsg(null);
    try {
      await login(data.email, data.password);
      navigate('/resources');
    } catch (err) {
      const apiErr = err as ApiError;
      setErrorMsg(apiErr.message || 'Invalid email or password');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-4rem)] flex items-center justify-center p-4">
      <div className="max-w-md w-full bg-slate-900 border border-slate-800 rounded-3xl p-8 shadow-2xl space-y-6">
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-sky-500/10 text-sky-400 border border-sky-500/20 mb-2">
            <Calendar className="w-8 h-8" />
          </div>
          <h2 className="text-2xl font-bold text-white tracking-tight">Welcome Back</h2>
          <p className="text-slate-400 text-sm">Sign in to access your resource bookings</p>
        </div>

        {errorMsg && (
          <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-start space-x-3">
            <AlertCircle className="w-5 h-5 shrink-0 mt-0.5" />
            <span>{errorMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Email Address
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Mail className="w-5 h-5" />
              </div>
              <input
                {...register('email')}
                type="email"
                placeholder="user@example.com"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-3 pl-11 pr-4 text-white text-sm focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all placeholder:text-slate-600"
              />
            </div>
            {errors.email && <p className="text-rose-400 text-xs mt-1.5">{errors.email.message}</p>}
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
              Password
            </label>
            <div className="relative">
              <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
                <Lock className="w-5 h-5" />
              </div>
              <input
                {...register('password')}
                type="password"
                placeholder="••••••••"
                className="w-full bg-slate-950 border border-slate-800 rounded-xl py-3 pl-11 pr-4 text-white text-sm focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all placeholder:text-slate-600"
              />
            </div>
            {errors.password && <p className="text-rose-400 text-xs mt-1.5">{errors.password.message}</p>}
          </div>

          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full py-3.5 px-4 bg-sky-600 hover:bg-sky-500 active:bg-sky-700 font-semibold text-white text-sm rounded-xl shadow-lg shadow-sky-600/25 transition-all flex items-center justify-center space-x-2 disabled:opacity-50"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                <span>Signing In...</span>
              </>
            ) : (
              <span>Sign In</span>
            )}
          </button>
        </form>

        <div className="text-center pt-2 text-sm text-slate-400 border-t border-slate-800/80">
          Don't have an account?{' '}
          <Link to="/register" className="text-sky-400 font-semibold hover:text-sky-300 transition-colors">
            Register now
          </Link>
        </div>
      </div>
    </div>
  );
};
