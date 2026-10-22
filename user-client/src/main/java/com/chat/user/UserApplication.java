package com.chat.user;

import com.chat.user.network.SocketClient;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

public final class UserApplication {
    private UserApplication() { }
    public static void main(String[] args) { FlatLightLaf.setup(); SwingUtilities.invokeLater(UserApplication::showWindow); }
    private static void showWindow() {
        JFrame frame = new JFrame("Chat System"); JLabel status = new JLabel("Connection: not tested"); JButton test = new JButton("Test server connection");
        test.addActionListener(event -> {
            test.setEnabled(false); status.setText("Connection: testing...");
            CompletableFuture.supplyAsync(() -> { try { return new SocketClient().ping().success() ? "connected (PONG)" : "server returned an error"; } catch (Exception exception) { return "failed - " + exception.getMessage(); } })
                    .thenAccept(result -> SwingUtilities.invokeLater(() -> { status.setText("Connection: " + result); test.setEnabled(true); }));
        });
        JPanel content = new JPanel(new BorderLayout(12, 12)); content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(new JLabel("Chat application foundation", SwingConstants.CENTER), BorderLayout.CENTER);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0)); footer.add(status); footer.add(test); content.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(content); frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE); frame.setSize(720, 480); frame.setLocationRelativeTo(null); frame.setVisible(true);
    }
}
