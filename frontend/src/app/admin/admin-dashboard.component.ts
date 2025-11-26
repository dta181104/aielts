import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { take } from 'rxjs';
import { AdminNavComponent } from './admin-nav.component';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, AdminNavComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats = {
    users: 0,
    roles: 0,
    permissions: 0,
    courses: 0
  };

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.refresh();
  }

  private refresh() {
    this.stats.users = this.adminService.getUsers().length;
    this.adminService
      .fetchCourses({ page: 0, size: 1 })
      .pipe(take(1))
      .subscribe({
        next: (page) => (this.stats.courses = page.totalElements),
        error: () => (this.stats.courses = this.adminService.getCachedCourses().length),
      });
    this.adminService.fetchRoles().pipe(take(1)).subscribe(list => {
      this.stats.roles = list.length;
    });
    this.adminService.fetchPermissions().pipe(take(1)).subscribe(list => {
      this.stats.permissions = list.length;
    });
  }
}
