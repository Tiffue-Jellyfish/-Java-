package main;

import ui.AdminMainFrame;
import ui.LoginFrame;
import ui.UserMainFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::showLoginFrame);
    }

    // 统一封装登录流程，确保每次创建 LoginFrame 都设置好回调
    public static void showLoginFrame() {
        LoginFrame loginFrame = new LoginFrame();

        // 设置登录成功后的回调处理逻辑
        loginFrame.setOnLoginSuccess(user -> {
            SwingUtilities.invokeLater(() -> {
                if (user.getRoleId() == 1) {
                    new AdminMainFrame(user);  // 管理员界面
                } else {
                    new UserMainFrame(user);   // 普通用户界面
                }
            });
        });
    }
}



