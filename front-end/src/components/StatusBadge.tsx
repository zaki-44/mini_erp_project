import { Badge } from './ui/badge';
import type { OrderStatus } from '../types';

interface StatusBadgeProps {
  status: OrderStatus;
}

export function StatusBadge({ status }: StatusBadgeProps) {
  const statusConfig = {
    pending: { label: 'Pending', variant: 'secondary' as const },
    confirmed: { label: 'Confirmed', variant: 'default' as const },
    picked_up: { label: 'Picked Up', variant: 'default' as const },
    in_transit: { label: 'In Transit', variant: 'default' as const },
    delivered: { label: 'Delivered', variant: 'default' as const },
    cancelled: { label: 'Cancelled', variant: 'destructive' as const },
    rejected_by_receiver: { label: 'Rejected', variant: 'destructive' as const },
  };

  const config = statusConfig[status];

  return (
    <Badge variant={config.variant} className="capitalize">
      {config.label}
    </Badge>
  );
}