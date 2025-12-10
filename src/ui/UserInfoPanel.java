package ui;

import model.Permission;
import model.User;
import service.PermissionService;
import service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class UserInfoPanel extends JPanel {
    private User currentUser;
    private UserService userService;
    private boolean isReadOnly;  // 是否为只读模式
    private PermissionService permissionService; // 添加权限服务字段

    private JTextField txtName;
    private JPasswordField txtPassword;
    private JTextField txtDepartment;
    private JTextField txtRole;

    private JButton btnSave;
    private JButton btnDelete;  // 删除按钮
    private JButton btnAdd;     // 新增按钮
    private JButton btnSearch;  // 搜索按钮
    private JButton btnEdit;    // 编辑按钮
    private JButton btnRefresh; // 刷新按钮
    // 新增返回主页按钮
    private JButton btnHome;
    private JTable usersTable;  // 用于展示所有用户信息的表格
    private JScrollPane scrollPane;
    private JButton btnStatistics;
    private ImageIcon eyeIcon;
    private ImageIcon lockIcon;

    // 新增图标路径常量
    private static final String ICON_PATH = "C:/Users/hsh20/eclipse-workspace/OfficeManagement/icons/";


    // 修改构造器
    public UserInfoPanel(User currentUser, UserService userService, boolean isReadOnly, PermissionService permissionService) {
        this.currentUser = currentUser;
        this.userService = userService;
        this.isReadOnly = isReadOnly;
        this.permissionService = permissionService;

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 显示用户信息
        if (currentUser.getRoleId() == 1) {  // 管理员
            displayAllUsers(gbc);
        } else {  // 普通用户
            displayUserInfo(gbc);
        }
    }

    // 管理员查看所有用户
    private void displayAllUsers(GridBagConstraints gbc) {
        JLabel lblTitle = new JLabel("所有用户信息");
        lblTitle.setFont(new Font("宋体", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3; // 增加列跨度
        add(lblTitle, gbc);

        // 用户表头
        String[] columns = {"用户ID", "用户名", "密码", "部门", "角色"};
        List<User> userList = userService.getAllUsers();

        // 用户数据
        Object[][] data = new Object[userList.size()][5];
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            data[i][0] = user.getUserId();
            data[i][1] = user.getName();
            data[i][2] = user.getPassword();
            data[i][3] = user.getDepartmentId();  // 显示部门ID
            data[i][4] = user.getRoleId() == 1 ? "管理员" : "普通用户";  // 根据角色显示管理员或普通用户
        }

        usersTable = new JTable(data, columns);
        scrollPane = new JScrollPane(usersTable);
        scrollPane.setPreferredSize(new Dimension(900, 200));  // 设置表格大小
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 3; // 增加列跨度
        add(scrollPane, gbc);

        // 按钮面板 - 使用GridBagLayout布局
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcButton = new GridBagConstraints();
        gbcButton.insets = new Insets(5, 10, 5, 10);
        gbcButton.fill = GridBagConstraints.HORIZONTAL;

        // 新增按钮
        btnAdd = new JButton("新增用户", createScaledIcon("add.jpeg"));
        gbcButton.gridx = 0; gbcButton.gridy = 0;
        buttonPanel.add(btnAdd, gbcButton);

        // 编辑按钮 (新增)
        btnEdit = new JButton("编辑用户", createScaledIcon("edit.jpeg"));
        gbcButton.gridx = 1; gbcButton.gridy = 0;
        buttonPanel.add(btnEdit, gbcButton);

        // 删除按钮
        btnDelete = new JButton("删除用户", createScaledIcon("delete.jpeg"));
        gbcButton.gridx = 2; gbcButton.gridy = 0;
        buttonPanel.add(btnDelete, gbcButton);

        // 搜索按钮
        btnSearch = new JButton("搜索用户", createScaledIcon("search.jpeg"));
        gbcButton.gridx = 3; gbcButton.gridy = 0;
        buttonPanel.add(btnSearch, gbcButton);

        // 刷新按钮
        btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        gbcButton.gridx = 4; gbcButton.gridy = 0;
        buttonPanel.add(btnRefresh, gbcButton);

        // 新增返回主页按钮
        btnHome = new JButton("返回主页", createScaledIcon("home.jpeg"));
        gbcButton.gridx = 5; gbcButton.gridy = 0;
        buttonPanel.add(btnHome, gbcButton);


        // 在按钮面板中添加统计按钮
        btnStatistics = new JButton("部门员工统计", createScaledIcon("statistics.jpeg"));
        gbcButton.gridx = 6; gbcButton.gridy = 0;
        buttonPanel.add(btnStatistics, gbcButton);

        // 添加按钮面板到主面板
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        add(buttonPanel, gbc);

        // 设置按钮的事件监听
        btnAdd.addActionListener(e -> addUser());
        btnEdit.addActionListener(e -> editUser()); // 编辑按钮事件
        btnDelete.addActionListener(e -> deleteUser());
        btnSearch.addActionListener(e -> searchUser());
        btnRefresh.addActionListener(e -> refreshUserList()); // 刷新按钮事件
        btnHome.addActionListener(e -> returnToHome());
        btnStatistics.addActionListener(e -> showDepartmentStatistics());
    }



    // 普通用户查看信息，带密码显示/隐藏按钮
    private void displayUserInfo(GridBagConstraints gbc) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> displayUserInfo(gbc));
            return;
        }

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(500, 400));

        JPanel infoPanel = new JPanel(new GridBagLayout());
        GridBagConstraints infoGbc = new GridBagConstraints();
        infoGbc.insets = new Insets(5, 5, 5, 5);
        infoGbc.fill = GridBagConstraints.HORIZONTAL;
        infoGbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        JLabel lblName = new JLabel("用户名:");
        infoGbc.gridx = 0;
        infoGbc.gridy = row;
        infoPanel.add(lblName, infoGbc);
        txtName = new JTextField(currentUser.getName(), 15);
        txtName.setEditable(false);
        infoGbc.gridx = 1;
        infoPanel.add(txtName, infoGbc);
        row++;

        // 密码 + 显示/隐藏按钮
        JLabel lblPassword = new JLabel("密码:");
        infoGbc.gridx = 0;
        infoGbc.gridy = row;
        infoPanel.add(lblPassword, infoGbc);

        JPanel passwordCombo = new JPanel(new BorderLayout());
        passwordCombo.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
        txtPassword = new JPasswordField(currentUser.getPassword(), 12);
        txtPassword.setEchoChar('•');
        txtPassword.setEditable(false);

        JButton showBtn = new JButton();
        if (eyeIcon != null && eyeIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            showBtn.setIcon(eyeIcon);
        } else {
            showBtn.setText("👁️");
            showBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        }
        showBtn.setPreferredSize(new Dimension(35, 25));
        showBtn.addActionListener(e -> togglePassword(showBtn));

        passwordCombo.add(txtPassword, BorderLayout.CENTER);
        passwordCombo.add(showBtn, BorderLayout.EAST);
        infoGbc.gridx = 1;
        infoPanel.add(passwordCombo, infoGbc);
        row++;

        // 部门、角色、权限...（原逻辑保留）
        JLabel lblDepartment = new JLabel("部门:");
        infoGbc.gridx = 0;
        infoGbc.gridy = row;
        infoPanel.add(lblDepartment, infoGbc);
        txtDepartment = new JTextField(String.valueOf(currentUser.getDepartmentId()));
        txtDepartment.setEditable(false);
        txtDepartment.setPreferredSize(new Dimension(200, 25));
        infoGbc.gridx = 1;
        infoPanel.add(txtDepartment, infoGbc);
        row++;

        JLabel lblRole = new JLabel("角色:");
        infoGbc.gridx = 0;
        infoGbc.gridy = row;
        infoPanel.add(lblRole, infoGbc);
        txtRole = new JTextField(currentUser.getRoleId() == 1 ? "管理员" : "普通用户");
        txtRole.setEditable(false);
        txtRole.setPreferredSize(new Dimension(200, 25));
        infoGbc.gridx = 1;
        infoPanel.add(txtRole, infoGbc);

        JPanel permissionPanel = new JPanel(new BorderLayout());
        permissionPanel.setBorder(BorderFactory.createTitledBorder("权限列表"));
        DefaultListModel<String> listModel = new DefaultListModel<>();
        List<Permission> permissions = permissionService.getPermissionsByUserId(currentUser.getUserId());
        for (Permission permission : permissions) {
            listModel.addElement(permission.getDescription());
        }
        JList<String> permissionsList = new JList<>(listModel);
        permissionsList.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(permissionsList);
        scrollPane.setPreferredSize(new Dimension(300, 150));
        permissionPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnHome = new JButton("返回主页", createScaledIcon("home.jpeg"));
        bottomPanel.add(btnHome);
        btnHome.addActionListener(e -> returnToHome());

        mainPanel.add(infoPanel, BorderLayout.NORTH);
        mainPanel.add(permissionPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(mainPanel, gbc);

        revalidate();
        repaint();
    }

    // 密码显示/隐藏切换逻辑
    private void togglePassword(JButton showBtn) {
        boolean isHidden = txtPassword.getEchoChar() == '•';
        if (isHidden) {
            txtPassword.setEchoChar((char) 0);
            if (lockIcon != null && lockIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                showBtn.setIcon(lockIcon);
            } else {
                showBtn.setText("🔒");
            }
        } else {
            txtPassword.setEchoChar('•');
            if (eyeIcon != null && eyeIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                showBtn.setIcon(eyeIcon);
            } else {
                showBtn.setText("👁️");
            }
        }
    }

    // 新增图标缩放方法
    private ImageIcon createScaledIcon(String filename) {
        ImageIcon originalIcon = new ImageIcon(ICON_PATH + filename);
        if (originalIcon.getIconWidth() <= 0) {
            return new ImageIcon();
        }
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    // 新增返回主页方法
    private void returnToHome() {
        // 获取顶级窗口
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        if (topWindow instanceof AdminMainFrame) {
            ((AdminMainFrame) topWindow).backToHome();
        } else if (topWindow instanceof UserMainFrame) {
            ((UserMainFrame) topWindow).backToHome();
        } else {
            JOptionPane.showMessageDialog(this, "无法返回主页，请联系管理员");
        }
    }

    // 自定义图表面板（不依赖任何外部库）
    private class ChartPanel extends JPanel {
        private final Map<String, Integer> stats;
        private final Map<Rectangle, String> barMap = new HashMap<>();
        private final Color[] colors = {
                new Color(79, 129, 189),   // 蓝色
                new Color(155, 187, 89),   // 绿色
                new Color(192, 80, 77),    // 红色
                new Color(128, 100, 162),  // 紫色
                new Color(75, 172, 198),   // 浅蓝
                new Color(247, 150, 70)    // 橙色
        };

        public ChartPanel(Map<String, Integer> stats) {
            this.stats = stats;
            setBackground(Color.WHITE);

            // 添加鼠标点击事件
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    for (Map.Entry<Rectangle, String> entry : barMap.entrySet()) {
                        if (entry.getKey().contains(e.getPoint())) {
                            showDepartmentEmployees(entry.getValue());
                            break;
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            barMap.clear();

            int width = getWidth();
            int height = getHeight();
            int padding = 50;
            int chartWidth = width - 2 * padding;
            int chartHeight = height - 2 * padding;

            // 绘制标题
            g.setColor(Color.BLACK);
            g.setFont(new Font("宋体", Font.BOLD, 16));
            drawCenteredString(g, "部门员工数量统计", width / 2, 30);

            // 绘制坐标轴
            g.drawLine(padding, height - padding, width - padding, height - padding); // X轴
            g.drawLine(padding, height - padding, padding, padding); // Y轴

            // 绘制轴标签
            g.setFont(new Font("宋体", Font.PLAIN, 14));
            drawCenteredString(g, "部门名称", width / 2, height - 10);

            // 旋转绘制Y轴标签
            Graphics2D g2d = (Graphics2D) g;
            AffineTransform orig = g2d.getTransform();
            g2d.rotate(-Math.PI / 2);
            drawCenteredString(g2d, "员工数量", -height / 2 - 30, 10);
            g2d.setTransform(orig);

            // 查找最大值
            int maxCount = stats.values().stream().max(Integer::compare).orElse(1);

            // 绘制刻度
            g.setFont(new Font("宋体", Font.PLAIN, 12));
            int tickCount = Math.min(10, maxCount);
            for (int i = 0; i <= tickCount; i++) {
                int value = i * maxCount / tickCount;
                int y = height - padding - (int) ((double) value / maxCount * chartHeight);

                // 绘制刻度线
                g.drawLine(padding - 5, y, padding, y);

                // 绘制刻度值
                g.drawString(String.valueOf(value), padding - 40, y + 5);
            }

            // 绘制柱状图
            int i = 0;
            int colorIndex = 0;
            int barSpacing = 20; // 柱子之间的间距
            int barWidth = (chartWidth - barSpacing * (stats.size() - 1)) / stats.size();

            for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                String dept = entry.getKey();
                int count = entry.getValue();

                // 计算柱状图位置和高度
                int barHeight = (int) ((double) count / maxCount * chartHeight);
                int x = padding + i * (barWidth + barSpacing);
                int y = height - padding - barHeight;

                // 绘制柱状图
                Color barColor = colors[colorIndex % colors.length];
                g.setColor(barColor);
                g.fillRect(x, y, barWidth, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, barWidth, barHeight);

                // 存储柱状图位置用于点击检测
                barMap.put(new Rectangle(x, y, barWidth, barHeight), dept);

                // 绘制部门名称
                drawCenteredString(g, dept, x + barWidth / 2, height - padding + 20);

                // 绘制员工数量
                drawCenteredString(g, String.valueOf(count), x + barWidth / 2, y - 10);

                i++;
                colorIndex++;
            }
        }

        // 辅助方法：居中绘制字符串
        private void drawCenteredString(Graphics g, String text, int x, int y) {
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g.drawString(text, x - textWidth / 2, y);
        }

        // 重载方法用于旋转后的绘图
        private void drawCenteredString(Graphics2D g2d, String text, int x, int y) {
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, x - textWidth / 2, y);
        }
    }

    // 显示部门员工详情（保持不变）
    private void showDepartmentEmployees(String departmentName) {
        // 从数据库获取部门员工列表
        List<User> employees = userService.getUsersByDepartmentName(departmentName);

        if (employees == null || employees.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该部门暂无员工信息");
            return;
        }

        // 创建员工列表模型
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (User user : employees) {
            listModel.addElement(user.getName() + " (ID: " + user.getUserId() + ")");
        }

        // 创建员工列表
        JList<String> employeeList = new JList<>(listModel);
        employeeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        employeeList.setFont(new Font("宋体", Font.PLAIN, 14));

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(employeeList);
        scrollPane.setPreferredSize(new Dimension(300, 200));

        // 创建详情对话框
        JDialog employeeDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "部门员工详情", true);
        employeeDialog.setLayout(new BorderLayout());

        // 添加标题
        JLabel titleLabel = new JLabel(departmentName + " 部门员工列表");
        titleLabel.setFont(new Font("宋体", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        employeeDialog.add(titleLabel, BorderLayout.NORTH);

        // 添加员工列表
        employeeDialog.add(scrollPane, BorderLayout.CENTER);

        // 添加关闭按钮
        JButton btnClose = new JButton("关闭");
        btnClose.addActionListener(e -> employeeDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnClose);
        employeeDialog.add(buttonPanel, BorderLayout.SOUTH);

        employeeDialog.pack();
        employeeDialog.setLocationRelativeTo(this);
        employeeDialog.setVisible(true);
    }

    private void showDepartmentStatistics() {
        Map<String, Integer> departmentStats = userService.getDepartmentEmployeeCount();

        if (departmentStats == null || departmentStats.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有可用的部门员工统计数据");
            return;
        }

        // 创建统计结果对话框
        JDialog statisticsDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "部门员工统计", true);
        statisticsDialog.setLayout(new BorderLayout());
        statisticsDialog.setSize(800, 600);

        // 创建绘图面板
        JPanel chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                int width = getWidth();
                int height = getHeight();
                int padding = 50;
                int chartWidth = width - 2 * padding;
                int chartHeight = height - 2 * padding;

                // 绘制坐标轴
                g.setColor(Color.BLACK);
                g.drawLine(padding, height - padding, width - padding, height - padding); // X轴
                g.drawLine(padding, height - padding, padding, padding); // Y轴

                // 绘制标题
                g.setFont(new Font("宋体", Font.BOLD, 16));
                g.drawString("部门员工数量统计", width / 2 - 70, 30);

                // 绘制轴标签
                g.setFont(new Font("宋体", Font.PLAIN, 14));
                g.drawString("部门名称", width / 2 - 30, height - 10);

                // 查找最大值
                int maxCount = 0;
                for (int count : departmentStats.values()) {
                    if (count > maxCount) maxCount = count;
                }

                // 绘制柱状图
                int barWidth = chartWidth / (departmentStats.size() * 2);
                int i = 0;
                int colorIndex = 0;
                Color[] colors = {new Color(79, 129, 189), new Color(155, 187, 89),
                        new Color(192, 80, 77), new Color(128, 100, 162),
                        new Color(75, 172, 198), new Color(247, 150, 70)};

                // 存储部门位置信息（用于点击检测）
                Map<Rectangle, String> departmentRects = new HashMap<>();

                for (Map.Entry<String, Integer> entry : departmentStats.entrySet()) {
                    String dept = entry.getKey();
                    int count = entry.getValue();

                    // 计算柱状图位置和高度
                    int barHeight = (int) ((double) count / maxCount * chartHeight);
                    int x = padding + barWidth / 2 + i * (barWidth * 2);
                    int y = height - padding - barHeight;

                    // 绘制柱状图
                    g.setColor(colors[colorIndex % colors.length]);
                    g.fillRect(x, y, barWidth, barHeight);
                    g.setColor(Color.BLACK);
                    g.drawRect(x, y, barWidth, barHeight);

                    // 存储部门位置
                    departmentRects.put(new Rectangle(x, y, barWidth, barHeight), dept);

                    // 绘制部门名称
                    g.setFont(new Font("宋体", Font.PLAIN, 12));
                    g.drawString(dept, x - barWidth / 2, height - padding + 20);

                    // 绘制员工数量
                    g.drawString(String.valueOf(count), x + barWidth / 2 - 5, y - 5);

                    i++;
                    colorIndex++;
                }

                // 添加点击事件监听器
                this.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        Point clickPoint = e.getPoint();
                        for (Map.Entry<Rectangle, String> entry : departmentRects.entrySet()) {
                            if (entry.getKey().contains(clickPoint)) {
                                showDepartmentEmployees(entry.getValue());
                                break;
                            }
                        }
                    }
                });
            }
        };

        statisticsDialog.add(chartPanel, BorderLayout.CENTER);

        // 添加关闭按钮
        JButton btnClose = new JButton("关闭");
        btnClose.addActionListener(ev -> statisticsDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnClose);
        statisticsDialog.add(buttonPanel, BorderLayout.SOUTH);

        statisticsDialog.setLocationRelativeTo(this);
        statisticsDialog.setVisible(true);
    }

    // 显示用户详细信息
    private void showUserDetails(int userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "用户信息不存在");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(5, 2, 5, 5));
        panel.add(new JLabel("用户ID:"));
        panel.add(new JLabel(String.valueOf(user.getUserId())));
        panel.add(new JLabel("用户名:"));
        panel.add(new JLabel(user.getName()));
        panel.add(new JLabel("部门:"));
        panel.add(new JLabel(String.valueOf(user.getDepartmentId())));
        panel.add(new JLabel("角色:"));
        panel.add(new JLabel(user.getRoleId() == 1 ? "管理员" : "普通用户"));
        panel.add(new JLabel("密码:"));
        panel.add(new JLabel(user.getPassword()));

        JOptionPane.showMessageDialog(this, panel, "用户详情", JOptionPane.INFORMATION_MESSAGE);
    }


    // 编辑用户信息 (新增方法)
    private void editUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户！");
            return;
        }

        int userId = (int) usersTable.getValueAt(selectedRow, 0);
        User user = userService.getUserById(userId);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "用户不存在！");
            return;
        }

        // 创建编辑对话框
        JPanel editPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        JTextField txtEditName = new JTextField(user.getName());
        JPasswordField txtEditPassword = new JPasswordField(user.getPassword());
        JTextField txtEditDepartment = new JTextField(String.valueOf(user.getDepartmentId()));
        JTextField txtEditRole = new JTextField(String.valueOf(user.getRoleId()));

        editPanel.add(new JLabel("用户名:"));
        editPanel.add(txtEditName);
        editPanel.add(new JLabel("密码:"));
        editPanel.add(txtEditPassword);
        editPanel.add(new JLabel("部门ID:"));
        editPanel.add(txtEditDepartment);
        editPanel.add(new JLabel("角色ID(1=管理员,2=普通用户):"));
        editPanel.add(txtEditRole);

        int result = JOptionPane.showConfirmDialog(
                this,
                editPanel,
                "编辑用户信息",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                // 获取编辑后的信息
                String newName = txtEditName.getText().trim();
                String newPassword = new String(txtEditPassword.getPassword());
                int newDepartmentId = Integer.parseInt(txtEditDepartment.getText().trim());
                int newRoleId = Integer.parseInt(txtEditRole.getText().trim());

                // 验证输入
                if (newName.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "用户名不能为空！");
                    return;
                }

                if (newPassword.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "密码不能为空！");
                    return;
                }

                if (newRoleId != 1 && newRoleId != 2) {
                    JOptionPane.showMessageDialog(this, "角色ID必须是1(管理员)或2(普通用户)！");
                    return;
                }

                // 更新用户信息
                user.setName(newName);
                user.setPassword(newPassword);
                user.setDepartmentId(newDepartmentId);
                user.setRoleId(newRoleId);

                if (userService.updateUser(user)) {
                    JOptionPane.showMessageDialog(this, "用户信息更新成功！");
                    refreshUserList(); // 刷新用户列表
                } else {
                    JOptionPane.showMessageDialog(this, "更新失败！");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "部门ID和角色ID必须是数字！");
            }
        }
    }

    // 刷新用户列表
    private void refreshUserList() {
        List<User> userList = userService.getAllUsers();

        // 用户数据
        Object[][] data = new Object[userList.size()][5];
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            data[i][0] = user.getUserId();
            data[i][1] = user.getName();
            data[i][2] = user.getPassword();
            data[i][3] = user.getDepartmentId();
            data[i][4] = user.getRoleId() == 1 ? "管理员" : "普通用户";
        }

        // 更新表格模型
        usersTable.setModel(new javax.swing.table.DefaultTableModel(
                data,
                new String[]{"用户ID", "用户名", "密码", "部门", "角色"}
        ));

        JOptionPane.showMessageDialog(this, "用户列表已刷新！");
    }

    // 管理员删除用户
    private void deleteUser() {
        int selectedRow = usersTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户！");
            return;
        }
        int userId = (int) usersTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "确认删除该用户吗？", "删除确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "用户删除成功！");
                refreshUserList(); // 删除后刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！");
            }
        }
    }

    // 管理员新增用户
    private void addUser() {
        String name = JOptionPane.showInputDialog(this, "请输入用户名:");
        String password = JOptionPane.showInputDialog(this, "请输入密码:");
        String departmentIdStr = JOptionPane.showInputDialog(this, "请输入部门ID:");
        String roleIdStr = JOptionPane.showInputDialog(this, "请输入角色ID (1: 管理员, 2: 普通用户):");

        if (name == null || password == null || departmentIdStr == null || roleIdStr == null) {
            JOptionPane.showMessageDialog(this, "输入信息不完整！");
            return;
        }

        try {
            int departmentId = Integer.parseInt(departmentIdStr);
            int roleId = Integer.parseInt(roleIdStr);

            User newUser = new User(name, password, departmentId, roleId);
            if (userService.addUser(newUser)) {
                JOptionPane.showMessageDialog(this, "用户新增成功！");
                refreshUserList(); // 新增后自动刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "用户新增失败！");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "部门ID或角色ID格式错误！");
        }
    }

    // 管理员根据用户名查询用户
    private void searchUser() {
        String searchName = JOptionPane.showInputDialog(this, "请输入要查询的用户名:");
        if (searchName != null && !searchName.trim().isEmpty()) {
            List<User> users = userService.searchUsersByName(searchName);
            if (users.isEmpty()) {
                JOptionPane.showMessageDialog(this, "没有找到匹配的用户！");
                return;
            }

            // 更新表格数据
            Object[][] data = new Object[users.size()][5];
            for (int i = 0; i < users.size(); i++) {
                User user = users.get(i);
                data[i][0] = user.getUserId();
                data[i][1] = user.getName();
                data[i][2] = user.getPassword();
                data[i][3] = user.getDepartmentId();
                data[i][4] = user.getRoleId() == 1 ? "管理员" : "普通用户";
            }

            usersTable.setModel(new JTable(data, new String[]{"用户ID", "用户名", "密码", "部门", "角色"}).getModel());
        } else {
            JOptionPane.showMessageDialog(this, "用户名不能为空！");
        }
    }
}





