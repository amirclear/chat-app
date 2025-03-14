import java.awt.*;
import java.io.*;
import java.net.Socket;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class s2 {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField messageField;
    private String lastSentMessage;

    public s2(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            createUI();
            startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createUI() {
        JFrame frame = new JFrame("Dark Chat");
        frame.setSize(500, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(Color.decode("#1C2526")); 

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(Color.decode("#1C2526"));
        chatPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); 

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); 
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = Color.decode("#4A6266"); 
                this.trackColor = Color.decode("#2E3839"); 
            }
        });

        messageField = new JTextField();
        messageField.setFont(new Font("Arial", Font.PLAIN, 14));
        messageField.setBackground(Color.decode("#2E3839"));
        messageField.setForeground(Color.decode("#E0E7E9")); 
        messageField.setCaretColor(Color.decode("#00CC99"));
        messageField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#4A6266"), 1), 
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 14));
        sendButton.setBackground(Color.decode("#00CC99")); 
        sendButton.setForeground(Color.WHITE); 
        sendButton.setFocusPainted(false);
        sendButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        sendButton.setEnabled(false);
        sendButton.setOpaque(true);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(Color.decode("#00E6B3")); 
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(Color.decode("#00CC99")); 
            }
        });

        messageField.addActionListener(e -> sendMessage());
        sendButton.addActionListener(e -> sendMessage());

        messageField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                sendButton.setEnabled(!messageField.getText().isEmpty());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                sendButton.setEnabled(!messageField.getText().isEmpty());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                sendButton.setEnabled(!messageField.getText().isEmpty());
            }
        });

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(Color.decode("#1C2526"));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            lastSentMessage = message;
            out.println(message);
            addMessageToChat(message, Color.decode("#00CC99"), FlowLayout.RIGHT); 
            messageField.setText("");
        }
    }

    private void startListening() {
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        publish(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    if (message.equals(lastSentMessage)) {
                        lastSentMessage = null;
                        continue;
                    }
                    addMessageToChat(message, Color.decode("#4A6266"), FlowLayout.LEFT); 
                }
            }
        }.execute();
    }

    private void addMessageToChat(String message, Color color, int alignment) {
        JPanel messagePanel = new JPanel(new FlowLayout(alignment));
        messagePanel.setBackground(Color.decode("#1C2526")); 
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel messageLabel = new JLabel("<html><body style='width: 300px'>" + message + "</body></html>");
        messageLabel.setOpaque(true);
        messageLabel.setBackground(color);
        messageLabel.setForeground(Color.decode("#E0E7E9")); 
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        messagePanel.add(messageLabel);
        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum());
    }

    public static void main(String[] args) {
        new s2("localhost", 1234);
    }
}