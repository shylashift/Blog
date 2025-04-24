-- 检查Disabled列是否存在，如果不存在则添加
IF NOT EXISTS (
    SELECT 1
    FROM sys.columns 
    WHERE object_id = OBJECT_ID('Users') 
    AND name = 'Disabled'
)
BEGIN
    ALTER TABLE Users
    ADD Disabled BIT NOT NULL DEFAULT 0;
END 