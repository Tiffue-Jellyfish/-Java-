package dao;
import model.Department;
import util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    // 获取所有部门
    public List<Department> getAllDepartments() throws SQLException {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM Department";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("departmentId"));
                d.setDepartmentName(rs.getString("departmentName"));
                list.add(d);
            }
        }

        return list;
    }

    // 根据 ID 获取部门名（可选功能）
    public String getDepartmentNameById(int departmentId) throws SQLException {
        String sql = "SELECT departmentName FROM Department WHERE departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("departmentName");
                }
            }
        }
        return null;
    }

    // 添加部门（自动生成ID）
    public boolean addDepartment(Department dept) throws SQLException {
        String getMaxIdSql = "SELECT MAX(departmentId) AS maxId FROM Department";
        String insertSql = "INSERT INTO Department (departmentId, departmentName) VALUES (?, ?)";

        int newId = 1; // 默认值

        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {

            if (rs.next()) {
                int maxId = rs.getInt("maxId");
                if (!rs.wasNull() && maxId > 0) { // 检查是否为null
                    newId = maxId + 1;
                }
            }

            // 调试信息
            System.out.println("添加部门参数: [" + newId + ", " + dept.getDepartmentName() + "]");

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setInt(1, newId);
                pstmt.setString(2, dept.getDepartmentName());

                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("添加部门失败: " + e.getMessage());
            System.err.println("SQL: " + insertSql);
            System.err.println("参数: [" + newId + ", " + dept.getDepartmentName() + "]");
            throw e;
        }
    }

    // 更新部门信息
    public boolean updateDepartment(Department dept) throws SQLException {
        String sql = "UPDATE Department SET departmentName = ? WHERE departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, dept.getDepartmentName());
            pstmt.setInt(2, dept.getDepartmentId());

            // 调试信息
            System.out.println("更新部门参数: [" + dept.getDepartmentId() + ", " + dept.getDepartmentName() + "]");

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("更新部门失败: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("参数: [" + dept.getDepartmentId() + ", " + dept.getDepartmentName() + "]");
            throw e;
        }
    }

    // 删除部门
    public boolean deleteDepartment(int departmentId) throws SQLException {
        String sql = "DELETE FROM Department WHERE departmentId = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, departmentId);

            // 调试信息
            System.out.println("删除部门参数: [" + departmentId + "]");

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.err.println("删除部门失败: " + e.getMessage());
            System.err.println("SQL: " + sql);
            System.err.println("部门ID: " + departmentId);
            throw e;
        }
    }

    // 根据部门名称获取部门（用于注册时选中项转ID）
    public Department getDepartmentByName(String departmentName) throws SQLException {
        String sql = "SELECT * FROM Department WHERE departmentName = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, departmentName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Department dept = new Department();
                    dept.setDepartmentId(rs.getInt("departmentId"));
                    dept.setDepartmentName(rs.getString("departmentName"));
                    return dept;
                }
            }
        }
        return null;
    }
}
