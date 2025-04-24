-- 查看用户表
SELECT * FROM Users;

-- 查看角色表
SELECT * FROM Roles;

-- 查看用户角色关联表
SELECT * FROM UserRoles;

-- 查看admin用户的角色关联
SELECT u.UserId, u.Username, u.Email, r.RoleId, r.RoleName
FROM Users u
JOIN UserRoles ur ON u.UserId = ur.UserId
JOIN Roles r ON ur.RoleId = r.RoleId
WHERE u.Username = 'admin';

-- 修复admin用户的角色（如果需要）
INSERT INTO UserRoles (UserId, RoleId, CreatedAt)
SELECT u.UserId, r.RoleId, GETDATE()
FROM Users u, Roles r
WHERE u.Username = 'admin' AND r.RoleName = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM UserRoles ur 
    WHERE ur.UserId = u.UserId AND ur.RoleId = r.RoleId
); 