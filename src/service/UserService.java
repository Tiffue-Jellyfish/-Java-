package service;

import dao.UserDao;
import model.User;
import util.DBUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.sun.jdi.connect.spi.Connection;

public class UserService {
    private UserDao userDao = new UserDao();

    public UserService() {}

    // 登录校验
    public User login(String username, String password) {
        return userDao.login(username, password);
    }

    // 修改添加用户方法
    public boolean addUser(User user) {
        if (userDao.addUser(user)) {
            // 如果是普通用户，自动分配文件权限
            if (user.getRoleId() == 2) {
                PermissionService permissionService = new PermissionService();
                // 分配文件查看、添加、编辑、删除权限
                permissionService.assignPermissionToUser(user.getUserId(), 1); // file_view

            }
            return true;
        }
        return false;
    }



    // 修改用户信息
    public boolean updateUser(User user) {
        return userDao.updateUser(user);
    }

    // 删除用户
    public boolean deleteUser(int userId) {
        return userDao.deleteUser(userId);
    }

    // 按ID获取用户
    public User getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    // 获取所有用户
    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    // 按部门筛选
    public List<User> getUsersByDepartment(int departmentId) {
        return userDao.getUsersByDepartment(departmentId);
    }

    // 按用户名模糊查询
    public List<User> searchUsersByName(String nameKeyword) {
        return userDao.searchUsersByName(nameKeyword);
    }

    // 分页查询（offset从0开始，limit为每页条数）
    public List<User> getUsersByPage(int offset, int limit) {
        return userDao.getUsersByPage(offset, limit);
    }

    public User getUserWithPermissions(int userId) {
        return userDao.getUserWithPermissions(userId);
    }

    // 判断用户名是否已存在
    public boolean isUsernameTaken(String username) {
        return userDao.getUserByUsername(username) != null;
    }

    // 按用户名获取完整用户 - 这是关键方法
    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    // 重置密码（找回密码用）
    public boolean resetPassword(String username, String newPassword) {
        return userDao.resetPassword(username, newPassword);
    }

    private boolean isConnectionValid() {
        // 明确使用 java.sql.Connection
        try (java.sql.Connection conn = DBUtil.getConnection()) {
            return conn != null && conn.isValid(2); // 2秒超时验证
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<String, Integer> getDepartmentEmployeeCount() {

        return userDao.getDepartmentEmployeeCount();
    }

    public List<User> getUsersByDepartmentName(String departmentName) {
        return userDao.getUsersByDepartmentName(departmentName);
    }

    public int getUserCountByDepartment(int departmentId) {
        return userDao.getUserCountByDepartment(departmentId);
    }
}

