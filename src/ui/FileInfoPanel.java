package ui;

import model.FileInfo;
import model.User;
import service.FileInfoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.image.BufferedImage;

public class FileInfoPanel extends JPanel {
    private JTextField txtSearchName;
    private JTextField txtSearchId;
    private JTextField txtRecorder;
    private JComboBox<String> cmbSearchType;
    private JButton btnSearch, btnAdd, btnEdit, btnDelete;
    private JButton btnRefresh;
    private JButton btnHome;
    private JTable table;
    private DefaultTableModel tableModel;
    private FileInfoService fileInfoService;
    private boolean isReadOnly;
    private User currentUser;
    private JButton btnRecycleBin; // 回收站按钮

    // 统计相关组件
    private JTable statsTable;
    private DefaultTableModel statsTableModel;
    private PieChartPanel pieChartPanel;

    // 图标路径（根据实际项目路径修改）
    private static final String ICON_PATH = "D:/Java/OfficeManagement/icons/";

    public FileInfoPanel(FileInfoService fileInfoService, boolean isReadOnly, User currentUser) {
        this.fileInfoService = fileInfoService;
        this.isReadOnly = isReadOnly;
        this.currentUser = currentUser;
        initUI();
        loadTableData(null, null, null, null);
        loadStatsTableData(); // 初始化统计数据
    }

    /**
     * 创建缩放图标（统一为24x24尺寸）
     */
    private ImageIcon createScaledIcon(String filename) {
        ImageIcon originalIcon = new ImageIcon(ICON_PATH + filename);
        if (originalIcon.getIconWidth() <= 0) {
            return new ImageIcon(); // 图标加载失败时返回空图标
        }
        Image img = originalIcon.getImage();
        Image scaledImg = img.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        return new ImageIcon(scaledImg);
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 主内容面板
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));

        // 顶部面板（查询+统计）
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));

        // 左侧查询面板
        JPanel searchPanel = createSearchPanel();
        topPanel.add(searchPanel, BorderLayout.CENTER);

        // 右侧统计面板
        JPanel statsPanel = createStatsPanel();
        topPanel.add(statsPanel, BorderLayout.EAST);

        mainContentPanel.add(topPanel, BorderLayout.NORTH);

        // 表格面板
        String[] columns = {"文件ID", "文件类型", "文件名", "存放位置", "记录人", "记录人ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 表格不可编辑
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false); // 禁止列拖动
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("文件列表"));
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = createButtonPanel();
        mainContentPanel.add(btnPanel, BorderLayout.SOUTH);

        add(mainContentPanel, BorderLayout.CENTER);
    }

    /**
     * 创建查询面板
     */
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("文件查询条件"));

        // 第一行：文件编号和文件名
        JPanel row1Panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        row1Panel.add(new JLabel("文件编号:"));
        txtSearchId = new JTextField(10);
        row1Panel.add(txtSearchId);

        row1Panel.add(new JLabel("文件名:"));
        txtSearchName = new JTextField(15);
        row1Panel.add(txtSearchName);
        searchPanel.add(row1Panel);

        // 第二行：文件类型、记录人和查询按钮
        JPanel row2Panel = new JPanel(new BorderLayout(10, 0));

        // 左侧查询条件
        JPanel leftPart = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leftPart.add(new JLabel("文件类型:"));
        cmbSearchType = new JComboBox<>(new String[]{"", "合同", "报表", "设计", "政策", "其他"});
        leftPart.add(cmbSearchType);

        leftPart.add(new JLabel("记录人:"));
        txtRecorder = new JTextField(15);
        leftPart.add(txtRecorder);

        row2Panel.add(leftPart, BorderLayout.CENTER);

        // 右侧查询按钮
        JPanel rightPart = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnSearch = new JButton("查询", createScaledIcon("search.jpeg"));
        btnSearch.addActionListener(e -> onSearch());
        rightPart.add(btnSearch);

        row2Panel.add(rightPart, BorderLayout.EAST);
        searchPanel.add(row2Panel);

        return searchPanel;
    }

    /**
     * 创建统计面板（饼图+表格）
     */
    private JPanel createStatsPanel() {
        // 使用BorderLayout替代GridLayout，实现左右分栏
        JPanel statsPanel = new JPanel(new BorderLayout(10, 0));
        statsPanel.setBorder(BorderFactory.createTitledBorder("文件统计"));
        statsPanel.setPreferredSize(new Dimension(600, 300)); // 增加宽度以适应新布局

        // 饼图面板（左边）
        pieChartPanel = new PieChartPanel();
        statsPanel.add(pieChartPanel, BorderLayout.CENTER); // 饼图占据中心区域

        // 统计表格（右边）
        String[] statsColumns = {"文件类型", "数量", "占比"};
        statsTableModel = new DefaultTableModel(statsColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        statsTable = new JTable(statsTableModel);
        statsTable.setRowHeight(22);
        statsTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane statsScrollPane = new JScrollPane(statsTable);
        statsScrollPane.setBorder(BorderFactory.createTitledBorder("统计详情"));
        statsScrollPane.setPreferredSize(new Dimension(200, 250)); // 设置表格宽度

        // 将表格放在右侧面板中
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(statsScrollPane, BorderLayout.CENTER);
        statsPanel.add(tablePanel, BorderLayout.EAST); // 表格放在右侧

        return statsPanel;
    }

    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel btnPanel = new JPanel(new BorderLayout());

        // 左侧按钮（刷新、主页）
        JPanel leftBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnHome = new JButton("返回主页", createScaledIcon("home.jpeg"));
        btnRefresh = new JButton("刷新", createScaledIcon("refresh.jpeg"));
        leftBtnPanel.add(btnHome);
        leftBtnPanel.add(btnRefresh);
        btnPanel.add(leftBtnPanel, BorderLayout.WEST);

        // 右侧按钮（回收站、添加、编辑、删除）
        JPanel rightBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnRecycleBin = new JButton("回收站", createScaledIcon("recycle.jpg"));
        btnRecycleBin.addActionListener(e -> openRecycleBin());
        btnAdd = new JButton("添加", createScaledIcon("add.jpeg"));
        btnEdit = new JButton("编辑", createScaledIcon("edit.jpeg"));
        btnDelete = new JButton("删除", createScaledIcon("delete.jpeg"));

        // 权限控制
        if (currentUser != null) {
            boolean canView = currentUser.hasPermission("file_view");
            boolean canAdd = currentUser.hasPermission("file_add");
            boolean canEdit = currentUser.hasPermission("file_edit");
            boolean canDelete = currentUser.hasPermission("file_delete");

            if (!canView) {
                JOptionPane.showMessageDialog(this, "您没有文件查看权限");
                return btnPanel;
            }

            btnAdd.setVisible(canAdd);
            btnEdit.setVisible(canEdit);
            btnDelete.setVisible(canDelete);
            btnRecycleBin.setVisible(currentUser.getRoleId() == 1); // 仅管理员可见
        }

        // 绑定事件
        btnRefresh.addActionListener(e -> onRefresh());
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnHome.addActionListener(e -> returnToHome());

        rightBtnPanel.add(btnRecycleBin);
        rightBtnPanel.add(btnAdd);
        rightBtnPanel.add(btnEdit);
        rightBtnPanel.add(btnDelete);
        btnPanel.add(rightBtnPanel, BorderLayout.EAST);

        return btnPanel;
    }

    /**
     * 加载表格数据
     */
    private void loadTableData(String name, String type, Integer fileId, String recorder) {
        tableModel.setRowCount(0);
        List<FileInfo> list = fileInfoService.searchFiles(name, type, fileId, null, recorder);
        if (list != null) {
            for (FileInfo f : list) {
                tableModel.addRow(new Object[]{
                        f.getFileId(),
                        f.getFileType(),
                        f.getFileName(),
                        f.getStoragePath(),
                        f.getRecorder(),
                        f.getUserId()
                });
            }
        }
    }

    /**
     * 加载统计数据
     */
    private void loadStatsTableData() {
        statsTableModel.setRowCount(0);
        Map<String, Integer> stats = fileInfoService.getFileTypeStatistics();

        if (stats == null || stats.isEmpty()) return;

        // 计算总数
        int total = stats.values().stream().mapToInt(Integer::intValue).sum();

        // 填充统计表格
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            String type = entry.getKey();
            int count = entry.getValue();
            double percentage = total > 0 ? (count * 100.0 / total) : 0;

            statsTableModel.addRow(new Object[]{
                    type,
                    count,
                    String.format("%.1f%%", percentage)
            });
        }

        // 更新饼图
        pieChartPanel.setData(stats);
        pieChartPanel.repaint();
    }

    /**
     * 操作后刷新数据
     */
    private void refreshAfterOperation() {
        loadTableData(null, null, null, null);
        loadStatsTableData();
    }

    /**
     * 返回主页
     */
    private void returnToHome() {
        Window topWindow = SwingUtilities.getWindowAncestor(this);
        if (topWindow instanceof AdminMainFrame) {
            ((AdminMainFrame) topWindow).backToHome();
        } else if (topWindow instanceof UserMainFrame) {
            ((UserMainFrame) topWindow).backToHome();
        } else {
            JOptionPane.showMessageDialog(this, "无法返回主页，请联系管理员");
        }
    }

    /**
     * 刷新数据
     */
    private void onRefresh() {
        txtSearchId.setText("");
        txtSearchName.setText("");
        cmbSearchType.setSelectedIndex(0);
        txtRecorder.setText("");

        loadTableData(null, null, null, null);
        loadStatsTableData();
        JOptionPane.showMessageDialog(this, "数据已刷新，显示所有文件");
    }

    /**
     * 搜索文件
     */
    private void onSearch() {
        String idStr = txtSearchId.getText().trim();
        String name = txtSearchName.getText().trim();
        String type = (String) cmbSearchType.getSelectedItem();
        String recorder = txtRecorder.getText().trim();

        // 处理空值
        if ("".equals(type)) type = null;
        if (name.isEmpty()) name = null;
        if (recorder.isEmpty()) recorder = null;

        Integer fileId = null;
        if (!idStr.isEmpty()) {
            try {
                fileId = Integer.parseInt(idStr);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "文件编号必须为数字", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        loadTableData(name, type, fileId, recorder);
    }

    /**
     * 添加文件
     */
    private void onAdd() {
        if (!currentUser.hasPermission("file_add")) {
            JOptionPane.showMessageDialog(this, "您没有添加文件的权限");
            return;
        }

        int nextId = fileInfoService.getMaxFileId() + 1;
        FileInfo f = new FileInfo();
        f.setFileId(nextId);

        f.setFileType(JOptionPane.showInputDialog(this, "请输入文件类型（如：合同、报表、设计、政策）"));
        if (f.getFileType() == null || f.getFileType().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "文件类型不能为空");
            return;
        }

        f.setFileName(JOptionPane.showInputDialog(this, "请输入文件名"));
        if (f.getFileName() == null || f.getFileName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "文件名不能为空");
            return;
        }

        f.setStoragePath(JOptionPane.showInputDialog(this, "请输入存放路径"));
        f.setRecorder(JOptionPane.showInputDialog(this, "请输入记录人"));

        String userIdStr = JOptionPane.showInputDialog(this, "请输入用户ID");
        try {
            f.setUserId(Integer.parseInt(userIdStr));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "用户ID必须为数字");
            return;
        }

        if (fileInfoService.addFile(f)) {
            JOptionPane.showMessageDialog(this, "添加成功");
            refreshAfterOperation();
        } else {
            JOptionPane.showMessageDialog(this, "添加失败");
        }
    }

    /**
     * 编辑文件
     */
    private void onEdit() {
        if (!currentUser.hasPermission("file_edit")) {
            JOptionPane.showMessageDialog(this, "您没有编辑文件的权限");
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择一条记录");
            return;
        }

        int fileId = (int) tableModel.getValueAt(row, 0);
        FileInfo f = fileInfoService.getFileById(fileId);
        if (f == null) {
            JOptionPane.showMessageDialog(this, "文件不存在");
            return;
        }

        String fileType = JOptionPane.showInputDialog(this, "文件类型", f.getFileType());
        String fileName = JOptionPane.showInputDialog(this, "文件名", f.getFileName());
        String storagePath = JOptionPane.showInputDialog(this, "存放路径", f.getStoragePath());
        String recorder = JOptionPane.showInputDialog(this, "记录人", f.getRecorder());
        String userIdStr = JOptionPane.showInputDialog(this, "用户ID", f.getUserId());

        try {
            int userId = Integer.parseInt(userIdStr);
            f.setFileType(fileType);
            f.setFileName(fileName);
            f.setStoragePath(storagePath);
            f.setRecorder(recorder);
            f.setUserId(userId);

            if (fileInfoService.updateFile(f)) {
                JOptionPane.showMessageDialog(this, "更新成功");
                refreshAfterOperation();
            } else {
                JOptionPane.showMessageDialog(this, "更新失败");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "用户ID必须为数字");
        }
    }

    /**
     * 删除文件
     */
    private void onDelete() {
        if (!currentUser.hasPermission("file_delete")) {
            JOptionPane.showMessageDialog(this, "您没有删除文件的权限");
            return;
        }

        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请选择一条记录");
            return;
        }

        int fileId = (int) tableModel.getValueAt(row, 0);
        FileInfo file = fileInfoService.getFileById(fileId);
        if (file == null) {
            JOptionPane.showMessageDialog(this, "文件不存在或已被删除");
            return;
        }

        Object[] options = {"是(Y)", "否(N)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "确定删除以下文件吗？\n\n" +
                        "文件ID: " + file.getFileId() + "\n" +
                        "文件名: " + file.getFileName() + "\n" +
                        "文件类型: " + file.getFileType() + "\n",
                "确认删除文件",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[1]
        );

        if (choice == JOptionPane.YES_OPTION) {
            if (fileInfoService.deleteFile(fileId)) {
                JOptionPane.showMessageDialog(this, "删除成功");
                refreshAfterOperation();
            } else {
                JOptionPane.showMessageDialog(this, "删除失败");
            }
        }
    }

    /**
     * 打开回收站
     */
    private void openRecycleBin() {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parentFrame, "回收站", true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(parentFrame);

        RecycleBinPanel recyclePanel = new RecycleBinPanel(fileInfoService);
        dialog.add(recyclePanel);
        dialog.setVisible(true);
    }

    /**
     * 显示指定类型的文件列表
     */
    private void showFilesByType(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "文件类型无效");
            return;
        }

        List<FileInfo> files = fileInfoService.searchFiles(null, fileType, null, null, null);
        if (files == null || files.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到类型为【" + fileType + "】的文件");
            return;
        }

        // 构建弹窗
        JPanel panel = new JPanel(new BorderLayout());
        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (FileInfo file : files) {
            listModel.addElement("ID: " + file.getFileId() + " | 名称: " + file.getFileName() + " | 记录人: " + file.getRecorder());
        }

        JList<String> fileList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(fileList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("类型为【" + fileType + "】的文件列表（共" + files.size() + "个）"));
        panel.add(scrollPane, BorderLayout.CENTER);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "文件类型详情：" + fileType);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.getContentPane().add(panel);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        // 同步更新主表格
        cmbSearchType.setSelectedItem(fileType);
        onSearch();
    }

    /**
     * 饼图面板（显示文件类型分布）
     */
    class PieChartPanel extends JPanel {
        private Map<String, Integer> data;  // 文件类型统计数据
        private Map<String, Color> colorMap = new HashMap<>();  // 类型 → 原始颜色
        private final Map<Color, String> displayColorToType = new HashMap<>(); // 实际显示颜色 → 类型
        private double total;
        private final List<String> typeOrder = Arrays.asList("合同", "报表", "设计", "政策", "其他");

        public PieChartPanel() {
            // 原始颜色映射
            colorMap.put("合同", new Color(65, 105, 225));    // 蓝色
            colorMap.put("报表", new Color(50, 205, 50));     // 绿色
            colorMap.put("设计", new Color(255, 165, 0));     // 橙色
            colorMap.put("政策", new Color(220, 20, 60));     // 红色
            colorMap.put("其他", new Color(148, 0, 211));     // 紫色

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (data == null || data.isEmpty()) return;
                    String clickedType = getTypeByClick(e.getX(), e.getY());
                    if (clickedType != null) {
                        showFilesByType(clickedType);
                    }
                }
            });
        }

        public void setData(Map<String, Integer> data) {
            this.data = data;
            this.total = data.values().stream().mapToInt(Integer::intValue).sum();
            repaint();
        }

        private String getTypeByClick(int x, int y) {
            if (total <= 0) return null;

            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            paintComponent(g2d); // 重绘一次图像到缓冲区
            g2d.dispose();

            if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) return null;

            int rgb = image.getRGB(x, y);
            Color clickedColor = new Color(rgb, true);

            for (Color displayColor : displayColorToType.keySet()) {
                if (isSimilarColor(displayColor, clickedColor)) {
                    return displayColorToType.get(displayColor);
                }
            }
            return null;
        }

        private boolean isSimilarColor(Color c1, Color c2) {
            int tolerance = 20;
            return Math.abs(c1.getRed() - c2.getRed()) < tolerance &&
                    Math.abs(c1.getGreen() - c2.getGreen()) < tolerance &&
                    Math.abs(c1.getBlue() - c2.getBlue()) < tolerance;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty() || total <= 0) {
                g.drawString("没有数据可显示", getWidth() / 2 - 30, getHeight() / 2);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diameter = (int) (Math.min(getWidth(), getHeight()) * 0.7);
            int centerX = getWidth() / 2;
            int centerY = (getHeight() - 80) / 2;
            double currentStartAngle = 0;

            displayColorToType.clear(); // 每次绘图清空颜色 → 类型映射

            for (String type : typeOrder) {
                if (!data.containsKey(type)) continue;

                int count = data.get(type);
                double sliceAngle = 360 * (count / total);

                Color originalColor = colorMap.getOrDefault(type, new Color(148, 0, 211));
                Color displayColor;

                // 颜色转换：蓝→红，红→蓝，绿→橙，橙→绿，其它保持
                if (originalColor.equals(new Color(65, 105, 225))) { // 蓝
                    displayColor = new Color(220, 20, 60); // 红
                } else if (originalColor.equals(new Color(220, 20, 60))) { // 红
                    displayColor = new Color(65, 105, 225); // 蓝
                } else if (originalColor.equals(new Color(50, 205, 50))) { // 绿
                    displayColor = new Color(255, 165, 0); // 橙
                } else if (originalColor.equals(new Color(255, 165, 0))) { // 橙
                    displayColor = new Color(50, 205, 50); // 绿
                } else {
                    displayColor = originalColor;
                }

                displayColorToType.put(displayColor, type); // 显示颜色 → 类型

                g2d.setColor(displayColor);
                g2d.fill(new Arc2D.Double(
                        centerX - diameter / 2,
                        centerY - diameter / 2,
                        diameter,
                        diameter,
                        currentStartAngle,
                        sliceAngle,
                        Arc2D.PIE
                ));

                currentStartAngle += sliceAngle;
            }

            // 绘制图例（仍用原始颜色）
            int legendX = centerX + diameter / 2 - 20;
            int legendY = centerY + diameter / 2 - 20;

            g2d.setFont(new Font("宋体", Font.BOLD, 11));
            g2d.drawString("文件类型图例：", legendX, legendY);
            legendY += 18;

            for (String type : typeOrder) {
                if (!data.containsKey(type)) continue;

                int count = data.get(type);
                double percentage = total > 0 ? (count * 100.0 / total) : 0;

                // 计算图例颜色（与扇形一致）
                Color originalColor = colorMap.getOrDefault(type, new Color(148, 0, 211));
                Color displayColor;

                if (originalColor.equals(new Color(65, 105, 225))) {
                    displayColor = new Color(220, 20, 60);
                } else if (originalColor.equals(new Color(220, 20, 60))) {
                    displayColor = new Color(65, 105, 225);
                } else if (originalColor.equals(new Color(50, 205, 50))) {
                    displayColor = new Color(255, 165, 0);
                } else if (originalColor.equals(new Color(255, 165, 0))) {
                    displayColor = new Color(50, 205, 50);
                } else {
                    displayColor = originalColor;
                }

                g2d.setColor(displayColor); // ✅ 与饼图一致
                g2d.fillRect(legendX, legendY, 12, 12);

                g2d.setColor(Color.BLACK);
                String text = type + "：" + count + "个（" + String.format("%.1f", percentage) + "%）";
                g2d.drawString(text, legendX + 15, legendY + 10);

                legendY += 18;
            }

        }
    }

}



