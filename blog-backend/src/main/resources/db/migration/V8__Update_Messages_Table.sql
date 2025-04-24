-- 添加新字段
ALTER TABLE Messages ADD SenderId INT;
ALTER TABLE Messages ADD SenderName NVARCHAR(100);
ALTER TABLE Messages ADD SenderEmail NVARCHAR(100);

-- 添加外键约束
ALTER TABLE Messages ADD CONSTRAINT FK_Messages_Sender
    FOREIGN KEY (SenderId) REFERENCES Users(UserId); 