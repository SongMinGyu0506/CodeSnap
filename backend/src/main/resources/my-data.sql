-- 사용자 pwd는 1234를 bcrypt로 암호화 한 것임
INSERT INTO users (user_id, pwd, del_yn) VALUES
('admin@example.com', '$2b$12$cqtPb6cdOByjFP8On.RSp.AptgBSi3KkvnXbKk90edljhxb/p43nq', 'N'),
('user@example.com', '$2b$12$cqtPb6cdOByjFP8On.RSp.AptgBSi3KkvnXbKk90edljhxb/p43nq', 'N');

-- 권한
INSERT INTO roles (role_id, role_name, description) VALUES
('ROLE_ADMN', '시스템 관리자', '모든 권한 보유'),
('ROLE_USER', '일반 사용자', '기본 접근 권한'),
('ROLE_0001', '메뉴 관리자', '메뉴 설정 및 권한 관리'),
('ROLE_0002', '사용자 관리자', '사용자 계정 관리');

-- 사용자-권한 매핑
INSERT INTO user_roles (user_id, role_id) VALUES
('admin@example.com', 'ROLE_ADMN'),
('user@example.com', 'ROLE_USER');

-- 메뉴
INSERT INTO menu (menu_id, parent_id, menu_name, url, sort_order, use_yn) VALUES
(100000, NULL, '관리자 메뉴', '/admin', 1, 'Y'),
(101000, 100000, '사용자 관리', '/admin/users', 1, 'Y'),
(102000, 100000, '메뉴 관리', '/admin/menus', 2, 'Y'),
(200000, NULL, '사용자 메뉴', '/user', 2, 'Y'),
(201000, 200000, '내 정보', '/user/profile', 1, 'Y');

-- 메뉴-권한 매핑
INSERT INTO menu_roles (role_id, menu_id) VALUES
('ROLE_ADMN', 100000),
('ROLE_ADMN', 101000),
('ROLE_ADMN', 102000),
('ROLE_USER', 200000),
('ROLE_ADMN', 200000),
('ROLE_USER', 201000);
