-- 用户表
CREATE TABLE Users (
    UserId INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Password NVARCHAR(100) NOT NULL,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    Avatar NVARCHAR(200),
    Bio NVARCHAR(500),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    Disabled BIT NOT NULL DEFAULT 0
);

-- 文章表
CREATE TABLE Posts (
    PostId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    Title NVARCHAR(200) NOT NULL,
    Content NVARCHAR(MAX) NOT NULL,
    Summary NVARCHAR(500),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    IsHidden BIT NOT NULL DEFAULT 0,
    FOREIGN KEY (UserId) REFERENCES Users(UserId)
);

-- 收藏表
CREATE TABLE Favorites (
    FavoriteId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    PostId INT NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES Users(UserId),
    FOREIGN KEY (PostId) REFERENCES Posts(PostId),
    UNIQUE (UserId, PostId)  -- 防止重复收藏
);

-- 评论表
CREATE TABLE Comments (
    CommentId INT IDENTITY(1,1) PRIMARY KEY,
    PostId INT NOT NULL,
    UserId INT NOT NULL,
    Content NVARCHAR(1000) NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    UpdatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (PostId) REFERENCES Posts(PostId),
    FOREIGN KEY (UserId) REFERENCES Users(UserId)
);

-- 消息表
CREATE TABLE Messages (
    MessageId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,  -- 接收消息的用户
    PostId INT NOT NULL,  -- 相关文章
    CommentId INT,        -- 相关评论（如果是评论消息）
    Type NVARCHAR(20) NOT NULL,  -- 'comment' 或 'favorite'
    IsRead BIT NOT NULL DEFAULT 0,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES Users(UserId),
    FOREIGN KEY (PostId) REFERENCES Posts(PostId),
    FOREIGN KEY (CommentId) REFERENCES Comments(CommentId)
);

-- 创建索引
CREATE INDEX IX_Posts_UserId ON Posts(UserId);
CREATE INDEX IX_Favorites_UserId ON Favorites(UserId);
CREATE INDEX IX_Favorites_PostId ON Favorites(PostId);
CREATE INDEX IX_Comments_PostId ON Comments(PostId);
CREATE INDEX IX_Comments_UserId ON Comments(UserId);
CREATE INDEX IX_Messages_UserId ON Messages(UserId);
CREATE INDEX IX_Messages_PostId ON Messages(PostId);

-- 创建触发器，在文章更新时自动更新 UpdatedAt
CREATE TRIGGER TR_Posts_UpdatedAt
ON Posts
AFTER UPDATE
AS
BEGIN
    UPDATE Posts
    SET UpdatedAt = GETDATE()
    FROM Posts p
    INNER JOIN inserted i ON p.PostId = i.PostId;
END;

-- 创建触发器，在评论更新时自动更新 UpdatedAt
CREATE TRIGGER TR_Comments_UpdatedAt
ON Comments
AFTER UPDATE
AS
BEGIN
    UPDATE Comments
    SET UpdatedAt = GETDATE()
    FROM Comments c
    INNER JOIN inserted i ON c.CommentId = i.CommentId;
END;

-- 角色表
CREATE TABLE Roles (
    RoleId INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL UNIQUE,
    Description NVARCHAR(200),
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE()
);

-- 用户角色关联表
CREATE TABLE UserRoles (
    UserRoleId INT IDENTITY(1,1) PRIMARY KEY,
    UserId INT NOT NULL,
    RoleId INT NOT NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (UserId) REFERENCES Users(UserId),
    FOREIGN KEY (RoleId) REFERENCES Roles(RoleId),
    UNIQUE (UserId, RoleId)
);

-- 插入基础角色
INSERT INTO Roles (RoleName, Description) VALUES
('ROLE_ADMIN', '管理员'),
('ROLE_USER', '普通用户');

-- 创建管理员账号（密码为：admin123）
INSERT INTO Users (Username, Password, Email, Bio) VALUES
('admin', '$2a$10$YMxsVcKoYgGEcPVnjxYCxOuXIxGQZRQXAKX9Qe3Ug5IYhBw5Y5YwC', 'admin@example.com', '系统管理员');

-- 为管理员分配角色
INSERT INTO UserRoles (UserId, RoleId)
SELECT u.UserId, r.RoleId
FROM Users u, Roles r
WHERE u.Username = 'admin' AND r.RoleName = 'ROLE_ADMIN';

-- 修改 Posts 表，添加 Tags 字段
ALTER TABLE Posts ADD
    Tags NVARCHAR(500);  -- 用于存储逗号分隔的标签字符串

-- 创建标签统计视图（可选）
CREATE VIEW TagStats AS
SELECT 
    value AS TagName,
    COUNT(*) AS TagCount
FROM Posts
CROSS APPLY STRING_SPLIT(Tags, ',')
WHERE Tags IS NOT NULL
GROUP BY value;

-- 添加作者相关字段
ALTER TABLE Posts
ADD AuthorName NVARCHAR(100),
    AuthorAvatar NVARCHAR(500);

-- 更新现有记录的作者信息
UPDATE p
SET p.AuthorName = u.Username,
    p.AuthorAvatar = u.Avatar
FROM Posts p
JOIN Users u ON p.UserId = u.UserId;

-- 添加用户禁用字段
ALTER TABLE Users
ADD Disabled BIT NOT NULL DEFAULT 0;

-- 添加文章隐藏字段
ALTER TABLE Posts
ADD IsHidden BIT NOT NULL DEFAULT 0;