package service;

import dao.DepartmentDao;
import model.Department;

import java.sql.SQLException;
import java.util.List;

public class DepartmentService {
    private DepartmentDao departmentDao = new DepartmentDao();

    public List<Department> getAllDepartments() {
        try {
            return departmentDao.getAllDepartments();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getDepartmentNameById(int departmentId) {
        try {
            return departmentDao.getDepartmentNameById(departmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 新增部门
    public boolean addDepartment(Department dept) {
        try {
            return departmentDao.addDepartment(dept);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新部门信息
    public boolean updateDepartment(Department dept) {
        try {
            return departmentDao.updateDepartment(dept);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除部门
    public boolean deleteDepartment(int departmentId) {
        try {
            return departmentDao.deleteDepartment(departmentId);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据部门名称获取 Department 对象
    public Department getDepartmentByName(String departmentName) {
        try {
            return departmentDao.getDepartmentByName(departmentName);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}


