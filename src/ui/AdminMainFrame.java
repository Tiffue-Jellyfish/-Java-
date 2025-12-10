package ui;

import service.AttendanceService;
import service.FileInfoService;
import service.NoticeService;
import service.PermissionService;
import service.UserService;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import dao.AttendanceDao;

public class AdminMainFrame extends JFrame implements BackToHomeListener {

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private AttendanceService attendanceService;
    private FileInfoService fileInfoService;
    private UserService userService;
    private User currentUser;

    // 考勤组件
    private JButton signInBtn;
    private JButton signOutBtn;
    private JLabel statusLabel;

    public AdminMainFrame(User currentUser) {
        this.currentUser = currentUser;
        setTitle("管理员主界面 - 欢迎 " + currentUser.getName());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 添加窗口监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // 主界面关闭时打开登录界面
                new LoginFrame();
            }
        });

        // 初始化服务
        attendanceService = new AttendanceService(new AttendanceDao(), currentUser);
        fileInfoService = new FileInfoService();
        userService = new UserService();

        // 创建菜单栏
        JMenuBar menuBar = new JMenuBar();
        // 在AdminMainFrame构造方法中
        String[] menuItems = {"文件管理", "考勤管理", "会议管理", "通知管理",
                "部门管理", "角色管理", "用户管理", "权限管理"}; // 新增权限管理
        for (String item : menuItems) {
            JMenuItem menuItem = new JMenuItem(item);
            menuItem.addActionListener(e -> cardLayout.show(mainPanel, item));
            menuBar.add(menuItem);
        }
        setJMenuBar(menuBar);

        // 系统菜单
        JMenu systemMenu = new JMenu("系统设置");

        // 编辑系统消息菜单项
        JMenuItem editMessageItem = new JMenuItem("编辑登录消息");
        editMessageItem.addActionListener(e -> showMessageEditor());
        systemMenu.add(editMessageItem);

        menuBar.add(systemMenu);
        setJMenuBar(menuBar);

        // 创建主面板
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // 权限服务实例化（只需一次）
        PermissionService permissionService = new PermissionService();

        // 欢迎面板（带考勤功能）
        mainPanel.add(createWelcomePanel(), "Welcome");

        // 功能面板（使用正确的类名）
        mainPanel.add(new FileInfoPanel(fileInfoService, false, currentUser), "文件管理"); // 修正为 FileInfoPanel
        mainPanel.add(new AttendancePanel(currentUser, attendanceService), "考勤管理");
        mainPanel.add(new MeetingPanel(currentUser, permissionService), "会议管理");
        mainPanel.add(new NoticePanel(this, false, currentUser, permissionService), "通知管理");
        mainPanel.add(new DepartmentPanel(false, currentUser), "部门管理");
        mainPanel.add(new RolePanel(), "角色管理");
        mainPanel.add(new UserInfoPanel(currentUser, userService, false, permissionService), "用户管理");
        // 修改权限管理面板创建方式
        mainPanel.add(new PermissionManagementPanel(currentUser), "权限管理");  // 传入当前用户

        add(mainPanel, BorderLayout.CENTER);

        // 添加状态栏
        add(createStatusBar(), BorderLayout.SOUTH);

        // 默认显示欢迎页面
        cardLayout.show(mainPanel, "Welcome");
        setVisible(true);
    }

    private void showMessageEditor() {
        JDialog editorDialog = new JDialog(this, "编辑系统通知", true);
        editorDialog.setSize(500, 300);
        editorDialog.setLocationRelativeTo(this);
        editorDialog.setLayout(new BorderLayout());

        // 加载当前系统通知
        NoticeService noticeService = new NoticeService();
        String currentMessage = noticeService.getSystemNoticeContent();

        // 文本区域
        JTextArea messageArea = new JTextArea(currentMessage);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);

        // 按钮面板
        JPanel buttonPanel = new JPanel();
        JButton btnSave = new JButton("保存");
        JButton btnCancel = new JButton("取消");

        btnSave.addActionListener(e -> {
            String newMessage = messageArea.getText().trim();
            if (!newMessage.isEmpty()) {
                if (noticeService.saveSystemNotice(newMessage)) {
                    JOptionPane.showMessageDialog(editorDialog, "系统通知已更新！");
                    editorDialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(editorDialog, "更新失败，请重试！");
                }
            } else {
                JOptionPane.showMessageDialog(editorDialog, "消息不能为空！");
            }
        });

        btnCancel.addActionListener(e -> editorDialog.dispose());

        buttonPanel.add(btnSave);
        buttonPanel.add(btnCancel);

        editorDialog.add(scrollPane, BorderLayout.CENTER);
        editorDialog.add(buttonPanel, BorderLayout.SOUTH);
        editorDialog.setVisible(true);
    }

    private JPanel createWelcomePanel() {
        // 创建自定义面板用于绘制背景
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    // 加载背景图片 - 使用用户提供的路径
                    String bgPath = "D:/Java/OfficeManagement/icons/admin.png/";
                    Image backgroundImage = new ImageIcon(bgPath).getImage();

                    // 绘制背景图片（适应面板大小）
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    // 图片加载失败时使用纯色背景
                    g.setColor(new Color(135, 206, 235));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    System.err.println("背景图片加载失败: " + e.getMessage());
                }

                // 添加半透明遮罩层
                Color overlay = new Color(255, 255, 255, 180); // 半透明白色
                g.setColor(overlay);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // 创建内容面板（使用GridBagLayout实现灵活布局）
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false); // 透明

        // 创建GridBagConstraints对象用于布局控制
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // 组件间距
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 创建欢迎标签并添加到顶部
        JLabel welcomeLabel = new JLabel("欢迎管理员，" + currentUser.getName() + "！", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(50, 50, 50)); // 深灰色文字

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0; // 占据顶部空间
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(200, 0, 20, 0); // 上边距更大
        contentPanel.add(welcomeLabel, gbc);

        // 创建按钮面板（签到/签退按钮和状态标签）
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 20));

        // 初始化考勤组件
        signInBtn = new JButton("签到");
        signOutBtn = new JButton("签退");
        statusLabel = new JLabel("考勤状态: 未签到");

        // 设置按钮样式
        signInBtn.setBackground(new Color(70, 130, 180)); // 钢蓝色
        signInBtn.setForeground(Color.WHITE);
        signInBtn.setFocusPainted(false);
        signInBtn.setFont(new Font("宋体", Font.BOLD, 16));

        signOutBtn.setBackground(new Color(70, 130, 180));
        signOutBtn.setForeground(Color.WHITE);
        signOutBtn.setFocusPainted(false);
        signOutBtn.setFont(new Font("宋体", Font.BOLD, 16));

        statusLabel.setFont(new Font("宋体", Font.BOLD, 16));
        statusLabel.setForeground(new Color(80, 80, 80));

        // 设置按钮尺寸
        Dimension buttonSize = new Dimension(100, 30); // 更大的按钮
        signInBtn.setPreferredSize(buttonSize);
        signInBtn.setMinimumSize(buttonSize);
        signInBtn.setMaximumSize(buttonSize);

        signOutBtn.setPreferredSize(buttonSize);
        signOutBtn.setMinimumSize(buttonSize);
        signOutBtn.setMaximumSize(buttonSize);

        // 添加组件到按钮面板
        buttonPanel.add(Box.createHorizontalGlue()); // 左侧弹性空间
        buttonPanel.add(signInBtn);
        buttonPanel.add(Box.createHorizontalStrut(30)); // 更大的间距
        buttonPanel.add(signOutBtn);
        buttonPanel.add(Box.createHorizontalStrut(30));
        buttonPanel.add(statusLabel);
        buttonPanel.add(Box.createHorizontalGlue()); // 右侧弹性空间

        // 将按钮面板添加到内容面板（中间靠下位置）
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0; // 不占用垂直空间
        gbc.anchor = GridBagConstraints.CENTER; // 居中
        gbc.insets = new Insets(0, 0, 150, 0); // 底部间距150像素
        contentPanel.add(buttonPanel, gbc);

        // 添加事件监听
        signInBtn.addActionListener(this::handleSignIn);
        signOutBtn.addActionListener(this::handleSignOut);

        // 将内容面板添加到主面板（居中显示）
        GridBagConstraints panelGbc = new GridBagConstraints();
        panelGbc.gridx = 0;
        panelGbc.gridy = 0;
        panelGbc.weightx = 1.0;
        panelGbc.weighty = 1.0;
        panelGbc.fill = GridBagConstraints.BOTH;
        panel.add(contentPanel, panelGbc);

        // 初始化按钮状态
        updateButtonStatus();

        return panel;
    }

    private void updateButtonStatus() {
        boolean hasSignedIn = attendanceService.hasSignedInToday(currentUser.getUserId());
        boolean hasSignedOut = attendanceService.hasSignedOutToday(currentUser.getUserId());

        signInBtn.setEnabled(!hasSignedIn);
        signOutBtn.setEnabled(hasSignedIn && !hasSignedOut);

        if (hasSignedOut) {
            statusLabel.setText("考勤状态: 已完成今日签到签退");
        } else if (hasSignedIn) {
            statusLabel.setText("考勤状态: 已签到，待签退");
        } else {
            statusLabel.setText("考勤状态: 未签到");
        }
    }

    private void handleSignIn(ActionEvent e) {
        Timestamp signInTime = new Timestamp(System.currentTimeMillis());
        if (attendanceService.signIn(currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "签到成功！时间: " +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(signInTime));
            updateButtonStatus();
        } else {
            JOptionPane.showMessageDialog(this, "签到失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleSignOut(ActionEvent e) {
        Timestamp signOutTime = new Timestamp(System.currentTimeMillis());
        if (attendanceService.signOut(currentUser.getUserId())) {
            JOptionPane.showMessageDialog(this, "签退成功！时间: " +
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(signOutTime));
            updateButtonStatus();
        } else {
            JOptionPane.showMessageDialog(this, "签退失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());

        JLabel timeLabel = new JLabel();
        new Timer(1000, e -> {
            timeLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }).start();

        statusPanel.add(new JLabel("用户: " + currentUser.getName() + "  "));
        statusPanel.add(new JLabel("角色: " + currentUser.getRoleId() + "  "));
        statusPanel.add(timeLabel);

        return statusPanel;
    }

    @Override
    public void backToHome() {
        cardLayout.show(mainPanel, "Welcome");
    }
}










