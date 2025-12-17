import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { finalize, take } from 'rxjs';
import { marked } from 'marked';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { ButtonComponent } from '../shared/button/button.component';
import { AdminService, Course } from '../../services/admin.service';

interface CourseItem {
  id: number;
  title: string;
  shortDesc: string;
  image?: string;
  price: number;
}

@Component({
  selector: 'app-courses',
  standalone: true,
  imports: [CommonModule, DecimalPipe, ButtonComponent],
  
  templateUrl: './courses.component.html',
  styleUrls: ['./courses.component.css'],
})
export class CoursesComponent implements OnInit {
  courses: CourseItem[] = [];
  loading = false;
  errorMessage?: string;
  markdownSample = `
# Markdown test\n## Heading level 2
## Heading level 2 \n## Heading level 3

- Item 1
- Item 2
- **Bold text**
- *Italic text*

\`\`\`ts
console.log('Hello Markdown');
\`\`\`
`;
  sample = "**Nhận xét chung**: Bài làm của bạn rất ấn tượng với tư duy mạch lạc và cách trình bày sáng tạo. Tôi thực sự đánh giá cao nỗ lực của bạn trong việc hoàn thành bài tập này. Hãy tiếp tục phát huy tinh thần học hỏi tuyệt vời này nhé!\n\n**Điểm mạnh**:\n- Bố cục bài viết rất chặt chẽ, dễ theo dõi.\n- Bạn đã vận dụng tốt các cấu trúc câu phức để diễn đạt ý tưởng.\n- Lập luận có chiều sâu và dẫn chứng thuyết phục.\n\n**Điểm cần cải thiện**:\n- **Lỗi ngữ pháp**: Trong câu \"The data show that...\", lưu ý nếu \"data\" được dùng như danh từ không đếm được trong ngữ cảnh này, động từ nên chia là \"shows\".\n- **Lỗi chính tả**: Từ \"environment\" bị viết thiếu chữ \"n\" ở đoạn 2.\n- **Cách dùng từ**: Cố gắng tránh lặp lại từ \"good\" quá nhiều, thay vào đó hãy dùng các tính từ miêu tả cụ thể hơn.\n\n**Gợi ý từ vựng nâng cao**:\n1. **Mitigate** (v): Giảm thiểu (thường dùng cho hậu quả, rủi ro).\n2. **Detrimental** (adj): Có hại, gây bất lợi (tương đương với harmful).\n3. **Imperative** (adj): Cấp bách, tối quan trọng.\n4. **Substantial** (adj): Đáng kể, to lớn.";

  markdownHtml?: SafeHtml;
  markdownHtml1?: SafeHtml;
  constructor(private router: Router, private adminService: AdminService, private sanitizer: DomSanitizer) {}

  ngOnInit(): void {
    this.loadCourses();
    const html = marked(this.markdownSample) as string;
    this.markdownHtml = this.sanitizer.bypassSecurityTrustHtml(html);
    const html1 = marked(this.sample) as string;
    this.markdownHtml1 = this.sanitizer.bypassSecurityTrustHtml(html1);
  }

  goDetail(id: number) {
    this.router.navigate(['/course', id]);
  }

  private loadCourses() {
    this.loading = true;
    this.errorMessage = undefined;
    this.adminService
      .fetchCourses({ status: 'PUBLISHED', page: 0, size: 12, sortBy: 'createdDate', direction: 'DESC' })
      .pipe(
        take(1),
        finalize(() => (this.loading = false))
      )
      .subscribe({
        next: (page) => {
          this.courses = page.content.map((course) => this.mapCourseItem(course));
        },
        error: (error: unknown) => {
          console.error('Failed to load public courses', error);
          this.courses = [];
          this.errorMessage = 'Không thể tải danh sách khóa học. Vui lòng thử lại sau.';
        },
      });
  }

  private mapCourseItem(course: Course): CourseItem {
    return {
      id: Number(course.id),
      title: course.title ?? 'Khóa học IELTS',
      shortDesc: course.description?.trim() || 'Nội dung đang được cập nhật.',
      image: course.thumbnail || course.imageUrl,
      price: course.price ?? 0,
    };
  }
}
