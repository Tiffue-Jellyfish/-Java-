package service;

import dao.RoleDao;
import model.Role;

import java.sql.SQLException;
import java.util.List;

public class RoleService {
    private RoleDao roleDao = new RoleDao();

    public List<Role> getAllRoles() {
        try {
            return roleDao.getAllRoles();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getRoleNameById(int roleId) {
        try {
            return roleDao.getRoleNameById(roleId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Role getRoleById(int roleId) {
        try {
            return roleDao.getRoleById(roleId); // 使用新增的获取角色方法
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addRole(Role role) {
        try {
            return roleDao.addRole(role);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRole(Role role) {
        try {
            return roleDao.updateRole(role);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRole(int roleId) {
        try {
            return roleDao.deleteRole(roleId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}



