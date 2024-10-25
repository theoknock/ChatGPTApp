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
    private JTextArea responseAreaGPT4;
    private JTextArea responseAreaGPT35;
    private JButton submitButton;

    public ChatGPTApp() {
        setTitle("ChatGPT Model Comparison App");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {
        // Prompt input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        promptField = new JTextField();
        submitButton = new JButton("Submit");

        inputPanel.add(new JLabel("Enter your prompt:"), BorderLayout.NORTH);
        inputPanel.add(promptField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        // Response panels
        responseAreaGPT4 = new JTextArea();
        responseAreaGPT4.setEditable(false);
        responseAreaGPT4.setLineWrap(true);
        responseAreaGPT4.setWrapStyleWord(true);

        responseAreaGPT35 = new JTextArea();
        responseAreaGPT35.setEditable(false);
        responseAreaGPT35.setLineWrap(true);
        responseAreaGPT35.setWrapStyleWord(true);

        JPanel responsePanelGPT4 = new JPanel(new BorderLayout());
        responsePanelGPT4.add(new JLabel("GPT-4 Response:"), BorderLayout.NORTH);
        responsePanelGPT4.add(new JScrollPane(responseAreaGPT4), BorderLayout.CENTER);

        JPanel responsePanelGPT35 = new JPanel(new BorderLayout());
        responsePanelGPT35.add(new JLabel("GPT-3.5-Turbo Response:"), BorderLayout.NORTH);
        responsePanelGPT35.add(new JScrollPane(responseAreaGPT35), BorderLayout.CENTER);

        // Split pane to hold both response panels
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, responsePanelGPT4, responsePanelGPT35);
        splitPane.setDividerLocation(400);

        // Main frame layout
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Action listener for the submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = promptField.getText().trim();
                if (!prompt.isEmpty()) {
                    responseAreaGPT4.setText("");
                    responseAreaGPT35.setText("");
                    submitButton.setEnabled(false);
                    // Start threads for both models
                    new Thread(() -> sendRequest(prompt, "gpt-4", responseAreaGPT4)).start();
                    new Thread(() -> sendRequest(prompt, "gpt-3.5-turbo", responseAreaGPT35)).start();
                }
            }
        });
    }

    private void sendRequest(String prompt, String model, JTextArea responseArea) {
        // Get your OpenAI API key from an environment variable
        String apiKey = "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";
        if (apiKey == null || apiKey.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                        "API key is not set. Please set the OPENAI_API_KEY environment variable.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                submitButton.setEnabled(true);
            });
            return;
        }

        // OpenAI API endpoint
        String apiURL = "https://api.openai.com/v1/chat/completions";

        // Build the request body
        String requestBody = "{\n" +
                "  \"model\": \"" + model + "\",\n" +
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
                            "Error from " + model + ": " + response.statusCode() + " - " + errorBody,
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
                        "An error occurred with " + model + ":\n" + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                submitButton.setEnabled(true);
            });
            e.printStackTrace();
        } finally {
            // Check if both responses are done
            SwingUtilities.invokeLater(() -> {
                if (!submitButton.isEnabled()) {
                    submitButton.setEnabled(true);
                }
            });
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