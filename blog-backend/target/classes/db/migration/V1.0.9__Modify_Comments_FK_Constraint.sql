-- 首先删除现有的外键约束
IF EXISTS (SELECT * FROM sys.foreign_keys WHERE object_id = OBJECT_ID(N'FK_Comments_PostId_36B12243') AND parent_object_id = OBJECT_ID(N'dbo.Comments'))
BEGIN
    ALTER TABLE Comments DROP CONSTRAINT FK_Comments_PostId_36B12243
END

-- 修改 PostId 列允许为 NULL
ALTER TABLE Comments ALTER COLUMN PostId INT NULL;

-- 添加新的外键约束，设置为 SET NULL
ALTER TABLE Comments ADD CONSTRAINT FK_Comments_PostId 
    FOREIGN KEY (PostId) REFERENCES Posts(PostId)
    ON DELETE SET NULL; 