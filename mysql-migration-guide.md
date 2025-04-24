# 从SQL Server迁移到MySQL 8.0指南

本文档提供了将博客系统从SQL Server迁移到MySQL 8.0的详细步骤和注意事项。

## 迁移步骤

### 1. 更新依赖

在`pom.xml`文件中，我们已经：
- 移除了SQL Server依赖
- 添加了MySQL连接器依赖
- 添加了Flyway支持MySQL的依赖

### 2. 修改数据库配置

在`application.yml`文件中，我们已经：
- 更新了数据库连接URL
- 更新了数据库驱动类
- 更新了数据库方言
- 添加了Flyway配置

### 3. 初始化数据库

在迁移前需要手动创建MySQL数据库：

```sql
CREATE DATABASE BlogDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. 迁移脚本

我们已经创建了适用于MySQL的Flyway迁移脚本：
- `V1__Init_Schema.sql`: 初始化数据库结构
- `V7__Create_Messages_Table.sql`: 创建消息表
- `V8__Update_Messages_Table.sql`: 更新消息表结构
- `V9__Create_Chat_Messages_Table.sql`: 创建聊天消息表

### 5. 数据迁移

如果需要迁移现有数据，建议使用以下方法之一：

1. **使用数据库导出导入工具**：
   - 从SQL Server导出CSV文件
   - 导入CSV文件到MySQL

2. **编写数据迁移脚本**：
   - 使用ETL工具或自定义程序读取SQL Server数据
   - 将数据写入MySQL

## 数据类型映射参考

| SQL Server类型 | MySQL类型 | 说明 |
|--------------|----------|-----|
| INT | INT | 整数 |
| BIGINT | BIGINT | 长整数 |
| NVARCHAR(n) | VARCHAR(n) | 变长字符串 |
| NVARCHAR(MAX) | LONGTEXT | 大文本 |
| DATETIME2 | DATETIME | 日期时间 |
| BIT | TINYINT(1) | 布尔值 |
| IDENTITY | AUTO_INCREMENT | 自增ID |

## 语法差异注意事项

1. **自增列**：
   - SQL Server: `IDENTITY(1,1)`
   - MySQL: `AUTO_INCREMENT`

2. **默认当前时间**：
   - SQL Server: `DEFAULT GETDATE()`
   - MySQL: `DEFAULT CURRENT_TIMESTAMP`

3. **自动更新时间**：
   - SQL Server: 需要触发器
   - MySQL: `ON UPDATE CURRENT_TIMESTAMP`

4. **布尔类型**：
   - SQL Server: `BIT`
   - MySQL: `TINYINT(1)`

5. **字符串比较**：
   - SQL Server默认不区分大小写
   - MySQL默认区分大小写，使用`COLLATE`设置

## 测试验证

迁移后，建议执行以下测试：

1. 用户注册登录功能
2. 文章的CRUD操作
3. 评论功能
4. 用户权限检查
5. 全文搜索功能

## 解决可能的问题

1. **字符集问题**：确保使用`utf8mb4`字符集和`utf8mb4_unicode_ci`排序规则，以支持完整的Unicode字符集。

2. **时区问题**：MySQL的时区设置可能与SQL Server不同，确保应用程序中正确处理时区。

3. **索引性能**：MySQL的索引实现与SQL Server不同，可能需要重新评估索引策略。

4. **查询优化**：可能需要调整某些查询以适应MySQL的查询优化器。

## 备份策略

配置适当的MySQL备份策略：

```bash
# 每日备份示例
mysqldump -u root -p --databases BlogDB > blogdb_backup_$(date +%Y%m%d).sql
```

## 代码修改说明

除了数据库配置和SQL脚本外，我们还对后端代码进行了一些修改，以确保与MySQL兼容：

### 1. SQL兼容性助手

我们创建了一个SQL兼容性助手类 `SqlCompatibilityHelper`，它是一个MyBatis拦截器，可以自动将SQL Server语法转换为MySQL语法：

- 将 `OFFSET-FETCH` 转换为 `LIMIT`
- 将 `STRING_SPLIT` 函数调用转换为MySQL兼容的语法
- 将字符串连接（使用 `+` 运算符）转换为 `CONCAT()`
- 将 `GETDATE()` 转换为 `NOW()`

### 2. 实体类修改

修改了实体类中的列定义：

- 将 `NVARCHAR(MAX)` 修改为 `LONGTEXT`
- 将其他SQL Server特有的数据类型修改为MySQL对应的类型

### 3. 迁移验证器

添加了 `MySQLMigrationValidator` 类，在应用启动时验证MySQL配置，并输出数据库信息。

## 手动修改的文件

1. `application.yml` - 更新数据库配置
2. `pom.xml` - 更新依赖
3. `db/migration/*.sql` - 更新迁移脚本
4. `entity/ChatMessage.java` - 修改列定义
5. `utils/SqlCompatibilityHelper.java` - 添加SQL兼容性助手
6. `config/MySQLMigrationValidator.java` - 添加迁移验证器

## MySQL特有功能

在迁移到MySQL后，可以使用一些MySQL特有的功能来优化应用：

1. **全文索引**：MySQL提供内置的全文索引功能，可用于文章搜索
   ```sql
   ALTER TABLE Posts ADD FULLTEXT INDEX ft_content (Title, Content);
   ```

2. **JSON支持**：MySQL 8.0提供了良好的JSON支持，可用于存储结构化数据
   ```sql
   ALTER TABLE Posts ADD Metadata JSON;
   ```

3. **窗口函数**：MySQL 8.0支持窗口函数，可用于复杂统计查询
   ```sql
   SELECT PostId, Title, ROW_NUMBER() OVER (PARTITION BY UserId ORDER BY CreatedAt DESC) as UserPostRank FROM Posts;
   ``` 