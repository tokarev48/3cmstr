package org.enterprise;

import java.util.ArrayList;
import java.util.List;

public class Department {
    private int id;
    private String name;
    private List<Employee> employees;

    public Department(String name) {
        this.name = name;
        this.employees = new ArrayList<>();
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public List<Employee> getEmployees() {
        return employees;
    }
    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }
    public void addEmployee(Employee employee) {
        employees.add(employee);
    }
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
    }
    public double calculateTotalSalary() {
        double totalSalary = 0;
        for (Employee employee : employees) {
            totalSalary += employee.getSalary();
        }
        return totalSalary;
    }
    public void saveToDatabase() {
        int departmentId = getId();
        if (departmentId == 0) {
            departmentId = DatabaseManager.saveDepartment(this);
            if (departmentId != -1) {
                setId(departmentId);
            } else {
                System.err.println("Failed to save department to the database.");
                return;
            }
        }
        DatabaseManager.deleteEmployeeDepartmentsForDepartment(departmentId);

        for (Employee employee : employees) {
            DatabaseManager.saveEmployeeDepartment(employee.getId(), departmentId);
        }
    }
    @Override
    public String toString() {
        return "Название: " + getName();
    }
    public String getFormattedEmployeeList() {
        StringBuilder employeeList = new StringBuilder("Сотрудники:\n");
        for (Employee employee : employees) {
            employeeList.append("(").append(employee.getId()).append(") ").append(employee.getFullName()).append(",\n");
        }
        return employeeList.toString();
    }
}