export default function LoadingSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
      {[1,2,3,4].map(i => <div className="skeleton-card" key={i} />)}
    </div>
  );
}
