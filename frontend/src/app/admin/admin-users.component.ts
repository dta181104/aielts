import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize, take } from 'rxjs';
import { AdminNavComponent } from './admin-nav.component';
import {
  AdminService,
  AdminUser,
  AdminRole,
  UserCreationRequest,
  UserUpdateRequest,
} from '../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, AdminNavComponent],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {
  users: AdminUser[] = [];
  roles: AdminRole[] = [];
  model: AdminUser = this.empty();
  editing = false;
  loading = false;
  saving = false;
  rolesLoading = false;

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.load();
  }

  private empty(): AdminUser {
    return { id: '', username: '', name: '', fullName: '', email: '', phone: '', roles: [], status: 'ACTIVE', password: '' };
  }

  load() {
    this.loading = true;
    this.rolesLoading = true;
    this.adminService
      .fetchUsers()
      .pipe(
        take(1),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (users) => (this.users = users),
        error: () => alert('Không thể tải danh sách người dùng.'),
      });

    this.adminService
      .fetchRoles()
      .pipe(
        take(1),
        finalize(() => (this.rolesLoading = false))
      )
      .subscribe({
        next: (roles) => (this.roles = roles),
        error: () => {
          this.roles = this.adminService.getRoles();
        },
      });
  }

  edit(user: AdminUser) {
    this.model = { ...user, roles: [...user.roles], password: '' };
    this.editing = true;
  }

  reset() {
    this.model = this.empty();
    this.editing = false;
  }

  toggleRole(roleId: string, event: Event) {
    const checked = (event.target as HTMLInputElement).checked;
    if (checked) {
      if (!this.model.roles.includes(roleId)) {
        this.model.roles = [...this.model.roles, roleId];
      }
    } else {
      this.model.roles = this.model.roles.filter(r => r !== roleId);
    }
  }

  save() {
    if (!this.model.username?.trim()) {
      alert('Vui lòng nhập username.');
      return;
    }
    if (!this.editing && !this.model.password?.trim()) {
      alert('Vui lòng nhập mật khẩu cho người dùng mới.');
      return;
    }

    const roleEntries = this.model.roles.filter(Boolean);

    const updatePayload: UserUpdateRequest = {
      name: this.model.name,
      fullName: this.model.fullName,
      email: this.model.email,
      phone: this.model.phone,
      roles: roleEntries,
      status: this.model.status,
    };
    const createPayload: UserCreationRequest = {
      username: this.model.username,
      name: this.model.name,
      ...updatePayload,
      pass: this.model.password?.trim(),
    };

    const request = this.editing
      ? this.adminService.updateUser(this.model.code ?? this.model.username, updatePayload)
      : this.adminService.createUser(createPayload);

    this.saving = true;
    request
      .pipe(
        take(1),
        finalize(() => (this.saving = false))
      )
      .subscribe({
        next: () => {
          this.load();
          this.reset();
        },
        error: (error: unknown) => {
          console.error(error);
          alert('Không thể lưu người dùng.');
        },
      });
  }

  remove(user: AdminUser) {
    if (confirm(`Xoá người dùng ${user.username}?`)) {
      const code = user.code ?? user.username;
      this.adminService
        .deleteUser(code)
        .pipe(take(1))
        .subscribe({
          next: () => this.load(),
          error: () => alert('Không thể xoá người dùng.'),
        });
    }
  }

  roleLabel(roleId: string): string {
    const role = this.roles.find(r => this.roleKey(r) === roleId);
    return role?.code || role?.name || roleId;
  }

  roleKey(role: AdminRole): string {
    if (!role) return '';
    const value = role.code ?? role.id ?? role.name;
    return value ? value.toString() : '';
  }
}
