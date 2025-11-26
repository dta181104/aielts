import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';
import { environment } from '../environments/environment';

export type CourseStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type CourseType = 'FULL' | 'SINGLE' | 'TIPS';
export type LessonType = 'VIDEO' | 'QUIZ' | 'DOCUMENT';

export interface CourseCategory {
  id: number;
  code: string;
  name: string;
  description?: string;
}

export interface Attachment {
  id: number;
  lessonId: number;
  name?: string;
  url: string;
  fileType?: string;
}

export interface Lesson {
  id: number;
  sectionId: number;
  title: string;
  type: LessonType;
  videoUrl?: string;
  content?: string;
  duration?: number;
  orderIndex?: number;
  createdDate?: string;
  updatedDate?: string;
  deleted?: boolean;
  attachments?: Attachment[];
}

export interface Section {
  id: number;
  courseId: number;
  categoryId?: number;
  categoryName?: string;
  title: string;
  orderIndex?: number;
  deleted?: boolean;
  lessons?: Lesson[];
}

export type QuizSkill = 'LISTENING' | 'READING' | 'WRITING' | 'SPEAKING';

export interface Question {
  id: number;
  quizId?: number;
  content: string;
  audioUrl?: string;
  options?: string[];
  correctOption?: string;
  explanation?: string;
  skill?: QuizSkill;
}

export interface Quiz {
  id: number;
  title: string;
  lessonId: number;
  duration?: number;
  passScore?: number;
  shuffleQuestions?: boolean;
  questions?: Question[];
}

export interface Course {
  id: number;
  title: string;
  levelName?: string;
  targetBand?: number;
  price?: number;
  thumbnail?: string;
  imageUrl?: string;
  description?: string;
  courseType?: CourseType;
  status?: CourseStatus;
  createdDate?: string;
  deleted?: boolean;
  sections?: Section[];
}

export interface PageResult<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first?: boolean;
  last?: boolean;
  empty?: boolean;
}

export interface CourseListParams {
  keyword?: string;
  status?: CourseStatus;
  type?: CourseType;
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: 'ASC' | 'DESC';
}

export interface CourseRequestPayload {
  title: string;
  levelName?: string;
  targetBand?: number | null;
  price?: number | null;
  thumbnail?: string;
  description?: string;
  courseType?: CourseType;
  status?: CourseStatus;
}

export interface SectionRequestPayload {
  courseId: number;
  categoryId?: number;
  title: string;
  orderIndex?: number;
}

export interface LessonRequestPayload {
  sectionId: number;
  title: string;
  type: LessonType;
  videoUrl?: string;
  content?: string;
  duration?: number;
  orderIndex?: number;
}

export interface AttachmentRequestPayload {
  lessonId: number;
  name?: string;
  url: string;
  fileType?: string;
}

export interface QuizRequestPayload {
  title: string;
  lessonId: number;
  duration?: number | null;
  passScore?: number | null;
  shuffleQuestions?: boolean;
  questions?: QuestionRequestPayload[];
}

export interface QuestionRequestPayload {
  content: string;
  audioUrl?: string | null;
  options?: string[] | null;
  correctOption?: string | null;
  explanation?: string | null;
  skill?: QuizSkill;
}

interface ApiResponse<T> {
  code?: number;
  message?: string;
  result?: T;
}

interface CourseResponsePayload {
  id: number;
  title: string;
  levelName?: string;
  targetBand?: number;
  price?: number;
  thumbnail?: string;
  description?: string;
  courseType?: CourseType;
  status?: CourseStatus;
  createdDate?: string;
  deleted?: boolean;
  sections?: SectionResponsePayload[];
}

interface SectionResponsePayload {
  id: number;
  courseId: number;
  categoryId?: number;
  categoryName?: string;
  title: string;
  orderIndex?: number;
  deleted?: boolean;
  lessons?: LessonResponsePayload[];
}

interface LessonResponsePayload {
  id: number;
  sectionId: number;
  title: string;
  type: LessonType;
  videoUrl?: string;
  content?: string;
  duration?: number;
  orderIndex?: number;
  createdDate?: string;
  updatedDate?: string;
  deleted?: boolean;
  attachments?: AttachmentResponsePayload[];
}

interface AttachmentResponsePayload {
  id: number;
  lessonId: number;
  name?: string;
  url: string;
  fileType?: string;
}

interface QuizResponsePayload {
  id: number;
  title: string;
  lessonId: number;
  duration?: number;
  passScore?: number;
  shuffleQuestions?: boolean;
  questions?: QuestionResponsePayload[];
}

interface QuestionResponsePayload {
  id: number;
  quizId?: number;
  content: string;
  audioUrl?: string;
  options?: string[];
  correctOption?: string;
  explanation?: string;
  skill?: QuizSkill;
}

export interface CourseQuiz {
  id: string;
  courseId: string;
  title: string;
  content?: string; // simple JSON or HTML representing quiz
}

export interface AdminPermission {
  id: string;
  code: string;
  name: string;
  description?: string;
  status?: string;
  createdDate?: string;
  updatedDate?: string | null;
  createdBy?: string;
  updatedBy?: string | null;
  deleted?: boolean;
}

export interface AdminRole {
  id: string;
  code: string;
  name: string;
  description?: string;
  status?: string;
  permissionIds: string[];
  permissions?: AdminPermission[];
}

export interface RoleRequest {
  code: string;
  name: string;
  description?: string;
  permissions?: string[];
}

export interface RoleResponse {
  id?: string;
  code: string;
  name?: string;
  description?: string;
  permissionIds?: string[];
  permissions?: AdminPermission[];
}

export interface PermissionResponse {
  id?: string | number;
  code: string;
  name?: string;
  description?: string;
  status?: string;
  createdDate?: string;
  updatedDate?: string | null;
  createdBy?: string;
  updatedBy?: string | null;
  deleted?: boolean;
}

export interface PermissionRequest {
  code: string;
  name: string;
  description?: string;
  status?: string;
}

export interface AdminUser {
  id: string;
  username: string;
  name?: string;
  fullName?: string;
  email?: string;
  phone?: string;
  roles: string[]; // role ids
  status?: 'ACTIVE' | 'INACTIVE' | string;
  code?: string;
  password?: string;
}

export interface UserCreationRequest {
  username: string;
  name?: string;
  fullName?: string;
  email?: string;
  phone?: string;
  roles?: string[];
  pass?: string;
  status?: string;
}

export interface UserUpdateRequest {
  name?: string;
  fullName?: string;
  email?: string;
  phone?: string;
  roles?: string[];
  status?: string;
}

export interface RoleSummaryResponse {
  id?: string;
  code: string;
  name?: string;
  status?: string;
}

export interface UserResponse {
  id?: string;
  code?: string;
  username: string;
  name?: string;
  fullName?: string;
  email?: string;
  phone?: string;
  roles?: Array<RoleSummaryResponse | string>;
  status?: string;
}


@Injectable({ providedIn: 'root' })
export class AdminService {
  private coursesKey = 'admin_courses';
  private quizzesKey = 'admin_course_quizzes';
  private permissionsKey = 'admin_permissions';
  private rolesKey = 'admin_roles';
  private usersKey = 'admin_users';
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;
  private adminBase = `${this.baseUrl}/admin`;
  private courseApi = `${this.baseUrl}/courses`;
  private courseCategoryApi = `${this.baseUrl}/course-categories`;
  private sectionApi = `${this.baseUrl}/sections`;
  private lessonApi = `${this.baseUrl}/lessons`;
  private attachmentApi = `${this.baseUrl}/attachments`;
  private quizApi = `${this.baseUrl}/quizzes`;
  private questionApi = `${this.baseUrl}/questions`;
  private courseCache: Course[] = [];
  private readonly optionLetters = Array.from('ABCDEFGHIJKLMNOPQRSTUVWXYZ');

  generateId(prefix = ''): string {
    return prefix + Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  getCachedCourses(): Course[] {
    if (this.courseCache.length) {
      return this.courseCache;
    }
    this.courseCache = this.readCoursesFromStorage();
    return this.courseCache;
  }

  getCachedCourseById(courseId: number | string): Course | undefined {
    const id = typeof courseId === 'number' ? courseId : Number(courseId);
    if (!Number.isFinite(id)) {
      return undefined;
    }
    return this.getCachedCourses().find((course) => course.id === id);
  }

  fetchCourseCategories(): Observable<CourseCategory[]> {
    return this.http.get<ApiResponse<CourseCategory[]>>(this.courseCategoryApi).pipe(
      map((response) => this.unwrapResult(response)),
      catchError((error) => {
        console.error('Failed to load course categories', error);
        return of<CourseCategory[]>([]);
      })
    );
  }

  fetchCourses(params: CourseListParams = {}): Observable<PageResult<Course>> {
    const pageSize = params.size ?? 10;
    const hasFilters = Boolean(params.keyword || params.status || params.type);
    const endpoint = hasFilters ? `${this.courseApi}/search` : this.courseApi;
    const httpParams = this.buildCourseParams(params);

    return this.http.get<ApiResponse<PageResult<CourseResponsePayload>>>(endpoint, { params: httpParams }).pipe(
      map((response) => this.mapCoursePage(response)),
      catchError((error) => {
        console.error('Failed to load courses from API', error);
        const fallback = this.getCachedCourses();
        return of<PageResult<Course>>({
          content: fallback,
          totalElements: fallback.length,
          totalPages: fallback.length ? 1 : 0,
          number: 0,
          size: Math.max(pageSize, fallback.length || 1),
          first: true,
          last: true,
          empty: fallback.length === 0,
        });
      })
    );
  }

  getCourseDetail(id: number): Observable<Course> {
    return this.http.get<ApiResponse<CourseResponsePayload>>(`${this.courseApi}/${id}`).pipe(
      map((response) => this.mapCourseResponse(this.unwrapResult(response)))
    );
  }

  createCourse(payload: CourseRequestPayload): Observable<Course> {
    return this.http.post<ApiResponse<CourseResponsePayload>>(this.courseApi, payload).pipe(
      map((response) => this.mapCourseResponse(this.unwrapResult(response)))
    );
  }

  updateCourse(id: number, payload: CourseRequestPayload): Observable<Course> {
    return this.http.put<ApiResponse<CourseResponsePayload>>(`${this.courseApi}/${id}`, payload).pipe(
      map((response) => this.mapCourseResponse(this.unwrapResult(response)))
    );
  }

  deleteCourse(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.courseApi}/${id}`).pipe(map(() => undefined));
  }

  getSectionsByCourse(courseId: number): Observable<Section[]> {
    return this.http.get<ApiResponse<SectionResponsePayload[]>>(`${this.sectionApi}/course/${courseId}`).pipe(
      map((response) => this.unwrapResult(response).map((section) => this.mapSectionResponse(section))),
      catchError((error) => {
        console.error('Failed to load sections', error);
        return of<Section[]>([]);
      })
    );
  }

  createSection(payload: SectionRequestPayload): Observable<Section> {
    return this.http.post<ApiResponse<SectionResponsePayload>>(this.sectionApi, payload).pipe(
      map((response) => this.mapSectionResponse(this.unwrapResult(response)))
    );
  }

  updateSection(sectionId: number, payload: SectionRequestPayload): Observable<Section> {
    return this.http.put<ApiResponse<SectionResponsePayload>>(`${this.sectionApi}/${sectionId}`, payload).pipe(
      map((response) => this.mapSectionResponse(this.unwrapResult(response)))
    );
  }

  deleteSection(sectionId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.sectionApi}/${sectionId}`).pipe(map(() => undefined));
  }

  getLessonsBySection(sectionId: number): Observable<Lesson[]> {
    return this.http.get<ApiResponse<LessonResponsePayload[]>>(`${this.lessonApi}/section/${sectionId}`).pipe(
      map((response) => this.unwrapResult(response).map((lesson) => this.mapLessonResponse(lesson))),
      catchError((error) => {
        console.error('Failed to load lessons', error);
        return of<Lesson[]>([]);
      })
    );
  }

  createLesson(payload: LessonRequestPayload): Observable<Lesson> {
    return this.http.post<ApiResponse<LessonResponsePayload>>(this.lessonApi, payload).pipe(
      map((response) => this.mapLessonResponse(this.unwrapResult(response)))
    );
  }

  updateLesson(lessonId: number, payload: LessonRequestPayload): Observable<Lesson> {
    return this.http.put<ApiResponse<LessonResponsePayload>>(`${this.lessonApi}/${lessonId}`, payload).pipe(
      map((response) => this.mapLessonResponse(this.unwrapResult(response)))
    );
  }

  deleteLesson(lessonId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.lessonApi}/${lessonId}`).pipe(map(() => undefined));
  }

  getAttachmentsByLesson(lessonId: number): Observable<Attachment[]> {
    return this.http.get<ApiResponse<AttachmentResponsePayload[]>>(`${this.attachmentApi}/lesson/${lessonId}`).pipe(
      map((response) => this.unwrapResult(response).map((attachment) => this.mapAttachmentResponse(attachment))),
      catchError((error) => {
        console.error('Failed to load attachments', error);
        return of<Attachment[]>([]);
      })
    );
  }

  createAttachment(payload: AttachmentRequestPayload): Observable<Attachment> {
    return this.http.post<ApiResponse<AttachmentResponsePayload>>(this.attachmentApi, payload).pipe(
      map((response) => this.mapAttachmentResponse(this.unwrapResult(response)))
    );
  }

  updateAttachment(attachmentId: number, payload: AttachmentRequestPayload): Observable<Attachment> {
    return this.http.put<ApiResponse<AttachmentResponsePayload>>(`${this.attachmentApi}/${attachmentId}`, payload).pipe(
      map((response) => this.mapAttachmentResponse(this.unwrapResult(response)))
    );
  }

  deleteAttachment(attachmentId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.attachmentApi}/${attachmentId}`).pipe(map(() => undefined));
  }

  getQuizzesByLesson(lessonId: number): Observable<Quiz[]> {
    return this.http.get<ApiResponse<QuizResponsePayload[]>>(`${this.quizApi}/lesson/${lessonId}`).pipe(
      map((response) => this.unwrapResult(response).map((quiz) => this.mapQuizResponse(quiz)))
    );
  }

  getQuizDetail(id: number): Observable<Quiz> {
    return this.http.get<ApiResponse<QuizResponsePayload>>(`${this.quizApi}/${id}`).pipe(
      map((response) => this.mapQuizResponse(this.unwrapResult(response)))
    );
  }

  createQuiz(payload: QuizRequestPayload): Observable<Quiz> {
    return this.http.post<ApiResponse<QuizResponsePayload>>(this.quizApi, this.normalizeQuizPayload(payload)).pipe(
      map((response) => this.mapQuizResponse(this.unwrapResult(response)))
    );
  }

  updateQuiz(id: number, payload: QuizRequestPayload): Observable<Quiz> {
    return this.http.put<ApiResponse<QuizResponsePayload>>(`${this.quizApi}/${id}`, this.normalizeQuizPayload(payload)).pipe(
      map((response) => this.mapQuizResponse(this.unwrapResult(response)))
    );
  }

  deleteQuiz(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.quizApi}/${id}`).pipe(map(() => undefined));
  }

  createQuizQuestion(quizId: number, payload: QuestionRequestPayload): Observable<Question> {
    const body = this.normalizeQuestionPayload(payload);
    return this.http.post<ApiResponse<QuestionResponsePayload>>(`${this.quizApi}/${quizId}/questions`, body).pipe(
      map((response) => this.mapQuestionResponse(this.unwrapResult(response)))
    );
  }

  updateQuizQuestion(questionId: number, payload: QuestionRequestPayload): Observable<Question> {
    const body = this.normalizeQuestionPayload(payload);
    return this.http.put<ApiResponse<QuestionResponsePayload>>(`${this.questionApi}/${questionId}`, body).pipe(
      map((response) => this.mapQuestionResponse(this.unwrapResult(response)))
    );
  }

  deleteQuizQuestion(questionId: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.questionApi}/${questionId}`).pipe(map(() => undefined));
  }

  private readCoursesFromStorage(): Course[] {
    try {
      const raw = localStorage.getItem(this.coursesKey);
      if (!raw) {
        return [];
      }
      const parsed = JSON.parse(raw);
      if (!Array.isArray(parsed)) {
        return [];
      }
      return parsed
        .map((entry) => this.normalizeCachedCourse(entry as Partial<Course>))
        .filter((course): course is Course => Boolean(course));
    } catch (error) {
      console.error('Failed to parse cached courses', error);
      return [];
    }
  }

  private normalizeCachedCourse(entry: Partial<Course> | undefined): Course | undefined {
    if (!entry || !entry.title) {
      return undefined;
    }
    const numericId = typeof entry.id === 'number' ? entry.id : Number(entry.id);
    const id = Number.isFinite(numericId) ? numericId : Date.now();

    return {
      id,
      title: entry.title,
      levelName: entry.levelName,
      targetBand: entry.targetBand,
      price: entry.price,
      thumbnail: entry.thumbnail ?? entry.imageUrl,
      imageUrl: entry.imageUrl ?? entry.thumbnail,
      description: entry.description,
      courseType: entry.courseType,
      status: entry.status,
      createdDate: entry.createdDate,
      deleted: entry.deleted,
      sections: entry.sections,
    };
  }

  private writeCourseCache(list: Course[]) {
    this.courseCache = list;
    localStorage.setItem(this.coursesKey, JSON.stringify(list));
  }

  private normalizeQuizPayload(payload: QuizRequestPayload): QuizRequestPayload {
    return {
      title: payload.title.trim(),
      lessonId: payload.lessonId,
      duration:
        payload.duration === null || payload.duration === undefined ? undefined : Number(payload.duration),
      passScore:
        payload.passScore === null || payload.passScore === undefined ? undefined : Number(payload.passScore),
      shuffleQuestions: payload.shuffleQuestions ?? false,
      questions: payload.questions?.map((question) => {
        const formattedOptions = this.normalizeQuestionOptions(question.options);
        return {
          content: question.content.trim(),
          audioUrl: question.audioUrl?.trim() || undefined,
          options: formattedOptions,
          correctOption: question.correctOption?.trim() || undefined,
          explanation: question.explanation?.trim() || undefined,
          skill: question.skill,
        };
      }),
    };
  }

  private normalizeQuestionPayload(payload: QuestionRequestPayload): QuestionRequestPayload {
    const normalizedOptions = this.normalizeQuestionOptions(payload.options);
    return {
      content: payload.content.trim(),
      audioUrl: payload.audioUrl?.trim() || undefined,
      options: normalizedOptions,
      correctOption: payload.correctOption?.trim() || undefined,
      explanation: payload.explanation?.trim() || undefined,
      skill: payload.skill,
    };
  }

  private normalizeQuestionOptions(options?: string[] | null): string[] | undefined {
    const cleanedOptions = (options ?? [])
      .map((option) => option?.trim() ?? '')
      .filter((option) => option.length)
      .map((option) => option.replace(/^"|"$/g, '').trim());

    if (!cleanedOptions.length) {
      return undefined;
    }

    return cleanedOptions.map((option, index) => this.ensureOptionLabel(option, index));
  }

  private ensureOptionLabel(option: string, index: number): string {
    if (/^[A-Z]\s*["']?\s*(?::|=)/i.test(option)) {
      return option;
    }
    const label = this.optionLetters[index] ?? `Option ${index + 1}`;
    return `${label}": "${option}`;
  }

  private buildCourseParams(params: CourseListParams): HttpParams {
    let httpParams = new HttpParams()
      .set('page', String(params.page ?? 0))
      .set('size', String(params.size ?? 10));

    if (params.sortBy) {
      httpParams = httpParams.set('sortBy', params.sortBy);
    }
    if (params.direction) {
      httpParams = httpParams.set('direction', params.direction);
    }
    if (params.keyword) {
      httpParams = httpParams.set('keyword', params.keyword);
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }
    if (params.type) {
      httpParams = httpParams.set('type', params.type);
    }

    return httpParams;
  }

  private mapCoursePage(response: ApiResponse<PageResult<CourseResponsePayload>>): PageResult<Course> {
    const page = this.unwrapResult(response);
    const mappedContent = (page.content ?? []).map((course) => this.mapCourseResponse(course));

    if (page.number === 0 || !this.courseCache.length) {
      this.writeCourseCache(mappedContent);
    }

    return {
      content: mappedContent,
      totalElements: page.totalElements,
      totalPages: page.totalPages,
      number: page.number,
      size: page.size,
      first: page.first,
      last: page.last,
      empty: page.empty,
    };
  }

  private mapCourseResponse(payload: CourseResponsePayload): Course {
    const price = payload.price !== undefined && payload.price !== null ? Number(payload.price) : undefined;
    return {
      id: payload.id,
      title: payload.title,
      levelName: payload.levelName,
      targetBand: payload.targetBand,
      price,
      thumbnail: payload.thumbnail,
      imageUrl: payload.thumbnail,
      description: payload.description,
      courseType: payload.courseType,
      status: payload.status,
      createdDate: payload.createdDate,
      deleted: payload.deleted,
      sections: payload.sections?.map((section) => this.mapSectionResponse(section)),
    };
  }

  private mapSectionResponse(payload: SectionResponsePayload): Section {
    return {
      id: payload.id,
      courseId: payload.courseId,
      categoryId: payload.categoryId,
      categoryName: payload.categoryName,
      title: payload.title,
      orderIndex: payload.orderIndex,
      deleted: payload.deleted,
      lessons: payload.lessons?.map((lesson) => this.mapLessonResponse(lesson)),
    };
  }

  private mapLessonResponse(payload: LessonResponsePayload): Lesson {
    return {
      id: payload.id,
      sectionId: payload.sectionId,
      title: payload.title,
      type: payload.type,
      videoUrl: payload.videoUrl,
      content: payload.content,
      duration: payload.duration,
      orderIndex: payload.orderIndex,
      createdDate: payload.createdDate,
      updatedDate: payload.updatedDate,
      deleted: payload.deleted,
      attachments: payload.attachments?.map((attachment) => this.mapAttachmentResponse(attachment)),
    };
  }

  private mapAttachmentResponse(payload: AttachmentResponsePayload): Attachment {
    return {
      id: payload.id,
      lessonId: payload.lessonId,
      name: payload.name,
      url: payload.url,
      fileType: payload.fileType,
    };
  }

  private mapQuizResponse(payload: QuizResponsePayload): Quiz {
    return {
      id: payload.id,
      title: payload.title,
      lessonId: payload.lessonId,
      duration: payload.duration ?? undefined,
      passScore: payload.passScore ?? undefined,
      shuffleQuestions: payload.shuffleQuestions ?? false,
      questions: payload.questions?.map((question) => this.mapQuestionResponse(question)),
    };
  }

  private mapQuestionResponse(payload: QuestionResponsePayload): Question {
    return {
      id: payload.id,
      quizId: payload.quizId,
      content: payload.content,
      audioUrl: payload.audioUrl ?? undefined,
      options: payload.options ?? undefined,
      correctOption: payload.correctOption ?? undefined,
      explanation: payload.explanation ?? undefined,
      skill: payload.skill ?? undefined,
    };
  }

  private unwrapResult<T>(response: ApiResponse<T>): T {
    if (response?.result !== undefined && response?.result !== null) {
      return response.result;
    }
    throw new Error('API response missing result payload');
  }

  private _readQuizzesMap(): Record<string, CourseQuiz[]> {
    try {
      const raw = localStorage.getItem(this.quizzesKey);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      return {};
    }
  }

  clearCourseQuizzes(courseId: number | string) {
    const map = this._readQuizzesMap();
    const key = courseId.toString();
    if (map[key]) {
      delete map[key];
      localStorage.setItem(this.quizzesKey, JSON.stringify(map));
    }
  }

  getQuizzes(courseId: string): CourseQuiz[] {
    const m = this._readQuizzesMap();
    return m[courseId] || [];
  }

  saveQuiz(courseId: string, quiz: CourseQuiz) {
    const m = this._readQuizzesMap();
    if (!m[courseId]) m[courseId] = [];
    const idx = m[courseId].findIndex(t => t.id === quiz.id);
    if (idx >= 0) m[courseId][idx] = quiz;
    else m[courseId].push(quiz);
    localStorage.setItem(this.quizzesKey, JSON.stringify(m));
  }

  // deleteQuiz(courseId: string, quizId: string) {
  //   const m = this._readQuizzesMap();
  //   if (!m[courseId]) return;
  //   m[courseId] = m[courseId].filter(t => t.id !== quizId);
  //   localStorage.setItem(this.quizzesKey, JSON.stringify(m));
  // }

  // Permissions
  getPermissions(): AdminPermission[] {
    try {
      const raw = localStorage.getItem(this.permissionsKey);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  savePermission(permission: AdminPermission) {
    const list = this.getPermissions();
    const idx = list.findIndex(p => p.id === permission.id);
    if (idx >= 0) list[idx] = permission;
    else list.push(permission);
    localStorage.setItem(this.permissionsKey, JSON.stringify(list));
  }

  private removeLocalPermission(id: string) {
    const list = this.getPermissions().filter(p => p.id !== id);
    localStorage.setItem(this.permissionsKey, JSON.stringify(list));
    // remove permission from roles referencing it
    const roles = this.getRoles();
    let changed = false;
    roles.forEach(r => {
      const next = r.permissionIds.filter(pid => pid !== id);
      if (next.length !== r.permissionIds.length) {
        r.permissionIds = next;
        changed = true;
      }
    });
    if (changed) localStorage.setItem(this.rolesKey, JSON.stringify(roles));
  }

  // Roles
  getRoles(): AdminRole[] {
    try {
      const raw = localStorage.getItem(this.rolesKey);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  private persistRole(role: AdminRole) {
    const list = this.getRoles();
    const idx = list.findIndex(r => r.id === role.id);
    if (idx >= 0) list[idx] = role;
    else list.push(role);
    localStorage.setItem(this.rolesKey, JSON.stringify(list));
  }

  private removeLocalRole(id: string) {
    const list = this.getRoles().filter(r => r.id !== id);
    localStorage.setItem(this.rolesKey, JSON.stringify(list));
    // detach from users
    const users = this.getUsers();
    let changed = false;
    users.forEach(u => {
      const next = u.roles.filter(rid => rid !== id);
      if (next.length !== u.roles.length) {
        u.roles = next;
        changed = true;
      }
    });
    if (changed) localStorage.setItem(this.usersKey, JSON.stringify(users));
  }

  // Users
  getUsers(): AdminUser[] {
    try {
      const raw = localStorage.getItem(this.usersKey);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  }

  saveUser(user: AdminUser) {
    const list = this.getUsers();
    const idx = list.findIndex(u => u.id === user.id);
    if (idx >= 0) list[idx] = user;
    else list.push(user);
    localStorage.setItem(this.usersKey, JSON.stringify(list));
  }

  fetchUsers(): Observable<AdminUser[]> {
    return this.http.get(`${this.adminBase}/accounts`).pipe(
      map((payload) => this.normalizeList<UserResponse>(payload).map((user) => this.mapUserResponse(user))),
      catchError((error) => {
        console.error('Failed to load users from API', error);
        return of(this.getUsers());
      })
    );
  }

  createUser(payload: UserCreationRequest): Observable<AdminUser> {
    return this.http.post(`${this.adminBase}/create`, payload).pipe(
      map((response) => {
        const user = this.extractSingle<UserResponse>(response);
        if (!user) {
          throw new Error('Empty user response');
        }
        return this.mapUserResponse(user);
      })
    );
  }

  updateUser(code: string, payload: UserUpdateRequest): Observable<AdminUser> {
    return this.http.put(`${this.adminBase}/update/${encodeURIComponent(code)}`, payload).pipe(
      map((response) => {
        const user = this.extractSingle<UserResponse>(response);
        if (!user) {
          throw new Error('Empty user response');
        }
        return this.mapUserResponse(user);
      })
    );
  }

  deleteUser(code: string): Observable<void> {
    return this.http.delete(`${this.adminBase}/delete/${encodeURIComponent(code)}`).pipe(
      map(() => undefined)
    );
  }

  fetchPermissions(): Observable<AdminPermission[]> {
    return this.http.get(`${this.baseUrl}/permissions`).pipe(
      map((payload) => {
        const normalized = this.normalizeList<PermissionResponse>(payload);
        return normalized.map((perm) => this.mapPermissionResponse(perm));
      }),
      catchError((error) => {
        console.error('Failed to load permissions from API', error);
        return of(this.getPermissions());
      })
    );
  }

  createPermission(payload: PermissionRequest): Observable<AdminPermission> {
    return this.http.post(`${this.baseUrl}/permissions`, payload).pipe(
      map((response) => {
        const permission = this.extractSingle<PermissionResponse>(response);
        if (!permission) {
          throw new Error('Empty permission response');
        }
        const mapped = this.mapPermissionResponse(permission);
        this.savePermission(mapped);
        return mapped;
      })
    );
  }

  deletePermission(code: string): Observable<AdminPermission> {
    return this.http.delete(`${this.baseUrl}/permissions/${encodeURIComponent(code)}`).pipe(
      map((response) => {
        const permission = this.extractSingle<PermissionResponse>(response);
        if (!permission) {
          throw new Error('Empty permission response');
        }
        const mapped = this.mapPermissionResponse(permission);
        if (mapped.id) {
          this.removeLocalPermission(mapped.id);
        }
        return mapped;
      })
    );
  }

  fetchRoles(): Observable<AdminRole[]> {
    return this.http.get(`${this.baseUrl}/roles`).pipe(
      map((payload) => {
        const normalized = this.normalizeList<RoleResponse>(payload);
        const mapped = normalized.map((role) => this.mapRoleResponse(role));
        mapped.forEach((role) => this.persistRole(role));
        return mapped;
      }),
      catchError((error) => {
        console.error('Failed to load roles from API', error);
        return of(this.getRoles());
      })
    );
  }

  createRole(payload: RoleRequest): Observable<AdminRole> {
    return this.http.post(`${this.baseUrl}/roles`, payload).pipe(
      map((response) => {
        const role = this.extractSingle<RoleResponse>(response);
        if (!role) {
          throw new Error('Empty role response');
        }
        const mapped = this.mapRoleResponse(role);
        this.persistRole(mapped);
        return mapped;
      })
    );
  }

  updateRole(roleId: string, payload: RoleRequest): Observable<AdminRole> {
    return this.http.put(`${this.baseUrl}/roles/${encodeURIComponent(roleId)}`, payload).pipe(
      map((response) => {
        const role = this.extractSingle<RoleResponse>(response);
        if (!role) {
          throw new Error('Empty role response');
        }
        const mapped = this.mapRoleResponse(role);
        this.persistRole(mapped);
        return mapped;
      })
    );
  }

  deleteRole(roleId: string): Observable<void> {
    return this.http.delete(`${this.baseUrl}/roles/${encodeURIComponent(roleId)}`).pipe(
      map(() => {
        this.removeLocalRole(roleId);
      })
    );
  }

  private normalizeList<T>(payload: unknown): T[] {
    if (!payload) {
      return [];
    }
    if (Array.isArray(payload)) {
      return payload;
    }
    if (typeof payload === 'object') {
      const obj = payload as Record<string, unknown>;
      const keys = ['result', 'data', 'items', 'content', 'records'];
      for (const key of keys) {
        const nested = obj[key];
        if (nested && nested !== payload) {
          const normalized = this.normalizeList<T>(nested);
          if (normalized.length) {
            return normalized;
          }
        }
      }
    }
    return [];
  }

  private extractSingle<T>(payload: unknown): T | undefined {
    if (!payload) {
      return undefined;
    }
    if (Array.isArray(payload)) {
      return payload[0] as T;
    }
    if (typeof payload === 'object') {
      const obj = payload as Record<string, unknown>;
      const keys = ['result', 'data', 'item'];
      for (const key of keys) {
        const nested = obj[key];
        if (nested && nested !== payload) {
          const candidate = this.extractSingle<T>(nested);
          if (candidate) {
            return candidate;
          }
        }
      }
    }
    return payload as T;
  }

  private mapUserResponse(response: UserResponse): AdminUser {
    const roleEntries = (response.roles ?? []).map(entry => {
      if (typeof entry === 'string') {
        return entry;
      }
      return entry.code ?? entry.name ?? entry.id ?? '';
    }).filter(Boolean);

    return {
      id: response.id?.toString() ?? response.code ?? this.generateId('user_'),
      username: response.username,
      name: response.name ?? response.fullName,
      fullName: response.fullName ?? response.name,
      email: response.email,
      phone: response.phone,
      roles: roleEntries,
      status: response.status ?? 'ACTIVE',
      code: response.code ?? response.username,
    };
  }

  private mapRoleResponse(response: RoleResponse): AdminRole {
    return {
      id: response.id ?? this.generateId('role_'),
      code: response.code,
      name: response.name ?? response.code,
      description: response.description,
      permissionIds: response.permissionIds ?? response.permissions?.map(p => p.id?.toString() ?? '').filter(Boolean) ?? [],
      permissions: response.permissions?.map(p => ({
        id: p.id?.toString() ?? '',
        code: p.code,
        name: p.name ?? '',
        description: p.description,
        status: p.status,
        createdDate: p.createdDate,
        updatedDate: p.updatedDate,
        createdBy: p.createdBy,
        updatedBy: p.updatedBy,
        deleted: p.deleted,
      })) ?? [],
    };
  }

  private mapPermissionResponse(response: PermissionResponse): AdminPermission {
    return {
      id: response.id?.toString() ?? '',
      code: response.code,
      name: response.name ?? response.code,
      description: response.description,
      status: response.status,
      createdDate: response.createdDate,
      updatedDate: response.updatedDate,
      createdBy: response.createdBy,
      updatedBy: response.updatedBy,
      deleted: response.deleted,
    };
  }
}
