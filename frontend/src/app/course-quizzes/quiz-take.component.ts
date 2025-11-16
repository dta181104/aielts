import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';
import { QuizComponent } from '../quiz/quiz.component';
import { ButtonComponent } from '../shared/button/button.component';

@Component({
  selector: 'app-quiz-take',
  standalone: true,
  imports: [CommonModule, FormsModule, QuizComponent, ButtonComponent],
  templateUrl: './quiz-take.component.html',
  styleUrls: ['./quiz-take.component.css']
})
export class QuizTakeComponent implements OnInit, OnDestroy {
  courseId: number | null = null;
  quizId: string | null = null;
  course: ProductItems | null = null;
  quizzes: any[] = [];
  selectedQuiz: any | null = null;
  quizSections: any[] = [];
  answers: { [k:string]: any } = {};
  textAnswers: { [k:string]: string } = {};
  quizSubmitted = false;
  quizAutoScore: number | null = null;
  private quizStorageKey = 'course_quiz_results';

  // Exam orchestration: order, metadata, timers and states
  sectionOrder = ['listening', 'reading', 'writing', 'speaking'];
  sectionMeta: { [id: string]: { title: string; minutes: number; desc?: string } } = {
    listening: { title: 'Kỹ năng Nghe (IELTS Listening)', minutes: 30, desc: '30 phút' },
    reading: { title: 'Kỹ năng Đọc (IELTS Reading)', minutes: 60, desc: '60 phút' },
    writing: { title: 'Kỹ năng Viết (IELTS Writing)', minutes: 60, desc: '60 phút' },
    speaking: { title: 'Kỹ năng Nói (IELTS Speaking)', minutes: 15, desc: '11-15 phút' }
  };
  currentSectionIndex: number | null = null;
  sectionRemaining: { [id: string]: number } = {};
  sectionStarted: { [id: string]: boolean } = {};
  sectionCompleted: { [id: string]: boolean } = {};
  sectionResults: { [sectionId: string]: any } = {};
  private activeTimer: any = null;

  get quizAutoTotal(): number {
    try {
      return this.quizSections.reduce((sum, sec) => {
        const cnt = sec.questions ? sec.questions.filter((q: any) => q.type === 'mcq').length : 0;
        return sum + cnt;
      }, 0);
    } catch (e) {
      return 0;
    }
  }

  constructor(private route: ActivatedRoute, private router: Router, private enrollment: EnrollmentService) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const qId = this.route.snapshot.paramMap.get('quizId');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    this.quizId = qId;
    if (!id || !qId) {
      this.router.navigate(['/my-courses']);
      return;
    }
    const enrolled = this.enrollment.getAll();
    this.course = enrolled.find((c: ProductItems) => c.id === id) || null;
    if (!this.course) {
      this.router.navigate(['/course', id]);
      return;
    }

    this.quizzes = this.buildMockQuizzes(this.course);
    this.selectedQuiz = this.quizzes.find(q => q.id === qId) || null;
    if (!this.selectedQuiz) {
      // if quiz not found, go back to list
      this.router.navigate(['/course', id, 'quizzes']);
      return;
    }
    this.quizSections = this.selectedQuiz.sections || [];
    // prepare per-section timers and states
    this.sectionOrder.forEach(sid => {
      const m = this.sectionMeta[sid]?.minutes ?? 0;
      this.sectionRemaining[sid] = m * 60;
      this.sectionStarted[sid] = false;
      this.sectionCompleted[sid] = false;
    });

    const saved = this.getSavedQuizResult(this.selectedQuiz.id);
    if (saved) {
      this.quizSubmitted = true;
      this.quizAutoScore = saved.autoScore ?? null;
      this.answers = saved.answers || {};
      this.textAnswers = saved.textAnswers || {};
    }
  }

  ngOnDestroy(): void {
    this.clearActiveTimer();
  }

  buildMockQuizSections(course: ProductItems) {
    const reading = { id: 'reading', title: 'Reading', questions: [1,2,3,4].map(n=>({ id:`R${n}`, type:'mcq', prompt:`Reading ${n}`, options:[{id:1,text:'A'},{id:2,text:'B'},{id:3,text:'C'},{id:4,text:'D'}], correctOptionId:(n%4)+1 })) };
    const listening = { id:'listening', title:'Listening', questions:[1,2,3].map(n=>({ id:`L${n}`, type:'mcq', prompt:`Listening ${n}`, options:[{id:1,text:'A'},{id:2,text:'B'},{id:3,text:'C'},{id:4,text:'D'}], correctOptionId:((n+1)%4)+1 })) };
    const writing = { id:'writing', title:'Writing', questions:[1,2].map(n=>({id:`W${n}`, type:'text', prompt: n===1? 'Task1' : 'Task2' })) };
    const speaking = { id:'speaking', title:'Speaking', questions:[1].map(n=>({ id:`S${n}`, type:'text', prompt:'Speaking task' })) };
    return [reading, listening, writing, speaking];
  }

  buildMockQuizzes(course: ProductItems) { const out:any[] = []; for(let i=1;i<=3;i++) out.push({ id:`Q${i}`, title:`Bài kiểm tra ${i}`, sections:this.buildMockQuizSections(course) }); return out; }

  getSavedQuizResult(quizId?: string) {
    try {
      const raw = localStorage.getItem(this.quizStorageKey) || '{}';
      const map = JSON.parse(raw);
      const cid = String(this.courseId);
      if (!map[cid]) return null;
      if (!quizId) return null;
      return map[cid][quizId] || null;
    } catch(e) { return null; }
  }

  onCancel() {
    if (this.courseId) this.router.navigate(['/course', String(this.courseId), 'quizzes']);
  }

  onSubmit() {
    // grade auto parts
    let autoTotal = 0; let autoScore = 0;
    this.quizSections.forEach(section => {
      section.questions.forEach((q:any) => {
        if (q.type === 'mcq') { autoTotal++; const a = this.answers[q.id]; if (a === q.correctOptionId) autoScore++; }
      });
    });
    this.quizAutoScore = autoScore;
    this.quizSubmitted = true;
    this.saveQuizResult(this.selectedQuiz?.id, { autoScore, autoTotal });
  }

  // --- Section / timer helpers ---
  openSection(index: number) {
    this.currentSectionIndex = index;
    const id = this.sectionOrder[index];
    const sect = this.selectedQuiz?.sections.find((s: any) => s.id === id);
    this.quizSections = sect ? [sect] : [];
    const saved = this.getSavedQuizResult(this.selectedQuiz?.id)?.sections || {};
    const sr = saved?.[id] || null;
    this.answers = sr?.answers || {};
    this.textAnswers = sr?.textAnswers || {};
    this.quizSubmitted = !!sr;
    this.quizAutoScore = sr?.autoScore ?? null;
  }

  startSection(index: number) {
    const id = this.sectionOrder[index];
    if (!this.sectionStarted[id]) {
      if (!this.sectionRemaining[id]) this.sectionRemaining[id] = (this.sectionMeta[id]?.minutes ?? 0) * 60;
      this.sectionStarted[id] = true;
    }
    this.currentSectionIndex = index;
    const sect = this.selectedQuiz?.sections.find((s: any) => s.id === id);
    this.quizSections = sect ? [sect] : [];
    this.quizSubmitted = false;
    this.quizAutoScore = null;
    this.clearActiveTimer();
    this.activeTimer = setInterval(() => {
      this.sectionRemaining[id] = Math.max(0, this.sectionRemaining[id] - 1);
      if (this.sectionRemaining[id] <= 0) {
        // auto-submit this section on timeout
        this.submitSection(index);
      }
    }, 1000);
  }

  clearActiveTimer() {
    if (this.activeTimer) { clearInterval(this.activeTimer); this.activeTimer = null; }
  }

  formatTime(sec: number) {
    const m = Math.floor(sec / 60).toString().padStart(2, '0');
    const s = Math.floor(sec % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  submitSection(index: number) {
    const id = this.sectionOrder[index];
    const section = this.selectedQuiz?.sections.find((s: any) => s.id === id);
    let autoTotal = 0; let autoScore = 0;
    if (section) {
      section.questions.forEach((q: any) => {
        if (q.type === 'mcq') {
          autoTotal++; const a = this.answers[q.id]; if (a === q.correctOptionId) autoScore++;
        }
      });
    }
    this.sectionResults[id] = { autoScore, autoTotal, answers: this.answers, textAnswers: this.textAnswers, timestamp: new Date().toISOString() };
    this.sectionCompleted[id] = true;
    this.clearActiveTimer();
    this.quizSubmitted = true;
    this.quizAutoScore = autoScore;
    // persist entire quiz result (including per-section map)
    this.saveQuizResult(this.selectedQuiz?.id);
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
        sections: this.sectionResults,
        timestamp: new Date().toISOString()
      };
      localStorage.setItem(this.quizStorageKey, JSON.stringify(map));
    } catch (e) { console.warn('Failed to save quiz result', e); }
  }
}
