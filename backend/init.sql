CREATE DATABASE aielts;
USE aielts;

create table user
(
    ID           bigint auto_increment
        primary key,
    CODE         varchar(255)                 null,
    SEX          bit                          null,
    ADDRESS      varchar(255)                 null,
    PHONE        varchar(255)                 null,
    EMAIL        varchar(255)                 null,
    STATUS       varchar(255)                 null,
    DATE         date                         null,
    USERNAME     varchar(255)                 null,
    PASSWORD     varchar(255)                 null,
    CREATED_DATE datetime(6)                  null,
    CREATED_BY   varchar(255)                 null,
    UPDATED_DATE datetime(6)                  null,
    UPDATED_BY   varchar(255)                 null,
    DELETED      bit                          null,
    NAME         varchar(255) charset utf8mb3 null,
    AVATAR       varchar(550)                 null
);

create table invalidatedtoken
(
    ID         varchar(500) not null
        primary key,
    EXPIRYTIME date         null
);

CREATE TABLE password_reset_tokens
(
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(550) NULL,
    expiry_date DATETIME NULL,
    user_id BIGINT NULL
);

create table role
(
    ID           bigint auto_increment
        primary key,
    CODE         varchar(255)                 null,
    NAME         varchar(255) charset utf8mb3 null,
    STATUS       varchar(255)                 null,
    CREATED_DATE datetime(6)                  null,
    CREATED_BY   varchar(255)                 null,
    UPDATED_DATE datetime(6)                  null,
    UPDATED_BY   varchar(255)                 null,
    DELETED      bit                          null
);

create table permission
(
    ID           bigint auto_increment
        primary key,
    CODE         varchar(255)                 null,
    NAME         varchar(255) charset utf8mb3 null,
    STATUS       varchar(255)                 null,
    CREATED_DATE datetime(6)                  null,
    CREATED_BY   varchar(255)                 null,
    UPDATED_DATE datetime(6)                  null,
    UPDATED_BY   varchar(255)                 null,
    DELETED      bit                          null
);

create table user_roles
(
    user_id  bigint not null,
    roles_id bigint not null,
    primary key (user_id, roles_id),
    constraint FK55itppkw3i07do3h7qoclqd4k
        foreign key (user_id) references user (ID),
    constraint FKj9553ass9uctjrmh0gkqsmv0d
        foreign key (roles_id) references role (ID)
);

create table role_permissions
(
    role_id        bigint not null,
    permissions_id bigint not null,
    primary key (role_id, permissions_id),
    constraint FKclluu29apreb6osx6ogt4qe16
        foreign key (permissions_id) references permission (ID),
    constraint FKlodb7xh4a2xjv39gc3lsop95n
        foreign key (role_id) references role (ID)
);

create table cart
(
    id           bigint auto_increment
        primary key,
    user_id      bigint       null,
    code         varchar(225) null,
    deleted      bit          null,
    created_date datetime     null,
    updated_date datetime     null,
    updated_by   varchar(225) null,
    created_by   varchar(225) null,
    constraint fk_user_relation
        foreign key (user_id) references user (ID)
            on delete cascade
);

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT
);

CREATE TABLE course (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    level_name VARCHAR(100),
    target_band FLOAT,
    price DECIMAL(10, 2) DEFAULT 0,
    thumbnail VARCHAR(500),
    description TEXT,
    course_type VARCHAR(20) DEFAULT 'FULL', 
    status VARCHAR(20) DEFAULT 'DRAFT',
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BIT(1) DEFAULT 0
);

CREATE TABLE section (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id BIGINT NOT NULL,
    category_id BIGINT, 
    title VARCHAR(255) NOT NULL,
    order_index INT DEFAULT 0,
    deleted BIT(1) DEFAULT 0,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES category(id)
);

CREATE TABLE lesson (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    type VARCHAR(20) NOT NULL,
    video_url VARCHAR(500),
    content TEXT,
    duration INT DEFAULT 0,
    order_index INT DEFAULT 0,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted BIT(1) DEFAULT 0,
    FOREIGN KEY (section_id) REFERENCES section(id) ON DELETE CASCADE
);

CREATE TABLE attachment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT NOT NULL,
    name VARCHAR(255),
    url VARCHAR(500) NOT NULL,
    file_type VARCHAR(20),
    FOREIGN KEY (lesson_id) REFERENCES lesson(id) ON DELETE CASCADE
);

CREATE TABLE enrollment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    progress_percent INT DEFAULT 0,
    enrolled_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    UNIQUE(user_id, course_id)
);

CREATE TABLE lesson_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    enrollment_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    is_completed BIT(1) DEFAULT 0,
    last_watched_second INT DEFAULT 0,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (enrollment_id) REFERENCES enrollment(id) ON DELETE CASCADE,
    FOREIGN KEY (lesson_id) REFERENCES lesson(id)
);

CREATE TABLE quiz (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lesson_id BIGINT,
    title VARCHAR(255) NOT NULL,
    pass_score INT DEFAULT 50,
    duration INT,
    FOREIGN KEY (lesson_id) REFERENCES lesson(id) ON DELETE CASCADE
);

CREATE TABLE question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    audio_url VARCHAR(500) DEFAULT NULL, -- dành cho câu listening  
    options JSON, 
    correct_option CHAR(1), 
	explanation TEXT,
	skill VARCHAR(20),
    deleted BIT(1) DEFAULT 0,
    FOREIGN KEY (quiz_id) REFERENCES quiz(id) ON DELETE CASCADE
);

CREATE TABLE quiz_submission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL, -- User nào làm
    quiz_id BIGINT NOT NULL, -- Bài quiz nào
    
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Thời gian bắt đầu làm
    submit_time TIMESTAMP, -- Thời gian nộp bài
    
    score DECIMAL(5, 2) DEFAULT 0, -- Tổng điểm (VD: 8.5)
    
    -- Trạng thái quan trọng cho IELTS: 
    -- 'DOING' (Đang làm), 'SUBMITTED' (Đã nộp - chờ chấm), 'GRADED' (Đã chấm xong)
    status VARCHAR(20) DEFAULT 'DOING', 
    
    teacher_feedback TEXT, -- Lời phê chung của giáo viên (cho Writing/Speaking)
    
    FOREIGN KEY (quiz_id) REFERENCES quiz(id),
    -- FOREIGN KEY (user_id) REFERENCES user(id) -- Bỏ comment nếu đã có bảng user
    INDEX (user_id, quiz_id) -- Index để tìm lịch sử thi nhanh hơn
);

CREATE TABLE submission_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL, -- Thuộc về bài làm nào
    question_id BIGINT NOT NULL,   -- Trả lời cho câu hỏi nào
    
    -- TRƯỜNG HỢP 1: Reading / Listening (Trắc nghiệm)
    selected_option VARCHAR(10), -- User chọn 'A', 'B'... (Lưu ý: Để VARCHAR lỡ user chọn nhiều đáp án)
    is_correct BIT(1),           -- True/False (Hệ thống tự chấm và lưu vào đây)
    
    -- TRƯỜNG HỢP 2: Writing (Tự luận)
    text_answer TEXT,            -- Bài Essay user viết
    
    -- TRƯỜNG HỢP 3: Speaking (Ghi âm)
    audio_url VARCHAR(500),      -- Link file ghi âm user nói
    
    -- Phần chấm điểm riêng cho từng câu (Dành cho chấm Writing/Speaking)
    grade_score DECIMAL(5, 2) DEFAULT 0, -- Điểm cho riêng câu này
    teacher_note TEXT,                   -- Nhận xét chi tiết (VD: "Sai ngữ pháp câu đầu")
    
    FOREIGN KEY (submission_id) REFERENCES quiz_submission(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES question(id)
);

INSERT INTO user
VALUES (
    1, 
    'ACC1', 
    NULL, 
    NULL, 
    '0396578942', 
    'dta181104@gmail.com', 
    NULL, 
    NULL, 
    'admin', 
    '$2a$10$5YGRs.1wZmDdonhzirmRL.iq5bEeUcWyuTBrd.5zlfu6A7ofPowla',
    NULL, 
    NULL, 
    NULL, 
    NULL, 
    0, 
    'Đặng Tuấn Anh',
    NULL
);

INSERT INTO permission (code, name, status, created_date, created_by, DELETED)
VALUES
('USER_VIEW', 'Xem danh sách người dùng', 'ACTIVE', NOW(), 'system', 0),
('USER_CREATE', 'Thêm người dùng', 'ACTIVE', NOW(), 'system', 0),
('USER_UPDATE', 'Cập nhật thông tin người dùng', 'ACTIVE', NOW(), 'system', 0),
('USER_DELETE', 'Xóa người dùng', 'ACTIVE', NOW(), 'system', 0),
('ROLE_VIEW', 'Xem danh sách vai trò', 'ACTIVE', NOW(), 'system', 0),
('ROLE_MANAGE', 'Quản lý quyền & vai trò', 'ACTIVE', NOW(), 'system', 0),
('PERMISSION_VIEW', 'Xem danh sách quyền', 'ACTIVE', NOW(), 'system', 0),
('COURSE_VIEW', 'Xem danh sách khóa học', 'ACTIVE', NOW(), 'system', 0),
('COURSE_CREATE', 'Tạo khóa học mới', 'ACTIVE', NOW(), 'system', 0),
('COURSE_UPDATE', 'Chỉnh sửa khóa học', 'ACTIVE', NOW(), 'system', 0),
('COURSE_DELETE', 'Xóa khóa học', 'ACTIVE', NOW(), 'system', 0),
('LESSON_VIEW', 'Xem bài học', 'ACTIVE', NOW(), 'system', 0),
('LESSON_CREATE', 'Tạo bài học mới', 'ACTIVE', NOW(), 'system', 0),
('LESSON_UPDATE', 'Chỉnh sửa bài học', 'ACTIVE', NOW(), 'system', 0),
('LESSON_DELETE', 'Xóa bài học', 'ACTIVE', NOW(), 'system', 0),
('TEST_VIEW', 'Xem bài kiểm tra', 'ACTIVE', NOW(), 'system', 0),
('TEST_CREATE', 'Tạo bài kiểm tra mới', 'ACTIVE', NOW(), 'system', 0),
('TEST_UPDATE', 'Chỉnh sửa bài kiểm tra', 'ACTIVE', NOW(), 'system', 0),
('TEST_DELETE', 'Xóa bài kiểm tra', 'ACTIVE', NOW(), 'system', 0),
('TEST_TAKE', 'Làm bài kiểm tra', 'ACTIVE', NOW(), 'system', 0),
('TEST_VIEW_RESULT', 'Xem kết quả bài làm', 'ACTIVE', NOW(), 'system', 0),
('COURSE_ENROLL', 'Đăng ký khóa học', 'ACTIVE', NOW(), 'system', 0),
('PROFILE_VIEW', 'Xem thông tin cá nhân', 'ACTIVE', NOW(), 'system', 0),
('PROFILE_UPDATE', 'Chỉnh sửa hồ sơ cá nhân', 'ACTIVE', NOW(), 'system', 0),
('COMMENT_CREATE', 'Bình luận trong bài học', 'ACTIVE', NOW(), 'system', 0),
('COMMENT_DELETE_SELF', 'Xóa bình luận của mình', 'ACTIVE', NOW(), 'system', 0),
('STATISTIC_VIEW', 'Xem thống kê học viên', 'ACTIVE', NOW(), 'system', 0),
('SYSTEM_LOG_VIEW', 'Xem log hệ thống', 'ACTIVE', NOW(), 'system', 0);

INSERT INTO role
VALUES
(1, 'ADMIN', 'QUAN TRI', 'ACTIVE', NULL, NULL, NULL, NULL, _binary '\0'), 
(2, 'USER', 'NGUOI DUNG', 'ACTIVE', NULL, NULL, NULL, NULL, _binary '\0');

INSERT INTO user_roles
VALUES (1, 1), (1, 2);

INSERT INTO role_permissions (role_id, permissions_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON 1=1 -- Kỹ thuật Cross Join để ghép Admin với từng quyền
WHERE r.code = 'ADMIN' 
AND p.code IN (
    -- Nhóm User
    'USER_VIEW', 'USER_CREATE', 'USER_UPDATE', 'USER_DELETE',
    -- Nhóm Role & Permission
    'ROLE_VIEW', 'ROLE_MANAGE', 'PERMISSION_VIEW',
    -- Nhóm Course
    'COURSE_VIEW', 'COURSE_CREATE', 'COURSE_UPDATE', 'COURSE_DELETE',
    -- Nhóm Lesson
    'LESSON_VIEW', 'LESSON_CREATE', 'LESSON_UPDATE', 'LESSON_DELETE',
    -- Nhóm Test (Dừng lại ở TEST_DELETE)
    'TEST_VIEW', 'TEST_CREATE', 'TEST_UPDATE', 'TEST_DELETE'
);

INSERT INTO role_permissions (role_id, permissions_id)
SELECT r.id, p.id
FROM role r
JOIN permission p ON p.code IN (
    'COURSE_VIEW', 'LESSON_VIEW',
    'TEST_VIEW', 'TEST_TAKE', 'TEST_VIEW_RESULT',
    'COURSE_ENROLL', 'PROFILE_VIEW', 'PROFILE_UPDATE',
    'COMMENT_CREATE', 'COMMENT_DELETE_SELF'
)
WHERE r.code = 'USER';

INSERT INTO category (id, code, name, description) VALUES 
(1, 'LISTENING', 'IELTS Listening', 'Kỹ năng Nghe'),
(2, 'READING', 'IELTS Reading', 'Kỹ năng Đọc'),
(3, 'WRITING', 'IELTS Writing', 'Kỹ năng Viết'),
(4, 'SPEAKING', 'IELTS Speaking', 'Kỹ năng Nói');


INSERT INTO course (id, title, level_name, target_band, price, course_type, status, thumbnail) VALUES 
(1, 'IELTS Foundation (Mất gốc)', 'Band 0 - 5.0', 5.0, 1500000, 'FULL', 'PUBLISHED', 'https://example.com/thumb1.jpg'),
(2, 'IELTS Intensive (Tăng tốc)', 'Band 5.0 - 7.5', 7.5, 2500000, 'FULL', 'PUBLISHED', 'https://example.com/thumb2.jpg'),
(3, 'IELTS Master (Về đích)', 'Band 7.5+', 9.0, 3500000, 'FULL', 'PUBLISHED', 'https://example.com/thumb3.jpg');

INSERT INTO section (course_id, category_id, title, order_index) VALUES 
(1, 1, 'Listening Chapter 1: Làm quen với ngữ âm', 1),
(1, 1, 'Listening Chapter 2: Nghe số và đánh vần', 2),
(1, 2, 'Reading Chapter 1: Ngữ pháp cơ bản cho bài đọc', 3),
(1, 2, 'Reading Chapter 2: Kỹ năng Skimming & Scanning', 4),
(1, 3, 'Writing Chapter 1: Cấu trúc câu đơn giản', 5),
(1, 3, 'Writing Chapter 2: Viết đoạn văn ngắn', 6),
(1, 4, 'Speaking Chapter 1: Phát âm chuẩn IPA', 7),
(1, 4, 'Speaking Chapter 2: Giới thiệu bản thân (Part 1)', 8);

INSERT INTO section (course_id, category_id, title, order_index) VALUES 
(2, 1, 'Listening Chapter 1: Chiến thuật Section 1 & 2', 1),
(2, 1, 'Listening Chapter 2: Dạng bài Map Labelling', 2),
(2, 2, 'Reading Chapter 1: Dạng bài True/False/Not Given', 3),
(2, 2, 'Reading Chapter 2: Dạng bài Matching Headings', 4),
(2, 3, 'Writing Chapter 1: Phân tích biểu đồ (Task 1)', 5),
(2, 3, 'Writing Chapter 2: Viết bài luận (Task 2 Basics)', 6),
(2, 4, 'Speaking Chapter 1: Trả lời theo công thức (Part 2)', 7),
(2, 4, 'Speaking Chapter 2: Phản xạ chủ đề khó', 8);

INSERT INTO section (course_id, category_id, title, order_index) VALUES 
(3, 1, 'Listening Chapter 1: Bẫy trong Section 3 & 4', 1),
(3, 1, 'Listening Chapter 2: Luyện đề tốc độ cao', 2),
(3, 2, 'Reading Chapter 1: Xử lý bài đọc khoa học phức tạp', 3),
(3, 2, 'Reading Chapter 2: Luyện đề Cambridge Full test', 4),
(3, 3, 'Writing Chapter 1: Nâng cấp từ vựng Band 8.0+', 5),
(3, 3, 'Writing Chapter 2: Tư duy phản biện trong Task 2', 6),
(3, 4, 'Speaking Chapter 1: Idioms & Collocations tự nhiên', 7),
(3, 4, 'Speaking Chapter 2: Mock test 1-1', 8);

INSERT INTO lesson (section_id, title, type, video_url, content, duration, order_index) VALUES 
(1, 'Bài 1.1: Bảng phiên âm IPA là gì?', 'VIDEO', 'https://video-link.com/ipa.mp4', '<p>Hôm nay học về nguyên âm...</p>', 600, 1),
(1, 'Bài 1.2: Tài liệu từ vựng chủ đề Family', 'DOCUMENT', NULL, '<p>Danh sách từ vựng cần nhớ...</p>', 0, 2);

INSERT INTO lesson (section_id, title, type, video_url, content, duration, order_index) VALUES 
(13, 'Lesson 1: Các dạng biểu đồ Line Graph', 'VIDEO', 'https://video-link.com/linegraph.mp4', '<p>Phân tích xu hướng tăng giảm...</p>', 1200, 1);

INSERT INTO attachment (lesson_id, name, url, file_type) VALUES 
(2, 'Tu_vung_Family_Band_5.0.pdf', 'https://s3-bucket.com/files/vocab_family.pdf', 'PDF');

INSERT INTO attachment (lesson_id, name, url, file_type) VALUES 
(3, 'Audio_Dictation_Unit1.mp3', 'https://s3-bucket.com/files/audio_u1.mp3', 'MP3');

INSERT INTO enrollment (user_id, course_id, status, progress_percent, enrolled_date, completed_date)
VALUES
(1, 1, 'ACTIVE', 10, NOW(), NULL),
(1, 2, 'ACTIVE', 0, NOW(), NULL);

INSERT INTO lesson_progress (enrollment_id, lesson_id, is_completed, last_watched_second, updated_date)
VALUES
(
  (SELECT id FROM enrollment WHERE user_id = 1 AND course_id = 1 LIMIT 1),
  (SELECT id FROM lesson WHERE title LIKE 'Bài 1.1:%' LIMIT 1),
  1, 600, NOW()
),
(
  (SELECT id FROM enrollment WHERE user_id = 1 AND course_id = 1 LIMIT 1),
  (SELECT id FROM lesson WHERE title LIKE 'Bài 1.3:%' LIMIT 1),
  0, 120, NOW()
);

INSERT INTO quiz (id, lesson_id, title, pass_score, duration) VALUES
-- KHÓA 1 (Foundation)
(1, 1, 'Foundation Mid-term Test', 50, 45),
(2, 1, 'Foundation Final Test', 60, 60),

-- KHÓA 2 (Intensive)
(3, 2, 'Intensive Practice Test 1', 65, 60),
(4, 2, 'Intensive Practice Test 2', 70, 90),

-- KHÓA 3 (Master)
(5, 3, 'Master Full Mock Test 1', 75, 120),
(6, 3, 'Master Full Mock Test 2', 80, 120);

-- ====================================================
-- BỘ CÂU HỎI CHO KHÓA 1 (Quiz ID = 1)
-- ====================================================
-- 1. Listening (Có Audio, Có Options)
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(1, 'LISTENING', 'What time does the library close on Sundays?', 'https://files.com/audio/lib_hours.mp3',
'{"A": "4 PM", "B": "6 PM", "C": "8 PM", "D": "Closed"}', 'A', 'Speaker mentions Sunday hours are shorter, closing at 4 PM.'),

(1, 'LISTENING', 'Where is the new student center located?', 'https://files.com/audio/map.mp3',
'{"A": "Next to the gym", "B": "Behind the library", "C": "Opposite the cafeteria", "D": "Near the main gate"}', 'C', 'The map description points to the building across from the cafeteria.');

-- 2. Reading (Không Audio, Có Options)
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(1, 'READING', 'According to the paragraph, why do birds migrate?', NULL,
'{"A": "To find food", "B": "To avoid predators", "C": "To find a mate", "D": "To explore new lands"}', 'A', 'The text explicitly states food scarcity drives migration.'),

(1, 'READING', 'The word "detrimental" in line 5 is closest in meaning to:', NULL,
'{"A": "Beneficial", "B": "Harmful", "C": "Neutral", "D": "Significant"}', 'B', 'Context clues suggest a negative impact.');

-- 3. Writing (Không Audio, Không Options - Tự luận)
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(1, 'WRITING', 'Task 1: Write a letter to your friend apologizing for missing their birthday party. (At least 150 words)', NULL, NULL, NULL, NULL);

-- 4. Speaking (Có thể có Audio đề bài, Không Options)
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(1, 'SPEAKING', 'Part 2: Describe a favorite toy you had as a child. You should say: what it was, who gave it to you, and why you liked it.', 'https://files.com/audio/speak_prompt1.mp3', NULL, NULL, NULL);


-- ====================================================
-- BỘ CÂU HỎI CHO KHÓA 2 (Quiz ID = 3)
-- ====================================================
-- 1. Listening
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(3, 'LISTENING', 'How much does the platinum membership cost?', 'https://files.com/audio/gym.mp3',
'{"A": "$50", "B": "$75", "C": "$100", "D": "$120"}', 'C', 'It is mentioned after the discount is applied.'),

(3, 'LISTENING', 'What implies the speaker about the project deadline?', 'https://files.com/audio/meeting.mp3',
'{"A": "It is flexible", "B": "It is strict", "C": "It has been extended", "D": "It is unknown"}', 'B', 'He stresses that no extensions will be granted.');

-- 2. Reading
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(3, 'READING', 'Which title best fits the passage?', NULL,
'{"A": "The Rise of AI", "B": "Dangers of Technology", "C": "AI in Healthcare", "D": "History of Computers"}', 'C', 'The passage focuses solely on medical applications.'),

(3, 'READING', 'What is NOT mentioned as a side effect?', NULL,
'{"A": "Headache", "B": "Nausea", "C": "Dizziness", "D": "Fatigue"}', 'D', 'Fatigue appears in the next paragraph, not the listed effects.');

-- 3. Writing
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(3, 'WRITING', 'Task 2: Some people believe that video games are bad for children. Discuss both views and give your opinion.', NULL, NULL, NULL, NULL);

-- 4. Speaking
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(3, 'SPEAKING', 'Part 3: Do you think technology has made people more isolated? Why or why not?', NULL, NULL, NULL, NULL);


-- ====================================================
-- BỘ CÂU HỎI CHO KHÓA 3 (Quiz ID = 5)
-- ====================================================

-- 1. Listening
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(5, 'LISTENING', 'The lecturer suggests that the primary cause of the decline was:', 'https://files.com/audio/lecture.mp3',
'{"A": "Climate change", "B": "Overhunting", "C": "Disease", "D": "Habitat loss"}', 'D', 'He emphasizes deforestation above all other factors.'),

(5, 'LISTENING', 'What action does the student decide to take?', 'https://files.com/audio/advisor.mp3',
'{"A": "Drop the course", "B": "Change majors", "C": "Hire a tutor", "D": "Talk to the professor"}', 'A', 'She mentions filling out the withdrawal form.');

-- 2. Reading
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(5, 'READING', 'The author uses the example of the steam engine to illustrate:', NULL,
'{"A": "Technological stagnation", "B": "Industrial revolution", "C": "Paradigm shifts", "D": "Economic collapse"}', 'C', 'It serves as a metaphor for changing worldviews.'),

(5, 'READING', 'It can be inferred from the text that the experiment:', NULL,
'{"A": "Failed completely", "B": "Yielded unexpected results", "C": "Was unethical", "D": "Cost too much"}', 'B', 'The results surprised the researchers.');

-- 3. Writing
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(5, 'WRITING', 'Task 1: The chart below shows the percentage of households with internet access in 2000 vs 2020. Summarize the information.', NULL, NULL, NULL, NULL);

-- 4. Speaking
INSERT INTO question (quiz_id, skill, content, audio_url, options, correct_option, explanation) VALUES
(5, 'SPEAKING', 'Part 2: Describe a difficult decision you had to make. You should say: what the decision was, why it was difficult, and how you feel about it now.', NULL, NULL, NULL, NULL);