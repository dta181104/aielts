import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';
import { FormsModule } from '@angular/forms';
import { QuizComponent } from '../quiz/quiz.component';
import { ButtonComponent } from '../shared/button/button.component';

interface Lesson {
  id: number;
  title: string;
  duration?: string;
  videoUrl?: string; // optional video url or embed
  content?: string;
}

@Component({
  selector: 'app-course-player',
  standalone: true,
  imports: [CommonModule, FormsModule, QuizComponent, ButtonComponent],
  templateUrl: './course-player.component.html',
  styleUrls: ['./course-player.component.css'],
})
export class CoursePlayerComponent implements OnInit {
  courseId: number | null = null;
  course: ProductItems | null = null;
  lessons: Lesson[] = [];
  selectedLesson: Lesson | null = null;
  // Quiz related
  showQuiz = false;
  // multiple quizzes per course
  quizzes: any[] = [];
  selectedQuiz: any | null = null;
  // structured quiz sections for current selected quiz
  quizSections: any[] = [];
  // answers for MCQ questions (keyed by question id string)
  answers: { [questionId: string]: number } = {};
  // text answers for writing/speaking
  textAnswers: { [questionId: string]: string } = {};
  quizSubmitted = false;
  // auto-graded score (reading + listening)
  quizAutoScore: number | null = null;
  private quizStorageKey = 'course_quiz_results';
  // whether the inline quiz picker in lesson view is visible
  showQuizPicker = false;

  get quizAutoTotal(): number {
    try {
      return this.quizSections.reduce((sum, s) => sum + (s.questions ? s.questions.filter((q:any) => q.type === 'mcq').length : 0), 0);
    } catch (e) {
      return 0;
    }
  }

  constructor(private route: ActivatedRoute, private router: Router, private enrollment: EnrollmentService) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    if (!id) {
      // invalid id -> go back
      this.router.navigate(['/my-courses']);
      return;
    }

    const enrolled = this.enrollment.getAll();
    this.course = enrolled.find(c => c.id === id) || null;

    // if course not found among enrolled, still allow a fallback: try to navigate to detail
    if (!this.course) {
      // fallback to course detail if available
      this.router.navigate(['/course', id]);
      return;
    }

    // create mock lessons if none exist
    this.lessons = this.buildMockLessons(this.course);
    if (this.lessons.length) this.selectedLesson = this.lessons[0];
    
    // Quiz related: build multiple mock quizzes for this course
    this.showQuiz = false;
    this.quizzes = this.buildMockQuizzes(this.course);
    this.selectedQuiz = this.quizzes.length ? this.quizzes[0] : null;
    this.quizSections = this.selectedQuiz ? this.selectedQuiz.sections : [];
    this.answers = {};
    this.textAnswers = {};
    this.quizSubmitted = false;
    this.quizAutoScore = null;
    const saved = this.getSavedQuizResult(this.selectedQuiz?.id);
    if (saved) {
      this.quizSubmitted = true;
      this.quizAutoScore = saved.autoScore ?? null;
      this.answers = saved.answers || {};
      this.textAnswers = saved.textAnswers || {};
    }
  }

  get progressPercent(): number {
    const total = this.lessons.length || 1;
    const current = this.selectedLesson ? (this.lessons.findIndex(l => l.id === this.selectedLesson!.id) + 1) : 0;
    return Math.round((current / total) * 100);
  }

  buildMockLessons(course: ProductItems): Lesson[] {
    // lightweight mock lesson generator based on course name/price
    return [1,2,3,4,5].map(n => ({
      id: n,
      title: `Bài ${n}: Nội dung ${n}`,
      duration: `${8 + n} phút`,
      videoUrl: '',
      content: `Nội dung chi tiết của bài ${n} cho khóa ${course.name}.`
    }));
  }

  selectLesson(lesson: Lesson) {
    this.selectedLesson = lesson;
  }

  buildMockQuizSections(course: ProductItems) {
    // Build four parts reflecting Reading, Listening, Writing, Speaking
    const reading = {
      id: 'reading',
      title: 'Reading',
      questions: [1,2,3,4].map(n => ({
        id: `R${n}`,
        type: 'mcq',
        prompt: `Reading passage - question ${n}: choose the correct answer.`,
        options: [
          { id: 1, text: 'A' },
          { id: 2, text: 'B' },
          { id: 3, text: 'C' },
          { id: 4, text: 'D' },
        ],
        correctOptionId: (n % 4) + 1
      }))
    };

    const listening = {
      id: 'listening',
      title: 'Listening',
      questions: [1,2,3].map(n => ({
        id: `L${n}`,
        type: 'mcq',
        prompt: `Listening clip - question ${n}: listen and choose the correct answer. (audio placeholder)`,
        audioUrl: '',
        options: [
          { id: 1, text: 'A' },
          { id: 2, text: 'B' },
          { id: 3, text: 'C' },
          { id: 4, text: 'D' },
        ],
        correctOptionId: ((n + 1) % 4) + 1
      }))
    };

    const writing = {
      id: 'writing',
      title: 'Writing',
      questions: [1,2].map(n => ({
        id: `W${n}`,
        type: 'text',
        prompt: n === 1 ? 'Task 1: Describe the graph in about 150 words.' : 'Task 2: Write an essay of about 250 words on the given topic.'
      }))
    };

    const speaking = {
      id: 'speaking',
      title: 'Speaking',
      questions: [1].map(n => ({
        id: `S${n}`,
        type: 'text',
        prompt: 'Part 2: Speak about the topic for 1-2 minutes and summarize your answer here.'
      }))
    };

    return [reading, listening, writing, speaking];
  }

  buildMockQuizzes(course: ProductItems) {
    // create multiple mock quizzes per course (e.g., 3)
    const count = 3;
    const quizzes = [];
    for (let i = 1; i <= count; i++) {
      quizzes.push({ id: `Q${i}`, title: `Bài kiểm tra ${i}`, sections: this.buildMockQuizSections(course) });
    }
    return quizzes;
  }

  startQuiz() {
    this.showQuiz = true;
    this.quizSubmitted = false;
    this.quizAutoScore = null;
    this.answers = {};
    this.textAnswers = {};
    // ensure quizSections reflect selected quiz
    this.quizSections = this.selectedQuiz ? this.selectedQuiz.sections : [];
    // hide inline picker if present
    this.showQuizPicker = false;
  }

  submitQuiz() {
    // Grade auto-gradable parts (mcq: Reading + Listening). Writing/Speaking responses are saved for manual review.
    let autoTotal = 0;
    let autoScore = 0;
    this.quizSections.forEach(section => {
      section.questions.forEach((q: any) => {
        if (q.type === 'mcq') {
          autoTotal++;
          const a = this.answers[q.id];
          if (a === q.correctOptionId) autoScore++;
        }
      });
    });

    this.quizAutoScore = autoScore;
    this.quizSubmitted = true;
    this.saveQuizResult(this.selectedQuiz?.id, { autoScore, autoTotal });
    this.showQuiz = false;
  }

  saveQuizResult(quizId: string | undefined, payload?: { autoScore?: number; autoTotal?: number }) {
    if (!quizId) return;
    try {
      const raw = localStorage.getItem(this.quizStorageKey) || '{}';
      const map = JSON.parse(raw);
      const cid = String(this.courseId);
      if (!map[cid]) map[cid] = {};
      map[cid][quizId] = {
        autoScore: payload?.autoScore ?? this.quizAutoScore,
        autoTotal: payload?.autoTotal ?? this.quizSections.reduce((s, sec) => s + sec.questions.filter((q:any)=>q.type==='mcq').length, 0),
        answers: this.answers,
        textAnswers: this.textAnswers,
        timestamp: new Date().toISOString()
      };
      localStorage.setItem(this.quizStorageKey, JSON.stringify(map));
    } catch (e) {
      console.warn('Failed to save quiz result', e);
    }
  }

  getSavedQuizResult(quizId?: string) {
    try {
      const raw = localStorage.getItem(this.quizStorageKey) || '{}';
      const map = JSON.parse(raw);
      const cid = String(this.courseId);
      if (!map[cid]) return null;
      if (!quizId) return map[cid];
      return map[cid][quizId] || null;
    } catch (e) {
      return null;
    }
    // scroll into view or additional logic
  }

  selectQuiz(quiz: any) {
    this.selectedQuiz = quiz;
    this.quizSections = quiz.sections || [];
    const saved = this.getSavedQuizResult(quiz.id);
    this.answers = saved?.answers || {};
    this.textAnswers = saved?.textAnswers || {};
    this.quizAutoScore = saved?.autoScore ?? null;
    this.quizSubmitted = !!saved;
    this.showQuiz = false;
  }

  back() {
    this.router.navigate(['/my-courses']);
  }

  openQuizzes() {
    if (!this.courseId) return;
    this.router.navigate(['/course', String(this.courseId), 'quizzes']);
  }
}
