package ui;

import model.Role;
import service.RoleService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RolePanel extends JPanel implements BackToHomeListener {

    private RoleService roleService = new RoleService();
    private DefaultTableModel tableModel;
    private JTable table;

    // 新增图标路径常量
    private static final String ICON_PATH = "D:/Java/OfficeManagement/icons/";
    public RolePanel() {
        setLayout(new BorderLayout());

        String[] columns = {"角色ID", "角色名称"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        JButton btnAdd = new JButton("新增", createScaledIcon("add.jpeg"));
        JButton btnEdit = new JButton("编辑", createScaledIcon("edit.jpeg"));
        JButton btnDelete = new JButton("删除", createScaledIcon("delete.jpeg"));
        JButton btnBack = new JButton("返回主页", createScaledIcon("home.jpeg"));

        // 添加按钮顺序：刷新 -> 新增 -> 编辑 -> 删除 -> 返回主页
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);

        loadTableData();

        btnRefresh.addActionListener(e -> loadTableData());

        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "请输入角色名称：");
            if (name != null && !name.trim().isEmpty()) {
                Role r = new Role();
                r.setRoleName(name.trim());
                if (roleService.addRole(r)) {
                    JOptionPane.showMessageDialog(this, "添加成功");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "添加失败");
                }
            }
        });

        btnEdit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String oldName = (String) tableModel.getValueAt(row, 1);
            String newName = JOptionPane.showInputDialog(this, "修改角色名称：", oldName);
            if (newName != null && !newName.trim().isEmpty()) {
                Role r = new Role(id, newName.trim());
                if (roleService.updateRole(r)) {
                    JOptionPane.showMessageDialog(this, "修改成功");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败");
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请选择一条记录");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "确认删除该角色吗？", "提示", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (roleService.deleteRole(id)) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败");
                }
            }
        });

        btnBack.addActionListener(e -> backToHome());
    }

    // 新增图标缩放方法
    private ImageIcon createScaledIcon(String filename) {
        ImageIcon originalIcon = new ImageIcon(ICON_PATH + filename);
        if (originalIcon.getIconWidth() <= 0) {
            // 图标加载失败时使用空图标
            return new ImageIcon();
        }
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Role> list = roleService.getAllRoles();
        for (Role r : list) {
            tableModel.addRow(new Object[]{r.getRoleId(), r.getRoleName()});
        }
    }

    @Override
    public void backToHome() {
        Container parent = getParent();
        while (parent != null && !(parent instanceof BackToHomeListener)) {
            parent = parent.getParent();
        }
        if (parent instanceof BackToHomeListener listener) {
            listener.backToHome();
        }
    }
}

