package service;

import dao.FileInfoDao;
import model.FileInfo;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileInfoService {

    private FileInfoDao fileInfoDao = new FileInfoDao();

    // 更新获取所有文件的方法
    public List<FileInfo> getAllFiles() {
        try {
            // 传入null表示不添加任何过滤条件
            return fileInfoDao.queryFiles(null, null, null, null, null);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 更新查询方法
    public List<FileInfo> searchFiles(String fileName, String fileType,
                                      Integer fileId, String storagePath,
                                      String recorder) {
        try {
            return fileInfoDao.queryFiles(fileName, fileType, fileId, storagePath, recorder);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据ID获取文件
    public FileInfo getFileById(int fileId) {
        try {
            return fileInfoDao.getFileById(fileId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 添加文件
    public boolean addFile(FileInfo fileInfo) {
        try {
            return fileInfoDao.addFile(fileInfo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新文件
    public boolean updateFile(FileInfo fileInfo) {
        try {
            return fileInfoDao.updateFile(fileInfo);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除文件
    public boolean deleteFile(int fileId) {
        try {
            return fileInfoDao.deleteFile(fileId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增：获取当前最大fileId
    public int getMaxFileId() {
        try {
            List<FileInfo> allFiles = getAllFiles();
            int maxId = 0;
            for (FileInfo file : allFiles) {
                if (file.getFileId() > maxId) {
                    maxId = file.getFileId();
                }
            }
            return maxId;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 新增恢复文件方法
    public boolean restoreFile(int fileId) {
        try {
            return fileInfoDao.restoreFile(fileId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增获取已删除文件列表方法
    public List<FileInfo> getDeletedFiles() {
        try {
            return fileInfoDao.getDeletedFiles();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 新增永久删除方法
    public boolean permanentDelete(int fileId) {
        try {
            return fileInfoDao.permanentDelete(fileId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Integer> getFileTypeStatistics() {
        try {
            Map<String, Integer> stats = new HashMap<>();
            List<FileInfo> allFiles = fileInfoDao.queryFiles(null, null, null, null, null);
            for (FileInfo file : allFiles) {
                String type = file.getFileType();
                stats.put(type, stats.getOrDefault(type, 0) + 1);
            }
            return stats;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}





