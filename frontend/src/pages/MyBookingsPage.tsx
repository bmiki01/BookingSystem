import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { bookingApi } from '../api/bookingApi';
import { ApiError } from '../types';
import { Clock, Calendar, AlertCircle, XCircle, CheckCircle2, Loader2 } from 'lucide-react';

export const MyBookingsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [cancellingId, setCancellingId] = useState<number | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['myBookings'],
    queryFn: () => bookingApi.getMyBookings(0, 50),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: number) => bookingApi.cancelBooking(id),
    onSuccess: () => {
      setCancellingId(null);
      setErrorMsg(null);
      queryClient.invalidateQueries({ queryKey: ['myBookings'] });
    },
    onError: (err: ApiError) => {
      setCancellingId(null);
      setErrorMsg(err.message || 'Failed to cancel booking.');
    },
  });

  const bookings = data?.content || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight">My Reservations</h1>
          <p className="text-slate-400 text-sm mt-1">Manage and review your active and past time slots</p>
        </div>
      </div>

      {errorMsg && (
        <div className="p-4 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center space-x-3">
          <AlertCircle className="w-5 h-5 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <Loader2 className="w-10 h-10 animate-spin text-sky-400" />
        </div>
      ) : isError ? (
        <div className="p-8 text-center bg-rose-500/10 border border-rose-500/20 rounded-2xl text-rose-400">
          Failed to load bookings. Please refresh or try again later.
        </div>
      ) : bookings.length === 0 ? (
        <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-3xl space-y-3">
          <Clock className="w-12 h-12 text-slate-600 mx-auto" />
          <h3 className="text-lg font-semibold text-white">No bookings yet</h3>
          <p className="text-slate-400 text-sm">Head over to the Resources catalog to reserve your first slot.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {bookings.map((b) => {
            const startDate = new Date(b.startTime);
            const endDate = new Date(b.endTime);
            const isCancelled = b.status === 'CANCELLED';

            return (
              <div
                key={b.id}
                className={`bg-slate-900 border rounded-2xl p-6 transition-all space-y-4 ${
                  isCancelled ? 'border-slate-800/60 opacity-60' : 'border-slate-800 hover:border-sky-500/30'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-xl font-bold text-white">{b.resourceName}</h3>
                    <p className="text-xs text-slate-500 mt-0.5">Booking ID: #{b.id}</p>
                  </div>
                  <span
                    className={`inline-flex items-center space-x-1 px-3 py-1 rounded-full text-xs font-bold ${
                      isCancelled
                        ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                        : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                    }`}
                  >
                    {isCancelled ? <XCircle className="w-3.5 h-3.5" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                    <span>{b.status}</span>
                  </span>
                </div>

                <div className="bg-slate-950 p-4 rounded-xl border border-slate-800/80 space-y-2 text-xs">
                  <div className="flex items-center space-x-2 text-slate-300">
                    <Calendar className="w-4 h-4 text-sky-400" />
                    <span className="font-semibold">
                      {startDate.toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' })}
                    </span>
                  </div>
                  <div className="flex items-center space-x-2 text-slate-300 pl-6">
                    <Clock className="w-3.5 h-3.5 text-indigo-400" />
                    <span>
                      {startDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                      {endDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  {b.notes && (
                    <div className="pt-2 border-t border-slate-800 text-slate-400 italic">
                      "{b.notes}"
                    </div>
                  )}
                </div>

                {!isCancelled && (
                  <div className="pt-2 flex justify-end">
                    <button
                      onClick={() => {
                        if (window.confirm('Are you sure you want to cancel this reservation?')) {
                          setCancellingId(b.id);
                          cancelMutation.mutate(b.id);
                        }
                      }}
                      disabled={cancellingId === b.id}
                      className="px-4 py-2 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 rounded-xl text-xs font-semibold transition-all flex items-center space-x-1.5"
                    >
                      {cancellingId === b.id ? (
                        <Loader2 className="w-3.5 h-3.5 animate-spin" />
                      ) : (
                        <XCircle className="w-3.5 h-3.5" />
                      )}
                      <span>Cancel Reservation</span>
                    </button>
                  </div>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
