package dao;

import model.Permission;
import util.DBUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionDao {

    // 获取所有权限
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM Permission";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Permission permission = new Permission();
                permission.setPermissionId(rs.getInt("permissionId")); // 驼峰命名
                permission.setPermissionName(rs.getString("permissionName")); // 驼峰命名
                permission.setDescription(rs.getString("description"));
                permissions.add(permission);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    // 根据用户ID获取权限
    public List<Permission> getPermissionsByUserId(int userId) {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT p.* FROM Permission p " + // 修改表名
                "JOIN UserPermission up ON p.permissionId = up.permissionId " + // 修改表名
                "WHERE up.userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setPermissionId(rs.getInt("permissionId")); // 驼峰命名
                    permission.setPermissionName(rs.getString("permissionName")); // 驼峰命名
                    permission.setDescription(rs.getString("description"));
                    permissions.add(permission);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    // 为用户分配权限
    public boolean assignPermissionToUser(int userId, int permissionId) {
        String sql = "INSERT INTO UserPermission (userId, permissionId) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, permissionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 移除用户的权限
    public boolean removePermissionFromUser(int userId, int permissionId) {
        String sql = "DELETE FROM UserPermission WHERE userId = ? AND permissionId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, permissionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取用户的所有权限ID
    public List<Integer> getPermissionIdsByUserId(int userId) {
        List<Integer> permissionIds = new ArrayList<>();
        // 修复：修正列名 permission_id -> permissionId
        String sql = "SELECT permissionId FROM UserPermission WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    permissionIds.add(rs.getInt("permissionId")); // 修正列名
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissionIds;
    }

    // 新增：移除用户所有权限
    public boolean removeAllPermissionsForUser(int userId) {
        String sql = "DELETE FROM UserPermission WHERE userId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() >= 0; // 即使没有权限也返回true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

