import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { workspaceService } from '@/services/workspaceService';
import { boardService, CreateBoardRequest } from '@/services/boardService';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Card, CardHeader, CardTitle, CardContent } from '@/components/ui/card';

interface CreateBoardDialogProps {
  onBoardCreated: (boardId: number) => void;
}

export default function CreateBoardDialog({ onBoardCreated }: CreateBoardDialogProps) {
  const [workspaceName, setWorkspaceName] = useState('');
  const [boardName, setBoardName] = useState('');
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState('');
  const queryClient = useQueryClient();

  const { data: workspaces } = useQuery({
    queryKey: ['workspaces'],
    queryFn: workspaceService.getMyWorkspaces,
    retry: false,
    onError: (error) => {
      console.error('Error fetching workspaces:', error);
    },
  });

  const createWorkspaceMutation = useMutation({
    mutationFn: workspaceService.createWorkspace,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workspaces'] });
    },
  });

  const createBoardMutation = useMutation({
    mutationFn: boardService.createBoard,
    onSuccess: (board) => {
      onBoardCreated(board.id);
    },
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsCreating(true);

    try {
      let workspaceId: number;

      // If user has workspaces, use the first one, otherwise create new
      if (workspaces && workspaces.length > 0) {
        workspaceId = workspaces[0].id;
      } else {
        const workspace = await createWorkspaceMutation.mutateAsync({
          name: workspaceName || 'My Workspace',
          description: '',
        });
        workspaceId = workspace.id;
      }

      // Create board
      const boardRequest: CreateBoardRequest = {
        name: boardName,
        description: '',
        workspaceId,
      };
      await createBoardMutation.mutateAsync(boardRequest);
    } catch (error: any) {
      console.error('Error creating board:', error);
      setError(error.response?.data?.message || 'Failed to create board. Please try again.');
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Create Your First Board</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}
          {(!workspaces || workspaces.length === 0) && (
            <div>
              <Input
                type="text"
                placeholder="Workspace Name (optional)"
                value={workspaceName}
                onChange={(e) => setWorkspaceName(e.target.value)}
              />
            </div>
          )}
          <div>
            <Input
              type="text"
              placeholder="Board Name"
              value={boardName}
              onChange={(e) => setBoardName(e.target.value)}
              required
            />
          </div>
          <Button type="submit" className="w-full" disabled={isCreating}>
            {isCreating ? 'Creating...' : 'Create Board'}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}

