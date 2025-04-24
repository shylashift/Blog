-- 添加 IsDeleted 列到 Posts 表
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('Posts') 
    AND name = 'IsDeleted'
)
BEGIN
    ALTER TABLE Posts
    ADD IsDeleted BIT NOT NULL DEFAULT 0;
END; 