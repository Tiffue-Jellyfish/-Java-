package dao;

import model.Permission;
import model.User;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class UserDao {

    // 登录验证
    public User login(String name, String password) {
        System.out.println("尝试登录，用户名：" + name + "，密码：" + password);
        String sql = "SELECT * FROM [dbo].[User] WHERE name = ? AND password = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User u = mapResultSetToUser(rs);

                // 修复：登录时加载用户权限
                PermissionDao permissionDao = new PermissionDao();
                List<Permission> permissions = permissionDao.getPermissionsByUserId(u.getUserId());
                u.setPermissions(permissions);

                System.out.println("查询到用户，数据库密码：" + u.getPassword());
                return u;
            } else {
                System.out.println("查询无匹配用户");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM [User] WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            throw new RuntimeException("检查用户存在性失败", e);
        }
    }

    public boolean addUser(User user) {
        // 获取当前最大用户ID
        String getMaxIdSql = "SELECT MAX(userId) AS maxId FROM [dbo].[User]";
        String insertSql = "INSERT INTO [dbo].[User] (userId, name, password, departmentId, roleId) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {

            int newId = 1;
            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (maxId > 0) {
                    newId = maxId + 1;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, user.getName());
                pstmt.setString(3, user.getPassword());
                pstmt.setInt(4, user.getDepartmentId());
                pstmt.setInt(5, user.getRoleId());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    user.setUserId(newId); // 更新用户对象的ID
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 修改用户，成功返回true
    public boolean updateUser(User user) {
        String sql = "UPDATE [dbo].[User] SET name = ?, password = ?, departmentId = ?, roleId = ? WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPassword());
            stmt.setInt(3, user.getDepartmentId());
            stmt.setInt(4, user.getRoleId());
            stmt.setInt(5, user.getUserId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除用户，成功返回true
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM [dbo].[User] WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据ID查询用户
    public User getUserById(int userId) {
        String sql = "SELECT * FROM [dbo].[User] WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 查询全部用户列表
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM [dbo].[User]";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 按部门查询用户
    public List<User> getUsersByDepartment(int departmentId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM [dbo].[User] WHERE departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 按用户名模糊查询
    public List<User> searchUsersByName(String nameKeyword) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM [dbo].[User] WHERE name LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nameKeyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 分页查询
    public List<User> getUsersByPage(int offset, int limit) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM [dbo].[User] ORDER BY userId OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, offset);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 工具方法：把ResultSet中的数据映射成User对象
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("userId"));
        u.setName(rs.getString("name"));
        u.setPassword(rs.getString("password"));
        u.setDepartmentId(rs.getInt("departmentId"));
        u.setRoleId(rs.getInt("roleId"));
        return u;
    }

    public User getUserWithPermissions(int userId) {
        String sql = "SELECT u.*, p.permissionId, p.permissionName, p.description " +
                "FROM [User] u " +
                "LEFT JOIN UserPermission up ON u.userId = up.userId " + // 修改表名
                "LEFT JOIN Permission p ON up.permissionId = p.permissionId " + // 修改表名
                "WHERE u.userId = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            User user = null;
            List<Permission> permissions = new ArrayList<>();

            while (rs.next()) {
                if (user == null) {
                    user = mapResultSetToUser(rs);
                }

                int permissionId = rs.getInt("permissionId");
                if (!rs.wasNull()) {
                    Permission permission = new Permission();
                    permission.setPermissionId(permissionId);
                    permission.setPermissionName(rs.getString("permissionName")); // 驼峰命名
                    permission.setDescription(rs.getString("description"));
                    permissions.add(permission);
                }
            }

            if (user != null) {
                user.setPermissions(permissions);
            }
            return user;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User getUserByUsername(String name) {
        String sql = "SELECT * FROM [User] WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setDepartmentId(rs.getInt("departmentId"));
                    user.setRoleId(rs.getInt("roleId"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean resetPassword(String name, String newPassword) {
        String sql = "UPDATE [User] SET password = ? WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, name);
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }




    public Map<String, Integer> getDepartmentEmployeeCount() {
        Map<String, Integer> stats = new HashMap<>();

        // 使用 try-with-resources 确保资源关闭
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT d.departmentName, COUNT(u.userId) AS employeeCount " +
                             "FROM department d " +
                             "LEFT JOIN [user] u ON d.departmentId = u.departmentId " +
                             "GROUP BY d.departmentName");
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String departmentName = rs.getString("departmentName");
                int count = rs.getInt("employeeCount");
                stats.put(departmentName, count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "查询部门统计时出错: " + e.getMessage());
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(null, "数据库连接为空，请检查连接设置");
        }
        return stats;
    }

    public List<User> getUsersByDepartmentName(String departmentName) {
        List<User> users = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT u.* FROM [user] u " +
                             "JOIN department d ON u.departmentId = d.departmentId " +
                             "WHERE d.departmentName = ?")) {

            pstmt.setString(1, departmentName);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("userId"));
                    user.setName(rs.getString("name"));
                    user.setPassword(rs.getString("password"));
                    user.setDepartmentId(rs.getInt("departmentId"));
                    user.setRoleId(rs.getInt("roleId"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "查询部门员工时出错: " + e.getMessage());
        }
        return users;
    }

    public int getUserCountByDepartment(int departmentId) {
        String sql = "SELECT COUNT(*) FROM [User] WHERE departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}


