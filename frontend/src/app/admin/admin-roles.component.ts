import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { take } from 'rxjs';
import { AdminNavComponent } from './admin-nav.component';
import { AdminService, AdminRole, AdminPermission, RoleRequest } from '../../services/admin.service';

@Component({
  selector: 'app-admin-roles',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminNavComponent],
  templateUrl: './admin-roles.component.html',
  styleUrls: ['./admin-roles.component.css']
})
export class AdminRolesComponent implements OnInit {
  roles: AdminRole[] = [];
  permissions: AdminPermission[] = [];
  model: AdminRole = this.empty();
  editing = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.load();
  }

  private empty(): AdminRole {
    return { id: '', code: '', name: '', description: '', permissionIds: [] };
  }

  load() {
    this.adminService.fetchPermissions().pipe(take(1)).subscribe({
      next: (list) => (this.permissions = list),
      error: () => (this.permissions = []),
    });

    this.adminService.fetchRoles().pipe(take(1)).subscribe({
      next: (list) => (this.roles = list),
      error: () => (this.roles = []),
    });
  }

  togglePermission(id: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      if (!this.model.permissionIds.includes(id)) {
        this.model.permissionIds = [...this.model.permissionIds, id];
      }
    } else {
      this.model.permissionIds = this.model.permissionIds.filter(pid => pid !== id);
    }
  }

  edit(role: AdminRole) {
    this.model = { ...role, permissionIds: [...role.permissionIds] };
    this.editing = true;
  }

  reset() {
    this.model = this.empty();
    this.editing = false;
  }

  save() {
    if (!this.model.code || !this.model.name) {
      alert('Vui lòng nhập mã và tên vai trò.');
      return;
    }
    const payload: RoleRequest = {
      code: this.model.code.toUpperCase(),
      name: this.model.name,
      description: this.model.description,
      permissions: [...this.model.permissionIds],
    };

    const request = this.editing
      ? this.adminService.updateRole(this.model.id, payload)
      : this.adminService.createRole(payload);

    request.pipe(take(1)).subscribe({
      next: (role) => {
        this.load();
        this.reset();
      },
      error: () => alert('Không thể lưu vai trò.'),
    });
  }

  remove(role: AdminRole) {
    if (confirm(`Xoá vai trò ${role.name}?`)) {
      if (!role.id) {
        alert('Không thể xoá vai trò không có ID.');
        return;
      }
      this.adminService.deleteRole(role.id)
        .pipe(take(1))
        .subscribe({
          next: () => {
            this.load();
          },
          error: () => alert('Không thể xoá vai trò.'),
        });
    }
  }

  permissionLabel(permissionId: string): string {
    const perm = this.permissions.find(p => p.id === permissionId);
    return perm?.code || perm?.name || '—';
  }
}
