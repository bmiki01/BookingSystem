import apiClient from './client';
import { PageResponse, Resource, ResourceAvailability, ResourceRequest } from '../types';

export const resourceApi = {
  getResources: async (page = 0, size = 20, includeInactive = false): Promise<PageResponse<Resource>> => {
    const res = await apiClient.get<PageResponse<Resource>>('/resources', {
      params: { page, size, includeInactive },
    });
    return res.data;
  },

  getResourceById: async (id: number): Promise<Resource> => {
    const res = await apiClient.get<Resource>(`/resources/${id}`);
    return res.data;
  },

  getAvailability: async (id: number, start: string, end: string): Promise<ResourceAvailability> => {
    const res = await apiClient.get<ResourceAvailability>(`/resources/${id}/availability`, {
      params: { start, end },
    });
    return res.data;
  },

  createResource: async (payload: ResourceRequest): Promise<Resource> => {
    const res = await apiClient.post<Resource>('/resources', payload);
    return res.data;
  },

  updateResource: async (id: number, payload: ResourceRequest): Promise<Resource> => {
    const res = await apiClient.put<Resource>(`/resources/${id}`, payload);
    return res.data;
  },

  deleteResource: async (id: number): Promise<void> => {
    await apiClient.delete(`/resources/${id}`);
  },
};
