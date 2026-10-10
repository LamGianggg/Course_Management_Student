-- Sample data for departments
INSERT INTO departments (id, name, description) VALUES
(1, 'Information Technology', 'Khoa Cong Nghe Thong Tin'),
(2, 'Business Administration', 'Khoa Quan Tri Kinh Doanh'),
(3, 'Computer Science', 'Khoa Khoa Hoc May Tinh')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Sample data for students
INSERT INTO students (id, name, dob, department_id) VALUES
(1, 'Nguyen Van A', '2003-01-15', 1),
(2, 'Tran Thi B', '2003-05-20', 1),
(3, 'Le Van C', '2002-11-10', 2),
(4, 'Pham Thi D', '2004-03-25', 3)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- Sample data for APP_DETAIL
INSERT INTO APP_DETAIL (ID, NAME, DESCRIPTION, CREATED_TIME) VALUES
('APP001', 'Visa Du Hoc', 'Ho so visa du hoc', NOW()),
('APP002', 'Hoc Bong', 'Ho so xin hoc bong', NOW())
ON DUPLICATE KEY UPDATE NAME=VALUES(NAME);
