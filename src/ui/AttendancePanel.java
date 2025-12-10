package ui;

import model.Attendance;
import model.User;
import service.AttendanceService;
import util.AuthUtil;
import util.DateUtil;
import dao.UserDao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.sql.Time;
import java.util.*;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final User currentUser;
    private final AttendanceService service;
    private final UserDao userDao;
    private JTable attendanceTable;
    private DefaultTableModel tableModel;
    private JButton addBtn, editBtn, deleteBtn;
    private Map<Integer, String> userNameCache = new HashMap<>();
    private JDateChooser dateChooser;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

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

    public AttendancePanel(User currentUser, AttendanceService attendanceService) {
        this.currentUser = currentUser;
        this.service = attendanceService;
        this.userDao = new UserDao();
        initializeUI();
        loadInitialData();
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

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 顶部工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        // 刷新按钮
        JButton refreshBtn = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        refreshBtn.addActionListener(e -> loadInitialData());
        toolPanel.add(refreshBtn);

        // 添加统计按钮
        JButton statsBtn = new JButton("考勤统计", createScaledIcon("statistics.jpg"));
        statsBtn.addActionListener(e -> showAttendanceRateChart());
        toolPanel.add(statsBtn);

        // 功能按钮
        addBtn = new JButton("新增", createScaledIcon("add.jpeg"));
        editBtn = new JButton("编辑", createScaledIcon("edit.jpeg"));
        deleteBtn = new JButton("删除", createScaledIcon("delete.jpeg"));

        // 设置按钮事件监听
        addBtn.addActionListener(this::handleAdd);
        editBtn.addActionListener(this::handleEdit);
        deleteBtn.addActionListener(this::handleDelete);

        // 根据权限设置按钮可见性
        addBtn.setVisible(currentUser.hasPermission("attendance_add"));
        editBtn.setVisible(currentUser.hasPermission("attendance_edit"));
        deleteBtn.setVisible(currentUser.hasPermission("attendance_delete"));

        toolPanel.add(addBtn);
        toolPanel.add(editBtn);
        toolPanel.add(deleteBtn);

        // 中间表格区域
        String[] columns = {"ID", "员工ID", "员工姓名", "日期", "开始时间", "结束时间", "状态", "工时"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        attendanceTable = new JTable(tableModel);
        attendanceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        attendanceTable.setAutoCreateRowSorter(true);

        // 创建主面板容器
        JPanel mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.add(new JScrollPane(attendanceTable), BorderLayout.CENTER);

        // 初始化分页控件并添加到表格下方
        JPanel paginationPanel = initPaginationControls();
        mainContentPanel.add(paginationPanel, BorderLayout.SOUTH);

        // 创建底部查询面板
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        // 快速查询面板
        JPanel quickSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton allRecordsBtn = createButton("全部记录", e -> loadAllRecords());
        JButton myRecordsBtn = createButton("我的记录", e -> loadMyRecords());
        quickSearchPanel.add(allRecordsBtn);
        quickSearchPanel.add(myRecordsBtn);

        // 高级查询面板
        JPanel advancedSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JComboBox<String> searchType = new JComboBox<>(new String[]{"按工号查询", "按状态查询"});
        JTextField searchField = new JTextField(15);

        searchType.addActionListener(e -> {
            if (!searchField.getText().trim().isEmpty()) {
                executeAdvancedSearch(
                        (String)searchType.getSelectedItem(),
                        searchField.getText()
                );
            }
        });

        JButton searchBtn = new JButton("查询", createScaledIcon("search.jpeg"));
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                searchField.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
                showErrorDialog("请输入查询内容");
            } else {
                searchField.setBorder(UIManager.getBorder("TextField.border"));
                executeAdvancedSearch(
                        (String)searchType.getSelectedItem(),
                        keyword
                );
            }
        });

        advancedSearchPanel.add(new JLabel("查询类型:"));
        advancedSearchPanel.add(searchType);
        advancedSearchPanel.add(searchField);
        advancedSearchPanel.add(searchBtn);

        // 日期查询面板
        JPanel dateSearchPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        // 单日查询行
        JPanel singleDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        singleDatePanel.add(new JLabel("单日查询:"));
        dateChooser = new JDateChooser();
        dateChooser.setDateFormat("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(120, 25));
        singleDatePanel.add(dateChooser);
        JButton dateQueryBtn = createButton("查询", e -> searchByDateInput());
        singleDatePanel.add(dateQueryBtn);

        // 日期范围查询行
        JPanel rangeDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        rangeDatePanel.add(new JLabel("范围查询:"));
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormat("yyyy-MM-dd");
        startDateChooser.setPreferredSize(new Dimension(120, 25));
        rangeDatePanel.add(startDateChooser);
        rangeDatePanel.add(new JLabel("至"));
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormat("yyyy-MM-dd");
        endDateChooser.setPreferredSize(new Dimension(120, 25));
        rangeDatePanel.add(endDateChooser);
        JButton dateRangeQueryBtn = createButton("查询", e -> searchByDateRangeInput());
        rangeDatePanel.add(dateRangeQueryBtn);

        dateSearchPanel.add(singleDatePanel);
        dateSearchPanel.add(rangeDatePanel);

        // 添加所有查询面板到主查询面板
        searchPanel.add(quickSearchPanel);
        searchPanel.add(advancedSearchPanel);
        searchPanel.add(dateSearchPanel);

        // 添加所有组件到主面板
        add(toolPanel, BorderLayout.NORTH);
        add(mainContentPanel, BorderLayout.CENTER);
        add(searchPanel, BorderLayout.SOUTH);
    }

    // 考勤统计饼状图面板
    private class PieChartPanel extends JPanel {
        private final Map<String, Integer> data;

        public PieChartPanel(Map<String, Integer> data) {
            this.data = data;
            setPreferredSize(new Dimension(400, 400));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (data == null || data.isEmpty()) {
                g.drawString("无数据", getWidth() / 2 - 20, getHeight() / 2);
                return;
            }

            // 计算总记录数
            int total = data.values().stream().mapToInt(Integer::intValue).sum();
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 饼图尺寸和位置
            int diameter = Math.min(getWidth(), getHeight()) - 50;
            int x = (getWidth() - diameter) / 2;
            int y = (getHeight() - diameter) / 2;

            // 饼图颜色
            Color[] colors = {Color.GREEN, Color.ORANGE, Color.RED, Color.BLUE, Color.MAGENTA};
            int colorIndex = 0;
            int startAngle = 0;

            // 绘制饼图和图例
            for (Map.Entry<String, Integer> entry : data.entrySet()) {
                String status = entry.getKey();
                int count = entry.getValue();
                int angle = (int) Math.round(count * 360.0 / total);

                g2.setColor(colors[colorIndex % colors.length]);
                g2.fill(new Arc2D.Double(x, y, diameter, diameter, startAngle, angle, Arc2D.PIE));

                // 绘制图例
                g2.fillRect(20, 30 + colorIndex * 30, 20, 20);
                g2.setColor(Color.BLACK);
                String label = String.format("%s: %.2f%% (%d)", status, count * 100.0 / total, count);
                g2.drawString(label, 50, 45 + colorIndex * 30);

                startAngle += angle;
                colorIndex++;
            }
        }
    }

    // 显示考勤统计饼状图
    private void showAttendanceRateChart() {
        try {
            // 1. 获取考勤数据
            List<Attendance> allData;
            if (AuthUtil.isAdmin(currentUser)) {
                allData = service.getAllAttendances(); // 管理员查看所有
            } else {
                allData = service.getAttendancesByUserId(currentUser.getUserId()); // 普通用户查看自己的
            }

            // 2. 统计各状态数量
            Map<String, Integer> pieData = new LinkedHashMap<>();
            for (Attendance att : allData) {
                String status = att.getStatus();
                if (status == null || status.trim().isEmpty()) {
                    status = "未知";
                }
                // 状态简化（合并同类状态）
                String simplifiedStatus = simplifyStatus(status);
                pieData.put(simplifiedStatus, pieData.getOrDefault(simplifiedStatus, 0) + 1);
            }

            if (pieData.isEmpty()) {
                JOptionPane.showMessageDialog(this, "没有考勤数据");
                return;
            }

            // 3. 显示饼状图对话框
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "考勤状态统计", true);
            dialog.getContentPane().add(new PieChartPanel(pieData));
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

        } catch (Exception ex) {
            showErrorDialog("生成统计图表失败: " + ex.getMessage());
        }
    }

    // 状态简化处理（合并相似状态）
    private String simplifyStatus(String status) {
        if (status.contains("迟到")) {
            return "迟到";
        } else if (status.contains("早退")) {
            return "早退";
        } else if (status.contains("缺勤")) {
            return "缺勤";
        } else if (status.contains("正常")) {
            return "正常";
        } else {
            return "其他";
        }
    }

    private void searchByDateInput() {
        Date date = dateChooser.getDate();
        if (date == null) {
            showErrorDialog("请选择日期");
            return;
        }
        searchByDate(date);
    }

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
                loadInitialData();
            }
        });

        btnNextPage.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadInitialData();
            }
        });

        btnGoto.addActionListener(e -> {
            try {
                int page = Integer.parseInt(txtGotoPage.getText());
                if (page >= 1 && page <= totalPages) {
                    currentPage = page;
                    loadInitialData();
                } else {
                    showErrorDialog("页码超出范围 (1-" + totalPages + ")");
                }
            } catch (NumberFormatException ex) {
                showErrorDialog("请输入有效的页码数字");
            }
        });

        return paginationPanel;
    }

    private void searchByDateRangeInput() {
        Date startDate = startDateChooser.getDate();
        Date endDate = endDateChooser.getDate();

        if (startDate == null || endDate == null) {
            showErrorDialog("请选择开始和结束日期");
            return;
        }

        if (startDate.after(endDate)) {
            showErrorDialog("开始日期不能晚于结束日期");
            return;
        }

        List<Attendance> result;
        if (AuthUtil.isAdmin(currentUser)) {
            result = service.getAttendancesByDateRange(startDate, endDate);
        } else {
            result = service.getAttendancesByDateRangeAndUser(startDate, endDate, currentUser.getUserId());
        }
        refreshTable(result);
    }

    private void searchByDate(Date date) {
        try {
            List<Attendance> result;
            if (AuthUtil.isAdmin(currentUser)) {
                result = service.getAttendancesByDate(date);
            } else {
                result = service.getAttendancesByDateAndUser(date, currentUser.getUserId());
            }
            refreshTable(result);
        } catch (Exception ex) {
            showErrorDialog("日期查询失败: " + ex.getMessage());
            refreshTable(Collections.emptyList());
        }
    }

    private void executeAdvancedSearch(String type, String keyword) {
        try {
            List<Attendance> result;
            switch(type) {
                case "按工号查询":
                    if (keyword == null || keyword.trim().isEmpty()) {
                        showErrorDialog("请输入工号");
                        return;
                    }

                    try {
                        int userId = Integer.parseInt(keyword.trim());
                        result = service.getAttendancesByUserId(userId);

                        if (result.isEmpty()) {
                            showMessageDialog("没有找到工号为 " + userId + " 的考勤记录");
                        }
                    } catch (NumberFormatException e) {
                        showErrorDialog("工号必须为数字");
                        return;
                    }
                    break;
                case "按状态查询":
                    if (!AuthUtil.isAdmin(currentUser)) {
                        throw new Exception("无权限按状态查询");
                    }

                    if (keyword == null || keyword.trim().isEmpty()) {
                        showErrorDialog("请输入状态关键词");
                        return;
                    }

                    result = service.getAttendancesByStatus(keyword.trim());

                    if (result.isEmpty()) {
                        showMessageDialog("没有找到状态包含 '" + keyword + "' 的记录");
                    }
                    break;
                default:
                    result = service.getAllAttendances();
            }
            refreshTable(result);
        } catch (Exception ex) {
            showErrorDialog("高级查询错误: " + ex.getMessage());
        }
    }

    private void loadInitialData() {
        if (AuthUtil.isAdmin(currentUser)) {
            loadAllRecords();
        } else {
            loadMyRecords();
        }
    }

    private void loadAllRecords() {
        try {
            List<Attendance> data = service.getAllAttendances();
            refreshTable(data);
        } catch (Exception e) {
            showErrorDialog("加载数据失败: " + e.getMessage());
        }
    }



    private void loadMyRecords() {
        try {
            List<Attendance> allData = service.getAttendancesByUserId(currentUser.getUserId());
            totalRecords = allData.size();
            totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            if (currentPage < 1) currentPage = 1;
            if (currentPage > totalPages) currentPage = totalPages;

            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalRecords);
            List<Attendance> pageData = allData.subList(start, end);
            refreshTable(pageData);
            updatePaginationInfo();
        } catch (Exception e) {
            showErrorDialog("加载数据失败: " + e.getMessage());
        }
    }


    private void updatePaginationInfo() {
        lblPaginationInfo.setText("第 " + currentPage + " 页 / 共 " + totalPages + " 页 （共 " + totalRecords + " 条）");
    }


    private void refreshTable(List<Attendance> data) {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Attendance> safeData = data != null ? data : Collections.emptyList();
                loadUserNames(safeData);
                tableModel.setRowCount(0);
                for (Attendance att : safeData) {
                    tableModel.addRow(new Object[]{
                            att.getId(),
                            att.getUserId(),
                            getUserName(att.getUserId()),
                            DateUtil.formatDate(att.getStartTime()),
                            formatTime(att.getStartTime()),
                            formatTime(att.getEndTime()),
                            att.getStatus(),
                            att.getWorkingHours()
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                showErrorDialog("刷新表格失败: " + e.getMessage());
            }
        });
    }

    private void loadUserNames(List<Attendance> attendances) {
        try {
            for (Attendance att : attendances) {
                if (!userNameCache.containsKey(att.getUserId())) {
                    User user = userDao.getUserById(att.getUserId());
                    userNameCache.put(att.getUserId(), user != null ? user.getName() : "未知用户");
                }
            }
        } catch (Exception e) {
            showErrorDialog("加载用户信息失败: " + e.getMessage());
        }
    }

    private String getUserName(int userId) {
        return userNameCache.getOrDefault(userId, "用户" + userId);
    }

    private String formatTime(Date time) {
        return time != null ?
                String.format("%02d:%02d", DateUtil.getHour(time), DateUtil.getMinute(time)) :
                "缺勤";
    }

    private void handleAdd(ActionEvent e) {
        if (!currentUser.hasPermission("attendance_add")) {
            showPermissionDenied();
            return;
        }
        showAddDialog();
    }

    private void handleEdit(ActionEvent e) {
        if (!currentUser.hasPermission("attendance_edit")) {
            showPermissionDenied();
            return;
        }

        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessageDialog("请先选择记录");
            return;
        }

        int attId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            Attendance att = service.getAttendanceById(attId);
            if (att.getUserId() != currentUser.getUserId() && !AuthUtil.isAdmin(currentUser)) {
                showPermissionDenied();
                return;
            }
            showEditDialog(att);
        } catch (Exception ex) {
            showErrorDialog("获取记录失败: " + ex.getMessage());
        }
    }

    private void handleDelete(ActionEvent e) {
        if (!currentUser.hasPermission("attendance_delete")) {
            showPermissionDenied();
            return;
        }

        int selectedRow = attendanceTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessageDialog("请先选择记录");
            return;
        }

        int attId = (int) tableModel.getValueAt(selectedRow, 0);
        int userId = (int) tableModel.getValueAt(selectedRow, 1);
        String userName = (String) tableModel.getValueAt(selectedRow, 2);
        String date = (String) tableModel.getValueAt(selectedRow, 3);
        String status = (String) tableModel.getValueAt(selectedRow, 6);

        try {
            Attendance att = service.getAttendanceById(attId);
            if (att.getUserId() != currentUser.getUserId() && !AuthUtil.isAdmin(currentUser)) {
                showPermissionDenied();
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    String.format("<html><b>确定删除以下考勤记录吗？</b><br><br>" +
                                    "员工姓名: <b>%s</b><br>" +
                                    "员工工号: %d<br>" +
                                    "考勤日期: %s<br>" +
                                    "考勤状态: %s</html>",
                            userName, userId, date, status),
                    "确认删除考勤记录",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                service.deleteAttendance(attId);
                loadInitialData();
                showMessageDialog(String.format("已成功删除 %s (工号:%d) 的考勤记录", userName, userId));
            }
        } catch (Exception ex) {
            showErrorDialog("删除失败: " + ex.getMessage());
        }
    }

    private void showAddDialog() {
        AttendanceDialog dialog = new AttendanceDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                null
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed() && dialog.getAttendance() != null) {
            try {
                service.addAttendance(dialog.getAttendance());
                loadInitialData();
                showMessageDialog("添加成功！");
            } catch (Exception ex) {
                showErrorDialog("添加失败: " + ex.getMessage());
            }
        }
        dialog.dispose();
    }

    private void showEditDialog(Attendance attendance) {
        AttendanceDialog dialog = new AttendanceDialog(
                (JFrame) SwingUtilities.getWindowAncestor(this),
                attendance
        );
        dialog.setVisible(true);

        if (dialog.isConfirmed() && dialog.getAttendance() != null) {
            try {
                service.updateAttendance(dialog.getAttendance());
                loadInitialData();
                showMessageDialog("更新成功！");
            } catch (Exception ex) {
                showErrorDialog("更新失败: " + ex.getMessage());
            }
        }
        dialog.dispose();
    }

    private JButton createButton(String text, ActionListener listener) {
        JButton button = new JButton(text);
        button.addActionListener(listener);
        return button;
    }

    private void showPermissionDenied() {
        JOptionPane.showMessageDialog(
                this,
                "操作失败：权限不足",
                "权限错误",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void showMessageDialog(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "错误",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static class AttendanceDialog extends JDialog {
        private final JTextField userIdField;
        private final JDateChooser dateChooser;
        private final JTextField startTimeField;
        private final JTextField endTimeField;
        private final JTextField statusField;
        private final JTextField workingTimeField;
        private final JTextArea notesArea;
        private boolean confirmed = false;
        private final Attendance attendance;

        public AttendanceDialog(JFrame parent, Attendance attendance) {
            super(parent, "考勤记录", true);
            this.attendance = attendance != null ? attendance : new Attendance();

            // 初始化组件
            userIdField = new JTextField(10);
            dateChooser = new JDateChooser();
            dateChooser.setDateFormat("yyyy-MM-dd");
            startTimeField = new JTextField(5);
            endTimeField = new JTextField(5);
            statusField = new JTextField(15);
            workingTimeField = new JTextField(10);
            notesArea = new JTextArea(5, 20);

            statusField.setEditable(false);
            workingTimeField.setEditable(false);
            notesArea.setLineWrap(true);

            initializeFields();
            setupLayout();
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            pack();
            setLocationRelativeTo(parent);
        }

        private void initializeFields() {
            if (attendance.getUserId() != 0) {
                userIdField.setText(String.valueOf(attendance.getUserId()));
            }

            if (attendance.getStartTime() != null) {
                dateChooser.setDate(attendance.getStartTime());
                startTimeField.setText(String.format("%02d:%02d",
                        DateUtil.getHour(attendance.getStartTime()),
                        DateUtil.getMinute(attendance.getStartTime())));

                if (attendance.getEndTime() != null) {
                    endTimeField.setText(String.format("%02d:%02d",
                            DateUtil.getHour(attendance.getEndTime()),
                            DateUtil.getMinute(attendance.getEndTime())));
                }

                statusField.setText(attendance.getStatus());
                workingTimeField.setText(attendance.getWorkingHours());
            } else {
                // 设置默认值
                dateChooser.setDate(new Date());
                startTimeField.setText("09:00");
            }
        }

        private void setupLayout() {
            JPanel mainPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.anchor = GridBagConstraints.WEST;

            // 第一行：用户ID
            gbc.gridx = 0; gbc.gridy = 0;
            mainPanel.add(new JLabel("用户ID:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            mainPanel.add(userIdField, gbc);

            // 第二行：日期
            gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
            mainPanel.add(new JLabel("日期(yyyy-MM-dd):"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            mainPanel.add(dateChooser, gbc);

            // 第三行：开始时间
            gbc.gridx = 0; gbc.gridy = 2;
            mainPanel.add(new JLabel("开始时间(HH:mm):"), gbc);
            gbc.gridx = 1;
            mainPanel.add(startTimeField, gbc);

            // 第四行：结束时间
            gbc.gridx = 0; gbc.gridy = 3;
            mainPanel.add(new JLabel("结束时间(HH:mm):"), gbc);
            gbc.gridx = 1;
            mainPanel.add(endTimeField, gbc);

            // 第五行：状态
            gbc.gridx = 0; gbc.gridy = 4;
            mainPanel.add(new JLabel("状态:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            mainPanel.add(statusField, gbc);

            // 第六行：工作时长
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
            mainPanel.add(new JLabel("工作时长:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            mainPanel.add(workingTimeField, gbc);

            // 第七行：备注
            gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
            mainPanel.add(new JLabel("备注:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
            mainPanel.add(new JScrollPane(notesArea), gbc);

            // 第八行：按钮
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton("确定");
            okButton.addActionListener(e -> {
                if (validateInput()) {
                    confirmed = true;
                    updateAttendance();
                    setVisible(false);
                }
            });
            JButton cancelButton = new JButton("取消");
            cancelButton.addActionListener(e -> dispose());
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 4;
            gbc.fill = GridBagConstraints.CENTER;
            mainPanel.add(buttonPanel, gbc);

            add(mainPanel);
        }

        private boolean validateInput() {
            try {
                // 验证用户ID
                if (userIdField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "用户ID不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                attendance.setUserId(Integer.parseInt(userIdField.getText().trim()));

                // 验证日期和时间
                if (dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "请选择日期", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                Calendar cal = Calendar.getInstance();
                cal.setTime(dateChooser.getDate());

                Time startTime = Time.valueOf(startTimeField.getText() + ":00");
                cal.set(Calendar.HOUR_OF_DAY, startTime.getHours());
                cal.set(Calendar.MINUTE, startTime.getMinutes());
                attendance.setStartTime(cal.getTime());

                if (!endTimeField.getText().isEmpty()) {
                    Time endTime = Time.valueOf(endTimeField.getText() + ":00");
                    cal.set(Calendar.HOUR_OF_DAY, endTime.getHours());
                    cal.set(Calendar.MINUTE, endTime.getMinutes());
                    attendance.setEndTime(cal.getTime());
                } else {
                    attendance.setEndTime(null);
                }

                attendance.calculateWorkingTime();
                statusField.setText(attendance.getStatus());
                workingTimeField.setText(attendance.getWorkingHours());

                return true;
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "输入格式不正确: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        private void updateAttendance() {

        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Attendance getAttendance() {
            return attendance;
        }
    }
}











