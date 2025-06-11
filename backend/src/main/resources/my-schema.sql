-- 사용자 정보 테이블
CREATE TABLE users (
    user_id VARCHAR(255) PRIMARY KEY,
    pwd VARCHAR(255) NOT NULL,
    del_yn CHAR(1) DEFAULT 'N' CHECK (del_yn IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 권한 정보 테이블
CREATE TABLE roles (
    role_id CHAR(9) PRIMARY KEY,
    role_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자-권한 매핑 테이블
CREATE TABLE user_roles (
    user_id VARCHAR(255) NOT NULL,
    role_id CHAR(9) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

-- 메뉴 정보 테이블
CREATE TABLE menu (
    menu_id BIGINT PRIMARY KEY,
    parent_id BIGINT,
    menu_name VARCHAR(100) NOT NULL,
    url VARCHAR(255),
    sort_order INT DEFAULT 0,
    use_yn CHAR(1) DEFAULT 'Y' CHECK (use_yn IN ('Y', 'N')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 메뉴-권한 매핑 테이블
CREATE TABLE menu_roles (
    role_id CHAR(9) NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE login_logs (
    email VARCHAR(255) NOT NULL,
    status CHAR(9) NOT NULL,
    login_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    client_ip VARCHAR(50),
    user_agent VARCHAR(255),
    PRIMARY KEY (login_at)
);

CREATE TABLE `api_access_logs`
(
  `access_at`     TIMESTAMP    NOT NULL,
  `url`           VARCHAR(255) NOT NULL,
  `email`         VARCHAR(255) NULL,
  `http_status`   CHAR(3)      NOT NULL,
  `request_json`  TEXT         NULL,
  `response_json` TEXT         NULL,
  PRIMARY KEY (`access_at`, `url`)
)
