-- 删除所有重复的用户角色关系
WITH DuplicateRoles AS (
    SELECT UserId, RoleId,
           ROW_NUMBER() OVER (PARTITION BY UserId ORDER BY CreatedAt) as rn
    FROM UserRoles
)
DELETE ur
FROM UserRoles ur
INNER JOIN DuplicateRoles dr 
    ON ur.UserId = dr.UserId 
    AND ur.RoleId = dr.RoleId
WHERE dr.rn > 1;

-- 确保每个用户都有一个角色（默认为ROLE_USER，RoleId = 2）
INSERT INTO UserRoles (UserId, RoleId, CreatedAt)
SELECT u.UserId, 2, GETDATE()
FROM Users u
WHERE NOT EXISTS (
    SELECT 1 FROM UserRoles ur WHERE ur.UserId = u.UserId
);

-- 确保ID为1的用户（主管理员）有ROLE_ADMIN角色
UPDATE ur
SET ur.RoleId = 1
FROM UserRoles ur
WHERE ur.UserId = 1;

-- 删除任何无效的角色关系（引用了不存在的角色ID）
DELETE ur
FROM UserRoles ur
WHERE NOT EXISTS (
    SELECT 1 FROM Roles r WHERE r.RoleId = ur.RoleId
); 