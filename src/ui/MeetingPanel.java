package ui;

import model.Meeting;
import model.User;
import service.MeetingService;
import service.PermissionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class MeetingPanel extends JPanel {

    // 搜索面板组件
    private JDateChooser dateChooserMeeting;
    private JTextField txtContent, txtLocation, txtParticipant, txtRecorder;
    private JButton btnSearch, btnClear;

    // 表格区域
    private JTable table;
    private DefaultTableModel tableModel;

    // 编辑表单区组件
    private JPanel formPanel;
    private JDateChooser formDateChooser;
    private JTextField formContent, formLocation, formParticipant, formRecorder, formUserId;
    private JButton btnSave, btnCancel;

    // 操作按钮
    private JButton btnAdd, btnEdit, btnDelete;

    private JButton btnHome; // 新增的主页按钮
    private JButton btnRefresh; // 新增的刷新按钮

    private MeetingService meetingService;
    private User currentUser;
    private PermissionService permissionService;
    // 在类中添加成员变量记录最后选中的行
    private int lastSelectedRow = -1;

    // 当前编辑会议ID（-1表示新增）
    private int editingMeetingId = -1;

    // 图标路径前缀
    private static final String ICON_PATH = "D:/Java/OfficeManagement/icons/";

    // 修改构造方法
    public MeetingPanel(User currentUser, PermissionService permissionService) {
        this.currentUser = currentUser;
        this.permissionService = permissionService;
        meetingService = new MeetingService();
        initUI();
        loadTableData(null, null, null, null, null);
        switchFormVisible(false);  // 初始隐藏编辑表单

        // 根据权限设置按钮状态
        setButtonPermissions();    // 设置按钮可见性
        updateButtonsEnabledState(); // 设置按钮可用状态
    }

    // 新增方法：根据权限更新按钮的可用状态
    private void updateButtonsEnabledState() {
        btnAdd.setEnabled(hasPermission("meeting_add"));
        btnEdit.setEnabled(hasPermission("meeting_edit"));
        btnDelete.setEnabled(hasPermission("meeting_delete"));
        btnSearch.setEnabled(true); // 搜索按钮始终可用
        btnClear.setEnabled(true);  // 清除按钮始终可用
    }

    // 创建缩放图标
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

    private void setReadOnlyMode(boolean isReadOnly) {
        // 根据只读模式禁用相应按钮和表格
        btnAdd.setEnabled(!isReadOnly);
        btnEdit.setEnabled(!isReadOnly);
        btnDelete.setEnabled(!isReadOnly);
        table.setEnabled(!isReadOnly);
        btnSearch.setEnabled(!isReadOnly);
        btnClear.setEnabled(!isReadOnly);
        formPanel.setVisible(!isReadOnly);  // 隐藏编辑表单
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. 查询面板
        JPanel searchPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("查询条件"));

        searchPanel.add(new JLabel("会议日期:"));
        dateChooserMeeting = new JDateChooser();
        dateChooserMeeting.setDateFormat("yyyy-MM-dd");
        searchPanel.add(dateChooserMeeting);

        searchPanel.add(new JLabel("内容:"));
        txtContent = new JTextField();
        searchPanel.add(txtContent);

        searchPanel.add(new JLabel("地点:"));
        txtLocation = new JTextField();
        searchPanel.add(txtLocation);

        searchPanel.add(new JLabel("参与人员:"));
        txtParticipant = new JTextField();
        searchPanel.add(txtParticipant);

        searchPanel.add(new JLabel("记录人:"));
        txtRecorder = new JTextField();
        searchPanel.add(txtRecorder);

        btnSearch = new JButton("查询", createScaledIcon("search.jpeg"));
        btnSearch.addActionListener(e -> onSearch());
        searchPanel.add(btnSearch);

        btnClear = new JButton("清除", createScaledIcon("clear.jpeg"));
        btnClear.addActionListener(e -> {
            dateChooserMeeting.getTextField().setText("");
            txtContent.setText("");
            txtLocation.setText("");
            txtParticipant.setText("");
            txtRecorder.setText("");
        });
        searchPanel.add(btnClear);

        add(searchPanel, BorderLayout.NORTH);

        // 2. 中间主面板：表格 + 表单
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // 2.1 表格部分
        String[] columns = {"会议ID", "会议时间", "内容", "地点", "参与人员", "记录人", "用户ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) {
                return false; // 禁止编辑表格内容
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. 操作按钮面板（新增/编辑/删除）
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        // 左侧按钮：返回主页和刷新
        JPanel leftBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnHome = new JButton("返回主页", createScaledIcon("home.jpeg"));
        btnHome.addActionListener(e -> returnToHome());
        leftBtnPanel.add(btnHome);
        // 新增刷新按钮
        btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        btnRefresh.addActionListener(e -> onRefresh());
        leftBtnPanel.add(btnRefresh);

        btnPanel.add(leftBtnPanel, BorderLayout.WEST);

        // 右侧按钮：操作按钮
        JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnAdd = new JButton("新增", createScaledIcon("add.jpeg"));
        btnAdd.addActionListener(e -> onAdd());
        btnEdit = new JButton("编辑", createScaledIcon("edit.jpeg"));
        btnEdit.addActionListener(e -> onEdit());
        btnDelete = new JButton("删除", createScaledIcon("delete.jpeg"));
        btnDelete.addActionListener(e -> onDelete());

        rightBtnPanel.add(btnAdd);
        rightBtnPanel.add(btnEdit);
        rightBtnPanel.add(btnDelete);
        btnPanel.add(rightBtnPanel, BorderLayout.EAST);

        centerPanel.add(btnPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // 4. 右侧编辑表单区域
        formPanel = new JPanel(new GridLayout(7, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("会议编辑"));

        formPanel.add(new JLabel("会议日期:"));
        formDateChooser = new JDateChooser();
        formDateChooser.setDateFormat("yyyy-MM-dd");
        formPanel.add(formDateChooser);

        formPanel.add(new JLabel("内容:"));
        formContent = new JTextField();
        formPanel.add(formContent);

        formPanel.add(new JLabel("地点:"));
        formLocation = new JTextField();
        formPanel.add(formLocation);

        formPanel.add(new JLabel("参与人员:"));
        formParticipant = new JTextField();
        formPanel.add(formParticipant);

        formPanel.add(new JLabel("记录人:"));
        formRecorder = new JTextField();
        formPanel.add(formRecorder);

        formPanel.add(new JLabel("用户ID:"));
        formUserId = new JTextField();
        formPanel.add(formUserId);

        btnSave = new JButton("保存", createScaledIcon("save.jpeg"));
        btnSave.addActionListener(e -> onSave());
        btnCancel = new JButton("取消", createScaledIcon("cancel.jpeg"));
        btnCancel.addActionListener(e -> onCancel());

        formPanel.add(btnSave);
        formPanel.add(btnCancel);

        add(formPanel, BorderLayout.EAST);
    }

    // 添加权限检查方法
    private boolean hasPermission(String permissionName) {
        if (currentUser != null) {
            return currentUser.hasPermission(permissionName);
        }
        return false;
    }

    // 根据权限设置按钮可见性
    private void setButtonPermissions() {
        // 检查具体权限
        boolean hasAdd = hasPermission("meeting_add");
        boolean hasEdit = hasPermission("meeting_edit");
        boolean hasDelete = hasPermission("meeting_delete");

        // 设置按钮可见性
        btnAdd.setVisible(hasAdd);
        btnEdit.setVisible(hasEdit);
        btnDelete.setVisible(hasDelete);

        // 更新按钮可用状态
        updateButtonsEnabledState();
    }

    // 刷新方法 - 清空搜索条件并重新加载所有会议数据
    private void onRefresh() {
        // 清空搜索框
        dateChooserMeeting.getTextField().setText("");
        txtContent.setText("");
        txtLocation.setText("");
        txtParticipant.setText("");
        txtRecorder.setText("");

        // 重新加载后设置焦点
        loadTableData(null, null, null, null, null);
        table.requestFocusInWindow();
        if (table.getRowCount() > 0) {
            table.setRowSelectionInterval(0, 0);
        }

        // 显示刷新成功的提示信息
        JOptionPane.showMessageDialog(this, "数据已刷新，显示所有会议");
    }

    // 返回主页方法
    private void returnToHome() {
        // 获取顶层窗口
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        if (topWindow instanceof AdminMainFrame) {
            ((AdminMainFrame) topWindow).backToHome();
        } else if (topWindow instanceof UserMainFrame) {
            ((UserMainFrame) topWindow).backToHome();
        } else {
            JOptionPane.showMessageDialog(this, "无法返回主页，请联系管理员");
        }
    }

    // 修改表单切换方法，确保表单关闭后表格可操作
    private void switchFormVisible(boolean visible) {
        formPanel.setVisible(visible);

        if (!visible) {
            // 表单隐藏时恢复表格焦点
            table.requestFocusInWindow();

            // 刷新权限状态
            updateButtonsEnabledState();

            // 如果有数据则选中第一行
            if (table.getRowCount() > 0) {
                table.setRowSelectionInterval(0, 0);
            }
        }
    }

    // 查询操作
    private void onSearch() {
        Date meetingDate = null;
        if (dateChooserMeeting.getDate() != null) {
            meetingDate = new Date(dateChooserMeeting.getDate().getTime());
        }
        loadTableData(meetingDate,
                txtContent.getText().trim(),
                txtLocation.getText().trim(),
                txtParticipant.getText().trim(),
                txtRecorder.getText().trim());
    }

    // 修改加载表格数据的方法，添加自动选中功能
    private void loadTableData(Date meetingTime, String content, String location, String participant, String recorder) {
        // 记录当前选中行（如果有）
        if (table.getSelectedRow() >= 0) {
            lastSelectedRow = table.getSelectedRow();
        }

        tableModel.setRowCount(0);
        List<Meeting> list = meetingService.searchMeetings(meetingTime, content, location, participant, recorder);
        if (list != null && !list.isEmpty()) {
            for (Meeting m : list) {
                tableModel.addRow(new Object[] {
                        m.getMeetingId(),
                        m.getMeetingTime(),
                        m.getContent(),
                        m.getLocation(),
                        m.getParticipants(),
                        m.getRecorder(),
                        m.getUserId()
                });
            }

            // 自动选中逻辑
            int rowToSelect = -1;
            if (lastSelectedRow >= 0 && lastSelectedRow < tableModel.getRowCount()) {
                rowToSelect = lastSelectedRow; // 选择之前选中的行
            } else if (tableModel.getRowCount() > 0) {
                rowToSelect = 0; // 没有历史选中则选第一行
            }

            if (rowToSelect >= 0) {
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
                table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
            }
        }

        // 重置选中行记录
        lastSelectedRow = -1;
    }

    // 新增按钮点击
    private void onAdd() {
        if (!hasPermission("meeting_add")) {
            JOptionPane.showMessageDialog(this, "您没有新增会议的权限！");
            return;
        }
        editingMeetingId = -1; // 新增
        clearForm();
        switchFormVisible(true);

        // 设置表单初始焦点
        formDateChooser.requestFocusInWindow();
    }

    // 编辑按钮点击
    private void onEdit() {
        if (!hasPermission("meeting_edit")) {
            JOptionPane.showMessageDialog(this, "您没有编辑会议的权限！");
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的会议！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        editingMeetingId = (int) tableModel.getValueAt(row, 0);
        Meeting meeting = meetingService.getMeetingById(editingMeetingId);
        if (meeting == null) {
            JOptionPane.showMessageDialog(this, "会议不存在！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        fillForm(meeting);
        switchFormVisible(true);
    }

    // 删除按钮点击
    private void onDelete() {
        if (!hasPermission("meeting_delete")) {
            JOptionPane.showMessageDialog(this, "您没有删除会议的权限！");
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的会议！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int meetingId = (int) tableModel.getValueAt(row, 0);

        // 获取会议详细信息
        Meeting meeting = meetingService.getMeetingById(meetingId);
        if (meeting == null) {
            JOptionPane.showMessageDialog(this, "会议不存在或已被删除！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建类似图片格式的确认对话框
        Object[] options = {"是(Y)", "否(N)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "确定删除以下会议记录吗？\n\n" +
                        "会议ID: " + meeting.getMeetingId() + "\n" +
                        "会议时间: " + meeting.getMeetingTime() + "\n" +
                        "会议内容: " + meeting.getContent() + "\n" +
                        "会议地点: " + meeting.getLocation() + "\n" +
                        "参与人员: " + meeting.getParticipants() + "\n" +
                        "记录人: " + meeting.getRecorder(),
                "确认删除会议",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]  // 默认选中"否"
        );

        if (choice == JOptionPane.YES_OPTION) {
            boolean success = meetingService.deleteMeeting(meetingId);
            if (success) {
                JOptionPane.showMessageDialog(this, "删除成功！");

                // 重新加载后设置焦点
                loadTableData(null, null, null, null, null);
                table.requestFocusInWindow();
                if (table.getRowCount() > 0) {
                    table.setRowSelectionInterval(0, 0);
                }
            } else {
                JOptionPane.showMessageDialog(this, "删除失败！");
            }
        }
    }

    // 保存按钮点击（新增或编辑）
    private void onSave() {
        try {
            java.util.Date selectedDate = formDateChooser.getDate();
            if (selectedDate == null) {
                JOptionPane.showMessageDialog(this, "请选择会议日期！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Meeting meeting = new Meeting();
            meeting.setMeetingTime(new Date(selectedDate.getTime()));
            meeting.setContent(formContent.getText().trim());
            meeting.setLocation(formLocation.getText().trim());
            meeting.setParticipants(formParticipant.getText().trim());
            meeting.setRecorder(formRecorder.getText().trim());
            meeting.setUserId(Integer.parseInt(formUserId.getText().trim()));

            boolean success;
            if (editingMeetingId == -1) {
                int newId = meetingService.addMeeting(meeting);
                success = newId > 0;
            } else {
                meeting.setMeetingId(editingMeetingId);
                success = meetingService.updateMeeting(meeting);
            }

            if (success) {
                JOptionPane.showMessageDialog(this, "保存成功！");
                switchFormVisible(false);
                loadTableData(null, null, null, null, null);

                // 添加焦点设置
                table.requestFocusInWindow();
                if (table.getRowCount() > 0) {
                    table.setRowSelectionInterval(0, 0);
                }
            } else {
                JOptionPane.showMessageDialog(this, "保存失败！");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "数据格式错误或填写不完整: " + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 取消按钮点击
    private void onCancel() {
        switchFormVisible(false);
    }

    // 清空表单
    private void clearForm() {
        formDateChooser.setDate(null);
        formContent.setText("");
        formLocation.setText("");
        formParticipant.setText("");
        formRecorder.setText("");
        formUserId.setText("");
    }

    // 填充表单
    private void fillForm(Meeting meeting) {
        formDateChooser.setDate(new java.util.Date(meeting.getMeetingTime().getTime()));
        formContent.setText(meeting.getContent());
        formLocation.setText(meeting.getLocation());
        formParticipant.setText(meeting.getParticipants());
        formRecorder.setText(meeting.getRecorder());
        formUserId.setText(String.valueOf(meeting.getUserId()));
    }
}

