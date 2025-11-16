import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { EnrollmentService } from '../../services/enrollment.service';
import { ProductItems } from '../types/productItem';
import { ButtonComponent } from '../shared/button/button.component';

@Component({
  selector: 'app-quiz-list',
  standalone: true,
  imports: [CommonModule, FormsModule, ButtonComponent],
  templateUrl: './quiz-list.component.html',
  styleUrls: ['./quiz-list.component.css']
})
export class QuizListComponent implements OnInit {
  courseId: number | null = null;
  course: ProductItems | null = null;
  quizzes: any[] = [];

  constructor(private route: ActivatedRoute, public router: Router, private enrollment: EnrollmentService) {}

  ngOnInit(): void {
    const idStr = this.route.snapshot.paramMap.get('id');
    const id = idStr ? parseInt(idStr, 10) : null;
    this.courseId = id;
    if (!id) {
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
  }

  goBack() {
    if (this.courseId) {
      // navigate back to course player (learn) for the current course
      this.router.navigate(['/learn', String(this.courseId)]);
    } else {
      this.router.navigate(['/my-courses']);
    }
  }

  buildMockQuizSections(course: ProductItems) {
    const reading = {
      id: 'reading',
      title: 'Reading',
      questions: [1,2,3,4].map(n => ({ id: `R${n}`, type: 'mcq', prompt: `Reading passage - question ${n}`, options: [
        {id:1,text:'A'},{id:2,text:'B'},{id:3,text:'C'},{id:4,text:'D'}], correctOptionId: (n%4)+1 }))
    };
    const listening = {
      id: 'listening',
      title: 'Listening',
      questions: [1,2,3].map(n => ({ id: `L${n}`, type: 'mcq', prompt: `Listening clip - question ${n}`, options: [
        {id:1,text:'A'},{id:2,text:'B'},{id:3,text:'C'},{id:4,text:'D'}], correctOptionId: ((n+1)%4)+1 }))
    };
    const writing = { id: 'writing', title: 'Writing', questions: [1,2].map(n => ({ id:`W${n}`, type:'text', prompt: n===1? 'Task1' : 'Task2' })) };
    const speaking = { id: 'speaking', title: 'Speaking', questions: [1].map(n => ({ id:`S${n}`, type:'text', prompt: 'Speaking task' })) };
    return [reading, listening, writing, speaking];
  }

  buildMockQuizzes(course: ProductItems) {
    const count = 3;
    const out:any[] = [];
    for (let i=1;i<=count;i++) out.push({ id:`Q${i}`, title:`Bài kiểm tra ${i}`, sections: this.buildMockQuizSections(course) });
    return out;
  }

  openQuiz(quiz:any) {
    if (!this.courseId) return;
    this.router.navigate(['/course', String(this.courseId), 'quizzes', quiz.id]);
  }
}
