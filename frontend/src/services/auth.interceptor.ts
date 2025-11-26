import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs/operators';

/**
 * AuthService — centralizes token handling. Tokens are stored in a cookie named `access_token`.
 * NOTE: This implementation stores the token in a non-HttpOnly cookie so the client can read it.
 * For production, prefer the backend setting a Secure, HttpOnly cookie and use refresh tokens.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl = 'http://localhost:8080/identity/auth';

  constructor(private http: HttpClient) {}

  private setCookie(name: string, value: string, days?: number) {
    let cookie = encodeURIComponent(name) + '=' + encodeURIComponent(value) + '; path=/';
    if (days && days > 0) {
      const expires = new Date(Date.now() + days * 864e5).toUTCString();
      cookie += '; expires=' + expires;
    }
    // You may want to add `; SameSite=Lax; Secure` in production (requires HTTPS)
    document.cookie = cookie;
  }

  private getCookie(name: string): string | null {
    if (typeof document === 'undefined') return null;
    const match = document.cookie.match(new RegExp('(^|;\\s*)' + encodeURIComponent(name) + '=([^;]+)'));
    return match ? decodeURIComponent(match[2]) : null;
  }

  private deleteCookie(name: string) {
    // Set expiry in the past
    document.cookie = encodeURIComponent(name) + '=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT';
  }

  /**
   * Store token. If remember=true the cookie will persist for 30 days, otherwise it's a session cookie.
   */
  setToken(token: string, remember = false) {
    if (!token) return;
    const days = remember ? 30 : undefined;
    this.setCookie('access_token', token, days);
  }

  getToken(): string | null {
    return this.getCookie('access_token');
  }

  clearToken() {
    this.deleteCookie('access_token');
  }

  login(username: string, password: string, remember = false) {
    // Keep endpoint flexible — some components post to /token directly. This helper posts to /login
    return this.http.post<any>(`${this.baseUrl}/login`, { username, password })
      .pipe(
        tap(response => {
          if (response?.token) {
            this.setToken(response.token, remember);
          }
        })
      );
  }

  logout() {
    this.clearToken();
    try {
      localStorage.removeItem('user_profile');
    } catch (e) {
      // ignore
    }
  }
}
