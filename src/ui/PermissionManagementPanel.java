package ui;

import model.Permission;
import model.User;
import service.PermissionService;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PermissionManagementPanel extends JPanel {
    private UserService userService = new UserService();
    private PermissionService permissionService = new PermissionService();
    private JComboBox<User> userComboBox;
    private JList<Permission> permissionList;
    private DefaultListModel<Permission> listModel;
    private User currentUser; // 添加当前用户字段

    // 在 PermissionManagementPanel 类中添加通知权限定义
    private static final String[] NOTICE_PERMISSIONS = {
            "notice_add", "notice_edit", "notice_delete"
    };

    private static final String[] NOTICE_DESCRIPTIONS = {
            "新增通知", "编辑通知", "删除通知"
    };

    // 在类中添加文件权限定义
    private static final String[] FILE_PERMISSIONS = {
            "file_view", "file_add", "file_edit", "file_delete"
    };

    private static final String[] FILE_DESCRIPTIONS = {
            "查看文件", "添加文件", "编辑文件", "删除文件"
    };

    // 添加考勤权限定义
    private static final String[] ATTENDANCE_PERMISSIONS = {
            "attendance_add", "attendance_edit", "attendance_delete"
    };

    private static final String[] ATTENDANCE_DESCRIPTIONS = {
            "新增考勤", "编辑考勤", "删除考勤"
    };

    // 在类中添加会议权限定义
    private static final String[] MEETING_PERMISSIONS = {
            "meeting_add", "meeting_edit", "meeting_delete"
    };

    private static final String[] MEETING_DESCRIPTIONS = {
            "新增会议", "编辑会议", "删除会议"
    };

    // 添加部门权限定义
    private static final String[] DEPARTMENT_PERMISSIONS = {
            "department_view", "department_add", "department_edit", "department_delete"
    };

    private static final String[] DEPARTMENT_DESCRIPTIONS = {
            "查看部门", "新增部门", "编辑部门", "删除部门"
    };

    // 修改构造函数接收当前用户
    public PermissionManagementPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout(10, 10));

        // 用户选择面板
        JPanel userPanel = new JPanel();
        userPanel.add(new JLabel("选择用户:"));
        userComboBox = new JComboBox<>();
        loadUsers();
        userPanel.add(userComboBox);
        add(userPanel, BorderLayout.NORTH);

        // 权限列表
        listModel = new DefaultListModel<>();
        permissionList = new JList<>(listModel);
        permissionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        permissionList.setCellRenderer(new PermissionListCellRenderer());
        add(new JScrollPane(permissionList), BorderLayout.CENTER);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("保存权限");
        saveButton.addActionListener(e -> savePermissions());
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // 加载选中用户的权限
        loadPermissionsForSelectedUser();

        // 用户选择改变时重新加载权限
        userComboBox.addActionListener(e -> loadPermissionsForSelectedUser());

        // 添加点击事件切换选择状态
        permissionList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int index = permissionList.locationToIndex(evt.getPoint());
                if (index != -1) {
                    Permission perm = listModel.getElementAt(index);
                    perm.setSelected(!perm.isSelected());
                    permissionList.repaint();
                }
            }
        });
    }

    private void loadUsers() {
        List<User> users = userService.getAllUsers();
        for (User user : users) {
            if (user.getRoleId() == 2) {
                userComboBox.addItem(user); // 现在会显示用户名
            }
        }
    }

    // 修改加载权限方法
    private void loadPermissionsForSelectedUser() {
        listModel.clear();
        User selectedUser = (User) userComboBox.getSelectedItem();
        if (selectedUser != null) {
            List<Integer> userPermissionIds = permissionService.getPermissionIdsByUserId(selectedUser.getUserId());

            // 获取所有权限（从数据库）
            List<Permission> allPermissions = permissionService.getAllPermissions();

            // 显示所有权限并标记选中状态
            for (Permission permission : allPermissions) {
                // 跳过以 "role_" 开头的权限
                if (permission.getPermissionName().startsWith("role_")) {
                    continue;
                }

                Permission perm = new Permission(
                        permission.getPermissionId(),
                        permission.getPermissionName(),
                        permission.getDescription()
                );
                perm.setSelected(userPermissionIds.contains(permission.getPermissionId()));
                listModel.addElement(perm);
            }
        }
    }

    // 修改 savePermissions 方法
    private void savePermissions() {
        User selectedUser = (User) userComboBox.getSelectedItem();
        if (selectedUser == null) return;

        // 获取选中的权限
        List<Permission> selectedPermissions = new ArrayList<>();
        for (int i = 0; i < listModel.size(); i++) {
            Permission perm = listModel.getElementAt(i);
            if (perm.isSelected()) {
                selectedPermissions.add(perm);
            }
        }

        // 保存到数据库
        boolean success = permissionService.savePermissionsForUser(
                selectedUser.getUserId(),
                selectedPermissions
        );

        if (success) {
            // 刷新当前用户的权限缓存 - 添加更多日志
            System.out.println("保存权限成功，用户ID: " + selectedUser.getUserId());

            // 刷新当前用户的权限缓存
            if (currentUser != null && currentUser.getUserId() == selectedUser.getUserId()) {
                currentUser.setPermissions(selectedPermissions);
                System.out.println("刷新当前用户权限缓存");
            }

            JOptionPane.showMessageDialog(this, "权限保存成功！");
        } else {
            JOptionPane.showMessageDialog(this, "权限保存失败！");
        }
    }

    // 权限列表渲染器
    private static class PermissionListCellRenderer extends JCheckBox implements ListCellRenderer<Permission> {
        public Component getListCellRendererComponent(
                JList<? extends Permission> list, Permission value,
                int index, boolean isSelected, boolean cellHasFocus) {

            setSelected(value.isSelected());
            setText(value.getPermissionName() + " - " + value.getDescription());
            setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
            setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
            return this;
        }
    }
}
