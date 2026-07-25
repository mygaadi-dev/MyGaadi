import { useQuery } from '@tanstack/react-query';
import api,{unwrap} from '../../api/api';
import DashboardLayout from '../../components/DashboardLayout';
import CarCard from '../../components/CarCard';
import EmptyState from '../../components/EmptyState';

export default function Wishlist(){
  const{data=[]}=useQuery({queryKey:['wishlist'],queryFn:()=>api.get('/wishlist').then(unwrap)});
  return <DashboardLayout title="Wishlist">
    {data.length? <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">{data.map(c=><CarCard car={c} key={c.id}/>)}</div> : <EmptyState title="No saved cars"/>}
  </DashboardLayout>;
}
