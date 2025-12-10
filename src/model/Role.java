package model;

public class Role {
    private int roleId;
    private String roleName;

    public Role() {
        // 无参构造器
    }

    // 带参数构造器
    public Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return roleName; // 用于 JComboBox 显示
    }
}



