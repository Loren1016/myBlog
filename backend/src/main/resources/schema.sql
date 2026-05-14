-- myblog database DDL (reference only; JPA hibernate ddl-auto=update handles table creation)
-- Run this manually if you prefer explicit schema control.

CREATE TABLE IF NOT EXISTS `user` (
    id          VARCHAR(36)  PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar      VARCHAR(500),
    bio         TEXT,
    skills      JSON,
    social_links JSON,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS blog (
    id          VARCHAR(36)  PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    summary     VARCHAR(500),
    content     LONGTEXT,
    cover_image VARCHAR(500),
    tags        JSON,
    category    VARCHAR(100),
    status      VARCHAR(20)  DEFAULT 'draft',
    published_at DATETIME,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    view_count  INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project (
    id          VARCHAR(36)  PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    slug        VARCHAR(255) NOT NULL UNIQUE,
    tagline     VARCHAR(200),
    description LONGTEXT,
    cover_image VARCHAR(500),
    screenshots JSON,
    tech_stack  JSON,
    demo_url    VARCHAR(500),
    source_url  VARCHAR(500),
    priority    INT DEFAULT 0,
    role        VARCHAR(100),
    completed_at DATETIME,
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS media (
    id          VARCHAR(36)  PRIMARY KEY,
    filename    VARCHAR(255) NOT NULL,
    url         VARCHAR(500) NOT NULL,
    size        BIGINT,
    mime_type   VARCHAR(100),
    uploaded_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
