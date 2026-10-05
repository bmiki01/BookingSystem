import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { ProtectedRoute } from './components/ProtectedRoute';
import { AdminRoute } from './components/AdminRoute';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { ResourceListPage } from './pages/ResourceListPage';
import { ResourceDetailPage } from './pages/ResourceDetailPage';
import { MyBookingsPage } from './pages/MyBookingsPage';
import { AdminResourcesPage } from './pages/AdminResourcesPage';
import { AdminBookingsPage } from './pages/AdminBookingsPage';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <BrowserRouter>
          <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-sky-500 selection:text-white">
            <Navbar />
            <main className="flex-grow">
              <Routes>
                {/* Public Auth Routes */}
                <Route path="/login" element={<LoginPage />} />
                <Route path="/register" element={<RegisterPage />} />

                {/* User Protected Routes */}
                <Route element={<ProtectedRoute />}>
                  <Route path="/resources" element={<ResourceListPage />} />
                  <Route path="/resources/:id" element={<ResourceDetailPage />} />
                  <Route path="/my-bookings" element={<MyBookingsPage />} />
                </Route>

                {/* Admin Protected Routes */}
                <Route element={<AdminRoute />}>
                  <Route path="/admin/resources" element={<AdminResourcesPage />} />
                  <Route path="/admin/bookings" element={<AdminBookingsPage />} />
                </Route>

                {/* Fallback */}
                <Route path="*" element={<Navigate to="/resources" replace />} />
              </Routes>
            </main>
          </div>
        </BrowserRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
}

export default App;
