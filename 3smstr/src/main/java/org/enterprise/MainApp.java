package org.enterprise;

public class MainApp {
    public static void main(String[] args) {
        DatabaseManager.createTables();
        EnterpriseGUI enterpriseGUI = new EnterpriseGUI();
        enterpriseGUI.showMainFrame();
    }
}