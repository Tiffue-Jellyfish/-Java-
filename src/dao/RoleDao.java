package dao;

import model.Role;
import util.DBUtil;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;  // 导入 ArrayList 类
public class RoleDao {

    // 查询所有角色
    public List<Role> getAllRoles() throws SQLException {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM Role";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("roleId"));
                role.setRoleName(rs.getString("roleName"));
                list.add(role);
            }
        }
        return list;
    }

    // 根据ID获取角色名
    public String getRoleNameById(int roleId) throws SQLException {
        String sql = "SELECT roleName FROM Role WHERE roleId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("roleName");
            }
        }
        return null;
    }

    // 添加角色（修改后）
    public boolean addRole(Role role) throws SQLException {
        String getMaxIdSql = "SELECT MAX(roleId) AS maxId FROM Role";
        String insertSql = "INSERT INTO Role (roleId, roleName) VALUES (?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmtMaxId = conn.prepareStatement(getMaxIdSql);
             ResultSet rs = pstmtMaxId.executeQuery()) {

            int newId = 1;
            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (maxId > 0) {
                    newId = maxId + 1;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, role.getRoleName());
                return pstmt.executeUpdate() > 0;
            }
        }
    }

    // 修改角色
    public boolean updateRole(Role role) throws SQLException {
        String sql = "UPDATE Role SET roleName = ? WHERE roleId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.getRoleName());
            stmt.setInt(2, role.getRoleId());
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // 删除角色
    public boolean deleteRole(int roleId) throws SQLException {
        String sql = "DELETE FROM Role WHERE roleId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            int affected = stmt.executeUpdate();
            return affected > 0;
        }
    }

    // 根据ID获取角色信息
    public Role getRoleById(int roleId) throws SQLException {
        String sql = "SELECT * FROM Role WHERE roleId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("roleId"));
                role.setRoleName(rs.getString("roleName"));
                return role;
            }
        }
        return null;
    }
}



