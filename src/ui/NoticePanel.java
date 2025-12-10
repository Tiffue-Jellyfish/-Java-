package ui;

import model.Notice;
import model.Permission;
import model.User;
import service.NoticeService;
import service.PermissionService;
import ui.JDateChooser; // JDateChooser 的导入
import ui.BackToHomeListener; // BackToHomeListener 的导入
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NoticePanel extends JPanel {
    private BackToHomeListener backListener;
    private NoticeService noticeService = new NoticeService();
    private User currentUser; // 添加当前用户字段
    private PermissionService permissionService; // 添加权限服务字段

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtRecipients; // 新增接收人输入框
    private JTextField txtSearch;
    private JButton btnRefresh; // 新增刷新按钮

    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;

    // 新增图标路径常量
    private static final String ICON_PATH = "D:/Java/OfficeManagement/icons/";

    // 修改构造器
    public NoticePanel(BackToHomeListener backListener, boolean isReadOnly, User currentUser, PermissionService permissionService) {
        this.backListener = backListener;
        this.currentUser = currentUser;
        this.permissionService = permissionService;

        setLayout(new BorderLayout());

        // 修改顶部查询栏布局
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2, 1, 5, 5)); // 改为两行一列的网格布局

        // 第一行面板（标题和接收人）
        JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(15);
        txtRecipients = new JTextField(15);
        row1Panel.add(new JLabel("标题关键字："));
        row1Panel.add(txtSearch);
        row1Panel.add(new JLabel("接收人："));
        row1Panel.add(txtRecipients);

        // 修改第二行面板（日期范围和查询按钮）添加刷新按钮
        JPanel row2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        startDateChooser = new JDateChooser();
        endDateChooser = new JDateChooser();
        JButton btnSearch = new JButton("查询", createScaledIcon("search.jpeg"));

        // 设置日期选择器尺寸保持一致
        startDateChooser.setPreferredSize(new Dimension(150, startDateChooser.getPreferredSize().height));
        endDateChooser.setPreferredSize(new Dimension(150, endDateChooser.getPreferredSize().height));

        row2Panel.add(new JLabel("起始日期："));
        row2Panel.add(startDateChooser);
        row2Panel.add(new JLabel("结束日期："));
        row2Panel.add(endDateChooser);
        row2Panel.add(btnSearch);

        // 将两行面板添加到主面板
        topPanel.add(row1Panel);
        topPanel.add(row2Panel);

        add(topPanel, BorderLayout.NORTH);

        // 表格
        String[] columnNames = {"ID", "标题", "内容", "发布时间", "接收人", "发布人ID"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 修改底部按钮栏
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // 使用右对齐布局

        JButton btnAdd = new JButton("新增", createScaledIcon("add.jpeg"));
        JButton btnEdit = new JButton("编辑", createScaledIcon("edit.jpeg"));
        JButton btnDelete = new JButton("删除", createScaledIcon("delete.jpeg"));
        btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        JButton btnBack = new JButton("返回主页", createScaledIcon("home.jpeg"));

        // 确保当前用户权限已加载
        if (currentUser != null && permissionService != null) {
            List<Permission> permissions = permissionService.getPermissionsByUserId(currentUser.getUserId());
            // 使用新添加的setPermissions方法
            currentUser.setPermissions(permissions);
        }

        // 调试输出 - 检查权限
        System.out.println("当前用户: " + currentUser.getName());
        System.out.println("角色ID: " + currentUser.getRoleId());
        System.out.println("拥有的权限:");
        for (Permission p : currentUser.getPermissions()) { // 使用getPermissions()
            System.out.println(" - " + p.getPermissionName() + " (" + p.getDescription() + ")");
        }

        // 检查具体权限
        boolean hasAdd = currentUser.hasPermission("notice_add");
        boolean hasEdit = currentUser.hasPermission("notice_edit");
        boolean hasDelete = currentUser.hasPermission("notice_delete");

        System.out.println("是否有 notice_add 权限: " + hasAdd);
        System.out.println("是否有 notice_edit 权限: " + hasEdit);
        System.out.println("是否有 notice_delete 权限: " + hasDelete);

        // 直接根据权限设置按钮可见性
        btnAdd.setVisible(hasAdd);
        btnEdit.setVisible(hasEdit);
        btnDelete.setVisible(hasDelete);

        bottomPanel.add(btnAdd);
        bottomPanel.add(btnEdit);
        bottomPanel.add(btnDelete);
        bottomPanel.add(btnRefresh);  // 刷新按钮在删除按钮右边
        bottomPanel.add(btnBack);     // 返回主页在刷新按钮右边

        add(bottomPanel, BorderLayout.SOUTH);

        // 修改查询按钮事件处理
        btnSearch.addActionListener(e -> {
            String keyword = txtSearch.getText().trim();
            String recipients = txtRecipients.getText().trim(); // 获取接收人输入
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();

            if (startDate != null && endDate != null && startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "起始日期不能晚于结束日期");
                return;
            }

            // 传递接收人参数
            loadTableData(keyword, startDate, endDate, recipients);
        });

        // 修改新增按钮事件监听器
        btnAdd.addActionListener(e -> {
            System.out.println("新增按钮点击事件触发");

            // 直接检查权限而不是使用 isReadOnly
            if (hasPermission("notice_add")) {
                System.out.println("用户拥有新增权限，打开对话框");
                openEditDialog(null);
            } else {
                System.out.println("用户无新增权限");
                JOptionPane.showMessageDialog(this, "您没有新增通知的权限！");
            }
        });

        // 修改编辑按钮事件监听器 - 添加权限检查
        btnEdit.addActionListener(e -> {
            System.out.println("编辑按钮点击事件触发");

            // 检查编辑权限
            if (hasPermission("notice_edit")) {
                System.out.println("用户拥有编辑权限");
                int selectedRow = table.getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(this, "请先选择一条记录");
                    return;
                }
                int noticeId = (int) tableModel.getValueAt(selectedRow, 0);
                Notice notice = noticeService.getNoticeById(noticeId);
                if (notice == null) {
                    JOptionPane.showMessageDialog(this, "通知不存在或已被删除");
                    return;
                }
                openEditDialog(notice);
            } else {
                System.out.println("用户无编辑权限");
                JOptionPane.showMessageDialog(this, "您没有编辑通知的权限！");
            }
        });

        // 修改删除按钮事件监听器 - 添加权限检查
        btnDelete.addActionListener(e -> {
            // 检查删除权限
            if (!hasPermission("notice_delete")) {
                JOptionPane.showMessageDialog(this, "您没有删除通知的权限！");
                return;
            }

            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "请先选择一条记录");
                return;
            }
            int noticeId = (int) tableModel.getValueAt(selectedRow, 0);
            Notice notice = noticeService.getNoticeById(noticeId);
            if (notice == null) {
                JOptionPane.showMessageDialog(this, "通知不存在或已被删除");
                return;
            }

            Object[] options = {"是(Y)", "否(N)"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "<html><b>确定删除以下通知吗？</b><br><br>" +
                            "通知ID: " + notice.getNoticeId() + "<br>" +
                            "标题: " + notice.getTitle() + "<br>" +
                            "内容: " + (notice.getContent().length() > 20 ?
                            notice.getContent().substring(0, 20) + "..." : notice.getContent()) + "<br>" +
                            "发布时间: " + (notice.getPublishTime() != null ?
                            notice.getPublishTime().toString() : "无") + "<br>" +
                            "接收人: " + notice.getRecipients() + "<br>" +
                            "发布人ID: " + notice.getUserId() + "</html>",
                    "确认删除通知",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]
            );

            if (choice == JOptionPane.YES_OPTION) {
                boolean success = noticeService.deleteNotice(noticeId);
                if (success) {
                    JOptionPane.showMessageDialog(this, "删除成功");
                    loadTableData(txtSearch.getText().trim(), startDateChooser.getDate(),
                            endDateChooser.getDate(), txtRecipients.getText().trim());
                } else {
                    JOptionPane.showMessageDialog(this, "删除失败");
                }
            }
        });

        btnBack.addActionListener(e -> {
            if (backListener != null) {
                backListener.backToHome();
            }
        });

        // 添加刷新按钮的事件监听
        btnRefresh.addActionListener(e -> onRefresh());

        // 初始化加载全部数据
        loadTableData(null, null, null, null);
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

    // 新增刷新方法
    private void onRefresh() {
        // 清空所有搜索条件
        txtSearch.setText("");
        txtRecipients.setText("");
        startDateChooser.setDate(null);
        endDateChooser.setDate(null);

        // 加载全部数据
        loadTableData(null, null, null, null);
        JOptionPane.showMessageDialog(this, "数据已刷新，显示所有通知");
    }

    // 修改加载数据方法，增加接收人参数
    private void loadTableData(String titleKeyword, Date startDate, Date endDate, String recipients) {
        // 传递接收人参数到Service
        List<Notice> list = noticeService.searchNotices(titleKeyword, startDate, endDate, recipients);
        tableModel.setRowCount(0);
        if (list != null) {
            for (Notice n : list) {
                String publishTimeStr = n.getPublishTime() == null ? "" : n.getPublishTime().toString();
                tableModel.addRow(new Object[]{
                        n.getNoticeId(),
                        n.getTitle(),
                        n.getContent(),
                        publishTimeStr,
                        n.getRecipients(),
                        n.getUserId()
                });
            }
        }
    }

    // 添加权限检查方法
    private boolean hasPermission(String permissionName) {
        if (currentUser != null) {
            return currentUser.hasPermission(permissionName);
        }
        return false;
    }

    private void openEditDialog(Notice notice) {
        System.out.println("尝试打开编辑对话框...");

        // 确保有父窗口
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            owner = JOptionPane.getRootFrame();
            System.out.println("使用备用父窗口");
        }
        JDialog dialog = new JDialog(owner, notice == null ? "新增通知" : "编辑通知", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel lblTitle = new JLabel("标题：");
        JTextField txtTitle = new JTextField(50);

        JLabel lblContent = new JLabel("内容：");
        JTextArea txtContent = new JTextArea(10, 50);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scrollContent = new JScrollPane(txtContent);
        scrollContent.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // ============== 修改开始：替换原有的发布时间输入框 ==============
        JLabel lblPublishTime = new JLabel("发布时间：");
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        // 日期选择器
        JDateChooser dateChooser = new JDateChooser("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(120, dateChooser.getPreferredSize().height));

        // 时间选择器 (小时)
        SpinnerNumberModel hourModel = new SpinnerNumberModel(0, 0, 23, 1);
        JSpinner hourSpinner = new JSpinner(hourModel);
        hourSpinner.setPreferredSize(new Dimension(50, hourSpinner.getPreferredSize().height));

        // 时间选择器 (分钟)
        SpinnerNumberModel minuteModel = new SpinnerNumberModel(0, 0, 59, 1);
        JSpinner minuteSpinner = new JSpinner(minuteModel);
        minuteSpinner.setPreferredSize(new Dimension(50, minuteSpinner.getPreferredSize().height));

        // 添加标签和分隔符
        timePanel.add(dateChooser);
        timePanel.add(new JLabel("时间:"));
        timePanel.add(hourSpinner);
        timePanel.add(new JLabel(":"));
        timePanel.add(minuteSpinner);

        JLabel lblRecipients = new JLabel("接收人：");
        JTextField txtRecipients = new JTextField(50);

        JLabel lblUserId = new JLabel("发布人ID：");
        JTextField txtUserId = new JTextField(50);

        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(lblTitle, gbc);
        gbc.gridx = 1;
        dialog.add(txtTitle, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(lblContent, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        dialog.add(scrollContent, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0;
        dialog.add(lblPublishTime, gbc);
        gbc.gridx = 1;
        dialog.add(timePanel, gbc);  // 使用面板替代原文本框

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(lblRecipients, gbc);
        gbc.gridx = 1;
        dialog.add(txtRecipients, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(lblUserId, gbc);
        gbc.gridx = 1;
        dialog.add(txtUserId, gbc);

        if (notice != null) {
            txtTitle.setText(notice.getTitle());
            txtContent.setText(notice.getContent());
            txtRecipients.setText(notice.getRecipients());
            txtUserId.setText(String.valueOf(notice.getUserId()));

            // 初始化时间选择器
            if (notice.getPublishTime() != null) {
                Timestamp ts = notice.getPublishTime();
                Date date = new Date(ts.getTime());
                dateChooser.setDate(date);

                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                hourSpinner.setValue(cal.get(Calendar.HOUR_OF_DAY));
                minuteSpinner.setValue(cal.get(Calendar.MINUTE));
            }
        } else {
            // 默认设置为当前时间
            dateChooser.setDate(new Date());
            Calendar now = Calendar.getInstance();
            hourSpinner.setValue(now.get(Calendar.HOUR_OF_DAY));
            minuteSpinner.setValue(now.get(Calendar.MINUTE));
        }

        JPanel btnPanel = new JPanel();
        JButton btnOk = new JButton("确定");
        JButton btnCancel = new JButton("取消");
        btnPanel.add(btnOk);
        btnPanel.add(btnCancel);

        row++;
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        btnOk.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            String content = txtContent.getText().trim();
            String recipients = txtRecipients.getText().trim();
            String userIdStr = txtUserId.getText().trim();

            if (title.isEmpty() || content.isEmpty() || userIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "标题、内容和发布人ID不能为空！");
                return;
            }

            // 从时间选择器获取时间
            java.sql.Timestamp publishTime = null;
            Date selectedDate = dateChooser.getDate();
            if (selectedDate != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(selectedDate);
                cal.set(Calendar.HOUR_OF_DAY, (Integer) hourSpinner.getValue());
                cal.set(Calendar.MINUTE, (Integer) minuteSpinner.getValue());
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                publishTime = new Timestamp(cal.getTimeInMillis());
            }

            int userId;
            try {
                userId = Integer.parseInt(userIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "发布人ID必须是数字");
                return;
            }

            if (notice == null) {
                Notice newNotice = new Notice();
                newNotice.setTitle(title);
                newNotice.setContent(content);
                newNotice.setPublishTime(publishTime);
                newNotice.setRecipients(recipients);
                newNotice.setUserId(userId);

                if (noticeService.addNotice(newNotice)) {
                    JOptionPane.showMessageDialog(dialog, "新增成功");
                    loadTableData(null, null, null, null);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "新增失败");
                }
            } else {
                notice.setTitle(title);
                notice.setContent(content);
                notice.setPublishTime(publishTime);
                notice.setRecipients(recipients);
                notice.setUserId(userId);

                if (noticeService.updateNotice(notice)) {
                    JOptionPane.showMessageDialog(dialog, "更新成功");
                    loadTableData(null, null, null, null);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "更新失败");
                }
            }
        });

        btnCancel.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }
}
