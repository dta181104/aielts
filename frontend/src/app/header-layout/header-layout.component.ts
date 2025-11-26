import { Component, Inject, PLATFORM_ID, OnDestroy } from '@angular/core';
import { Router, RouterLink, NavigationEnd } from '@angular/router';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AuthService } from '../../services/auth.interceptor';
import { Subscription } from 'rxjs';

@Component({
  selector: 'header-layout',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './header-layout.component.html',
  styleUrls: ['./header-layout.component.css'],
})
export class HeaderLayoutComponent implements OnDestroy {
  isLoggedIn = false;
  showAccountDropdown = false;
  isAdmin = false;
  // hide header on auth pages like /login and /register
  hideOnAuthPages = false;
  private isBrowser: boolean;
  private routerSub: Subscription | null = null;
  private storageHandler: ((this: Window, ev: StorageEvent) => any) | null = null;

  constructor(
    private router: Router,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    if (this.isBrowser) {
      this.checkLogin();

      // listen for changes to localStorage (login/logout in other tabs)
      this.storageHandler = () => this.checkLogin();
      window.addEventListener('storage', this.storageHandler);

      // also re-check login after each successful navigation so same-tab logins show up
      this.routerSub = this.router.events.subscribe((ev) => {
        if (ev instanceof NavigationEnd) {
          this.checkLogin();
          // close dropdown on navigation
          this.showAccountDropdown = false;
          // update whether header should be hidden on certain routes
          const url = (ev as NavigationEnd).urlAfterRedirects || (ev as NavigationEnd).url;
          this.updateVisibility(url);
        }
      });

      // initial visibility based on current router url
      try {
        this.updateVisibility(this.router.url);
      } catch (e) {
        // ignore
      }
    }
  }

  private updateVisibility(url: string) {
    if (!this.isBrowser) return;
    const path = (url || '').split('?')[0].replace(/#.*$/, '');
    // hide on login and register pages
    this.hideOnAuthPages = path === '/login' || path === '/register';
  }

  private checkLogin() {
    if (this.isBrowser) {
      this.isLoggedIn = !!this.authService.getToken();
      // derive roles/admin flag from saved user profile (if available).
      try {
        const raw = localStorage.getItem('user_profile');
        if (raw) {
          const profile = JSON.parse(raw);
          const roles = profile?.roles ?? profile?.result?.roles ?? profile?.user?.roles ?? [];
          this.isAdmin = Array.isArray(roles) && roles.some((r: any) => {
            const code = (r?.code || r?.name || '').toString().toUpperCase();
            return code === 'ADMIN' || code.includes('ADMIN');
          });
        } else {
          this.isAdmin = false;
        }
      } catch (e) {
        this.isAdmin = false;
      }
    }
  }

  toggleDropdown() {
    this.showAccountDropdown = !this.showAccountDropdown;
  }

  logout() {
    if (this.isBrowser) {
      this.authService.logout();
      this.showAccountDropdown = false;
      this.checkLogin();
      this.router.navigate(['/login']);
    }
  }

  ngOnDestroy(): void {
    if (this.routerSub) {
      this.routerSub.unsubscribe();
      this.routerSub = null;
    }
    if (this.storageHandler) {
      window.removeEventListener('storage', this.storageHandler);
      this.storageHandler = null;
    }
  }
}
