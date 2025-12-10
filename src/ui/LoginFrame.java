package ui;

import dao.UserDao;
import model.User;
import service.NoticeService;
import service.UserService;
import util.CodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;
import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginFrame extends JFrame {

    private Consumer<User> onLoginSuccess;
    private int failCount = 0;
    private long lockEndTime = 0;
    private boolean passwordVisible = false;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> roleBox;
    private JButton btnLogin;
    private Timer lockTimer;
    private JLabel lblLockCountdown;
    private JButton btnShowPassword;
    private JLabel lblMessage;
    private Timer messageTimer;
    private int messagePosition = 0;
    private UserService userService = new UserService();

    // 汉字验证码相关变量
    private String generatedChineseCode;
    private List<String> shuffledChineseChars;
    private List<String> userSelectedChars = new ArrayList<>();
    private JPanel chineseCodePanel;
    private JLabel chineseCodeHint;
    private JLabel lblOriginalCodeHint;

    // 滑块验证码相关组件
    private SliderCaptchaPanel captchaPanel;
    private JLabel lblSliderCode;
    private JButton btnRefreshSlider;

    // 波浪标题栏
    private WaveTitleBar waveTitleBar;

    public void setOnLoginSuccess(Consumer<User> listener) {
        this.onLoginSuccess = listener;
    }

    // 波浪标题栏类
    private class WaveTitleBar extends JPanel {
        private int mouseX, mouseY;

        public WaveTitleBar() {
            setLayout(null);
            setOpaque(false);

            // 添加标题文本
            JLabel titleLabel = new JLabel("办公室管理系统 - 登录");
            titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            titleLabel.setForeground(Color.WHITE);
            titleLabel.setBounds(20, 10, 300, 20);
            add(titleLabel);

            // 添加关闭按钮 - 修复显示问题
            JButton closeButton = new JButton("×");
            closeButton.setFont(new Font("Dialog", Font.BOLD, 20)); // 使用更通用的字体
            closeButton.setForeground(Color.WHITE);
            closeButton.setContentAreaFilled(false);
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setBounds(1150, 5, 40, 30);
            closeButton.addActionListener(e -> System.exit(0));
            closeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    closeButton.setForeground(new Color(220, 50, 50));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeButton.setForeground(Color.WHITE);
                }
            });
            add(closeButton);

            // 添加鼠标拖动事件
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    mouseX = e.getX();
                    mouseY = e.getY();
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                public void mouseDragged(MouseEvent e) {
                    int x = e.getXOnScreen() - mouseX;
                    int y = e.getYOnScreen() - mouseY;
                    LoginFrame.this.setLocation(x, y); // 修正这里
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 绘制天蓝色背景
            g2d.setColor(new Color(135, 206, 235)); // 天蓝色
            g2d.fillRect(0, 0, getWidth(), getHeight());

            // 绘制波浪底部边框
            int waveHeight = 10; // 波浪高度
            g2d.setColor(new Color(100, 180, 220)); // 深一点的天蓝色

            Path2D wavePath = new Path2D.Double();
            wavePath.moveTo(0, getHeight());

            // 波浪路径（两个完整周期）
            double amplitude = 5; // 振幅
            double frequency = 0.03; // 频率
            for (int x = 0; x <= getWidth(); x++) {
                double y = getHeight() - waveHeight - amplitude * Math.sin(frequency * x);
                wavePath.lineTo(x, y);
            }

            wavePath.lineTo(getWidth(), getHeight());
            wavePath.closePath();
            g2d.fill(wavePath);

            g2d.dispose();
        }
    }

    public LoginFrame() {
        setTitle("办公室管理系统 - 登录");
        setSize(1200, 750); // 使用的窗口大小
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); // 禁止调整大小
        setUndecorated(true); // 隐藏默认标题栏

        // 创建波浪标题栏
        waveTitleBar = new WaveTitleBar();
        waveTitleBar.setBounds(0, 0, 1200, 40);

        // 创建背景图片的 JPanel
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setBounds(0, 40, 1200, 710); // 下移40px为标题栏留出空间
        backgroundPanel.setLayout(null);
        setLayout(null);

        // 添加波浪标题栏和背景面板
        add(waveTitleBar);
        add(backgroundPanel);

        // 创建半透明的登录面板
        JPanel loginPanel = new JPanel();
        loginPanel.setDoubleBuffered(true); // 添加双缓冲
        loginPanel.setBounds(600, 60, 480, 550); // 调整位置
        loginPanel.setOpaque(true);
        loginPanel.setBackground(new Color(255, 255, 255, 200)); // 半透明白色
        loginPanel.setLayout(null);
        backgroundPanel.add(loginPanel);

        JLabel lblSpeaker = new JLabel("🔊");
        lblSpeaker.setBounds(60, 10, 30, 25);
        lblSpeaker.setFont(new Font("SansSerif", Font.BOLD, 16));
        loginPanel.add(lblSpeaker);  // 添加到背景面板

        // 消息标签
        lblMessage = new JLabel();
        lblMessage.setDoubleBuffered(true); // 添加双缓冲
        lblMessage.setBounds(90, 10, 400, 25);
        lblMessage.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        lblMessage.setForeground(Color.BLUE);
        loginPanel.add(lblMessage);

        // 加载系统消息
        NoticeService noticeService = new NoticeService();
        String message = noticeService.getSystemNoticeContent();
        startMessageScrolling(message);

        // 用户名标签与输入框
        JLabel lblUser = new JLabel("用户名:");
        lblUser.setBounds(60, 40, 60, 25);
        loginPanel.add(lblUser);
        txtUser = new JTextField();
        txtUser.setBounds(130, 40, 250, 30);
        loginPanel.add(txtUser);

        // 密码标签
        JLabel lblPass = new JLabel("密码:");
        lblPass.setBounds(60, 80, 60, 25);
        loginPanel.add(lblPass);

        // 密码框
        txtPass = new JPasswordField();
        txtPass.setBounds(130, 80, 250, 30);
        loginPanel.add(txtPass);

        // 显示/隐藏密码按钮
        btnShowPassword = new JButton("👁️");
        btnShowPassword.setBounds(380, 80, 40, 30);
        btnShowPassword.setMargin(new Insets(0, 0, 0, 0));
        btnShowPassword.setFocusPainted(false);
        btnShowPassword.setContentAreaFilled(false);
        btnShowPassword.setBorderPainted(false);
        btnShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowPassword.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                txtPass.setEchoChar((char) 0);
                btnShowPassword.setText("🔒");
            } else {
                txtPass.setEchoChar('•');
                btnShowPassword.setText("👁️");
            }
        });
        loginPanel.add(btnShowPassword);

        // 身份标签与下拉框
        JLabel lblRole = new JLabel("身份:");
        lblRole.setBounds(60, 120, 60, 25);
        loginPanel.add(lblRole);
        roleBox = new JComboBox<>(new String[]{"管理员", "普通员工"});
        roleBox.setBounds(130, 120, 250, 30);
        loginPanel.add(roleBox);

        // 汉字验证码标签
        JLabel lblCode = new JLabel("验证码:");
        lblCode.setBounds(60, 160, 60, 25);
        loginPanel.add(lblCode);

        // 原始汉字顺序提示标签
        lblOriginalCodeHint = new JLabel();
        lblOriginalCodeHint.setBounds(130, 160, 250, 25);
        lblOriginalCodeHint.setFont(new Font("微软雅黑", Font.BOLD, 14));
        lblOriginalCodeHint.setForeground(new Color(0, 100, 0));
        loginPanel.add(lblOriginalCodeHint);

        // 汉字验证码面板
        chineseCodePanel = new JPanel();
        chineseCodePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        chineseCodePanel.setBounds(130, 190, 250, 60);
        loginPanel.add(chineseCodePanel);

        // 汉字验证码提示标签
        chineseCodeHint = new JLabel("请按原始顺序点击汉字:");
        chineseCodeHint.setBounds(130, 250, 250, 25);
        loginPanel.add(chineseCodeHint);

        // 刷新验证码按钮
        JButton btnRefreshCode = new JButton("刷新验证码");
        btnRefreshCode.setBounds(320, 260, 110, 25);
        btnRefreshCode.addActionListener(e -> generateNewCode());
        loginPanel.add(btnRefreshCode);

        // 滑块验证码标签
        lblSliderCode = new JLabel("安全验证:");
        lblSliderCode.setBounds(60, 290, 80, 25);
        lblSliderCode.setVisible(false); // 初始不可见
        loginPanel.add(lblSliderCode);

        // 滑块验证码面板
        captchaPanel = new SliderCaptchaPanel();
        captchaPanel.setBounds(60, 320, 350, 100);
        captchaPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        captchaPanel.setVisible(false); // 初始不可见
        loginPanel.add(captchaPanel);

        // 刷新滑块验证码按钮
        btnRefreshSlider = new JButton("刷新滑块验证");
        btnRefreshSlider.setBounds(320, 290, 110, 25);
        btnRefreshSlider.setVisible(false); // 初始不可见
        btnRefreshSlider.addActionListener(e -> captchaPanel.refreshCaptcha());
        loginPanel.add(btnRefreshSlider);

        // 登录按钮
        btnLogin = new JButton("登录");
        btnLogin.setBounds(190, 450, 100, 30);
        btnLogin.setBackground(new Color(173, 216, 230)); // 浅蓝色背景
        btnLogin.setForeground(Color.BLACK);
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        loginPanel.add(btnLogin);

        // 注册新用户按钮
        JButton btnRegister = new JButton("注册新用户");
        btnRegister.setBounds(110, 500, 120, 30);
        btnRegister.setBackground(new Color(173, 216, 230));
        btnRegister.setForeground(Color.BLACK);
        btnRegister.setFocusPainted(false);
        btnRegister.setBorderPainted(false);
        loginPanel.add(btnRegister);

        // 忘记密码按钮
        JButton btnForget = new JButton("忘记密码");
        btnForget.setBounds(250, 500, 120, 30);
        btnForget.setBackground(new Color(173, 216, 230));
        btnForget.setForeground(Color.BLACK);
        btnForget.setFocusPainted(false);
        btnForget.setBorderPainted(false);
        loginPanel.add(btnForget);

        // 锁定倒计时提示标签
        lblLockCountdown = new JLabel();
        lblLockCountdown.setBounds(150, 425, 200, 25);
        lblLockCountdown.setForeground(Color.RED);
        loginPanel.add(lblLockCountdown);

        // 初始化验证码
        generateNewCode();

        // 事件绑定
        btnLogin.addActionListener(e -> attemptLogin());
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });
        btnForget.addActionListener(e -> {
            dispose();
            new ResetPasswordFrame();
        });

        setVisible(true);
    }

    // 背景面板类
    private class BackgroundPanel extends JPanel {
        private BufferedImage[] backgroundImages = new BufferedImage[3];
        private int imageX = 0;
        private Timer scrollTimer;

        public BackgroundPanel() {
            try {
                // 加载背景图片（需要替换为实际路径）
                backgroundImages[0] = javax.imageio.ImageIO.read(new File("C:/Users/jellyfish/Pictures/公司2.png/"));
                backgroundImages[1] = javax.imageio.ImageIO.read(new File("C:/Users/jellyfish/Pictures/公司3.jpg/"));
                backgroundImages[2] = javax.imageio.ImageIO.read(new File("C:/Users/jellyfish/Pictures/公司1.png/"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            scrollTimer = new Timer(30, e -> {
                imageX -= 2;
                if (imageX <= -getWidth()) {
                    imageX = 0;
                }
                repaint();
            });
            scrollTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < backgroundImages.length; i++) {
                g.drawImage(backgroundImages[i], imageX + i * getWidth(), 0, getWidth(), getHeight(), this);
            }
        }
    }

    // 修改 startMessageScrolling 方法
    private void startMessageScrolling(String message) {
        if (message == null || message.trim().isEmpty()) {
            lblMessage.setText("");
            return;
        }

        // 短消息直接显示
        if (message.length() < 20) {
            lblMessage.setText(message);
            return;
        }

        // 使用 StringBuilder 提高性能
        StringBuilder scrollingMessage = new StringBuilder(message);
        scrollingMessage.append("     "); // 添加分隔符

        // 设置双缓冲
        lblMessage.setDoubleBuffered(true);

        // 增加延迟时间减少闪烁
        messageTimer = new Timer(400, e -> {
            messagePosition++;
            if (messagePosition > scrollingMessage.length()) {
                messagePosition = 0;
            }

            // 优化字符串截取算法
            int endPos = messagePosition + 20;
            String displayText;
            if (endPos <= scrollingMessage.length()) {
                displayText = scrollingMessage.substring(messagePosition, endPos);
            } else {
                int remaining = endPos - scrollingMessage.length();
                displayText = scrollingMessage.substring(messagePosition) +
                        scrollingMessage.substring(0, remaining);
            }

            // 仅在文本变化时更新
            if (!displayText.equals(lblMessage.getText())) {
                lblMessage.setText(displayText);
            }
        });
        messageTimer.start();
    }

    private void generateNewCode() {
        // 生成四字成语验证码
        generatedChineseCode = CodeUtil.generateChineseCode(4);

        // 显示原始成语提示
        lblOriginalCodeHint.setText("原始词: " + generatedChineseCode);

        // 创建乱序的汉字列表
        shuffledChineseChars = new ArrayList<>();
        for (int i = 0; i < generatedChineseCode.length(); i++) {
            shuffledChineseChars.add(String.valueOf(generatedChineseCode.charAt(i)));
        }
        Collections.shuffle(shuffledChineseChars);

        userSelectedChars.clear();
        updateChineseCodePanel();

        // 隐藏滑块验证码
        lblSliderCode.setVisible(false);
        captchaPanel.setVisible(false);
        btnRefreshSlider.setVisible(false);
    }

    // 更新汉字验证码面板
    private void updateChineseCodePanel() {
        chineseCodePanel.removeAll();

        for (String chineseChar : shuffledChineseChars) {
            JButton btnChar = new JButton(chineseChar);
            btnChar.setPreferredSize(new Dimension(50, 40));
            btnChar.setFont(new Font("微软雅黑", Font.BOLD, 16));
            btnChar.setBackground(new Color(240, 240, 240));
            btnChar.setFocusPainted(false);
            btnChar.addActionListener(e -> {
                // 防止重复点击同一个汉字
                if (!userSelectedChars.contains(chineseChar)) {
                    userSelectedChars.add(chineseChar);
                    updateChineseCodeHint();

                    // 当选择了所有汉字时自动验证并显示滑块验证
                    if (userSelectedChars.size() == generatedChineseCode.length()) {
                        verifyChineseCode();
                    }
                }
            });
            chineseCodePanel.add(btnChar);
        }

        chineseCodePanel.revalidate();
        chineseCodePanel.repaint();
        updateChineseCodeHint();
    }

    // 验证汉字验证码
    private void verifyChineseCode() {
        // 验证用户点击顺序是否与原始顺序一致
        StringBuilder userInput = new StringBuilder();
        for (String ch : userSelectedChars) {
            userInput.append(ch);
        }

        if (userInput.toString().equals(generatedChineseCode)) {
            // 汉字验证通过，显示滑块验证
            lblSliderCode.setVisible(true);
            captchaPanel.setVisible(true);
            btnRefreshSlider.setVisible(true);
            captchaPanel.refreshCaptcha(); // 生成新的滑块验证码
        } else {
            JOptionPane.showMessageDialog(this, "汉字顺序错误！请按原始顺序点击");
            generateNewCode();
        }
    }

    // 更新汉字验证码提示
    private void updateChineseCodeHint() {
        StringBuilder hint = new StringBuilder("已选择: ");
        for (String ch : userSelectedChars) {
            hint.append(ch);
        }
        chineseCodeHint.setText(hint.toString());
    }

    private void attemptLogin() {
        if (isLocked()) {
            JOptionPane.showMessageDialog(this, "登录失败次数过多，请一分钟后再试。\n倒计时：" + getRemainingLockSeconds() + "秒");
            return;
        }

        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();
        String selectedRole = (String) roleBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码不能为空！");
            return;
        }

        // 验证汉字验证码
        if (userSelectedChars.size() != generatedChineseCode.length()) {
            JOptionPane.showMessageDialog(this, "请完成汉字验证码！");
            return;
        }

        // 验证滑块验证码
        if (!captchaPanel.isVerificationPassed()) {
            JOptionPane.showMessageDialog(this, "请完成滑块验证！");
            return;
        }

        // 使用UserService登录
        User user = userService.login(username, password);

        if (user != null) {
            // 获取完整用户权限信息
            User fullUser = userService.getUserWithPermissions(user.getUserId());

            if (fullUser == null) {
                JOptionPane.showMessageDialog(this, "加载用户权限失败");
                return;
            }

            boolean isAdmin = fullUser.getRoleId() == 1;
            if ((isAdmin && !"管理员".equals(selectedRole)) || (!isAdmin && !"普通员工".equals(selectedRole))) {
                JOptionPane.showMessageDialog(this, "身份选择错误！");
                return;
            }

            JOptionPane.showMessageDialog(this, "登录成功！欢迎：" + fullUser.getName());
            if (onLoginSuccess != null) {
                onLoginSuccess.accept(fullUser);
            }
            dispose();
        } else {
            failCount++;
            if (failCount >= 3) {
                lockEndTime = System.currentTimeMillis() + 60 * 1000;
                disableLoginWithCountdown();
                JOptionPane.showMessageDialog(this, "密码错误3次，请1分钟后再试！");
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！");
                generateNewCode(); // 刷新所有验证码
            }
        }
    }

    private boolean isLocked() {
        return System.currentTimeMillis() < lockEndTime;
    }

    private long getRemainingLockSeconds() {
        long remaining = (lockEndTime - System.currentTimeMillis()) / 1000;
        return Math.max(remaining, 0);
    }

    private void disableLoginWithCountdown() {
        txtUser.setEnabled(false);
        txtPass.setEnabled(false);
        roleBox.setEnabled(false);
        btnLogin.setEnabled(false);
        btnShowPassword.setEnabled(false);
        chineseCodePanel.setEnabled(false);
        captchaPanel.setEnabled(false);

        lblLockCountdown.setText("锁定中，请稍后... 60秒");

        lockTimer = new Timer(1000, (ActionEvent e) -> {
            long remaining = getRemainingLockSeconds();
            if (remaining <= 0) {
                lockTimer.stop();
                txtUser.setEnabled(true);
                txtPass.setEnabled(true);
                roleBox.setEnabled(true);
                btnLogin.setEnabled(true);
                btnShowPassword.setEnabled(true);
                chineseCodePanel.setEnabled(true);
                captchaPanel.setEnabled(true);
                lblLockCountdown.setText("");
                failCount = 0;
                generateNewCode();
            } else {
                lblLockCountdown.setText("锁定中，请稍后... " + remaining + "秒");
            }
        });
        lockTimer.start();
    }

    public void dispose() {
        if (messageTimer != null && messageTimer.isRunning()) {
            messageTimer.stop();
        }
        if (lockTimer != null && lockTimer.isRunning()) {
            lockTimer.stop();
        }
        super.dispose();
    }

    // 滑块验证码面板
    private class SliderCaptchaPanel extends JPanel {
        private BufferedImage backgroundImage;
        private BufferedImage sliderImage;
        private int sliderX = 0;
        private int targetX;
        private boolean isDragging = false;
        private int startDragX;
        private boolean verificationPassed = false;
        private static final int SLIDER_WIDTH = 50;
        private static final int SLIDER_HEIGHT = 50;

        public SliderCaptchaPanel() {
            setLayout(null);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            generateCaptchaImages();

            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getX() >= sliderX && e.getX() <= sliderX + SLIDER_WIDTH &&
                            e.getY() >= 25 && e.getY() <= 25 + SLIDER_HEIGHT) {
                        isDragging = true;
                        startDragX = e.getX();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDragging) {
                        isDragging = false;
                        verificationPassed = Math.abs(sliderX - targetX) <= 5;
                        repaint();
                    }
                }
            };

            MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDragging && !verificationPassed) {
                        int newX = sliderX + (e.getX() - startDragX);
                        if (newX < 0) newX = 0;
                        if (newX > getWidth() - SLIDER_WIDTH - 10) {
                            newX = getWidth() - SLIDER_WIDTH - 10;
                        }

                        sliderX = newX;
                        startDragX = e.getX();
                        repaint();
                    }
                }
            };

            addMouseListener(mouseAdapter);
            addMouseMotionListener(motionAdapter);
        }

        private void generateCaptchaImages() {
            backgroundImage = new BufferedImage(350, 100, BufferedImage.TYPE_INT_RGB);
            Graphics g = backgroundImage.getGraphics();

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, backgroundImage.getWidth(), backgroundImage.getHeight());

            Random random = new Random();
            targetX = 50 + random.nextInt(backgroundImage.getWidth() - 150);

            drawRandomShapes(g);

            g.setColor(Color.WHITE);
            g.fillOval(targetX, 25, SLIDER_WIDTH, SLIDER_HEIGHT);

            sliderImage = new BufferedImage(SLIDER_WIDTH + 10, SLIDER_HEIGHT + 10, BufferedImage.TYPE_INT_ARGB);
            Graphics sg = sliderImage.getGraphics();

            sg.setColor(new Color(70, 130, 180));
            sg.fillOval(5, 5, SLIDER_WIDTH, SLIDER_HEIGHT);
            sg.setColor(Color.WHITE);
            sg.fillOval(15, 15, 30, 30);

            sliderX = 0;
            verificationPassed = false;
        }

        private void drawRandomShapes(Graphics g) {
            Random random = new Random();
            for (int i = 0; i < 10; i++) {
                g.setColor(new Color(random.nextInt(100) + 50, random.nextInt(100) + 50, random.nextInt(100) + 50));
                int x1 = random.nextInt(backgroundImage.getWidth());
                int y1 = random.nextInt(backgroundImage.getHeight());
                int x2 = random.nextInt(backgroundImage.getWidth());
                int y2 = random.nextInt(backgroundImage.getHeight());
                g.drawLine(x1, y1, x2, y2);
            }

            for (int i = 0; i < 200; i++) {
                g.setColor(new Color(random.nextInt(50) + 100, random.nextInt(50) + 100, random.nextInt(50) + 100));
                int x = random.nextInt(backgroundImage.getWidth());
                int y = random.nextInt(backgroundImage.getHeight());
                g.fillRect(x, y, 2, 2);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage != null) {
                g.drawImage(backgroundImage, 0, 0, this);
            }

            if (sliderImage != null) {
                g.drawImage(sliderImage, sliderX, 20, this);
            }

            g.setColor(Color.GRAY);
            g.drawRect(5, 50, getWidth() - 10, 5);

            g.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            if (verificationPassed) {
                g.setColor(Color.GREEN);
                g.drawString("✅ 验证通过", getWidth() - 80, 20);
            } else if (sliderX > 0) {
                g.setColor(Color.RED);
                g.drawString("❌ 请对齐缺口", 10, 20);
            } else {
                g.setColor(Color.BLACK);
                g.drawString("→ 请拖动滑块对齐缺口", 10, 20);
            }
        }

        public void refreshCaptcha() {
            generateCaptchaImages();
            repaint();
        }

        public boolean isVerificationPassed() {
            return verificationPassed;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}












