package com.chat.admin;

import com.chat.admin.network.SocketClient;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public final class AdminApplication {
    private AdminApplication() { }
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(AdminApplication::showWindow);
    }
    private static void showWindow() {
        JFrame frame = new JFrame("Chat System - Admin");
        JLabel status = new JLabel("Connection: not tested");
        JButton test = new JButton("Test server connection");
        test.addActionListener(event -> {
            test.setEnabled(false); status.setText("Connection: testing...");
            CompletableFuture.supplyAsync(() -> {
                try { return new SocketClient().ping().success() ? "connected (PONG)" : "server returned an error"; }
                catch (Exception exception) { return "failed - " + exception.getMessage(); }
            }).thenAccept(result -> SwingUtilities.invokeLater(() -> { status.setText("Connection: " + result); test.setEnabled(true); }));
        });
        JPanel content = new JPanel(new BorderLayout(12, 12)); content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(new JLabel("Admin dashboard foundation", SwingConstants.CENTER), BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0)); footer.add(status); footer.add(test); content.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(content); frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); frame.setSize(640, 400); frame.setLocationRelativeTo(null); frame.setVisible(true);
    }
}
