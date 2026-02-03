import { Badge } from './ui/badge';

interface StatusBadgeProps {
  // We accept string to be safe against unexpected API values
  status: string | undefined | null; 
}

export function StatusBadge({ status }: StatusBadgeProps) {
  // 1. Safety Check: Handle missing status
  if (!status) {
    return <Badge variant="outline">Unknown</Badge>;
  }

  // 2. Normalize: Convert to Uppercase to fix case mismatches (e.g. "created" -> "CREATED")
  const normalizedStatus = status.toUpperCase();

  const statusConfig: Record<string, { label: string; variant: 'default' | 'secondary' | 'destructive' | 'outline' }> = {
    CREATED: { label: 'Created', variant: 'secondary' },
    ASSIGNED: { label: 'Assigned', variant: 'default' },
    IN_TRANSIT: { label: 'In Transit', variant: 'default' },
    DELIVERED: { label: 'Delivered', variant: 'default' },
    CANCELLED: { label: 'Cancelled', variant: 'destructive' },
    // Add common variations just in case
    PENDING: { label: 'Pending', variant: 'secondary' },
    COMPLETED: { label: 'Completed', variant: 'default' },
  };

  // 3. Fallback: If status is not in the list, use a generic gray badge
  const config = statusConfig[normalizedStatus] || { 
    label: normalizedStatus, 
    variant: 'outline' 
  };

  return (
    <Badge variant={config.variant as any} className="capitalize">
      {config.label}
    </Badge>
  );
}