package ui;

import model.Department;
import model.User;
import service.DepartmentService;
import service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RegisterFrame extends JFrame {

    private static final String COMPANY_SECRET = "Admin@2024";
    private boolean passwordVisible = false; // 控制密码是否可见

    public RegisterFrame() {
        setTitle("注册新用户");
        setSize(400, 380);
        setLocationRelativeTo(null);
        setLayout(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JLabel lblUsername = new JLabel("用户名:");
        lblUsername.setBounds(40, 30, 80, 25);
        add(lblUsername);

        JTextField txtUsername = new JTextField();
        txtUsername.setBounds(140, 30, 200, 25);
        add(txtUsername);

        JLabel lblPassword = new JLabel("密码:");
        lblPassword.setBounds(40, 70, 80, 25);
        add(lblPassword);

        // 密码框和眼睛按钮
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setBounds(140, 70, 200, 25); // 宽度减小
        add(txtPassword);

        // 密码眼睛按钮
        JButton btnShowPassword = new JButton("👁️");
        btnShowPassword.setBounds(345, 70, 35, 25);
        btnShowPassword.setMargin(new Insets(0, 0, 0, 0));
        btnShowPassword.setFocusPainted(false);
        btnShowPassword.setContentAreaFilled(false);
        btnShowPassword.setBorderPainted(false);
        btnShowPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowPassword.addActionListener(e -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                txtPassword.setEchoChar((char) 0); // 显示密码
                btnShowPassword.setText("🔒");
            } else {
                txtPassword.setEchoChar('•'); // 隐藏密码
                btnShowPassword.setText("👁️");
            }
        });
        add(btnShowPassword);

        JLabel lblConfirm = new JLabel("确认密码:");
        lblConfirm.setBounds(40, 110, 80, 25);
        add(lblConfirm);

        // 确认密码框和眼睛按钮
        JPasswordField txtConfirm = new JPasswordField();
        txtConfirm.setBounds(140, 110, 200, 25); // 宽度减小
        add(txtConfirm);

        // 确认密码眼睛按钮
        JButton btnShowConfirm = new JButton("👁️");
        btnShowConfirm.setBounds(345, 110, 35, 25);
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

        JLabel lblDept = new JLabel("所属部门:");
        lblDept.setBounds(40, 150, 80, 25);
        add(lblDept);

        JComboBox<String> deptBox = new JComboBox<>();
        deptBox.setBounds(140, 150, 200, 25);
        add(deptBox);

        DepartmentService deptService = new DepartmentService();
        List<Department> departments = deptService.getAllDepartments();
        for (Department d : departments) {
            deptBox.addItem(d.getDepartmentName());
        }

        JLabel lblSecret = new JLabel("公司密钥:");
        lblSecret.setBounds(40, 190, 80, 25);
        add(lblSecret);

        JTextField txtSecret = new JTextField();
        txtSecret.setBounds(140, 190, 200, 25);
        add(txtSecret);

        JButton btnRegister = new JButton("注册");
        btnRegister.setBounds(80, 250, 100, 30);
        add(btnRegister);

        JButton btnCancel = new JButton("取消");
        btnCancel.setBounds(200, 250, 100, 30);
        add(btnCancel);

        btnRegister.addActionListener((ActionEvent e) -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword());
            String confirm = new String(txtConfirm.getPassword());
            String secret = txtSecret.getText().trim();
            String deptName = (String) deptBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty() || deptName == null || secret.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请填写所有字段！");
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "两次密码不一致！");
                return;
            }
            if (!secret.equals(COMPANY_SECRET)) {
                JOptionPane.showMessageDialog(this, "公司密钥错误！");
                return;
            }

            UserService userService = new UserService();
            if (userService.isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(this, "用户名已存在！");
                return;
            }

            Department dept = deptService.getDepartmentByName(deptName);
            if (dept == null) {
                JOptionPane.showMessageDialog(this, "部门选择无效！");
                return;
            }

            User user = new User();
            user.setName(username);
            user.setPassword(password);
            user.setDepartmentId(dept.getDepartmentId());
            user.setRoleId(2); // 普通员工

            if (userService.addUser(user)) {
                JOptionPane.showMessageDialog(this, "注册成功！");
                dispose();
                new LoginFrame();
            } else {
                JOptionPane.showMessageDialog(this, "注册失败，请稍后再试！");
            }
        });

        btnCancel.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        setVisible(true);
    }
}

