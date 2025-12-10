package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int userId;
    private String name;
    private String password;
    private int departmentId;
    private int roleId;

    // 无参构造器
    public User() {}

    // 带参数的构造器
    public User(String name, String password, int departmentId, int roleId) {
        this.name = name;
        this.password = password;
        this.departmentId = departmentId;
        this.roleId = roleId;
    }

    // getter 和 setter 方法
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    private List<Permission> permissions = new ArrayList<>();

    public List<Permission> getPermissions() {
        return permissions;
    }

    // 添加缺失的setPermissions方法
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions != null ? permissions : new ArrayList<>();
    }

    // 优化权限检查方法
    public boolean hasPermission(String permissionName) {
        // 管理员(roleId=1)拥有所有权限
        if (this.roleId == 1) {
            return true;
        }

        // 确保权限列表不为null
        if (permissions == null) {
            return false;
        }

        for (Permission p : permissions) {
            if (p.getPermissionName().equals(permissionName)) {
                return true;
            }
        }
        return false;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }
    @Override
    public String toString() {
        return name; // 关键修改：返回用户名
    }
}





