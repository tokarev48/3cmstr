package org.enterprise;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class EnterpriseGUI {
    private final Enterprise enterprise;
    private final JTextArea textArea;

    public EnterpriseGUI() {
        this.enterprise = new Enterprise();
        this.textArea = new JTextArea(22, 50);
    }
    public void showMainFrame() {
        SwingUtilities.invokeLater(() -> {
            initComponents();
            loadDepartmentsFromDatabase();
            loadEmployeesFromDatabase();
        });
    }
    private void initComponents() {
        JFrame frame = new JFrame("Enterprise Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1400, 500);

        JButton showAllButton = new JButton("Отделы/сотрудники");
        showAllButton.addActionListener(e -> showAllDepartments());

        JButton addButton = new JButton("Добавить отдел");
        addButton.addActionListener(e -> addDepartment());

        JButton removeButton = new JButton("Удалить отдел");
        removeButton.addActionListener(e -> removeDepartment());

        JButton editButton = new JButton("Редактировать отдел");
        editButton.addActionListener(e -> editDepartment());

        JButton addEmployeeButton = new JButton("Добавить сотрудника");
        addEmployeeButton.addActionListener(e -> addEmployee());

        JButton editEmployeeButton = new JButton("Редактировать сотрудника");
        editEmployeeButton.addActionListener(e -> editEmployee());

        JButton removeEmployeeButton = new JButton("Удалить сотрудника");
        removeEmployeeButton.addActionListener(e -> removeEmployee());

        JButton showAllEmployeesButton = new JButton("Все сотрудники");
        showAllEmployeesButton.addActionListener(e -> showAllEmployees());

        JPanel panel = new JPanel();
        panel.add(showAllButton);
        panel.add(addButton);
        panel.add(removeButton);
        panel.add(editButton);
        panel.add(addEmployeeButton);
        panel.add(editEmployeeButton);
        panel.add(removeEmployeeButton);
        panel.add(showAllEmployeesButton);


        JScrollPane scrollPane = new JScrollPane(textArea);
        JPanel mainPanel = new JPanel();
        mainPanel.add(panel);
        mainPanel.add(scrollPane);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
    private void showAllDepartments() {
        StringBuilder output = new StringBuilder("Отделы:\n");
        for (Department department : enterprise.getDepartments()) {
            output.append(department).append(",\n").append(department.getFormattedEmployeeList())
                    .append("Зарплата отдела: ").append(department.calculateTotalSalary()).append("\n\n");
        }
        textArea.setText(output.toString());
    }
    private void addDepartment() {
        String departmentName = JOptionPane.showInputDialog("Введите имя отдела:");
        if (departmentName == null || departmentName.trim().isEmpty()) {
            showError("Не верный ввод. Имя отдела не может быть пустым.");
            return;
        }

        Department newDepartment = new Department(departmentName);
        enterprise.addDepartment(newDepartment);
        newDepartment.saveToDatabase();

        showMessage("Отдел успешно добавлен.");
    }
    private void removeDepartment() {
        String departmentName = JOptionPane.showInputDialog("Введите имя отдела:");
        Department departmentToRemove = findDepartmentByName(departmentName);

        if (departmentToRemove != null) {
            enterprise.removeDepartment(departmentToRemove);
            DatabaseManager.removeDepartment(departmentToRemove);
            showMessage("Отдел успешно удалён.");
        } else {
            showError("Отдел не найден.");
        }
    }
    private void editDepartment() {
        String departmentName = JOptionPane.showInputDialog("Введите имя отдела:");
        Department selectedDepartment = findDepartmentByName(departmentName);

        if (selectedDepartment != null) {
            String newDepartmentName = JOptionPane.showInputDialog("Введите новое имя отдела:");
            selectedDepartment.setName(newDepartmentName);
            showAllDepartments();
            selectedDepartment.setName(newDepartmentName);
            showMessage("Отдел успешно изменён.");
        } else {
            showError("Отдел не найден.");
        }
    }
    private Department findDepartmentByName(String name) {
        for (Department department : enterprise.getDepartments()) {
            if (department.getName().equals(name)) {
                return department;
            }
        }
        return null;
    }
    private Department chooseDepartment(String message) {
        Object[] departmentOptions = enterprise.getDepartments().toArray();
        if (departmentOptions.length == 0) {
            showError("Отделы недоступны. Сначала добавьте отдел.");
            return null;
        }

        Object selectedDepartment = JOptionPane.showInputDialog(
                null,
                message,
                "Выбирите отдел",
                JOptionPane.QUESTION_MESSAGE,
                null,
                departmentOptions,
                departmentOptions[0]);

        return (Department) selectedDepartment;
    }
    private void addEmployee() {
        String fullName = JOptionPane.showInputDialog("Введите полное имя сотрудника:");
        int age = Integer.parseInt(JOptionPane.showInputDialog("Введите возраст сотрудника:"));
        double salary = Double.parseDouble(JOptionPane.showInputDialog("Введите з/п сотрудника:"));

        Department selectedDepartment = chooseDepartment("Выберите отдел для сотрудника:");
        if (selectedDepartment == null) {
            showError("Отдел не выбран. Сотрудник не добавлен.");
            return;
        }

        Employee newEmployee = new Employee(fullName, age, salary);
        int employeeId = DatabaseManager.saveEmployee(newEmployee);

        if (employeeId != -1) {
            showMessage("Сотрудник успешно добавлен с ID: " + employeeId);

            selectedDepartment.addEmployee(newEmployee);
            selectedDepartment.saveToDatabase();
        } else {
            showError("Ошибка при добавлении сотрудника.");
        }
    }
    private void removeEmployee() {
        int employeeId = Integer.parseInt(JOptionPane.showInputDialog("Введите ID сотрудника для удаления:"));
        Employee employeeToRemove = findEmployeeById(employeeId);

        if (employeeToRemove != null) {
            removeEmployeeFromCurrentDepartment(employeeToRemove);

            DatabaseManager.removeEmployee(employeeToRemove);

            showMessage("Сотрудник успешно удален.");
        } else {
            showError("Сотрудник не найден.");
        }
    }
    private void showAllEmployees() {
        List<Employee> allEmployees = DatabaseManager.loadAllEmployees();
        StringBuilder output = new StringBuilder("Все сотрудники:\n");

        for (Employee employee : allEmployees) {
            output.append("ID: ").append(employee.getId()).append(", ФИО: ").append(employee.getFullName())
                    .append(", Возраст: ").append(employee.getAge()).append(", Зарплата: ").append(employee.getSalary()).append("\n");
        }

        textArea.setText(output.toString());
    }
    private Employee findEmployeeById(int employeeId) {
        List<Employee> allEmployees = DatabaseManager.loadAllEmployees();

        for (Employee employee : allEmployees) {
            if (employee.getId() == employeeId) {
                return employee;
            }
        }

        return null;
    }
    private void editEmployee() {
        int employeeId = Integer.parseInt(JOptionPane.showInputDialog("Введите ID сотрудника для редактирования:"));
        Employee existingEmployee = findEmployeeById(employeeId);

        if (existingEmployee != null) {
            Object[] options = {"Изменить информацию", "Изменить отдел", "Отменить"};
            int choice = JOptionPane.showOptionDialog(
                    null,
                    "Выберите действие:",
                    "Редактировать сотрудника",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[2]);

            switch (choice) {
                case 0:
                    editEmployeeInformation(existingEmployee);
                    break;
                case 1:
                    editEmployeeDepartment(existingEmployee);
                    break;
            }
        } else {
            showError("Сотрудник не найден.");
        }
    }
    private void editEmployeeInformation(Employee employee) {
        String newFullName = JOptionPane.showInputDialog("Введите новое ФИО:");
        int newAge = Integer.parseInt(JOptionPane.showInputDialog("Введите новый возраст:"));
        double newSalary = Double.parseDouble(JOptionPane.showInputDialog("Введите новую зарплату:"));

        employee.setFullName(newFullName);
        employee.setAge(newAge);
        employee.setSalary(newSalary);

        DatabaseManager.updateEmployee(employee);
        showMessage("Информация о сотруднике успешно обновлена.");
    }
    private void editEmployeeDepartment(Employee employee) {
        Department selectedDepartment = chooseDepartment("Выберите новый отдел для сотрудника:");
        if (selectedDepartment != null) {
            removeEmployeeFromCurrentDepartment(employee);
            selectedDepartment.addEmployee(employee);
            selectedDepartment.saveToDatabase();
            showMessage("Отдел сотрудников успешно обновлен.");
        } else {
            showError("Отдел не выбран. Отдел сотрудников не обновлен.");
        }
    }
    private void removeEmployeeFromCurrentDepartment(Employee employee) {
        for (Department department : enterprise.getDepartments()) {
            if (department.getEmployees().contains(employee)) {
                department.removeEmployee(employee);
                department.saveToDatabase();
                break;
            }
        }
    }
    private void loadDepartmentsFromDatabase() {
        List<Department> departments = DatabaseManager.loadAllDepartments();
        enterprise.getDepartments().addAll(departments);
    }
    private void loadEmployeesFromDatabase() {
        List<Employee> employees = DatabaseManager.loadAllEmployees();
        for (Department department : enterprise.getDepartments()) {
            List<Employee> departmentEmployees = DatabaseManager.loadDepartmentEmployees(department);
            department.getEmployees().addAll(departmentEmployees);
        }
    }
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    private void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
}