const BACKEND_ORIGIN = 'http://localhost:8080';

const MODEL_FALLBACKS = [
  ['swift', '/demo-cars/swift-2020/front.jpg'],
  ['creta', '/demo-cars/creta-2021/front.jpg'],
  ['city', '/demo-cars/city-2019/front.jpg'],
  ['nexon', '/demo-cars/nexon-2022/front.jpg'],
  ['xuv700', '/demo-cars/xuv700-2022/front.jpg'],
  ['innova', '/demo-cars/innova-2018/front.jpg'],
  ['seltos', '/demo-cars/seltos-2021/front.jpg'],
  ['baleno', '/demo-cars/baleno-2020/front.jpg'],
  ['hector', '/demo-cars/hector-2021/front.jpg'],
  ['tiago', '/demo-cars/tiago-2022/front.jpg'],
];

export function normalizeImageUrl(url) {
  if (!url) return null;
  const value = String(url).trim();
  if (!value) return null;
  if (/^https?:\/\//i.test(value) || value.startsWith('data:') || value.startsWith('blob:')) return value;
  return `${BACKEND_ORIGIN}${value.startsWith('/') ? value : `/${value}`}`;
}

export function fallbackForCar(car) {
  const text = `${car?.brand || ''} ${car?.model || ''} ${car?.title || ''}`.toLowerCase();
  const match = MODEL_FALLBACKS.find(([keyword]) => text.includes(keyword));
  return normalizeImageUrl(match?.[1] || '/demo-cars/swift-2020/front.jpg');
}

export function getCarImages(car) {
  const structured = Array.isArray(car?.images)
    ? [...car.images]
        .filter((image) => image && image.imageUrl)
        .sort((a, b) => {
          if (a.primaryImage !== b.primaryImage) return a.primaryImage ? -1 : 1;
          return Number(a.displayOrder ?? 0) - Number(b.displayOrder ?? 0);
        })
        .map((image) => normalizeImageUrl(image.imageUrl))
    : [];

  const legacy = Array.isArray(car?.imageUrls)
    ? car.imageUrls.map(normalizeImageUrl).filter(Boolean)
    : [];

  const unique = [...new Set([...structured, ...legacy].filter(Boolean))];
  return unique.length ? unique : [fallbackForCar(car)];
}

export function primaryCarImage(car) {
  return getCarImages(car)[0];
}
