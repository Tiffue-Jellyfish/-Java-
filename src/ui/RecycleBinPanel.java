package ui;

import model.FileInfo;
import service.FileInfoService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class RecycleBinPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private FileInfoService fileInfoService;
    private JButton btnRestore, btnPermanentDelete, btnBack;

    public RecycleBinPanel(FileInfoService fileInfoService) {
        this.fileInfoService = fileInfoService;
        initUI();
        loadDeletedFiles();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10)); // 增加间距

        // 表格设置
        String[] columns = {"文件ID", "文件类型", "文件名", "存放位置", "记录人"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnRestore = new JButton("恢复文件");
        btnPermanentDelete = new JButton("永久删除");
        btnBack = new JButton("返回"); // 新增返回按钮

        btnRestore.addActionListener(this::onRestore);
        btnPermanentDelete.addActionListener(this::onPermanentDelete);
        btnBack.addActionListener(e -> closeWindow()); // 关闭窗口

        btnPanel.add(btnRestore);
        btnPanel.add(btnPermanentDelete);
        btnPanel.add(btnBack); // 添加返回按钮
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadDeletedFiles() {
        tableModel.setRowCount(0);
        List<FileInfo> deletedFiles = fileInfoService.getDeletedFiles();
        if (deletedFiles != null) {
            for (FileInfo file : deletedFiles) {
                tableModel.addRow(new Object[]{
                        file.getFileId(),
                        file.getFileType(),
                        file.getFileName(),
                        file.getStoragePath(),
                        file.getRecorder()
                });
            }
        }
    }

    private void onRestore(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要恢复的文件");
            return;
        }

        int fileId = (int) tableModel.getValueAt(row, 0);
        if (fileInfoService.restoreFile(fileId)) {
            JOptionPane.showMessageDialog(this, "文件已恢复");
            loadDeletedFiles(); // 刷新列表
        } else {
            JOptionPane.showMessageDialog(this, "恢复失败");
        }
    }

    private void onPermanentDelete(ActionEvent e) {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择要删除的文件");
            return;
        }

        int fileId = (int) tableModel.getValueAt(row, 0);

        // 添加确认对话框
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "确定要永久删除此文件吗？此操作不可恢复！",
                "永久删除确认",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (fileInfoService.permanentDelete(fileId)) {
                JOptionPane.showMessageDialog(this, "文件已永久删除");
                loadDeletedFiles(); // 刷新列表
            } else {
                JOptionPane.showMessageDialog(this, "删除失败");
            }
        }
    }

    // 新增关闭窗口的方法
    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

}