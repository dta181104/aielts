# IELTS Course Management API Documentation

## Overview
API CRUD đầy đủ cho hệ thống quản lý khóa học IELTS, bao gồm Categories, Courses, Sections, Lessons và Attachments.

## API Endpoints

### 1. Course Categories (Danh mục kỹ năng)

**Base URL:** `/identity/course-categories`

#### Create Category
- **POST** `/identity/course-categories`
- **Body:**
```json
{
  "code": "LISTENING",
  "name": "IELTS Listening",
  "description": "Kỹ năng Nghe"
}
```

#### Get All Categories
- **GET** `/identity/course-categories`
- **Response:** List of categories

#### Get Category by ID
- **GET** `/identity/course-categories/{id}`

#### Get Category by Code
- **GET** `/identity/course-categories/code/{code}`

#### Update Category
- **PUT** `/identity/course-categories/{id}`
- **Body:** Same as Create

#### Delete Category
- **DELETE** `/identity/course-categories/{id}`

---

### 2. Courses (Khóa học)

**Base URL:** `/identity/courses`

#### Create Course
- **POST** `/identity/courses`
- **Body:**
```json
{
  "title": "IELTS Foundation",
  "levelName": "Band 0 - 5.0",
  "targetBand": 5.0,
  "price": 1500000,
  "thumbnail": "https://example.com/thumb.jpg",
  "description": "Khóa học dành cho người mất gốc",
  "courseType": "FULL",
  "status": "PUBLISHED"
}
```

**Course Types:** `FULL`, `SINGLE`, `TIPS`  
**Course Status:** `DRAFT`, `PUBLISHED`, `ARCHIVED`

#### Get All Courses (Paginated)
- **GET** `/identity/courses?page=0&size=10&sortBy=id&direction=DESC`

#### Get Course by ID
- **GET** `/identity/courses/{id}`

#### Get Courses by Status
- **GET** `/identity/courses/status/{status}?page=0&size=10`
- **Example:** `/identity/courses/status/PUBLISHED`

#### Get Courses by Type
- **GET** `/identity/courses/type/{type}?page=0&size=10`
- **Example:** `/identity/courses/type/FULL`

#### Search Courses
- **GET** `/identity/courses/search?status=PUBLISHED&type=FULL&keyword=foundation&page=0&size=10`
- **Query Params:** All optional
  - `status`: DRAFT, PUBLISHED, ARCHIVED
  - `type`: FULL, SINGLE, TIPS
  - `keyword`: Search in title and description

#### Update Course
- **PUT** `/identity/courses/{id}`
- **Body:** Same as Create

#### Delete Course (Soft Delete)
- **DELETE** `/identity/courses/{id}`

---

### 3. Sections (Chương học)

**Base URL:** `/identity/sections`

#### Create Section
- **POST** `/identity/sections`
- **Body:**
```json
{
  "courseId": 1,
  "categoryId": 1,
  "title": "Chapter 1: Listening Basics",
  "orderIndex": 1
}
```

#### Get Sections by Course ID
- **GET** `/identity/sections/course/{courseId}`

#### Get Section by ID
- **GET** `/identity/sections/{id}`

#### Get Sections by Category ID
- **GET** `/identity/sections/category/{categoryId}`

#### Update Section
- **PUT** `/identity/sections/{id}`
- **Body:** Same as Create

#### Delete Section (Soft Delete)
- **DELETE** `/identity/sections/{id}`

---

### 4. Lessons (Bài học)

**Base URL:** `/identity/lessons`

#### Create Lesson
- **POST** `/identity/lessons`
- **Body:**
```json
{
  "sectionId": 1,
  "title": "Bài 1.1: Bảng phiên âm IPA",
  "type": "VIDEO",
  "videoUrl": "https://video-link.com/ipa.mp4",
  "content": "<p>Hôm nay học về nguyên âm...</p>",
  "duration": 600,
  "orderIndex": 1
}
```

**Lesson Types:** `VIDEO`, `DOCUMENT`

#### Get Lessons by Section ID
- **GET** `/identity/lessons/section/{sectionId}`

#### Get Lesson by ID
- **GET** `/identity/lessons/{id}`

#### Get Lessons by Section ID and Type
- **GET** `/identity/lessons/section/{sectionId}/type/{type}`
- **Example:** `/identity/lessons/section/1/type/VIDEO`

#### Update Lesson
- **PUT** `/identity/lessons/{id}`
- **Body:** Same as Create

#### Delete Lesson (Soft Delete)
- **DELETE** `/identity/lessons/{id}`

---

### 5. Attachments (Tài liệu đính kèm)

**Base URL:** `/identity/attachments`

#### Create Attachment
- **POST** `/identity/attachments`
- **Body:**
```json
{
  "lessonId": 2,
  "name": "Tu_vung_Family_Band_5.0.pdf",
  "url": "https://s3-bucket.com/files/vocab_family.pdf",
  "fileType": "PDF"
}
```

#### Get Attachments by Lesson ID
- **GET** `/identity/attachments/lesson/{lessonId}`

#### Get Attachment by ID
- **GET** `/identity/attachments/{id}`

#### Update Attachment
- **PUT** `/identity/attachments/{id}`
- **Body:** Same as Create

#### Delete Attachment (Hard Delete)
- **DELETE** `/identity/attachments/{id}`

---

## 6. Quizzes (Bài kiểm tra)

**Base URL:** `/identity/quizzes`

### Create Quiz (có thể kèm câu hỏi lồng nhau)
- **POST** `/identity/quizzes`
- **Body:** (server chấp nhận mảng `questions` lồng nhau)
```json
{
  "title": "Quiz 1: Listening Basics",
  "lessonId": 12,
  "timeLimitSeconds": 600,
  "shuffleQuestions": true,
  "questions": [
    {
      "type": "MULTIPLE_CHOICE",
      "text": "What does IPA stand for?",
      "options": ["International Phonetic Alphabet", "International Pronunciation Association"],
      "correctOptionIndex": 0,
      "score": 1
    },
    {
      "type": "SHORT_ANSWER",
      "text": "Write one example of a vowel sound.",
      "score": 1
    }
  ]
}
```
- **Validation:** `title` required; `lessonId` required and must point to existing `lesson` of type `QUIZ` (nếu không, trả 400).
- **Response:** 201 Created, trả về `QuizDTO` bao gồm `questions` với `id` của từng câu hỏi.

### Get Quiz by ID
- **GET** `/identity/quizzes/{id}`
- **Response:** `QuizDTO` với danh sách `questions` (đã được serialize)

### Get Quizzes by Lesson
- **GET** `/identity/quizzes/lesson/{lessonId}`

### Update Quiz (thay thế toàn bộ hoặc cập nhật metadata và câu hỏi)
- **PUT** `/identity/quizzes/{id}`
- **Body:** tương tự Create; server hỗ trợ cập nhật toàn bộ danh sách câu hỏi (thêm/sửa/xóa động theo `id`)

### Delete Quiz (Hard Delete)
- **DELETE** `/identity/quizzes/{id}`

---

## 7. Questions (Câu hỏi)

**Base URL:** `/identity/questions` (và nested endpoints dưới quizzes)

> Thông thường quản lý câu hỏi qua `/identity/quizzes/{quizId}/questions`, nhưng cũng cung cấp endpoint riêng để thao tác trên 1 câu hỏi.

### Create Single Question (gắn vào quiz)
- **POST** `/identity/quizzes/{quizId}/questions`
- **Body:**
```json
{
  "type": "MULTIPLE_CHOICE",
  "text": "Choose the correct IPA symbol for /i:/",
  "options": ["i", "ɪ", "iː"],
  "correctOptionIndex": 2,
  "score": 1
}
```
- **Validation:**
  - `type` required
  - `text` required
  - `options` required và `size>=2` nếu `MULTIPLE_CHOICE`
  - `correctOptionIndex` phải trong khoảng `0..options.size-1` nếu `MULTIPLE_CHOICE`
  - `score` >= 0 (mặc định 1 nếu không cung cấp)
- **Response:** 201 Created, trả về `QuestionDTO` với `id`.

### Update Question
- **PUT** `/identity/questions/{id}`
- **Body:** giống Create Single Question

### Delete Question
- **DELETE** `/identity/questions/{id}`

### Get Question
- **GET** `/identity/questions/{id}`

---

## 8. Enrollments (Khóa học đã đăng ký)

**Endpoint:** GET `/identity/users/{userId}/courses`

Description:
- Trả về danh sách các khóa học mà user đã đăng ký (bảng `enrollment`). Mỗi item gồm thông tin tóm tắt để hiển thị trong trang dashboard học viên.

Path parameters:
- `userId` (long) — id của user cần lấy danh sách khóa học đã đăng ký.

Response (200):
- JSON wrapper `ApiResponse` với `result` là mảng các đối tượng:
  - `id` (long) — course id
  - `title` (string)
  - `thumbnail` (string) — ảnh đại diện
  - `progressPercent` (int)
  - `enrolledDate` (string, ISO datetime)
  - `status` (string) — trạng thái đăng ký

Example request:
- GET `/identity/users/1/courses`

Example response:
```json
{
  "code": 1000,
  "message": "OK",
  "result": [
    {
      "id": 1,
      "title": "IELTS Foundation (Mất gốc)",
      "thumbnail": "https://example.com/thumb1.jpg",
      "progressPercent": 10,
      "enrolledDate": "2025-11-26T10:00:00",
      "status": "ACTIVE"
    }
  ]
}
```

Notes:
- Implementation uses a JPA projection to avoid loading full entities.
- If you require authenticated user's courses instead of passing `userId`, consider wiring the controller to use current principal and expose `/identity/me/courses`.

---

## Validation rules (Tóm tắt)
- Quiz:
  - `title`: `@NotBlank`
  - `lessonId`: `@NotNull`, phải tham chiếu đến `lesson` tồn tại và `lesson.type == "QUIZ"` nếu áp dụng ràng buộc bài thi
- Question:
  - `type`: `@NotNull`
  - `text`: `@NotBlank`
  - `options`: `@Size(min=2)` nếu `type == MULTIPLE_CHOICE`
  - `correctOptionIndex`: phải được cung cấp và nằm trong khoảng hợp lệ nếu MULTIPLE_CHOICE
  - `score`: `@Min(0)` (mặc định 1)

**Mã lỗi (gợi ý)**:
- `400_QUIZ_TITLE_REQUIRED`
- `400_QUIZ_LESSON_INVALID`
- `400_QUESTION_TEXT_REQUIRED`
- `400_QUESTION_OPTIONS_INVALID`
- `400_QUESTION_CORRECT_INDEX_INVALID`

---

## Serialization / Deserialization (DTOs)
- QuizDTO:
  - id: Long
  - title: String
  - lessonId: Long
  - timeLimitSeconds: Integer
  - shuffleQuestions: Boolean
  - List<QuestionDTO> questions

- QuestionDTO:
  - id: Long
  - type: String
  - text: String
  - List<String> options
  - Integer correctOptionIndex
  - Double score

Ghi chú:
- Server chấp nhận `questions` lồng nhau khi tạo/cập nhật quiz. Service sẽ validate từng câu hỏi;
- Khi tạo quiz kèm `questions`, thao tác phải là atomic: nếu 1 câu hỏi không hợp lệ -> rollback toàn bộ create quiz;
- Hỗ trợ thêm/sửa/xóa câu hỏi khi update quiz dựa trên `id` của question (nếu question không có `id` thì là thêm mới).
- Timestamps theo ISO 8601. Các response tuân theo convention hiện tại của dự án (200/201/204 cho thành công, 4xx cho lỗi validation kèm mã lỗi JSON).

---

## Example flows (nhanh)
1) Tạo quiz kèm câu hỏi:
- POST `/identity/quizzes` (body như trên) -> trả về QuizDTO với questions[].id

2) Thêm 1 câu hỏi sau:
- POST `/identity/quizzes/{quizId}/questions` -> trả về QuestionDTO

3) Lấy quiz:
- GET `/identity/quizzes/{id}` -> QuizDTO với mảng questions

4) Cập nhật 1 câu hỏi:
- PUT `/identity/questions/{id}` -> trả về QuestionDTO cập nhật

---

## 9. Quiz Submissions (Làm bài kiểm tra)

**Base URL:** `/identity` (project sử dụng context-path `/identity`)

Mục đích: Các endpoint phục vụ việc học viên làm bài kiểm tra (tạo submission, lưu câu trả lời từng câu, nộp bài, lấy chi tiết và cho phép giáo viên chấm).

Lưu ý: server trả về wrapper `ApiResponse` chung của dự án:
{
  "code": 1000,
  "message": "OK",
  "result": { ... }
}

Các endpoint chi tiết (kèm request body + sample response):

1) Start a submission (bắt đầu làm bài)
- Method: POST
- URL: `/identity/quizzes/{quizId}/submissions?userId={userId}`
- Mô tả: Tạo record `quiz_submission` với trạng thái `DOING` và `start_time` = now. Trong production, nên lấy user từ JWT thay vì truyền `userId`.

Request params (query):
- userId (Long) — bắt buộc khi testing.

Response 200 (ApiResponse<QuizSubmissionResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 10,
    "userId": 1,
    "quizId": 1,
    "startTime": "2025-11-27T08:00:00",
    "status": "DOING",
    "score": 0.0
  }
}

2) Add or update an answer (thêm / cập nhật đáp án cho 1 câu)
- Method: POST
- URL: `/identity/quizzes/submissions/{submissionId}/answers`
- Description: Lưu hoặc cập nhật `submission_answer` cho câu hỏi cụ thể. Hỗ trợ MCQ (selectedOption), writing (textAnswer) và speaking (audioUrl hoặc audioFile via multipart endpoint).

Request body (JSON):
{
  "questionId": 5,
  "selectedOption": "A",    // cho MCQ
  "textAnswer": "My essay answer", // cho writing
  "audioUrl": "https://..." // nếu đã upload audio ở client
}

Response 200 (ApiResponse<SubmissionAnswerResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 21,
    "questionId": 5,
    "selectedOption": "A",
    "isCorrect": true,
    "textAnswer": null,
    "audioUrl": null,
    "gradeScore": null,
    "teacherNote": null
  }
}

3) Submit audio answer (nộp phần speaking — multipart)
- Method: POST
- URL: `/identity/quizzes/submissions/{submissionId}/audio-answer`
- Consumes: multipart/form-data
- Params: query or form fields as specified below

Form fields (multipart):
- audioFile (file) — file audio (MP3/WAV/etc)
- questionId (long) — id câu hỏi trong quiz

Example curl (multipart):
```bash
curl -X POST "http://localhost:8080/identity/quizzes/submissions/10/audio-answer?questionId=5" \
  -H "Authorization: Bearer <token>" \
  -F "audioFile=@/path/to/answer.mp3"
```

Server behavior:
- Server nhận `audioFile`, upload lên Cloudinary (hoặc storage cấu hình), lưu `audioUrl` vào `submission_answer` tương ứng. Nếu chưa có `submission_answer` cho question đó, sẽ tạo mới.

Response 200 (ApiResponse<SubmissionAnswerResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 33,
    "questionId": 5,
    "selectedOption": null,
    "isCorrect": null,
    "textAnswer": null,
    "audioUrl": "https://res.cloudinary.com/.../AIELTS/submission/question_5/question_5.mp3_submission_10",
    "gradeScore": null,
    "teacherNote": null
  }
}

4) Submit (nộp bài, hoàn tất và auto-grade cho MCQ)
- Method: PUT
- URL: `/identity/quizzes/submissions/{submissionId}/submit`

Behavior:
- Đặt `submit_time`, `status = SUBMITTED`.
- Auto-grade các câu MCQ (các question có `correctOption` không null). Công thức: score = (số MCQ đúng / tổng MCQ có trong bài) * 100.

Response 200 (ApiResponse<QuizSubmissionResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 10,
    "userId": 1,
    "quizId": 1,
    "startTime": "2025-11-27T08:00:00",
    "submitTime": "2025-11-27T08:15:00",
    "status": "SUBMITTED",
    "score": 75.0,
    "answers": [
      {
        "id": 21,
        "questionId": 1,
        "selectedOption": "A",
        "isCorrect": true,
        "textAnswer": null,
        "audioUrl": null,
        "gradeScore": null
      },
      {
        "id": 22,
        "questionId": 5,
        "selectedOption": null,
        "isCorrect": null,
        "textAnswer": "My essay...",
        "audioUrl": null,
        "gradeScore": null
      }
    ]
  }
}

5) Get submission details
- Method: GET
- URL: `/identity/quizzes/submissions/{submissionId}`

Response 200 (ApiResponse<QuizSubmissionResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 10,
    "userId": 1,
    "quizId": 1,
    "startTime": "2025-11-27T08:00:00",
    "submitTime": "2025-11-27T08:15:00",
    "status": "SUBMITTED",
    "score": 75.0,
    "teacherFeedback": null,
    "answers": [ /* array of SubmissionAnswerResponse */ ]
  }
}

6) (Optional) List submissions by quiz (admin)
- Method: GET
- URL: `/identity/quizzes/{quizId}/submissions`

Response 200 (ApiResponse<List<QuizSubmissionResponse>>)

7) (Optional) List submissions by user
- Method: GET
- URL: `/identity/users/{userId}/submissions`

Response 200 (ApiResponse<List<QuizSubmissionResponse>>)

8) (Teacher) Grade an answer / finalize grading for an answer
- Method: PUT
- URL: `/identity/submissions/{submissionId}/answers/{answerId}/grade`
- Body (JSON):
{
  "gradeScore": 8.5,
  "teacherNote": "Good structure"
}

Response 200 (ApiResponse<SubmissionAnswerResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 22,
    "questionId": 5,
    "gradeScore": 8.5,
    "teacherNote": "Good structure"
  }
}

9) (Teacher) Finalize grading for a submission
- Method: PUT
- URL: `/identity/submissions/{submissionId}/grade`
- Body (JSON):
{
  "teacherFeedback": "Overall good",
  "status": "GRADED"
}

Response 200 (ApiResponse<QuizSubmissionResponse>):
{
  "code": 1000,
  "message": "OK",
  "result": {
    "id": 10,
    "status": "GRADED",
    "teacherFeedback": "Overall good"
  }
}

---

### DTO / Response shapes (chi tiết)

- SubmissionAnswerRequest
  - questionId: Long (required)
  - selectedOption: String (ví dụ "A")
  - textAnswer: String
  - audioUrl: String
  - audioFile: MultipartFile (cho endpoint multipart)

- SubmissionAnswerResponse
  - id: Long
  - questionId: Long
  - selectedOption: String
  - isCorrect: Boolean
  - textAnswer: String
  - audioUrl: String
  - gradeScore: Double
  - teacherNote: String

- QuizSubmissionResponse
  - id: Long
  - userId: Long
  - quizId: Long
  - startTime: ISO datetime
  - submitTime: ISO datetime
  - score: Double (percentage)
  - status: String (DOING | SUBMITTED | GRADED)
  - teacherFeedback: String
  - answers: List<SubmissionAnswerResponse>

---

### Validation rules (tóm tắt)
- `questionId` required khi thêm/cập nhật câu trả lời.
- `selectedOption` phải là 1 trong các key option nếu question là MCQ.
- Khi submit, nếu không có câu MCQ nào thì điểm auto = 0.0.

### Security notes
- Tránh truyền `userId` từ client; nên lấy từ token JWT.
- Endpoint upload audio là multipart; đảm bảo cấu hình `MultipartResolver` và giới hạn kích thước file hợp lý.

---

## 10. Lịch sử làm bài (Quiz Submissions)

**Base URL:** `/identity/quizzes`

Mục đích: Các endpoint phục vụ việc lấy danh sách và chi tiết các lần làm bài kiểm tra của học viên.

6.a) Get all submissions of a user for a specific quiz (multiple attempts)
- Method: GET
- URL: `/identity/quizzes/{quizId}/users/{userId}/submissions`
- Description: Trả về danh sách tất cả `QuizSubmission` mà `userId` đã thực hiện cho `quizId`. Dùng khi hệ thống cho phép học viên làm quiz nhiều lần.

Path parameters:
- quizId (Long) — id của quiz
- userId (Long) — id của user

Behavior:
- Trả về HTTP 200 với `ApiResponse<List<QuizSubmissionResponse>>`.
- Nếu không có submission nào: trả về 200 với `result: []` (mảng rỗng).
- Considerations: Có thể cần paging nếu số lượng submissions lớn; hiện API trả tất cả.

Example request (curl):
```bash
curl -v -X GET "http://localhost:8080/identity/quizzes/2/users/1/submissions" \
  -H "Authorization: Bearer <token>"
```

Example successful response (200):
{
  "code": 1000,
  "message": "OK",
  "result": [
    {
      "id": 34,
      "userId": 1,
      "quizId": 2,
      "startTime": "2025-12-11T08:00:00",
      "submitTime": "2025-12-11T08:15:00",
      "status": "SUBMITTED",
      "score": 80.0,
      "teacherFeedback": null,
      "answers": [ /* ... */ ]
    },
    {
      "id": 40,
      "userId": 1,
      "quizId": 2,
      "startTime": "2025-12-05T09:00:00",
      "submitTime": "2025-12-05T09:12:00",
      "status": "SUBMITTED",
      "score": 72.5,
      "teacherFeedback": null,
      "answers": [ /* ... */ ]
    }
  ]
}

Notes / Next steps:
- Nếu bạn muốn 404 thay vì mảng rỗng khi không có submission, tôi có thể thay đổi behavior.
- Nếu cần, tôi sẽ thêm paging (page, size) và bảo mật (chỉ owner hoặc admin/teacher xem được).
