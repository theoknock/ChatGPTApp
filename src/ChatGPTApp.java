import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class ChatGPTApp extends JFrame {
    private JTextField promptField;
    private JTextArea responseArea;
    private JButton submitButton;

    public ChatGPTApp() {
        setTitle("ChatGPT App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        promptField = new JTextField();
        responseArea = new JTextArea();
        responseArea.setEditable(false);
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);

        submitButton = new JButton("Submit");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel("Enter your prompt:"), BorderLayout.NORTH);
        panel.add(promptField, BorderLayout.CENTER);
        panel.add(submitButton, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane(responseArea);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = promptField.getText().trim();
                if (!prompt.isEmpty()) {
                    responseArea.setText("");
                    submitButton.setEnabled(false);
                    new Thread(() -> sendRequest(prompt)).start();
                }
            }
        });
    }

    private void sendRequest(String prompt) {
        // Get your OpenAI API key from an environment variable
        String apiKey = "";
        if (apiKey == null || apiKey.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "API key is not set. Please set the OPENAI_API_KEY environment variable.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            submitButton.setEnabled(true);
            return;
        }

        // OpenAI API endpoint
        String apiURL = "https://api.openai.com/v1/chat/completions";

        // Build the request body
        String requestBody = "{\n" +
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(prompt) + "\"}\n" +
                "  ],\n" +
                "  \"stream\": true\n" +
                "}";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .timeout(Duration.ofMinutes(2))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                // Read the error response body
                String errorBody = new String(response.body().readAllBytes());
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Error: " + response.statusCode() + " - " + errorBody,
                            "API Error",
                            JOptionPane.ERROR_MESSAGE);
                    submitButton.setEnabled(true);
                });
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("data: ")) {
                    String data = line.substring("data: ".length()).trim();

                    if (data.equals("[DONE]")) {
                        break;
                    }

                    // Now parse the JSON in data
                    String content = extractContent(data);

                    if (content != null) {
                        SwingUtilities.invokeLater(() -> {
                            responseArea.append(content);
                        });
                    }
                }
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "An error occurred:\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                submitButton.setEnabled(true);
            });
            e.printStackTrace();
        } finally {
            SwingUtilities.invokeLater(() -> submitButton.setEnabled(true));
        }
    }

    private static String extractContent(String jsonData) {
        // Naive JSON parsing to extract the "content" field
        int deltaIndex = jsonData.indexOf("\"delta\":");
        if (deltaIndex == -1) {
            return null;
        }

        int contentIndex = jsonData.indexOf("\"content\":", deltaIndex);
        if (contentIndex == -1) {
            return null;
        }

        int startQuoteIndex = jsonData.indexOf("\"", contentIndex + "\"content\":".length());
        if (startQuoteIndex == -1) {
            return null;
        }

        int endQuoteIndex = jsonData.indexOf("\"", startQuoteIndex + 1);
        if (endQuoteIndex == -1) {
            return null;
        }

        String content = jsonData.substring(startQuoteIndex + 1, endQuoteIndex);

        // Unescape escaped characters
        content = content.replace("\\n", "\n")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        return content;
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatGPTApp app = new ChatGPTApp();
            app.setVisible(true);
        });
    }
}