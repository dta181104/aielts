import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { forkJoin, Observable, of } from 'rxjs';
import { map } from 'rxjs/operators';
import { environment } from '../environments/environment';

interface ApiResponse<T> {
  code?: number;
  message?: string;
  result?: T;
}

export interface SubmissionAnswerPayload {
  questionId: number;
  selectedOption?: string | null;
  textAnswer?: string | null;
  audioFile?: File | null;
  audioUrl?: string | null;
}

export interface SubmissionAnswerResponse {
  id: number;
  questionId: number;
  selectedOption?: string | null;
  isCorrect?: boolean;
  textAnswer?: string | null;
  audioUrl?: string | null;
  gradeScore?: number | null;
  teacherNote?: string | null;
}

export interface QuizSubmissionResponse {
  id: number;
  userId: number;
  quizId: number;
  startTime?: string;
  submitTime?: string | null;
  score?: number | null;
  status?: string;
  teacherFeedback?: string | null;
  answers?: SubmissionAnswerResponse[];
}

@Injectable({ providedIn: 'root' })
export class QuizSubmissionService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  startSubmission(quizId: number, userId: number): Observable<QuizSubmissionResponse> {
    const params = new HttpParams().set('userId', String(userId));
    return this.http
      .post<ApiResponse<QuizSubmissionResponse>>(`${this.baseUrl}/quizzes/${quizId}/submissions`, {}, { params })
      .pipe(map((res) => (res?.result ?? (res as unknown as QuizSubmissionResponse))));
  }

  saveAnswer(submissionId: number, payload: SubmissionAnswerPayload): Observable<SubmissionAnswerResponse> {
    return this.http
      .post<ApiResponse<SubmissionAnswerResponse>>(
        `${this.baseUrl}/quizzes/submissions/${submissionId}/answers`,
        payload
      )
      .pipe(map((res) => (res?.result ?? (res as unknown as SubmissionAnswerResponse))));
  }

  saveAnswersBulk(submissionId: number, payloads: SubmissionAnswerPayload[]): Observable<SubmissionAnswerResponse[]> {
    if (!payloads.length) {
      return of([]);
    }
    return forkJoin(payloads.map((payload) => this.saveAnswer(submissionId, payload)));
  }

  // New: upload audio file as multipart/form-data for an answer
  uploadAnswerAudio(submissionId: number, questionId: number, file: File): Observable<SubmissionAnswerResponse> {
    const form = new FormData();
    // backend expects the uploaded audio under field name `audioFile`
    form.append('audioFile', file, file.name);
    // backend expects questionId as a request param (query param)
    const params = new HttpParams().set('questionId', String(questionId));
    return this.http
      .post<ApiResponse<SubmissionAnswerResponse>>(
        `${this.baseUrl}/quizzes/submissions/${submissionId}/audio-answer`,
        form,
        { params }
      )
      .pipe(map((res) => (res?.result ?? (res as unknown as SubmissionAnswerResponse))));
  }

  // Lấy tất cả submission của một user trong 1 quiz
  getUserSubmissions(quizId: number, userId: number): Observable<QuizSubmissionResponse[]> {
    return this.http
      .get<ApiResponse<QuizSubmissionResponse[]>>(
        `${this.baseUrl}/quizzes/${quizId}/users/${userId}/submissions`
      )
      .pipe(map((res) => (res?.result ?? (res as unknown as QuizSubmissionResponse[]))));
  }

  submitSubmission(submissionId: number): Observable<QuizSubmissionResponse> {
    return this.http
      .put<ApiResponse<QuizSubmissionResponse>>(`${this.baseUrl}/quizzes/submissions/${submissionId}/submit`, {})
      .pipe(map((res) => (res?.result ?? (res as unknown as QuizSubmissionResponse))));
  }
}
