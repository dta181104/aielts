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
        }
      });
    }
  }

  private checkLogin() {
    if (this.isBrowser) {
      this.isLoggedIn = !!this.authService.getToken();
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
