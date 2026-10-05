import React, { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { resourceApi } from '../api/resourceApi';
import { Search, Users, Clock, Calendar, ArrowRight, Loader2, Sparkles } from 'lucide-react';

export const ResourceListPage: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('ALL');

  const { data, isLoading, isError } = useQuery({
    queryKey: ['resources'],
    queryFn: () => resourceApi.getResources(0, 50, false),
  });

  const resources = data?.content || [];

  const filteredResources = resources.filter((res) => {
    const matchesSearch =
      res.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (res.description && res.description.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesType = selectedType === 'ALL' || res.type === selectedType;
    return matchesSearch && matchesType;
  });

  const uniqueTypes = ['ALL', ...Array.from(new Set(resources.map((r) => r.type)))];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Header Banner */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-gradient-to-r from-sky-900/40 via-slate-900 to-indigo-900/30 p-8 rounded-3xl border border-sky-500/20 shadow-xl">
        <div className="space-y-2">
          <div className="inline-flex items-center space-x-2 px-3 py-1 rounded-full bg-sky-500/10 text-sky-400 text-xs font-semibold border border-sky-500/20">
            <Sparkles className="w-3.5 h-3.5" />
            <span>Available Resources</span>
          </div>
          <h1 className="text-3xl font-bold text-white tracking-tight">Reserve Time Slots</h1>
          <p className="text-slate-400 text-sm max-w-xl">
            Choose from meeting rooms, equipment, and consultation hours. All reservations are updated in real-time.
          </p>
        </div>
      </div>

      {/* Filters & Search */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="relative w-full sm:w-80">
          <div className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-500">
            <Search className="w-4 h-4" />
          </div>
          <input
            type="text"
            placeholder="Search resources..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full bg-slate-900 border border-slate-800 rounded-xl py-2.5 pl-10 pr-4 text-white text-sm focus:outline-none focus:border-sky-500 focus:ring-1 focus:ring-sky-500 transition-all placeholder:text-slate-500"
          />
        </div>

        <div className="flex items-center space-x-2 overflow-x-auto w-full sm:w-auto pb-2 sm:pb-0">
          {uniqueTypes.map((type) => (
            <button
              key={type}
              onClick={() => setSelectedType(type)}
              className={`px-3.5 py-1.5 rounded-lg text-xs font-semibold transition-all whitespace-nowrap ${
                selectedType === type
                  ? 'bg-sky-600 text-white shadow-md shadow-sky-600/20'
                  : 'bg-slate-900 text-slate-400 hover:text-white border border-slate-800 hover:border-slate-700'
              }`}
            >
              {type === 'ALL' ? 'All Types' : type}
            </button>
          ))}
        </div>
      </div>

      {/* Resource Grid */}
      {isLoading ? (
        <div className="flex justify-center items-center py-20">
          <Loader2 className="w-10 h-10 animate-spin text-sky-400" />
        </div>
      ) : isError ? (
        <div className="p-8 text-center bg-rose-500/10 border border-rose-500/20 rounded-2xl text-rose-400">
          Failed to load resources. Please refresh or try again later.
        </div>
      ) : filteredResources.length === 0 ? (
        <div className="p-12 text-center bg-slate-900 border border-slate-800 rounded-3xl space-y-3">
          <Calendar className="w-12 h-12 text-slate-600 mx-auto" />
          <h3 className="text-lg font-semibold text-white">No resources found</h3>
          <p className="text-slate-400 text-sm">Try adjusting your search query or filters.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredResources.map((resource) => (
            <div
              key={resource.id}
              className="bg-slate-900 border border-slate-800 hover:border-sky-500/40 rounded-2xl p-6 transition-all duration-300 flex flex-col justify-between group hover:shadow-xl hover:shadow-sky-950/40"
            >
              <div className="space-y-4">
                <div className="flex items-start justify-between">
                  <span className="text-xs font-bold uppercase tracking-wider px-2.5 py-1 rounded-md bg-slate-800 text-sky-400 border border-slate-700">
                    {resource.type}
                  </span>
                  <div className="flex items-center space-x-1.5 text-xs text-slate-400 bg-slate-950/60 px-2.5 py-1 rounded-md border border-slate-800">
                    <Users className="w-3.5 h-3.5 text-slate-400" />
                    <span>Cap: {resource.capacity}</span>
                  </div>
                </div>

                <div>
                  <h3 className="text-xl font-bold text-white group-hover:text-sky-300 transition-colors">
                    {resource.name}
                  </h3>
                  <p className="text-slate-400 text-sm mt-1.5 line-clamp-2">
                    {resource.description || 'No description provided.'}
                  </p>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs pt-3 border-t border-slate-800/80 text-slate-400">
                  <div className="flex items-center space-x-1.5">
                    <Clock className="w-3.5 h-3.5 text-sky-400" />
                    <span>Slot: {resource.slotGranularityMinutes}m</span>
                  </div>
                  <div className="flex items-center space-x-1.5">
                    <Calendar className="w-3.5 h-3.5 text-indigo-400" />
                    <span>Min: {resource.minBookingDurationMinutes}m</span>
                  </div>
                </div>
              </div>

              <div className="pt-6">
                <Link
                  to={`/resources/${resource.id}`}
                  className="w-full py-2.5 px-4 bg-slate-800 hover:bg-sky-600 text-slate-200 hover:text-white font-semibold text-sm rounded-xl transition-all flex items-center justify-center space-x-2 group-hover:bg-sky-600 group-hover:text-white"
                >
                  <span>View Availability</span>
                  <ArrowRight className="w-4 h-4" />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
