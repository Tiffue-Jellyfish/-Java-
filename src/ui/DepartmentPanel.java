package ui;

import model.Department;
import model.User;
import service.DepartmentService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class DepartmentPanel extends JPanel implements BackToHomeListener {

    private DepartmentService departmentService = new DepartmentService();
    private DefaultTableModel tableModel;
    private JTable table;
    private boolean isReadOnly;  // 是否只读模式
    private User currentUser; // 当前用户字段

    // 分页功能变量
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalRecords = 0;
    private int totalPages = 1;
    private JLabel lblPaginationInfo;
    private JButton btnPrevPage, btnNextPage;
    private JTextField txtGotoPage;
    private JButton btnGoto;

    // 图标路径常量
    private static final String ICON_PATH = "D:/Java/OfficeManagement/icons/";

    public DepartmentPanel(boolean isReadOnly, User currentUser) {
        this.isReadOnly = isReadOnly;
        this.currentUser = currentUser;
        initUI();
        loadTableData(); // 初始化加载数据
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // 表格列名
        String[] columns = {"部门ID", "部门名称"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false;  // 不允许编辑表格
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 创建主内容面板，包含表格和分页控件
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(new JScrollPane(table), BorderLayout.CENTER);

        // 初始化分页控件并添加到表格下方
        JPanel paginationPanel = initPaginationControls();
        mainContentPanel.add(paginationPanel, BorderLayout.SOUTH);

        add(mainContentPanel, BorderLayout.CENTER);

        // 底部按钮面板
        JPanel bottomPanel = new JPanel();
        JButton btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        JButton btnAdd = new JButton("新增", createScaledIcon("add.jpeg"));
        JButton btnEdit = new JButton("编辑", createScaledIcon("edit.jpeg"));
        JButton btnDelete = new JButton("删除", createScaledIcon("delete.jpeg"));
        JButton btnBack = new JButton("返回主页", createScaledIcon("home.jpeg"));

        // 根据权限设置按钮的可见性
        if (currentUser != null) {
            btnAdd.setVisible(currentUser.hasPermission("department_add"));
            btnEdit.setVisible(currentUser.hasPermission("department_edit"));
            btnDelete.setVisible(currentUser.hasPermission("department_delete"));
        }

        // 添加按钮
        bottomPanel.add(btnRefresh);
        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnBack);

        add(bottomPanel, BorderLayout.SOUTH);

        // 按钮事件绑定
        btnRefresh.addActionListener(e -> {
            currentPage = 1; // 刷新时回到第一页
            loadTableData();
        });

        btnAdd.addActionListener(e -> {
            if (!currentUser.hasPermission("department_add")) {
                JOptionPane.showMessageDialog(this, "您没有新增部门的权限");
                return;
            }

            String name = JOptionPane.showInputDialog(this, "请输入部门名称：");
            if (name != null && !name.trim().isEmpty()) {
                Department d = new Department();
                d.setDepartmentName(name.trim());
                if (departmentService.addDepartment(d)) {
                    JOptionPane.showMessageDialog(this, "添加成功");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "添加失败");
                }
            }
        });

        btnEdit.addActionListener(e -> {
            if (!currentUser.hasPermission("department_edit")) {
                JOptionPane.showMessageDialog(this, "您没有编辑部门的权限");
                return;
            }

            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String oldName = (String) tableModel.getValueAt(row, 1);
            String newName = JOptionPane.showInputDialog(this, "修改部门名称：", oldName);
            if (newName != null && !newName.trim().isEmpty()) {
                Department d = new Department();
                d.setDepartmentId(id);
                d.setDepartmentName(newName.trim());
                if (departmentService.updateDepartment(d)) {
                    JOptionPane.showMessageDialog(this, "修改成功");
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "修改失败");
                }
            }
        });

        btnDelete.addActionListener(e -> {
            if (!currentUser.hasPermission("department_delete")) {
                JOptionPane.showMessageDialog(this, "您没有删除部门的权限");
                return;
            }

            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请选择一条记录");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "确认删除该部门吗？", "提示", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (departmentService.deleteDepartment(id)) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    // 如果删除后当前页没有数据，返回上一页
                    if (tableModel.getRowCount() <= 1 && currentPage > 1) {
                        currentPage--;
                    }
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败");
                }
            }
        });

        btnBack.addActionListener(e -> backToHome());
    }

    // 初始化分页控件
    private JPanel initPaginationControls() {
        JPanel paginationPanel = new JPanel();
        paginationPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 5));
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        btnPrevPage = new JButton("上一页");
        btnNextPage = new JButton("下一页");
        lblPaginationInfo = new JLabel("第 1 页 / 共 1 页");
        txtGotoPage = new JTextField(3);
        btnGoto = new JButton("跳转");

        // 设置组件尺寸
        Dimension btnSize = new Dimension(80, 25);
        btnPrevPage.setPreferredSize(btnSize);
        btnNextPage.setPreferredSize(btnSize);
        btnGoto.setPreferredSize(btnSize);

        // 添加组件
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(btnNextPage);
        paginationPanel.add(lblPaginationInfo);
        paginationPanel.add(new JLabel("前往:"));
        paginationPanel.add(txtGotoPage);
        paginationPanel.add(btnGoto);

        // 添加事件监听器
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadTableData();
            }
        });

        btnNextPage.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadTableData();
            }
        });

        btnGoto.addActionListener(e -> {
            try {
                int page = Integer.parseInt(txtGotoPage.getText());
                if (page >= 1 && page <= totalPages) {
                    currentPage = page;
                    loadTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "页码超出范围 (1-" + totalPages + ")");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "请输入有效的页码数字");
            }
        });

        return paginationPanel;
    }

    // 更新分页信息
    private void updatePaginationInfo() {
        lblPaginationInfo.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页 （共 " + totalRecords + " 条）");
    }

    // 图标缩放方法
    private ImageIcon createScaledIcon(String filename) {
        ImageIcon originalIcon = new ImageIcon(ICON_PATH + filename);
        if (originalIcon.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    private void loadTableData() {
        try {
            // 获取所有部门数据
            List<Department> allDepartments = departmentService.getAllDepartments();
            totalRecords = allDepartments != null ? allDepartments.size() : 0;

            // 计算总页数
            totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (totalPages == 0) totalPages = 1; // 确保至少有一页

            // 校正当前页码
            if (currentPage < 1) currentPage = 1;
            if (currentPage > totalPages) currentPage = totalPages;

            // 计算当前页数据范围
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalRecords);

            // 清空表格并添加当前页数据
            tableModel.setRowCount(0);
            if (allDepartments != null && !allDepartments.isEmpty()) {
                for (int i = start; i < end; i++) {
                    Department d = allDepartments.get(i);
                    tableModel.addRow(new Object[]{d.getDepartmentId(), d.getDepartmentName()});
                }
            }

            // 更新分页信息
            updatePaginationInfo();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "加载数据失败: " + e.getMessage());
            e.printStackTrace();
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




