package drone;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        for (String a : args) if (a.equals("--demo")) { Demo.run(System.out); return; }
        for (String a : args) if (a.equals("--web")) {
            try { WebServer.start(8080); } catch (java.io.IOException e) { System.out.println("Could not start web server: " + e.getMessage()); }
            return;
        }
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            JFrame f = new JFrame("Intelligent Drone Fleet Management System - DSA-II");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new FleetApp().build());
            f.setSize(1150, 760);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}
