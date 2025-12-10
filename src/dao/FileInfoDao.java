package dao;  // 声明包名为dao，用于数据访问对象

import model.FileInfo;  // 导入文件信息模型类
import util.DBUtil;  // 导入数据库工具类，用于获取数据库连接

import java.sql.*;  // 导入JDBC相关类
import java.util.ArrayList;  // 导入ArrayList类用于存储列表数据
import java.util.List;  // 导入List接口

// 文件信息数据访问对象类，负责与数据库的文件信息表交互
public class FileInfoDao {

    // 多条件查询文件方法，支持根据文件名、类型、ID、存储路径和记录人查询
    public List<FileInfo> queryFiles(String fileName, String fileType, Integer fileId, String storagePath, String recorder) throws SQLException {
        List<FileInfo> fileList = new ArrayList<>();  // 创建文件列表集合
        StringBuilder sql = new StringBuilder("SELECT * FROM FileInfo WHERE isdeleted = 0");  // 基础SQL，只查询未删除的文件

        // 根据传入参数动态构建SQL查询条件
        if (fileId != null) {
            sql.append(" AND fileId = ?");  // 添加文件ID条件
        }
        if (fileName != null && !fileName.isEmpty()) {
            sql.append(" AND fileName LIKE ?");  // 添加文件名模糊查询条件
        }
        if (fileType != null && !fileType.isEmpty()) {
            sql.append(" AND fileType = ?");  // 添加文件类型条件
        }
        if (storagePath != null && !storagePath.isEmpty()) {
            sql.append(" AND storagePath LIKE ?");  // 添加存储路径模糊查询条件
        }
        if (recorder != null && !recorder.isEmpty()) {
            sql.append(" AND recorder LIKE ?");  // 添加记录人模糊查询条件
        }

        // 使用try-with-resources自动关闭数据库连接和语句对象
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {  // 创建预编译语句

            int idx = 1;  // 参数索引计数器
            if (fileId != null) {
                pstmt.setInt(idx++, fileId);  // 设置文件ID参数
            }
            if (fileName != null && !fileName.isEmpty()) {
                pstmt.setString(idx++, "%" + fileName + "%");  // 设置文件名模糊查询参数
            }
            if (fileType != null && !fileType.isEmpty()) {
                pstmt.setString(idx++, fileType);  // 设置文件类型参数
            }
            if (storagePath != null && !storagePath.isEmpty()) {
                pstmt.setString(idx++, "%" + storagePath + "%");  // 设置存储路径模糊查询参数
            }
            if (recorder != null && !recorder.isEmpty()) {
                pstmt.setString(idx, "%" + recorder + "%");  // 设置记录人模糊查询参数
            }

            try (ResultSet rs = pstmt.executeQuery()) {  // 执行查询获取结果集
                while (rs.next()) {  // 遍历结果集
                    FileInfo f = new FileInfo();  // 创建文件信息对象
                    f.setFileId(rs.getInt("fileId"));  // 设置文件ID
                    f.setFileType(rs.getString("fileType"));  // 设置文件类型
                    f.setFileName(rs.getString("fileName"));  // 设置文件名
                    f.setStoragePath(rs.getString("storagePath"));  // 设置存储路径
                    f.setRecorder(rs.getString("recorder"));  // 设置记录人
                    f.setUserId(rs.getInt("userId"));  // 设置用户ID
                    f.setIsdeleted(rs.getInt("isdeleted"));  // 设置删除标志
                    fileList.add(f);  // 将文件信息添加到列表
                }
            }
        }
        return fileList;  // 返回文件列表
    }

    // 根据文件ID查询文件详情（只查询未删除的文件）
    public FileInfo getFileById(int fileId) throws SQLException {
        String sql = "SELECT * FROM FileInfo WHERE fileId = ? AND isdeleted = 0";  // SQL查询语句
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql)) {  // 创建预编译语句

            pstmt.setInt(1, fileId);  // 设置文件ID参数

            try (ResultSet rs = pstmt.executeQuery()) {  // 执行查询获取结果集
                if (rs.next()) {  // 如果结果集有数据
                    FileInfo f = new FileInfo();  // 创建文件信息对象
                    f.setFileId(rs.getInt("fileId"));  // 设置文件ID
                    f.setFileType(rs.getString("fileType"));  // 设置文件类型
                    f.setFileName(rs.getString("fileName"));  // 设置文件名
                    f.setStoragePath(rs.getString("storagePath"));  // 设置存储路径
                    f.setRecorder(rs.getString("recorder"));  // 设置记录人
                    f.setUserId(rs.getInt("userId"));  // 设置用户ID
                    f.setIsdeleted(rs.getInt("isdeleted"));  // 设置删除标志
                    return f;  // 返回文件信息对象
                }
            }
        }
        return null;  // 未找到文件返回null
    }

    // 添加文件（自动生成ID）
    public boolean addFile(FileInfo fileInfo) throws SQLException {
        String getMaxIdSql = "SELECT MAX(fileId) AS maxId FROM FileInfo";  // 获取当前最大文件ID的SQL
        // 插入语句，默认isdeleted为0（未删除）
        String insertSql = "INSERT INTO FileInfo(fileId, fileType, fileName, storagePath, recorder, userId, isdeleted) VALUES (?, ?, ?, ?, ?, ?, 0)";

        int newId = 1;  // 默认新ID为1

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             Statement stmt = conn.createStatement();  // 创建普通语句对象
             ResultSet rs = stmt.executeQuery(getMaxIdSql)) {  // 执行获取最大ID的查询

            if (rs.next()) {  // 如果结果集有数据
                int maxId = rs.getInt("maxId");  // 获取当前最大ID
                if (!rs.wasNull() && maxId > 0) {  // 检查是否为null且大于0
                    newId = maxId + 1;  // 新ID为最大ID+1
                }
            }

            // 打印调试信息，显示要添加的文件参数
            System.out.println("添加文件参数: [" + newId + ", " + fileInfo.getFileType() + ", "
                    + fileInfo.getFileName() + ", " + fileInfo.getStoragePath() + ", "
                    + fileInfo.getRecorder() + ", " + fileInfo.getUserId() + "]");

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {  // 创建插入预编译语句
                pstmt.setInt(1, newId);  // 设置文件ID参数
                pstmt.setString(2, fileInfo.getFileType());  // 设置文件类型参数
                pstmt.setString(3, fileInfo.getFileName());  // 设置文件名参数
                pstmt.setString(4, fileInfo.getStoragePath());  // 设置存储路径参数
                pstmt.setString(5, fileInfo.getRecorder());  // 设置记录人参数
                pstmt.setInt(6, fileInfo.getUserId());  // 设置用户ID参数

                int result = pstmt.executeUpdate();  // 执行插入操作，返回受影响行数
                return result == 1;  // 返回是否插入成功
            }
        } catch (SQLException e) {  // 捕获SQL异常
            System.err.println("添加文件失败: " + e.getMessage());  // 打印错误信息
            System.err.println("SQL: " + insertSql);  // 打印SQL语句
            throw e;  // 重新抛出异常
        }
    }

    // 更新文件信息
    public boolean updateFile(FileInfo fileInfo) throws SQLException {
        // 更新语句，只更新未删除的文件
        String sql = "UPDATE FileInfo SET fileType=?, fileName=?, storagePath=?, recorder=?, userId=? WHERE fileId=? AND isdeleted=0";

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql)) {  // 创建预编译语句

            pstmt.setString(1, fileInfo.getFileType());  // 设置文件类型参数
            pstmt.setString(2, fileInfo.getFileName());  // 设置文件名参数
            pstmt.setString(3, fileInfo.getStoragePath());  // 设置存储路径参数
            pstmt.setString(4, fileInfo.getRecorder());  // 设置记录人参数
            pstmt.setInt(5, fileInfo.getUserId());  // 设置用户ID参数
            pstmt.setInt(6, fileInfo.getFileId());  // 设置文件ID参数（WHERE条件）

            int result = pstmt.executeUpdate();  // 执行更新操作，返回受影响行数
            return result == 1;  // 返回是否更新成功
        } catch (SQLException e) {  // 捕获SQL异常
            System.err.println("更新文件失败: " + e.getMessage());  // 打印错误信息
            System.err.println("SQL: " + sql);  // 打印SQL语句
            // 打印更新参数
            System.err.println("参数: [" + fileInfo.getFileId() + ", " + fileInfo.getFileType() + ", "
                    + fileInfo.getFileName() + ", " + fileInfo.getStoragePath() + ", "
                    + fileInfo.getRecorder() + ", " + fileInfo.getUserId() + "]");
            throw e;  // 重新抛出异常
        }
    }

    // 删除文件（软删除）
    public boolean deleteFile(int fileId) throws SQLException {
        String sql = "UPDATE FileInfo SET isdeleted = 1 WHERE fileId = ?";  // 软删除SQL（标记为已删除）

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql)) {  // 创建预编译语句

            pstmt.setInt(1, fileId);  // 设置文件ID参数

            int result = pstmt.executeUpdate();  // 执行更新操作，返回受影响行数
            return result == 1;  // 返回是否删除成功
        } catch (SQLException e) {  // 捕获SQL异常
            System.err.println("删除文件失败: " + e.getMessage());  // 打印错误信息
            System.err.println("SQL: " + sql);  // 打印SQL语句
            System.err.println("文件ID: " + fileId);  // 打印文件ID
            throw e;  // 重新抛出异常
        }
    }

    // 恢复文件（从回收站恢复）
    public boolean restoreFile(int fileId) throws SQLException {
        String sql = "UPDATE FileInfo SET isdeleted = 0 WHERE fileId = ?";  // 恢复文件SQL（标记为未删除）

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql)) {  // 创建预编译语句

            pstmt.setInt(1, fileId);  // 设置文件ID参数

            int result = pstmt.executeUpdate();  // 执行更新操作，返回受影响行数
            return result == 1;  // 返回是否恢复成功
        } catch (SQLException e) {  // 捕获SQL异常
            System.err.println("恢复文件失败: " + e.getMessage());  // 打印错误信息
            System.err.println("SQL: " + sql);  // 打印SQL语句
            System.err.println("文件ID: " + fileId);  // 打印文件ID
            throw e;  // 重新抛出异常
        }
    }

    // 永久删除文件（物理删除）
    public boolean permanentDelete(int fileId) throws SQLException {
        String sql = "DELETE FROM FileInfo WHERE fileId = ?";  // 物理删除SQL

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql)) {  // 创建预编译语句

            pstmt.setInt(1, fileId);  // 设置文件ID参数

            int result = pstmt.executeUpdate();  // 执行删除操作，返回受影响行数
            return result == 1;  // 返回是否永久删除成功
        } catch (SQLException e) {  // 捕获SQL异常
            System.err.println("永久删除文件失败: " + e.getMessage());  // 打印错误信息
            System.err.println("SQL: " + sql);  // 打印SQL语句
            System.err.println("文件ID: " + fileId);  // 打印文件ID
            throw e;  // 重新抛出异常
        }
    }

    // 获取已删除文件列表（回收站）
    public List<FileInfo> getDeletedFiles() throws SQLException {
        List<FileInfo> fileList = new ArrayList<>();  // 创建文件列表集合
        String sql = "SELECT * FROM FileInfo WHERE isdeleted = 1";  // 查询已删除文件的SQL

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DBUtil.getConnection();  // 获取数据库连接
             PreparedStatement pstmt = conn.prepareStatement(sql);  // 创建预编译语句
             ResultSet rs = pstmt.executeQuery()) {  // 执行查询获取结果集

            while (rs.next()) {  // 遍历结果集
                FileInfo f = new FileInfo();  // 创建文件信息对象
                f.setFileId(rs.getInt("fileId"));  // 设置文件ID
                f.setFileType(rs.getString("fileType"));  // 设置文件类型
                f.setFileName(rs.getString("fileName"));  // 设置文件名
                f.setStoragePath(rs.getString("storagePath"));  // 设置存储路径
                f.setRecorder(rs.getString("recorder"));  // 设置记录人
                f.setUserId(rs.getInt("userId"));  // 设置用户ID
                f.setIsdeleted(rs.getInt("isdeleted"));  // 设置删除标志
                fileList.add(f);  // 将文件信息添加到列表
            }
        }
        return fileList;  // 返回已删除文件列表
    }
}



