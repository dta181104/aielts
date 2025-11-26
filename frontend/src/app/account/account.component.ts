import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth.interceptor';

interface UserProfile {
  fullName: string;
  yearOfBirth?: number | null;
  email?: string;
  phone?: string;
}

@Component({
  selector: 'app-account',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './account.component.html',
  styleUrls: ['./account.component.css'],
})
export class AccountComponent implements OnInit {
  model: UserProfile = { fullName: '', yearOfBirth: null, email: '', phone: '' };
  saving = false;
  loading = false;
  apiError = '';
  profileCode: string | null = null;
  private readonly storageKey = 'account_profile';
  private readonly myInfoUrl = 'http://localhost:8080/identity/users/myInfo';
  private readonly userApiBase = 'http://localhost:8080/identity/users';
  // read-only fields from token
  username: string | null = null;
  roles: string[] = [];

  constructor(private router: Router, private authService: AuthService, private http: HttpClient) {}

  ngOnInit(): void {
    // require login — read token from cookie via AuthService
    const token = this.authService.getToken();
    if (!token) {
      // redirect to login and preserve return url
      this.router.navigate(['/login'], { state: { returnUrl: '/account' } });
      return;
    }

    this.extractTokenInfo(token);

    this.fetchProfile();
  }

  private extractTokenInfo(token: string) {
    try {
      // JWT: header.payload.signature
      const parts = token.split('.');
      if (parts.length < 2) return;
      const payload = parts[1];
      const json = atob(payload.replace(/-/g, '+').replace(/_/g, '/'));
      const obj = JSON.parse(decodeURIComponent(escape(json)));
      // common claims: sub, preferred_username, username
      this.username = obj.preferred_username || obj.username || obj.sub || null;
      // roles may be in 'roles', 'role', 'authorities', or in realm_access.roles
      if (obj.roles && Array.isArray(obj.roles)) this.roles = obj.roles;
      else if (obj.role && Array.isArray(obj.role)) this.roles = obj.role;
      else if (obj.authorities && Array.isArray(obj.authorities)) this.roles = obj.authorities;
      else if (obj.realm_access && Array.isArray(obj.realm_access.roles)) this.roles = obj.realm_access.roles;
      else if (typeof obj.role === 'string') this.roles = [obj.role];
    } catch (e) {
      console.warn('Failed to decode access token payload', e);
    }
  }

  private fetchProfile() {
    this.loading = true;
    this.apiError = '';
    this.http.get<{ result?: any }>(this.myInfoUrl).subscribe({
      next: (res) => {
        this.loading = false;
        const payload = res?.result ?? res;
        if (payload) {
          this.applyApiProfile(payload);
          this.saveSnapshot();
        } else {
          this.loadFromStorage();
        }
      },
      error: (err) => {
        this.loading = false;
        this.apiError = err?.error?.message || 'Không thể tải thông tin tài khoản từ máy chủ.';
        this.loadFromStorage();
      },
    });
  }

  private applyApiProfile(payload: any) {
    this.model = {
      fullName: payload.name || payload.fullName || '',
      yearOfBirth: payload.yearOfBirth ?? payload.year_of_birth ?? null,
      email: payload.email || '',
      phone: payload.phone || payload.phoneNumber || payload.mobile || '',
    };
    if (payload.username) this.username = payload.username;
    this.profileCode = payload.code || null;
  }

  private loadFromStorage() {
    try {
      const raw = localStorage.getItem(this.storageKey);
      if (raw) {
        const parsed = JSON.parse(raw);
        this.model = {
          fullName: parsed.fullName || '',
          yearOfBirth: parsed.yearOfBirth || null,
          email: parsed.email || '',
          phone: parsed.phone || '',
        };
        if (parsed.username && !this.username) this.username = parsed.username;
        if (parsed.profileCode && !this.profileCode) this.profileCode = parsed.profileCode;
      }
    } catch (e) {
      console.warn('Failed to read profile from storage', e);
    }
  }

  private saveSnapshot() {
    try {
      const snapshot = {
        ...this.model,
        username: this.username,
        profileCode: this.profileCode,
      };
      localStorage.setItem(this.storageKey, JSON.stringify(snapshot));
    } catch (e) {
      console.warn('Failed to persist account snapshot', e);
    }
  }

  save() {
    if (!this.profileCode) {
      this.persistLocally();
      alert('Không tìm thấy mã tài khoản nên chỉ lưu cục bộ trên trình duyệt.');
      return;
    }

    const payload = {
      code: this.profileCode,
      name: this.model.fullName,
      email: this.model.email,
      phone: this.model.phone,
      yearOfBirth: this.model.yearOfBirth ?? null,
    };

    this.saving = true;
    this.http.put(`${this.userApiBase}/${this.profileCode}`, payload).subscribe({
      next: () => {
        this.saving = false;
        this.saveSnapshot();
        alert('Đã cập nhật thông tin tài khoản.');
      },
      error: (err) => {
        this.saving = false;
        console.error('Failed to update account info', err);
        alert(err?.error?.message || 'Cập nhật thất bại. Vui lòng thử lại sau.');
      }
    });
  }

  private persistLocally() {
    try {
      const copy = { ...this.model } as any;
      if (!copy.yearOfBirth) copy.yearOfBirth = null;
      localStorage.setItem(this.storageKey, JSON.stringify({
        ...copy,
        username: this.username,
        profileCode: this.profileCode,
      }));
    } catch (e) {
      console.error('Failed to save profile locally', e);
    }
  }

  cancel() {
    this.router.navigate(['/']);
  }
}
