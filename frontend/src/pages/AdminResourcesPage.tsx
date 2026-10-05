import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { resourceApi } from '../api/resourceApi';
import { Resource, ResourceRequest, OpeningHours } from '../types';
import { Plus, Edit2, Trash2, AlertCircle, Loader2 } from 'lucide-react';

const defaultOpeningHours: OpeningHours[] = [
  { dayOfWeek: 'MONDAY', openTime: '08:00', closeTime: '18:00', isClosed: false },
  { dayOfWeek: 'TUESDAY', openTime: '08:00', closeTime: '18:00', isClosed: false },
  { dayOfWeek: 'WEDNESDAY', openTime: '08:00', closeTime: '18:00', isClosed: false },
  { dayOfWeek: 'THURSDAY', openTime: '08:00', closeTime: '18:00', isClosed: false },
  { dayOfWeek: 'FRIDAY', openTime: '08:00', closeTime: '18:00', isClosed: false },
  { dayOfWeek: 'SATURDAY', openTime: '08:00', closeTime: '18:00', isClosed: true },
  { dayOfWeek: 'SUNDAY', openTime: '08:00', closeTime: '18:00', isClosed: true },
];

export const AdminResourcesPage: React.FC = () => {
  const queryClient = useQueryClient();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingResource, setEditingResource] = useState<Resource | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  // Form State
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [capacity, setCapacity] = useState(1);
  const [type, setType] = useState('ROOM');
  const [active, setActive] = useState(true);
  const [slotGranularityMinutes, setSlotGranularityMinutes] = useState(30);
  const [minBookingDurationMinutes, setMinBookingDurationMinutes] = useState(30);
  const [maxBookingDurationMinutes, setMaxBookingDurationMinutes] = useState(240);
  const [openingHours, setOpeningHours] = useState<OpeningHours[]>(defaultOpeningHours);

  const { data, isLoading, isError } = useQuery({
    queryKey: ['adminResources'],
    queryFn: () => resourceApi.getResources(0, 50, true),
  });

  const saveMutation = useMutation({
    mutationFn: (payload: ResourceRequest) => {
      if (editingResource) {
        return resourceApi.updateResource(editingResource.id, payload);
      }
      return resourceApi.createResource(payload);
    },
    onSuccess: () => {
      setIsModalOpen(false);
      resetForm();
      queryClient.invalidateQueries({ queryKey: ['adminResources'] });
      queryClient.invalidateQueries({ queryKey: ['resources'] });
    },
    onError: (err: any) => {
      setErrorMsg(err.message || 'Operation failed');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => resourceApi.deleteResource(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminResources'] });
      queryClient.invalidateQueries({ queryKey: ['resources'] });
    },
    onError: (err: any) => {
      alert(err.message || 'Delete failed');
    },
  });

  const openCreateModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const openEditModal = (r: Resource) => {
    setEditingResource(r);
    setName(r.name);
    setDescription(r.description || '');
    setCapacity(r.capacity);
    setType(r.type);
    setActive(r.active);
    setSlotGranularityMinutes(r.slotGranularityMinutes);
    setMinBookingDurationMinutes(r.minBookingDurationMinutes);
    setMaxBookingDurationMinutes(r.maxBookingDurationMinutes);
    setOpeningHours(r.openingHours && r.openingHours.length > 0 ? r.openingHours : defaultOpeningHours);
    setErrorMsg(null);
    setIsModalOpen(true);
  };

  const resetForm = () => {
    setEditingResource(null);
    setName('');
    setDescription('');
    setCapacity(1);
    setType('ROOM');
    setActive(true);
    setSlotGranularityMinutes(30);
    setMinBookingDurationMinutes(30);
    setMaxBookingDurationMinutes(240);
    setOpeningHours(defaultOpeningHours);
    setErrorMsg(null);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    saveMutation.mutate({
      name,
      description,
      capacity,
      type,
      active,
      slotGranularityMinutes,
      minBookingDurationMinutes,
      maxBookingDurationMinutes,
      openingHours,
    });
  };

  const resources = data?.content || [];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Resource Management</h1>
          <p className="text-slate-400 text-sm mt-1">Create and update resources, operational hours, and booking constraints</p>
        </div>
        <button
          onClick={openCreateModal}
          className="px-4 py-2.5 bg-sky-600 hover:bg-sky-500 text-white font-semibold text-sm rounded-xl shadow-lg shadow-sky-600/25 transition-all flex items-center space-x-2"
        >
          <Plus className="w-4 h-4" />
          <span>Add Resource</span>
        </button>
      </div>

      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <Loader2 className="w-10 h-10 animate-spin text-sky-400" />
        </div>
      ) : isError ? (
        <div className="p-8 text-center bg-rose-500/10 border border-rose-500/20 rounded-2xl text-rose-400">
          Failed to load resources.
        </div>
      ) : (
        <div className="bg-slate-900 border border-slate-800 rounded-3xl overflow-hidden shadow-2xl">
          <table className="w-full text-left text-sm text-slate-300">
            <thead className="bg-slate-950 text-xs font-semibold uppercase tracking-wider text-slate-400 border-b border-slate-800">
              <tr>
                <th className="px-6 py-4">Resource</th>
                <th className="px-6 py-4">Type / Capacity</th>
                <th className="px-6 py-4">Slot Step</th>
                <th className="px-6 py-4">Duration Range</th>
                <th className="px-6 py-4">Status</th>
                <th className="px-6 py-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/60">
              {resources.map((r) => (
                <tr key={r.id} className="hover:bg-slate-800/40 transition-colors">
                  <td className="px-6 py-4 font-bold text-white">
                    {r.name}
                    <p className="text-xs font-normal text-slate-400 line-clamp-1">{r.description}</p>
                  </td>
                  <td className="px-6 py-4">
                    <span className="font-semibold text-xs text-sky-400">{r.type}</span>
                    <span className="text-slate-500 text-xs ml-2">(Cap: {r.capacity})</span>
                  </td>
                  <td className="px-6 py-4 font-mono text-xs">{r.slotGranularityMinutes} mins</td>
                  <td className="px-6 py-4 font-mono text-xs">
                    {r.minBookingDurationMinutes}m - {r.maxBookingDurationMinutes}m
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-block px-2.5 py-1 rounded-full text-xs font-bold ${
                        r.active
                          ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20'
                          : 'bg-slate-800 text-slate-500 border border-slate-700'
                      }`}
                    >
                      {r.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right space-x-2">
                    <button
                      onClick={() => openEditModal(r)}
                      className="p-2 text-slate-400 hover:text-sky-400 hover:bg-sky-500/10 rounded-lg transition-all"
                      title="Edit"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => {
                        if (window.confirm(`Delete resource "${r.name}"?`)) {
                          deleteMutation.mutate(r.id);
                        }
                      }}
                      className="p-2 text-slate-400 hover:text-rose-400 hover:bg-rose-500/10 rounded-lg transition-all"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Modal Form */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 bg-black/75 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-slate-900 border border-slate-800 max-w-2xl w-full rounded-3xl p-6 space-y-6 shadow-2xl my-8">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="text-xl font-bold text-white">
                {editingResource ? 'Edit Resource' : 'Create New Resource'}
              </h3>
              <button onClick={() => setIsModalOpen(false)} className="text-slate-400 hover:text-white font-bold">
                ✕
              </button>
            </div>

            {errorMsg && (
              <div className="p-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm flex items-center space-x-2">
                <AlertCircle className="w-4 h-4" />
                <span>{errorMsg}</span>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Name</label>
                  <input
                    type="text"
                    required
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm focus:border-sky-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Type</label>
                  <input
                    type="text"
                    required
                    value={type}
                    onChange={(e) => setType(e.target.value)}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm focus:border-sky-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Description</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm focus:border-sky-500 h-20"
                />
              </div>

              <div className="grid grid-cols-4 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Capacity</label>
                  <input
                    type="number"
                    min={1}
                    value={capacity}
                    onChange={(e) => setCapacity(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Step (Min)</label>
                  <input
                    type="number"
                    min={5}
                    value={slotGranularityMinutes}
                    onChange={(e) => setSlotGranularityMinutes(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Min Duration</label>
                  <input
                    type="number"
                    min={5}
                    value={minBookingDurationMinutes}
                    onChange={(e) => setMinBookingDurationMinutes(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 uppercase mb-1">Max Duration</label>
                  <input
                    type="number"
                    min={15}
                    value={maxBookingDurationMinutes}
                    onChange={(e) => setMaxBookingDurationMinutes(Number(e.target.value))}
                    className="w-full bg-slate-950 border border-slate-800 rounded-xl p-2.5 text-white text-sm"
                  />
                </div>
              </div>

              <div className="flex items-center space-x-2 pt-2">
                <input
                  type="checkbox"
                  id="active"
                  checked={active}
                  onChange={(e) => setActive(e.target.checked)}
                  className="rounded bg-slate-950 border-slate-800 text-sky-600 focus:ring-0"
                />
                <label htmlFor="active" className="text-sm font-semibold text-white">Active for booking</label>
              </div>

              <div className="flex justify-end space-x-3 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 bg-slate-800 text-slate-300 text-sm font-semibold rounded-xl"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saveMutation.isPending}
                  className="px-4 py-2 bg-sky-600 hover:bg-sky-500 text-white text-sm font-semibold rounded-xl"
                >
                  {saveMutation.isPending ? 'Saving...' : 'Save Resource'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
