-- 添加 IsHidden 列到 Posts 表
IF NOT EXISTS (
    SELECT * FROM sys.columns 
    WHERE object_id = OBJECT_ID('Posts') 
    AND name = 'IsHidden'
)
BEGIN
    ALTER TABLE Posts
    ADD IsHidden BIT NOT NULL DEFAULT 0;
END; 