import { Badge } from './ui/badge';
import type { OrderStatus } from '../types';

interface StatusBadgeProps {
  status: OrderStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const statusConfig: Record<OrderStatus, { label: string; variant: 'default' | 'secondary' | 'destructive' }> = {
    CREATED: { label: 'Created', variant: 'secondary' },
    ASSIGNED: { label: 'Assigned', variant: 'default' },
    IN_TRANSIT: { label: 'In Transit', variant: 'default' },
    DELIVERED: { label: 'Delivered', variant: 'default' },
    CANCELLED: { label: 'Cancelled', variant: 'destructive' },
  };

  const config = statusConfig[status];

  return (
    <Badge variant={config.variant} className="capitalize">
      {config.label}
    </Badge>
  );
}