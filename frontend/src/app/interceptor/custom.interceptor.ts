import { HttpInterceptorFn } from '@angular/common/http';

export const customInterceptor: HttpInterceptorFn = (req, next) => {
  const getCookie = (name: string): string | null => {
    if (typeof document === 'undefined') return null;
    const match = document.cookie.match(new RegExp('(^|;\\s*)' + encodeURIComponent(name) + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  };

  const getStoredToken = (): string | null => {
    if (typeof window !== 'undefined' && typeof localStorage !== 'undefined') {
      const stored = localStorage.getItem('access_token');
      if (stored) {
        return stored;
      }
    }
    return getCookie('access_token');
  };

  const token = getStoredToken();
  const headers: Record<string, string> = token ? { Authorization: `Bearer ${token}` } : {};
  req = req.clone({ withCredentials: true, setHeaders: headers });
  return next(req);
};
