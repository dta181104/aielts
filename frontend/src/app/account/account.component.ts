import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
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
  private key = 'user_profile';
  // read-only fields from token
  username: string | null = null;
  roles: string[] = [];

  constructor(private router: Router, private authService: AuthService) {}

  ngOnInit(): void {
    // require login — read token from cookie via AuthService
    const token = this.authService.getToken();
    if (!token) {
      // redirect to login and preserve return url
      this.router.navigate(['/login'], { state: { returnUrl: '/account' } });
      return;
    }

    this.extractTokenInfo(token);

    this.load();
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

  load() {
    try {
      const raw = localStorage.getItem(this.key);
      if (raw) {
        const parsed = JSON.parse(raw);
        this.model = {
          fullName: parsed.fullName || '',
          yearOfBirth: parsed.yearOfBirth || null,
          email: parsed.email || '',
          phone: parsed.phone || '',
        };
      }
    } catch (e) {
      console.warn('Failed to read profile from storage', e);
    }
  }

  save() {
    this.saving = true;
    try {
      const copy = { ...this.model } as any;
      if (!copy.yearOfBirth) copy.yearOfBirth = null;
      localStorage.setItem(this.key, JSON.stringify(copy));
      // simple confirmation — integrate NotificationService if available
      alert('Thông tin tài khoản đã được lưu.');
    } catch (e) {
      console.error('Failed to save profile', e);
      alert('Lưu thông tin thất bại. Vui lòng thử lại.');
    } finally {
      this.saving = false;
    }
  }

  cancel() {
    this.router.navigate(['/']);
  }
}
