package ui;

import model.User;
import service.UserService;
import util.CodeUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ResetPasswordFrame extends JFrame {

    private String generatedCode = "";
    private boolean passwordVisible = false; // 控制密码是否可见

    public ResetPasswordFrame() {
        setTitle("忘记密码");
        setSize(400, 350);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setBounds(40, 30, 80, 25);
        add(lblUsername);

        JTextField txtUsername = new JTextField();
        txtUsername.setBounds(140, 30, 200, 25);
        add(txtUsername);

        JLabel lblNewPass = new JLabel("新密码:");
        lblNewPass.setBounds(40, 70, 80, 25);
        add(lblNewPass);

        // 新密码字段和眼睛按钮
        JPasswordField txtNewPass = new JPasswordField();
        txtNewPass.setBounds(140, 70, 200, 25); // 宽度减小
        add(txtNewPass);

        // 新密码眼睛按钮
        JButton btnShowNewPass = new JButton("👁️");
        btnShowNewPass.setBounds(340, 70, 35, 25);
        btnShowNewPass.setMargin(new Insets(0, 0, 0, 0));
        btnShowNewPass.setFocusPainted(false);
        btnShowNewPass.setContentAreaFilled(false);
        btnShowNewPass.setBorderPainted(false);
        btnShowNewPass.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowNewPass.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                txtNewPass.setEchoChar((char) 0); // 显示密码
                btnShowNewPass.setText("🔒");
            } else {
                txtNewPass.setEchoChar('•'); // 隐藏密码
                btnShowNewPass.setText("👁️");
            }
        });
        add(btnShowNewPass);

        JLabel lblConfirm = new JLabel("确认密码:");
        lblConfirm.setBounds(40, 110, 80, 25);
        add(lblConfirm);

        // 确认密码字段和眼睛按钮
        JPasswordField txtConfirm = new JPasswordField();
        txtConfirm.setBounds(140, 110, 200, 25); // 宽度减小
        add(txtConfirm);

        // 确认密码眼睛按钮
        JButton btnShowConfirm = new JButton("👁️");
        btnShowConfirm.setBounds(340, 110, 35, 25);
        btnShowConfirm.setMargin(new Insets(0, 0, 0, 0));
        btnShowConfirm.setFocusPainted(false);
        btnShowConfirm.setContentAreaFilled(false);
        btnShowConfirm.setBorderPainted(false);
        btnShowConfirm.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowConfirm.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                txtConfirm.setEchoChar((char) 0); // 显示密码
                btnShowConfirm.setText("🔒");
            } else {
                txtConfirm.setEchoChar('•'); // 隐藏密码
                btnShowConfirm.setText("👁️");
            }
        });
        add(btnShowConfirm);

        JLabel lblCode = new JLabel("验证码:");
        lblCode.setBounds(40, 150, 80, 25);
        add(lblCode);

        JTextField txtCode = new JTextField();
        txtCode.setBounds(140, 150, 100, 25);
        add(txtCode);

        JButton btnGetCode = new JButton("获取验证码");
        btnGetCode.setBounds(250, 150, 100, 25);
        add(btnGetCode);

        JButton btnReset = new JButton("重置密码");
        btnReset.setBounds(80, 210, 100, 30);
        add(btnReset);

        JButton btnBack = new JButton("返回登录");
        btnBack.setBounds(200, 210, 120, 30);
        add(btnBack);

        // 事件：点击获取验证码
        btnGetCode.addActionListener((ActionEvent e) -> {
            generatedCode = CodeUtil.generateCode(6);
            System.out.println("验证码是：" + generatedCode);
            JOptionPane.showMessageDialog(this, "验证码已发送（控制台查看）");
        });

        // 事件：重置密码
        btnReset.addActionListener((ActionEvent e) -> {
            String username = txtUsername.getText().trim();
            String newPass = new String(txtNewPass.getPassword());
            String confirmPass = new String(txtConfirm.getPassword());
            String inputCode = txtCode.getText().trim();

            if (username.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty() || inputCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写所有字段！");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "两次密码不一致！");
                return;
            }

            if (!inputCode.equals(generatedCode)) {
                JOptionPane.showMessageDialog(this, "验证码错误！");
                return;
            }

            UserService userService = new UserService();
            User user = userService.getUserByUsername(username);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "用户名不存在！");
                return;
            }

            // ============== 新增的密码检查 ============== //
            // 检查新密码是否与旧密码相同
            if (newPass.equals(user.getPassword())) {
                JOptionPane.showMessageDialog(this, "新密码不能与旧密码相同！");
                return;
            }

            user.setPassword(newPass);
            if (userService.updateUser(user)) {
                JOptionPane.showMessageDialog(this, "密码重置成功！");
                dispose();
                new LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, "密码重置失败！");
            }
        });

        // 返回登录页面
        btnBack.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}

