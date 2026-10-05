import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { bookingApi } from '../api/bookingApi';
import { BookingStatus } from '../types';
import { Shield, XCircle, CheckCircle2, Loader2 } from 'lucide-react';

export const AdminBookingsPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [selectedStatus, setSelectedStatus] = useState<BookingStatus | 'ALL'>('ALL');

  const { data, isLoading, isError } = useQuery({
    queryKey: ['adminBookings', selectedStatus],
    queryFn: () =>
      bookingApi.getAllBookings({
        status: selectedStatus === 'ALL' ? undefined : selectedStatus,
        page: 0,
        size: 50,
      }),
  });

  const cancelMutation = useMutation({
    mutationFn: (id: number) => bookingApi.cancelBooking(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminBookings'] });
    },
  });

  const bookings = data?.content || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <div className="inline-flex items-center space-x-1.5 text-xs font-semibold px-2.5 py-1 rounded-full bg-amber-500/10 text-amber-400 border border-amber-500/20 mb-2">
            <Shield className="w-3.5 h-3.5" />
            <span>Admin Control Panel</span>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">All System Reservations</h1>
        </div>

        <div className="flex items-center space-x-2">
          {(['ALL', 'CONFIRMED', 'CANCELLED'] as const).map((st) => (
            <button
              key={st}
              onClick={() => setSelectedStatus(st)}
              className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold transition-all ${
                selectedStatus === st
                  ? 'bg-amber-500/20 text-amber-300 border border-amber-500/40 shadow-sm'
                  : 'bg-slate-900 text-slate-400 border border-slate-800 hover:text-white'
              }`}
            >
              {st}
            </button>
          ))}
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <Loader2 className="w-10 h-10 animate-spin text-sky-400" />
        </div>
      ) : isError ? (
        <div className="p-8 text-center bg-rose-500/10 border border-rose-500/20 rounded-2xl text-rose-400">
          Failed to load bookings.
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 text-xs font-semibold uppercase tracking-wider text-slate-400 border-b border-slate-800">
              <tr>
                <th className="px-6 py-4">ID</th>
                <th className="px-6 py-4">Resource</th>
                <th className="px-6 py-4">User</th>
                <th className="px-6 py-4">Time Slot (Local)</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {bookings.map((b) => {
                const startDate = new Date(b.startTime);
                const endDate = new Date(b.endTime);
                const isCancelled = b.status === 'CANCELLED';

                return (
                  <tr key={b.id} className="hover:bg-slate-800/40 transition-colors">
                    <td className="px-6 py-4 font-mono text-xs text-slate-500">#{b.id}</td>
                    <td className="px-6 py-4 font-bold text-white">{b.resourceName}</td>
                    <td className="px-6 py-4">
                      <p className="font-semibold text-slate-200">{b.userFullName}</p>
                      <p className="text-xs text-slate-500">{b.userEmail}</p>
                    </td>
                    <td className="px-6 py-4 text-xs font-mono">
                      {startDate.toLocaleDateString()} {startDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} - {endDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </td>
                    <td className="px-6 py-4">
                      <span
                        className={`inline-flex items-center space-x-1 px-2.5 py-1 rounded-full text-xs font-bold ${
                          isCancelled
                            ? 'bg-rose-500/10 text-rose-400 border border-rose-500/20'
                            : 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                        }`}
                      >
                        {isCancelled ? <XCircle className="w-3.5 h-3.5" /> : <CheckCircle2 className="w-3.5 h-3.5" />}
                        <span>{b.status}</span>
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      {!isCancelled && (
                        <button
                          onClick={() => {
                            if (window.confirm(`Cancel reservation #${b.id} for ${b.userFullName}?`)) {
                              cancelMutation.mutate(b.id);
                            }
                          }}
                          className="px-3 py-1.5 bg-rose-500/10 hover:bg-rose-500/20 text-rose-400 border border-rose-500/20 rounded-lg text-xs font-semibold transition-all"
                        >
                          Cancel
                        </button>
                      )}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
