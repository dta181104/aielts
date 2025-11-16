import { HttpClient } from "@angular/common/http";
import { Component, inject } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { Router } from "@angular/router";
import { CommonModule } from '@angular/common';
import { NotificationService } from "../../services/notification.service";
import { AuthService } from '../../services/auth.interceptor';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  user = {
    username: "",
    pass: ""
  };

  errorMessage = '';
  rememberMe = false;

  // Quên mật khẩu
  showForgotPasswordModal = false;
  showOtpModal = false;
  showResetPasswordModal = false;
  isSending = false;

  forgotEmail = '';
  forgotUsername = '';
  forgotMessage = '';
  otpCode = '';
  otpMessage = '';
  newPassword = '';
  resetMessage = '';
  isSubmitting = false;

  private http = inject(HttpClient);
  private router = inject(Router);
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  // Đăng nhập
  onLogin() {
    this.errorMessage = '';

  // Clear any previous token (we now store tokens in cookies)
  this.authService.clearToken();

    if (!this.user.username || !this.user.pass) {
      this.errorMessage = 'Vui lòng nhập đầy đủ tên người dùng và mật khẩu.';
      return;
    }

    this.isSubmitting = true;
    this.http.post("http://localhost:8080/identity/auth/token", this.user, {
      headers: { 'Content-Type': 'application/json' }
    })
    .subscribe({
      next: (res: any) => {
        // response received
        this.isSubmitting = false;
        if (res?.result?.authenticated && res?.result?.token) {
          // Store token in cookie. If rememberMe is true, persist for 30 days.
          this.authService.setToken(res.result.token, this.rememberMe);

          // Optionally store user profile (non-sensitive) — keep existing behavior
          try {
            if (res.result.profile) localStorage.setItem('user_profile', JSON.stringify(res.result.profile));
          } catch (e) {
            // ignore
          }

          this.notificationService.show('success', 'Đăng nhập thành công!');
          this.router.navigateByUrl('/');
        } else {
          this.isSubmitting = false;
          this.errorMessage = res?.message || "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.";
          this.notificationService.show('error', this.errorMessage);
        }
      },
      error: (err) => {
        console.error("Lỗi đăng nhập:", err);
        this.isSubmitting = false;
        this.errorMessage = err?.error?.message || "Yêu cầu đăng nhập thất bại. Vui lòng thử lại sau.";
        this.notificationService.show('error', this.errorMessage);
      }
    });
  }

  // Điều hướng sang trang đăng ký
  goToRegister(): void {
    this.router.navigate(['/register']);
  }

  // Mở modal quên mật khẩu
  openForgotPasswordModal(event: Event) {
    event.preventDefault();
    this.showForgotPasswordModal = true;
  }

  // Đóng modal
  closeForgotPasswordModal() {
    this.showForgotPasswordModal = false;
  }
  closeOtpModal() {
    this.showOtpModal = false;
  }

  // Gửi email quên mật khẩu
  onForgotPassword() {
    if (!this.forgotUsername) {
      this.forgotMessage = "Vui lòng nhập username.";
      return;
    }
    if (!this.forgotEmail) {
      this.forgotMessage = "Vui lòng nhập email.";
      return;
    }

    if (this.isSending) return;
    this.isSending = true;
    this.forgotMessage = "";

    this.http.post(
      `http://localhost:8080/identity/auth/forgot-password?email=${encodeURIComponent(this.forgotEmail)}&username=${encodeURIComponent(this.forgotUsername)}`,
      {}
    )
    .subscribe({
      next: () => {
        this.isSending = false;
        this.showForgotPasswordModal = false;
        this.showOtpModal = true;
      },
      error: (err) => {
        this.isSending = false;
        this.forgotMessage = err?.error?.message || "Không thể gửi yêu cầu. Vui lòng thử lại.";
      }
    });
  }

  // Xác thực mã OTP
  onVerifyOtp() {
    if (!this.otpCode) {
      this.otpMessage = "Vui lòng nhập mã OTP.";
      return;
    }

    this.http.post(
      `http://localhost:8080/identity/auth/verify-code?email=${encodeURIComponent(this.forgotEmail)}&token=${encodeURIComponent(this.otpCode)}`,
      {}
    )
    .subscribe({
      next: () => {
        this.otpMessage = "Xác nhận thành công! Giờ bạn có thể đặt lại mật khẩu.";
        this.showOtpModal = false;
        this.showResetPasswordModal = true;
      },
      error: (err) => {
        this.otpMessage = err?.error?.message || "Mã OTP không hợp lệ.";
      }
    });
  }

  // Đặt lại mật khẩu
  onResetPassword() {
    if (!this.newPassword) {
      this.resetMessage = "Vui lòng nhập mật khẩu mới.";
      return;
    }

    this.http.post(
      `http://localhost:8080/identity/auth/reset-password?email=${encodeURIComponent(this.forgotEmail)}&newPassword=${encodeURIComponent(this.newPassword)}`,
      {}
    )
    .subscribe({
      next: () => {
        this.resetMessage = 'Đặt lại mật khẩu thành công!';
        setTimeout(() => {
          this.showResetPasswordModal = false;
          this.newPassword = "";
        }, 1500);
      },
      error: (err) => {
        this.resetMessage = err?.error?.message || 'Đặt lại mật khẩu thất bại.';
      }
    });
  }
}
