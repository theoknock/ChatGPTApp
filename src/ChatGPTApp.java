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
import java.util.LinkedHashMap;
import java.util.Map;

public class ChatGPTApp extends JFrame {

    private JTextField promptField;
    private JButton submitButton;
    private JTabbedPane tabbedPane;
    private Map<String, String> assistants;
    private Map<String, JTextArea> responseAreas;

    public ChatGPTApp() {
        setTitle("GPT Assistants App");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initAssistants();
        initComponents();
    }

    private void initAssistants() {
        assistants = new LinkedHashMap<>();
        responseAreas = new LinkedHashMap<>();

        // Initialize assistants with their system prompts
        assistants.put("AbstractGPT", "Your role is to write an abstract of a given psalm that conforms to specific quality and content standards...\n[Include the full system prompt for AbstractGPT here]");
        assistants.put("ParaphraseGPT", "This GPT is designed to provide paraphrasing for a single psalm from the King James Version (KJV) unless otherwise specified...\n[Include the full system prompt for ParaphraseGPT here]");
        assistants.put("ChristologyGPT", "Analyze the given Psalm to uncover any themes, typologies, and prophecies about Jesus Christ...\n[Include the full system prompt for ChristologyGPT here]");
        assistants.put("IntratextualGPT", "Your task is to perform a synoptic analysis of a given psalm, grouping verses that have similar themes...\n[Include the full system prompt for IntratextualGPT here]");
        assistants.put("ExegesisGPT", "Your role is to provide contemporary theological exegesis for any given Psalm or relevant biblical text...\n[Include the full system prompt for ExegesisGPT here]");
        assistants.put("TheophanyGPT", "Your task is to perform a theophanic analysis of a given psalm, focusing on how the psalm embodies or reflects God’s divine nature...\n[Include the full system prompt for TheophanyGPT here]");
        assistants.put("IntertextualGPT", "Your instructions are to perform an intertextual analysis of a given Psalm, identifying and comparing thematic connections...\n[Include the full system prompt for IntertextualGPT here]");
    }

    private void initComponents() {
        // Prompt input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        promptField = new JTextField();
        submitButton = new JButton("Submit");

        inputPanel.add(new JLabel("Enter your prompt:"), BorderLayout.NORTH);
        inputPanel.add(promptField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        // Tabbed pane to hold response areas
        tabbedPane = new JTabbedPane();

        // Initialize response areas for each assistant
        for (String assistantName : assistants.keySet()) {
            JTextArea responseArea = new JTextArea();
            responseArea.setEditable(false);
            responseArea.setLineWrap(true);
            responseArea.setWrapStyleWord(true);
            responseAreas.put(assistantName, responseArea);

            JPanel responsePanel = new JPanel(new BorderLayout());
            responsePanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);

            tabbedPane.addTab(assistantName, responsePanel);
        }

        // Main frame layout
        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Action listener for the submit button
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String prompt = promptField.getText().trim();
                if (!prompt.isEmpty()) {
                    submitButton.setEnabled(false);

                    // Clear previous responses
                    for (JTextArea responseArea : responseAreas.values()) {
                        responseArea.setText("");
                    }

                    // Start a new thread for each assistant
                    for (String assistantName : assistants.keySet()) {
                        String systemPrompt = assistants.get(assistantName);
                        JTextArea responseArea = responseAreas.get(assistantName);
                        new Thread(() -> sendRequest(prompt, systemPrompt, responseArea)).start();
                    }
                }
            }
        });
    }

    private void sendRequest(String userPrompt, String systemPrompt, JTextArea responseArea) {
        // Get your OpenAI API key from an environment variable
        String apiKey = "";
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
                "  \"model\": \"gpt-4\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"stream\": true\n" +
                "}";

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .timeout(Duration.ofMinutes(5))
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
            // Check if all responses are done
            SwingUtilities.invokeLater(() -> {
                boolean allDone = true;
                for (JTextArea area : responseAreas.values()) {
                    if (area.getText().isEmpty()) {
                        allDone = false;
                        break;
                    }
                }
                if (allDone) {
                    submitButton.setEnabled(true);
                }
            });
        }
    }

    private static String extractContent(String jsonData) {
        // Naive JSON parsing to extract the "content" field
        int contentIndex = jsonData.indexOf("\"content\":");
        if (contentIndex == -1) {
            return null;
        }

        int startQuoteIndex = jsonData.indexOf("\"", contentIndex + "\"content\":".length());
        if (startQuoteIndex == -1) {
            return null;
        }

        int endQuoteIndex = jsonData.indexOf("\"", startQuoteIndex + 1);
        while (endQuoteIndex != -1 && jsonData.charAt(endQuoteIndex - 1) == '\\') {
            // Skip escaped quote
            endQuoteIndex = jsonData.indexOf("\"", endQuoteIndex + 1);
        }
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


