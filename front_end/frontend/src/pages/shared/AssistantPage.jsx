import ChatBotWidget from '../../components/ChatBotWidget.jsx';

export default function AssistantPage() {
  return <div className="max-w-5xl mx-auto px-4 py-8">
    <div className="mb-6">
      <p className="text-sm font-bold text-blue-700">GenAI / RAG Assistance</p>
      <h1 className="text-3xl font-extrabold text-slate-900">Find the best available car</h1>
      <p className="text-slate-500">Ask your budget, fuel preference, transmission, city, family usage or running requirement. The bot reads available cars from MySQL and suggests the best match.</p>
    </div>
    <ChatBotWidget fullPage />
  </div>;
}
