import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs';
import { AdminNavComponent } from './admin-nav.component';
import { AdminService, AdminPermission, PermissionRequest } from '../../services/admin.service';

@Component({
  selector: 'app-admin-permissions',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminNavComponent],
  templateUrl: './admin-permissions.component.html',
  styleUrls: ['./admin-permissions.component.css']
})
export class AdminPermissionsComponent implements OnInit {
  permissions: AdminPermission[] = [];
  model: AdminPermission = this.empty();
  editing = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.load();
  }

  private empty(): AdminPermission {
    return { id: '', code: '', name: '', description: '' };
  }

  load() {
    this.adminService.fetchPermissions().pipe(take(1)).subscribe({
      next: (list) => (this.permissions = list),
      error: () => (this.permissions = []),
    });
  }

  edit(item: AdminPermission) {
    this.model = { ...item };
    this.editing = true;
  }

  reset() {
    this.model = this.empty();
    this.editing = false;
  }

  save() {
    if (!this.model.code || !this.model.name) {
      alert('Vui lòng nhập mã và tên quyền.');
      return;
    }
    this.model.code = this.model.code.toUpperCase();
    const payload: PermissionRequest = {
      code: this.model.code,
      name: this.model.name,
      description: this.model.description,
    };
    this.adminService.createPermission(payload)
      .pipe(take(1))
      .subscribe({
        next: () => {
          this.load();
          this.reset();
        },
        error: () => alert('Không thể lưu quyền.'),
      });
  }

  remove(item: AdminPermission) {
    if (confirm(`Xoá quyền ${item.name}?`)) {
      this.adminService.deletePermission(item.code)
        .pipe(take(1))
        .subscribe({
          next: () => this.load(),
          error: () => alert('Không thể xoá quyền.'),
        });
    }
  }
}
