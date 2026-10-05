import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { resourceApi } from '../api/resourceApi';
import { bookingApi } from '../api/bookingApi';
import { ApiError, TimeSlot } from '../types';
import { Calendar as CalendarIcon, Clock, ChevronLeft, ChevronRight, CheckCircle2, XCircle, AlertTriangle, Loader2 } from 'lucide-react';

export const ResourceDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const resourceId = Number(id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [selectedDate, setSelectedDate] = useState<string>(
    new Date().toISOString().split('T')[0]
  );
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null);
  const [durationMinutes, setDurationMinutes] = useState<number>(30);
  const [notes, setNotes] = useState('');
  const [bookingError, setBookingError] = useState<string | null>(null);
  const [bookingSuccess, setBookingSuccess] = useState<boolean>(false);

  // Fetch Resource Details
  const { data: resource, isLoading: isResourceLoading } = useQuery({
    queryKey: ['resource', resourceId],
    queryFn: () => resourceApi.getResourceById(resourceId),
    enabled: !isNaN(resourceId),
  });

  // Calculate start & end UTC for selected date
  const startUTC = `${selectedDate}T00:00:00Z`;
  const endUTC = `${selectedDate}T23:59:59Z`;

  // Fetch Availability
  const { data: availability, isLoading: isAvailabilityLoading } = useQuery({
    queryKey: ['availability', resourceId, selectedDate],
    queryFn: () => resourceApi.getAvailability(resourceId, startUTC, endUTC),
    enabled: !isNaN(resourceId) && !!resource,
  });

  // Create Booking Mutation
  const bookingMutation = useMutation({
    mutationFn: (payload: { resourceId: number; startTime: string; endTime: string; notes?: string }) =>
      bookingApi.createBooking(payload),
    onSuccess: () => {
      setBookingSuccess(true);
      setBookingError(null);
      setSelectedSlot(null);
      setNotes('');
      queryClient.invalidateQueries({ queryKey: ['availability', resourceId] });
      queryClient.invalidateQueries({ queryKey: ['myBookings'] });
    },
    onError: (err: ApiError) => {
      setBookingError(err.message || 'Failed to complete booking.');
    },
  });

  const handleSlotSelect = (slot: TimeSlot) => {
    setSelectedSlot(slot);
    setBookingError(null);
    setBookingSuccess(false);
    if (resource) {
      setDurationMinutes(resource.minBookingDurationMinutes);
    }
  };

  const handleConfirmBooking = () => {
    if (!selectedSlot || !resource) return;

    // Calculate end time based on selected start slot and duration
    const startInst = new Date(selectedSlot.startTime);
    const endInst = new Date(startInst.getTime() + durationMinutes * 60 * 1000);

    bookingMutation.mutate({
      resourceId: resource.id,
      startTime: startInst.toISOString(),
      endTime: endInst.toISOString(),
      notes,
    });
  };

  const handleDateChange = (days: number) => {
    const d = new Date(selectedDate);
    d.setDate(d.getDate() + days);
    setSelectedDate(d.toISOString().split('T')[0]);
    setSelectedSlot(null);
    setBookingError(null);
  };

  if (isResourceLoading) {
    return (
      <div className="flex justify-center items-center py-32">
        <Loader2 className="w-10 h-10 animate-spin text-sky-400" />
      </div>
    );
  }

  if (!resource) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-16 text-center space-y-4">
        <h2 className="text-2xl font-bold text-white">Resource Not Found</h2>
        <button
          onClick={() => navigate('/resources')}
          className="px-4 py-2 bg-sky-600 text-white rounded-xl text-sm font-semibold"
        >
          Back to Resources
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Back Button & Resource Header */}
      <div className="space-y-4">
        <button
          onClick={() => navigate('/resources')}
          className="inline-flex items-center space-x-2 text-sm text-slate-400 hover:text-white transition-colors"
        >
          <ChevronLeft className="w-4 h-4" />
          <span>Back to Resources</span>
        </button>

        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 space-y-6">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div>
              <div className="flex items-center space-x-3">
                <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-1 rounded-md bg-sky-500/10 text-sky-400 border border-sky-500/20">
                  {resource.type}
                </span>
                <span className="text-xs font-semibold text-slate-400 bg-slate-950 px-2.5 py-1 rounded-md border border-slate-800">
                  Cap: {resource.capacity} people
                </span>
              </div>
              <h1 className="text-3xl font-bold text-white mt-2">{resource.name}</h1>
              <p className="text-slate-400 text-sm mt-1">{resource.description}</p>
            </div>

            <div className="grid grid-cols-2 gap-3 text-xs text-slate-300 bg-slate-950 p-4 rounded-2xl border border-slate-800">
              <div>
                <p className="text-slate-500 font-semibold uppercase">Slot Step</p>
                <p className="font-bold text-white mt-0.5">{resource.slotGranularityMinutes} mins</p>
              </div>
              <div>
                <p className="text-slate-500 font-semibold uppercase">Duration</p>
                <p className="font-bold text-white mt-0.5">
                  {resource.minBookingDurationMinutes}m - {resource.maxBookingDurationMinutes}m
                </p>
              </div>
            </div>
          </div>

          {/* Weekly Opening Hours Pills */}
          <div className="pt-4 border-t border-slate-800/80">
            <h4 className="text-xs font-semibold text-slate-400 uppercase tracking-wider mb-3">Weekly Schedule</h4>
            <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-7 gap-2">
              {resource.openingHours.map((oh) => (
                <div
                  key={oh.dayOfWeek}
                  className={`p-2.5 rounded-xl border text-center text-xs ${
                    oh.isClosed
                      ? 'bg-slate-950/40 border-slate-800 text-slate-600'
                      : 'bg-slate-950 border-slate-800 text-slate-300'
                  }`}
                >
                  <p className="font-bold uppercase text-[11px]">{oh.dayOfWeek.slice(0, 3)}</p>
                  <p className="mt-1 text-[11px] font-medium">
                    {oh.isClosed ? 'Closed' : `${oh.openTime.slice(0, 5)} - ${oh.closeTime.slice(0, 5)}`}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Booking Status Feedback Alerts */}
      {bookingSuccess && (
        <div className="p-4 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 text-emerald-300 flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-400" />
            <span className="font-medium text-sm">Booking reserved successfully! Check "My Bookings" for details.</span>
          </div>
          <button
            onClick={() => setBookingSuccess(false)}
            className="text-xs font-semibold underline hover:text-white"
          >
            Dismiss
          </button>
        </div>
      )}

      {bookingError && (
        <div className="p-4 rounded-2xl bg-rose-500/10 border border-rose-500/20 text-rose-300 flex items-start space-x-3">
          <AlertTriangle className="w-5 h-5 text-rose-400 shrink-0 mt-0.5" />
          <div className="space-y-1 text-sm">
            <p className="font-semibold text-rose-200">Reservation Failed</p>
            <p>{bookingError}</p>
          </div>
        </div>
      )}

      {/* Date Navigation & Availability Grid */}
      <div className="bg-slate-900 border border-slate-800 rounded-3xl p-8 space-y-6">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-6 border-b border-slate-800">
          <div className="flex items-center space-x-3">
            <CalendarIcon className="w-5 h-5 text-sky-400" />
            <h2 className="text-xl font-bold text-white">Availability Grid</h2>
          </div>

          <div className="flex items-center space-x-3">
            <button
              onClick={() => handleDateChange(-1)}
              className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
            <input
              type="date"
              value={selectedDate}
              onChange={(e) => {
                setSelectedDate(e.target.value);
                setSelectedSlot(null);
              }}
              className="bg-slate-950 border border-slate-800 rounded-xl px-3 py-1.5 text-white text-sm font-semibold focus:outline-none focus:border-sky-500"
            />
            <button
              onClick={() => handleDateChange(1)}
              className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-colors"
            >
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Legend */}
        <div className="flex items-center space-x-6 text-xs text-slate-400">
          <div className="flex items-center space-x-2">
            <div className="w-3.5 h-3.5 rounded bg-emerald-500/20 border border-emerald-500/40"></div>
            <span>Available</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-3.5 h-3.5 rounded bg-rose-500/20 border border-rose-500/40"></div>
            <span>Booked</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-3.5 h-3.5 rounded bg-sky-600"></div>
            <span>Selected</span>
          </div>
        </div>

        {/* Time Slots Grid */}
        {isAvailabilityLoading ? (
          <div className="flex justify-center items-center py-16">
            <Loader2 className="w-8 h-8 animate-spin text-sky-400" />
          </div>
        ) : !availability || (availability.freeSlots.length === 0 && availability.bookedSlots.length === 0) ? (
          <div className="text-center py-12 text-slate-400 text-sm">
            Resource is closed or has no available operation hours on this date ({selectedDate}).
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
            {availability.freeSlots.map((slot) => {
              const startStr = new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
              const isSelected = selectedSlot?.startTime === slot.startTime;

              return (
                <button
                  key={slot.startTime}
                  onClick={() => handleSlotSelect(slot)}
                  className={`p-3 rounded-xl border text-center transition-all flex flex-col items-center justify-center space-y-1 ${
                    isSelected
                      ? 'bg-sky-600 text-white border-sky-400 shadow-lg shadow-sky-600/30 ring-2 ring-sky-400'
                      : 'bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-300 border-emerald-500/20'
                  }`}
                >
                  <Clock className="w-4 h-4 opacity-75" />
                  <span className="font-bold text-sm">{startStr}</span>
                  <span className="text-[10px] font-medium opacity-80">Free</span>
                </button>
              );
            })}

            {availability.bookedSlots.map((slot) => {
              const startStr = new Date(slot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
              const endStr = new Date(slot.endTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

              return (
                <div
                  key={slot.startTime}
                  className="p-3 rounded-xl border bg-rose-500/10 border-rose-500/20 text-rose-400 opacity-60 text-center flex flex-col items-center justify-center space-y-1 cursor-not-allowed"
                >
                  <XCircle className="w-4 h-4" />
                  <span className="font-bold text-sm">{startStr}</span>
                  <span className="text-[10px] font-medium">Until {endStr}</span>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Reservation Drawer / Modal when slot selected */}
      {selectedSlot && (
        <div className="fixed inset-0 z-50 bg-black/70 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 max-w-md w-full rounded-3xl p-6 space-y-6 shadow-2xl">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="text-xl font-bold text-white">Confirm Reservation</h3>
              <button
                onClick={() => setSelectedSlot(null)}
                className="text-slate-400 hover:text-white text-sm font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4">
              <div className="p-4 rounded-2xl bg-slate-950 border border-slate-800 space-y-2 text-sm">
                <p className="text-slate-400">Resource: <span className="text-white font-semibold">{resource.name}</span></p>
                <p className="text-slate-400">Date: <span className="text-white font-semibold">{selectedDate}</span></p>
                <p className="text-slate-400">
                  Start Time:{' '}
                  <span className="text-sky-400 font-bold">
                    {new Date(selectedSlot.startTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                  Duration (Minutes)
                </label>
                <select
                  value={durationMinutes}
                  onChange={(e) => setDurationMinutes(Number(e.target.value))}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl py-2.5 px-3 text-white text-sm focus:outline-none focus:border-sky-500"
                >
                  {Array.from({
                    length: Math.floor(
                      (resource.maxBookingDurationMinutes - resource.minBookingDurationMinutes) /
                        resource.slotGranularityMinutes
                    ) + 1,
                  }).map((_, idx) => {
                    const duration = resource.minBookingDurationMinutes + idx * resource.slotGranularityMinutes;
                    return (
                      <option key={duration} value={duration}>
                        {duration} Minutes ({duration / 60} hrs)
                      </option>
                    );
                  })}
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                  Reservation Notes (Optional)
                </label>
                <textarea
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  placeholder="e.g. Project presentation with 5 attendees"
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 text-white text-sm focus:outline-none focus:border-sky-500 h-24 resize-none placeholder:text-slate-600"
                />
              </div>
            </div>

            <div className="flex items-center space-x-3 pt-2">
              <button
                onClick={() => setSelectedSlot(null)}
                className="w-1/2 py-3 bg-slate-800 hover:bg-slate-700 text-slate-300 font-semibold text-sm rounded-xl transition-all"
              >
                Cancel
              </button>
              <button
                onClick={handleConfirmBooking}
                disabled={bookingMutation.isPending}
                className="w-1/2 py-3 bg-sky-600 hover:bg-sky-500 text-white font-semibold text-sm rounded-xl shadow-lg shadow-sky-600/25 transition-all flex items-center justify-center space-x-2"
              >
                {bookingMutation.isPending ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  <span>Confirm Booking</span>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
