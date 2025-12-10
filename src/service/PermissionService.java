package service;

import dao.PermissionDao;
import model.Permission;
import java.util.List;

public class PermissionService {
    private PermissionDao permissionDao = new PermissionDao();

    public List<Permission> getAllPermissions() {
        return permissionDao.getAllPermissions();
    }

    public List<Permission> getPermissionsByUserId(int userId) {
        return permissionDao.getPermissionsByUserId(userId);
    }

    public boolean assignPermissionToUser(int userId, int permissionId) {
        return permissionDao.assignPermissionToUser(userId, permissionId);
    }

    public boolean removePermissionFromUser(int userId, int permissionId) {
        return permissionDao.removePermissionFromUser(userId, permissionId);
    }

    public List<Integer> getPermissionIdsByUserId(int userId) {
        return permissionDao.getPermissionIdsByUserId(userId);
    }

    public boolean savePermissionsForUser(int userId, List<Permission> permissions) {
        try {
            //  Ƴ  û       Ȩ
            if (!permissionDao.removeAllPermissionsForUser(userId)) {
                throw new Exception(" Ƴ Ȩ  ʧ  ");
            }

            //    ѡ е Ȩ
            for (Permission perm : permissions) {
                if (perm.isSelected()) {
                    if (!permissionDao.assignPermissionToUser(userId, perm.getPermissionId())) {
                        throw new Exception("   Ȩ  ʧ  : " + perm.getPermissionName());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("     û Ȩ  ʧ  : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}