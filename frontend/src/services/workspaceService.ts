import api from '@/lib/api';

export interface WorkspaceDTO {
  id: number;
  name: string;
  description?: string;
  ownerId: number;
}

export interface CreateWorkspaceRequest {
  name: string;
  description?: string;
}

export const workspaceService = {
  createWorkspace: async (data: CreateWorkspaceRequest): Promise<WorkspaceDTO> => {
    const response = await api.post<WorkspaceDTO>('/workspaces', data);
    return response.data;
  },
  
  getMyWorkspaces: async (): Promise<WorkspaceDTO[]> => {
    const response = await api.get<WorkspaceDTO[]>('/workspaces/my');
    return response.data;
  },
};

