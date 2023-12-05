package org.enterprise;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:enterprise.db";

    public static void createTables() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.err.println("SQLite JDBC driver not found");
            return;
        }
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {

            String createEmployeeTable = "CREATE TABLE IF NOT EXISTS employees (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "full_name TEXT," +
                    "age INTEGER," +
                    "salary REAL);";
            statement.executeUpdate(createEmployeeTable);

            String createDepartmentTable = "CREATE TABLE IF NOT EXISTS departments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT);";
            statement.executeUpdate(createDepartmentTable);

            String createEmployeeDepartmentTable = "CREATE TABLE IF NOT EXISTS employee_department (" +
                    "employee_id INTEGER," +
                    "department_id INTEGER," +
                    "FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE," +
                    "FOREIGN KEY (department_id) REFERENCES departments (id) ON DELETE CASCADE);";
            statement.executeUpdate(createEmployeeDepartmentTable);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    public static int saveEmployee(Employee employee) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO employees (full_name, age, salary) VALUES (?, ?, ?)")) {
            statement.setString(1, employee.getFullName());
            statement.setInt(2, employee.getAge());
            statement.setDouble(3, employee.getSalary());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating employee failed, no rows affected.");
            }

            try (Statement idStatement = connection.createStatement()) {
                try (ResultSet resultSet = idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (resultSet.next()) {
                        int employeeId = resultSet.getInt(1);
                        employee.setId(employeeId);
                        return employeeId;
                    } else {
                        throw new SQLException("Creating employee failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public static int saveDepartment(Department department) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("INSERT INTO departments (name) VALUES (?)")) {
            statement.setString(1, department.getName());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating department failed, no rows affected.");
            }

            try (Statement idStatement = connection.createStatement()) {
                try (ResultSet resultSet = idStatement.executeQuery("SELECT last_insert_rowid()")) {
                    if (resultSet.next()) {
                        int departmentId = resultSet.getInt(1);
                        department.setId(departmentId);
                        return departmentId;
                    } else {
                        throw new SQLException("Creating department failed, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }
    public static void saveEmployeeDepartment(int employeeId, int departmentId) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO employee_department (employee_id, department_id) VALUES (?, ?)")) {

            if (!employeeExists(connection, employeeId)) {
                System.err.println("Employee with ID " + employeeId + " does not exist.");
                return;
            }

            if (!departmentExists(connection, departmentId)) {
                System.err.println("Department with ID " + departmentId + " does not exist.");
                return;
            }

            statement.setInt(1, employeeId);
            statement.setInt(2, departmentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<Employee> loadAllEmployees() {
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM employees";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Employee employee = new Employee(resultSet.getString("full_name"), resultSet.getInt("age"), resultSet.getDouble("salary"));
                    employee.setId(resultSet.getInt("id"));
                    employees.add(employee);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }
    public static List<Department> loadAllDepartments() {
        List<Department> departments = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement()) {
            String query = "SELECT * FROM departments";
            try (ResultSet resultSet = statement.executeQuery(query)) {
                while (resultSet.next()) {
                    Department department = new Department(resultSet.getString("name"));
                    department.setId(resultSet.getInt("id"));
                    departments.add(department);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return departments;
    }
    public static void updateEmployee(Employee employee) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("UPDATE employees SET full_name = ?, age = ?, salary = ? WHERE id = ?")) {
            statement.setString(1, employee.getFullName());
            statement.setInt(2, employee.getAge());
            statement.setDouble(3, employee.getSalary());
            statement.setInt(4, employee.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void updateDepartment(Department department) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("UPDATE departments SET name = ? WHERE id = ?")) {
            statement.setString(1, department.getName());
            statement.setInt(2, department.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeEmployee(Employee employee) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM employees WHERE id = ?")) {
            statement.setInt(1, employee.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void removeDepartment(Department department) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM departments WHERE id = ?")) {
            statement.setInt(1, department.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void deleteEmployeeDepartmentsForDepartment(int departmentId) {
        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement("DELETE FROM employee_department WHERE department_id = ?")) {
            statement.setInt(1, departmentId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static List<Employee> loadDepartmentEmployees(Department department) {
        List<Employee> employees = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(URL);
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT employees.id, employees.full_name, employees.age, employees.salary FROM employees " +
                             "JOIN employee_department ON employees.id = employee_department.employee_id " +
                             "WHERE employee_department.department_id = ?")) {
            statement.setInt(1, department.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Employee employee = new Employee(resultSet.getString("full_name"), resultSet.getInt("age"), resultSet.getDouble("salary"));
                    employee.setId(resultSet.getInt("id"));
                    employees.add(employee);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return employees;
    }
    private static boolean employeeExists(Connection connection, int employeeId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM employees WHERE id = ?")) {
            statement.setInt(1, employeeId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static boolean departmentExists(Connection connection, int departmentId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id FROM departments WHERE id = ?")) {
            statement.setInt(1, departmentId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}