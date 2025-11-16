import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import path from 'path';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'home', component: HomeComponent },
  {
    path: 'product/:id',
    loadComponent: () =>
      import('./detail/detail.component').then((m) => m.DetailComponent),
  },
  {
    path: 'course/:id',
    loadComponent: () => import('./detail/detail.component').then((m) => m.DetailComponent),
  },
  {
    path: 'courses',
    loadComponent: () => import('./courses/courses.component').then((m) => m.CoursesComponent),
  },
  {
    path: 'my-courses',
    loadComponent: () => import('./my-courses/my-courses.component').then(m => m.MyCoursesComponent)
  },
  // {path: 'create', component: CreateComponent}
  {
    path: 'login',
    loadComponent: () =>
      import('./login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'cart',
    loadComponent: () =>
      import('./cart/cart.component').then((m) => m.CartComponent),
  },
  {
    path: 'checkout',
    loadComponent: () =>
      import('./checkout/checkout.component').then((m) => m.CheckoutComponent),
  },
  {
    path: 'vnpay-return',
    loadComponent: () =>
      import('./vnpay-return/vnpay-return.component').then((m) => m.VnpayReturnComponent),
  },
  {
    path: 'momo-return',
    loadComponent: () =>
      import('./momo-return/momo-return.component').then((m) => m.MomoReturnComponent),
  },
  {
    path: 'payment-result',
    loadComponent: () =>
      import('./payment-result/payment-result.component').then((m) => m.PaymentResultComponent),
  },
  {
    path: 'account',
    loadComponent: () => import('./account/account.component').then(m => m.AccountComponent)
  },
  {
    path: 'learn/:id',
    loadComponent: () => import('./course-player/course-player.component').then(m => m.CoursePlayerComponent)
  },
  {
    path: 'course/:id/quizzes',
    loadComponent: () => import('./course-quizzes').then(m => m.QuizListComponent)
  },
  {
    path: 'course/:id/quizzes/:quizId',
    loadComponent: () => import('./course-quizzes').then(m => m.QuizTakeComponent)
  },
  // Admin pages (simple local, no auth guard for now)
  {
    path: 'admin',
    loadComponent: () => import('./admin/admin-courses.component').then(m => m.AdminCoursesComponent)
  },
  {
    path: 'admin/course/:id/tests',
    loadComponent: () => import('./admin/admin-course-tests.component').then(m => m.AdminCourseTestsComponent)
  }
];
