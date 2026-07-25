import api from '../api/api';

export async function uploadFile(file) {
  if (!file) return null;
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await api.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return data.data.url;
}

export async function uploadMany(files = []) {
  const selected = Array.from(files).filter(Boolean);
  const urls = [];
  for (const file of selected) {
    urls.push(await uploadFile(file));
  }
  return urls.filter(Boolean);
}
