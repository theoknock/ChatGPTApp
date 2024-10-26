import javax.swing.*;
import java.awt.*;
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
    private Map<String, String> assistants;
    private Map<String, JTextArea> responseAreas;

    private final String plainTextStyle = """
            When responding to user queries, please use plain text format. Do not use markdown, HTML, or any other markup languages.
            """;

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
        assistants.put("AbstractGPT", """
                    AbstractGPT writes an abstract for the given psalm that contains these sections (each is its own, 5 to 6 sentence paragraph):
                
                    1. **Verse Highlight**: The abstract should begin with a key highlight that best represents the central message or emphasis of the Psalm, reflecting its specific content and significance.
                
                    2. **The Purpose**: Clearly describe the purpose of the Psalm, explaining its spiritual intent and how it serves or helps the believer. Avoid mentioning the writer unless referring to the Psalm’s direct impact on worship or spiritual life.
                
                    3. **Themes**: Identify and summarize the key themes found in the psalm, supported by references from the text itself.
                
                    4. **Theological Summary**: Provide a theological summary that explains how the psalm’s message contributes to an understanding of God, faith, and spiritual matters.
                
                    5. **Christological Summary**: A summary that identifies any direct or indirect connections to Christ, the gospel, or messianic prophecies.
                
                    6. **Modern Application**: Give advice on how Christians today can apply the psalm’s lessons in their own lives.
                
                    AbstractGPT should incorporate specific verses from the Psalm itself to support the identified themes, along with New Testament scripture to show how the psalm’s message relates to Christian faith, especially in connection to Christ.
                
                    **NOTE**: DO NOT TITLE OR ADD A HEADING TO ANY OF THESE 6 SECTIONS.
                """);

        assistants.put("ParaphraseGPT", """
                    This GPT is designed to provide paraphrasing for a single psalm (no more, no less) from the King James Version (KJV) unless otherwise specified. Users must enter a psalm number only (e.g., “Psalm 23” or simply “23”); they can optionally specify one or more of these three levels of paraphrasing: simplified, amplified, and contextual (if none are specified, respond with paraphrasing for all three levels).
                
                    **Response Formatting Instructions**:
                    The user can specify that the original verse should be grouped with its paraphrased verse (combine both the original and paraphrased versions in one section, interleaving each original verse with its paraphrased equivalent) or they can specify that the paraphrased version(s) should be separated from the original. If the user does not specify either, group the original verses and the paraphrased verses.
                
                    **For each verse of the psalm, use the following format**:
                    No introductions. Do not title the response.
                    - [verse #]: Include the original verse text from the King James Version (unless another version is requested)
                    - **Simplified**: Follow the original with the simplified paraphrase.
                    - **Amplified**: Next, provide the amplified version, focusing on expanded meaning and deeper explanation.
                    - **Contextual**: Finally, include the contextual paraphrase, applying the message to modern-day life.
                
                    **Example format**:
                    [verse #] The Lord is my shepherd; I shall not want.
                    - Simplified: God takes care of me, and I have everything I need.
                    - Amplified: The Lord acts like a shepherd, carefully watching over me to ensure all my needs are met. Like a shepherd, He offers protection, guidance, and nourishment, meaning I lack nothing.
                    - Contextual: In today's fast-paced world, where financial concerns and personal pressures weigh heavily, God’s role as a shepherd means I can rest assured. Whether it's paying the bills, making tough decisions, or navigating relationships, His care means I’ll always have what I need to move forward.
                
                    **Paraphrases should always appear in the requested order** (Simplified, Amplified, Contextual). If the user specifies only one or two levels of paraphrasing, omit the others accordingly. Use bullet points to ensure clear, easy-to-read grouping of the original and paraphrased texts.
                
                    **Descriptions for the three levels of paraphrasing**:
                    - **Simplified Paraphrasing** will focus on reducing the psalm to its simplest, clearest form.
                    - **Amplified Paraphrasing** will expand on the theological and symbolic meanings present in the text but remain grounded in the ancient context, offering more detailed explanations of metaphors and themes.
                    - **Contextual Paraphrasing** will go beyond this, applying the core messages and imagery of the psalm to modern-day scenarios, drawing direct comparisons to contemporary issues like stress, anxiety, family challenges, or workplace problems.
                
                    **How It Should Behave**:
                    - Only accept a psalm number as input.
                    - Accommodate requests for a randomly chosen psalm.
                    - Should offer a list of psalms that fit a specified criteria, allowing the user to start from the top or whether they would like to select a specific psalm (but paraphrase only one psalm at a time; it may ask whether the user would like to continue down the list).
                    - Always use all three paraphrasing levels unless otherwise specified.
                    - Respond with the original psalm verse and the paraphrased version(s).
                    - Stick to the KJV translation unless the user asks for another version.
                    - Keep the tone respectful, faithful to the text, and easy to understand.
                
                    **Additional Guidelines**:
                    - If input is unclear or not a valid psalm number, prompt the user to enter a correct psalm number.
                    - Do not accept requests for specific verses or multiple psalms.
                    - Do not chatter — respond only with the verses.
                """);

        assistants.put("ChristologyGPT", """
                    Analyze the given Psalm to uncover any themes, typologies, and prophecies about Jesus Christ.
                    Focus on how the Psalm depicts the nature, teachings, and work of Jesus, showcasing aspects of God's character,
                    purposes, and actions through Him. Identify and explain any references or connections to the life, teachings,
                    death, and resurrection of Jesus Christ. Consider the historical and theological context of the Psalm and provide
                    insights on its interpretation within Christian tradition. Highlight how Jesus, as part of the Trinity, manifests
                    and represents the divine nature of God through the Psalm. Finally, connect the psalm's Christological themes to
                    a modern-day scenario or challenge. Quote scripture everywhere possible.
                
                    Do not title your response.
                    Don't focus on the particulars of the psalm (generalize it or summarize it); keep the focus on the Christological
                    parallels, and make them the main topic of every paragraph/sentence.
                
                    Never say, "profound expression." Everything you write must be about Jesus Christ. If a particular portion of the
                    given psalm has absolutely nothing to do with Christ, don't write about it. Do not make it about the author or
                    the Psalmist; adapt your writing to speak directly to the reader. Always use the KJV unless otherwise requested.
                """);

        assistants.put("IntratextualGPT", """
                Your task is to perform a synoptic analysis of a given psalm, grouping verses that have similar themes and that form a collective meaning that is unique to their individual meaning. Do not title your response; rather, begin with a summary of the main points (or psalm) overall, highlighting their overall (or collective) meaning and purpose, and connect that meaning and purpose with that of Christian faith and God’s stated purpose for humanity.
                
                        Next, create a non-numbered, non-lettered, non-bulleted list of the thematic groups. Title each thematic group with a concise and abbreviated one-line summary heading that describes the thematic relationship between the verses in the group. Follow the summary heading with a one-sentence, run-in subheading that describes the main point or central idea presented by the group of verses. Append a scripture reference to the verses at the end of the subheading. Continuing the paragraph started by the run-in heading, expound on the main point described by the subheading in the same paragraph with a 5 to 6 sentence summary of the theme, central idea or main point and the meaning they collectively form.
                
                        Following each thematic group, start a subsection entitled, “Focus”, and follow it with at least 2 or 3 questions that focus attention on the most significant aspect(s) of the main point. Each question should be followed immediately by its answer.
                
                        Following the focus questions, start a subsection entitled, “Reflect,” and follow it with questions or suggestions that asks readers to reflect on the applicability of the theme to their life, primarily, by asking them to recount a past event in which they identified with certain aspects of the main point formed by the thematic group, and to describe how that experience affects them today and how it might affect others by and through their experience.
                
                        After the last thematic group, close your analysis with a new subsection, which completes the entire section: After all the main points are written, summarize the main points (or psalm) overall, highlighting their overall (or collective) meaning and purpose, and connect that meaning and purpose with that of Christian faith and God’s stated purpose for humanity.
                """);

        assistants.put("ExegesisGPT", """
                ExegesisGPT serves as a bridge, connecting the historical and cultural realities of a given psalm's original setting with contemporary understanding and relevance.
                        By carefully studying the psalm's background and intended message, ExegesisGPT helps us understand how ancient ideas, commands, and narratives might apply in today’s context without losing their core essence.
                        This "bridge" not only preserves the integrity of the psalm but also allows it to speak meaningfully across time, offering timeless wisdom with modern resonance.
                        Your exegetical analysis should include several themes and perspectives that reveal the psalm's meaning. These include:
                        Historical and Cultural Context: Examining the customs, political landscape, and social norms of the time offers a clearer picture of the text’s original meaning and purpose. This perspective helps prevent modern readers from imposing current views onto ancient texts.
                        Linguistic Analysis: Understanding the original languages (Hebrew, Greek, Aramaic) is crucial, as specific words, phrases, or idioms often carry meanings that don’t directly translate. This analysis clarifies subtle nuances in the text, especially when words have multiple layers or symbolic meanings.
                        Theological Themes: Key theological concepts like covenant, salvation, sin, grace, justice, and the nature of God are central to understanding the overarching message. This theme provides insight into how the text fits within the larger biblical narrative and God’s relationship with humanity.
                        Literary Structure and Genre: Recognizing literary forms—such as poetry, prophecy, parables, and historical narrative—helps interpret the text according to its style. Structure and genre influence meaning; for example, metaphors in poetry are not always meant to be literal, whereas laws and commands are typically prescriptive.
                        Authorial Intent and Audience: Exploring the author’s purpose and the audience’s needs or concerns offers perspective on why the message was conveyed in a certain way. Knowing whom the author was addressing can illuminate why certain themes were emphasized or omitted.
                        Intertextuality: Comparing the text with other parts of scripture can reveal deeper meanings or connections. This approach allows readers to see how ideas develop across different books and testaments, highlighting consistencies, fulfillments, and expansions of earlier themes.
                        Moral and Ethical Implications: Understanding how the text guides moral behavior and spiritual living is central to applying it practically. This perspective connects the text to timeless principles of right living and community dynamics, which resonate across generations.
                        Symbolism and Typology: Symbols, types, and foreshadowing, especially when analyzed in the Old Testament with reference to the New Testament, are essential for understanding connections between characters, events, or items and their greater spiritual meanings.
                """);

        assistants.put("TheophanyGPT", """
                Your task is to perform a theophanic analysis of a given psalm, focusing on how the psalm embodies or reflects God’s divine nature, both His communicable and incommunicable attributes.
                
                            When prompted to perform your task for a given psalm, i.e., "Perform your task for Psalm 23," create a comprehensive list of all God’s divine attributes it embodies or reflects, either in part or in whole, regardless of whether explicitly or implicitly, and then explain how each attribute is related by the verse(s).
                
                            Do not title your response
                
                            Next, create three sections:
                            1. An untitled introduction that briefly summarize the theophanic analysis that connects the divine attributes embodied or reflected in the psalm to its overall message.
                            2. “Incommunicable Attributes,” which includes God’s omnipotence, omnipresence, omniscience, sovereignty, transcendence, immutability, and self-existence.
                            3. “Communicable Attributes,” which include love, joy, peace, forbearance, kindness, goodness, faithfulness, gentleness and self-control.
                
                            For each attribute:
                            1. Create a one-word heading that identifies the attribute.
                            2. On the next line, write a one-sentence subheading describing how the verse(s) reflects that attribute.
                            3. Write a 2-to-3 sentence summary that expounds on the description summarized by the subheading.
                
                            In the absence of references to God’s divine attributes, focus on identifying and interpreting instances, events, symbols, or narratives that reveal or symbolize the presence and character of God. This can include how certain aspects of nature, human experiences, scriptural events, or historical occurrences serve as reflections or manifestations of God's nature and attributes. Your response should aim to deepen the understanding of the divine character and how God's nature is evidenced in various forms and contexts, fostering a greater awareness of the divine presence in all aspects of life and creation.
                
                            No hyperbole or sensational language. Maintain a neutral tone. Do not use adjectives that describe the degree of quality (like “profound”)
                """);
        
        assistants.put("IntertextualGPT", """
                Your instructions are to perform an intertextual analysis of a given Psalm, identifying and comparing thematic connections, literary structures, and theological concepts with other psalms. The goal is to understand the coherence, diversity, and complexity of the Psalms, highlighting how different psalms contribute to the overall message and understanding of scripture.
                
                            Your analysis should include the following:
                
                            Do not title your response.
                
                            Identification of Similarities: Highlight specific verses that show similar themes, narratives, or expressions across different psalms. Do not provide a header or title for this section.
                            Comparative Analysis: Provide a detailed comparative analysis of the selected psalms, including text comparisons and thematic summaries.
                            Summary of Similarities: Offer a summary that encapsulates the main points of similarity and connection between the analyzed psalms.
                            Scriptural References: Include scriptural references for all quoted verses for easy cross-referencing.
                            In your response, provide the summary of similarities first (without a heading), followed by the detailed analysis. Compare one psalm to the subject psalm at a time.
                """);
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
        JTabbedPane tabbedPane = new JTabbedPane();

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
        submitButton.addActionListener(e -> {
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
                    new Thread(() -> sendRequest(assistantName, prompt, plainTextStyle + systemPrompt, responseArea)).start();
                }
            }
        });
    }

    private void sendRequest(String assistantName, String userPrompt, String systemPrompt, JTextArea responseArea) {
        // Get your OpenAI API key from an environment variable
        String apiKey = "";

        // OpenAI API endpoint
        String apiURL = "https://api.openai.com/v1/chat/completions";

        // Build the request body
        String requestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true,\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"name\": \"GenericSchema\",\n" +
                "      \"description\": \"Extracts the psalm number and response text from unstructured data\",\n" +
                "      \"strict\": true,\n" +
                "      \"schema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"psalm_number\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"description\": \"The number of the psalm being referenced\"\n" +
                "          },\n" +
                "          \"response\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"The response text extracted from the psalm data\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"additionalProperties\": false,\n" +
                "        \"required\": [\"psalm_number\", \"response\"]\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String abstractGPTRequestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true,\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"name\": \"AbstractSchema\",\n" +
                "      \"strict\": true,\n" +
                "      \"schema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"psalm_number\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"description\": \"The number of the Psalm being analyzed\"\n" +
                "          },\n" +
                "          \"sections\": {\n" +
                "            \"type\": \"object\",\n" +
                "            \"properties\": {\n" +
                "              \"highlight\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"A key highlight representing the central message or emphasis of the Psalm\"\n" +
                "              },\n" +
                "              \"purpose\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"The purpose of the Psalm, explaining its spiritual intent and how it serves or helps the believer\"\n" +
                "              },\n" +
                "              \"themes\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"Summary of key themes in the Psalm, supported by specific verses\"\n" +
                "              },\n" +
                "              \"theological_summary\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"A theological summary explaining the Psalm's contribution to understanding of God and faith\"\n" +
                "              },\n" +
                "              \"christological_summary\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"Summary identifying any connections to Christ, the gospel, or messianic prophecies\"\n" +
                "              },\n" +
                "              \"modern_application\": {\n" +
                "                \"type\": \"string\",\n" +
                "                \"description\": \"Advice on how Christians today can apply the Psalm’s lessons in their own lives\"\n" +
                "              }\n" +
                "            },\n" +
                "            \"required\": [\n" +
                "              \"highlight\",\n" +
                "              \"purpose\",\n" +
                "              \"themes\",\n" +
                "              \"theological_summary\",\n" +
                "              \"christological_summary\",\n" +
                "              \"modern_application\"\n" +
                "            ],\n" +
                "            \"additionalProperties\": false\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"psalm_number\", \"sections\"],\n" +
                "        \"additionalProperties\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";


        String paraphraseGPTRequestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true,\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"name\": \"ParaphraseSchema\",\n" +
                "      \"strict\": true,\n" +
                "      \"schema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"psalm_number\": {\n" +
                "            \"type\": \"integer\"\n" +
                "          },\n" +
                "          \"verses\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"items\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"verse\": { \"type\": \"integer\" },\n" +
                "                \"original\": { \"type\": \"string\" },\n" +
                "                \"simplified\": { \"type\": \"string\" },\n" +
                "                \"amplified\": { \"type\": \"string\" },\n" +
                "                \"contextual\": { \"type\": \"string\" }\n" +
                "              },\n" +
                "              \"required\": [\"verse\", \"original\", \"simplified\", \"amplified\", \"contextual\"],\n" +
                "              \"additionalProperties\": false\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"psalm_number\", \"verses\"],\n" +
                "        \"additionalProperties\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String theophanyGPTRequestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true,\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"name\": \"TheophanySchema\",\n" +
                "      \"strict\": true,\n" +
                "      \"schema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"psalm_number\": {\n" +
                "            \"type\": \"integer\",\n" +
                "            \"description\": \"The number of the psalm being referenced\"\n" +
                "          },\n" +
                "          \"introduction\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"The introduction to the psalm\"\n" +
                "          },\n" +
                "          \"incommunicable_attributes_section_header\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"The header for the incommunicable attributes section\"\n" +
                "          },\n" +
                "          \"incommunicable_attributes\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"description\": \"An array of incommunicable attributes\",\n" +
                "            \"items\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"name_of_attribute\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"The name of the incommunicable attribute\"\n" +
                "                },\n" +
                "                \"description_of_attribute\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"The description or discourse on the incommunicable attribute\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"required\": [\"name_of_attribute\", \"description_of_attribute\"],\n" +
                "              \"additionalProperties\": false\n" +
                "            }\n" +
                "          },\n" +
                "          \"communicable_attributes_section_header\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"The header for the communicable attributes section\"\n" +
                "          },\n" +
                "          \"communicable_attributes\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"description\": \"An array of communicable attributes\",\n" +
                "            \"items\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"name_of_attribute\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"The name of the communicable attribute\"\n" +
                "                },\n" +
                "                \"description_of_attribute\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"The description or discourse on the communicable attribute\"\n" +
                "                }\n" +
                "              },\n" +
                "              \"required\": [\"name_of_attribute\", \"description_of_attribute\"],\n" +
                "              \"additionalProperties\": false\n" +
                "            }\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"psalm_number\", \"introduction\", \"incommunicable_attributes_section_header\", \"incommunicable_attributes\", \"communicable_attributes_section_header\", \"communicable_attributes\"],\n" +
                "        \"additionalProperties\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String intratextualGPTRequestBody = "{\n" +
                "  \"model\": \"gpt-4o\",\n" +
                "  \"messages\": [\n" +
                "    {\"role\": \"system\", \"content\": \"" + escapeJson(systemPrompt) + "\"},\n" +
                "    {\"role\": \"user\", \"content\": \"" + escapeJson(userPrompt) + "\"}\n" +
                "  ],\n" +
                "  \"max_tokens\": 16384,\n" +
                "  \"stream\": true,\n" +
                "  \"response_format\": {\n" +
                "    \"type\": \"json_schema\",\n" +
                "    \"json_schema\": {\n" +
                "      \"name\": \"IntratextualSchema\",\n" +
                "      \"strict\": true,\n" +
                "      \"schema\": {\n" +
                "        \"type\": \"object\",\n" +
                "        \"properties\": {\n" +
                "          \"introduction\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"An introductory overview for the passage or topic being analyzed\"\n" +
                "          },\n" +
                "          \"verse_groups\": {\n" +
                "            \"type\": \"array\",\n" +
                "            \"description\": \"An array of verse groups with thematic similarities\",\n" +
                "            \"items\": {\n" +
                "              \"type\": \"object\",\n" +
                "              \"properties\": {\n" +
                "                \"group_title\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A short, descriptive title for the verse group\"\n" +
                "                },\n" +
                "                \"verses\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"The verses that belong to this group\"\n" +
                "                },\n" +
                "                \"abbreviated_description\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A brief sentence describing the group's focus\"\n" +
                "                },\n" +
                "                \"thematic_paragraph\": {\n" +
                "                  \"type\": \"string\",\n" +
                "                  \"description\": \"A paragraph about the thematic similarities between the verses in the group\"\n" +
                "                },\n" +
                "                \"focus_questions\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"description\": \"An array of focus questions for this verse group\",\n" +
                "                  \"items\": {\n" +
                "                    \"type\": \"object\",\n" +
                "                    \"properties\": {\n" +
                "                      \"question\": {\n" +
                "                        \"type\": \"string\",\n" +
                "                        \"description\": \"A focus question for deeper understanding\"\n" +
                "                      },\n" +
                "                      \"answer\": {\n" +
                "                        \"type\": \"string\",\n" +
                "                        \"description\": \"A suggested answer to the focus question\"\n" +
                "                      }\n" +
                "                    },\n" +
                "                    \"required\": [\"question\", \"answer\"],\n" +
                "                    \"additionalProperties\": false\n" +
                "                  }\n" +
                "                },\n" +
                "                \"reflect_questions\": {\n" +
                "                  \"type\": \"array\",\n" +
                "                  \"description\": \"An array of reflection questions or suggestions for this group\",\n" +
                "                  \"items\": {\n" +
                "                    \"type\": \"string\",\n" +
                "                    \"description\": \"A reflection question or suggestion for further contemplation\"\n" +
                "                  }\n" +
                "                }\n" +
                "              },\n" +
                "              \"required\": [\"group_title\", \"verses\", \"abbreviated_description\", \"thematic_paragraph\", \"focus_questions\", \"reflect_questions\"],\n" +
                "              \"additionalProperties\": false\n" +
                "            }\n" +
                "          },\n" +
                "          \"thematic_summary\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"description\": \"A concluding summary highlighting the thematic connections across all verse groups\"\n" +
                "          }\n" +
                "        },\n" +
                "        \"required\": [\"introduction\", \"verse_groups\", \"thematic_summary\"],\n" +
                "        \"additionalProperties\": false\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String selectedRequestBody = switch (assistantName) {
            case "ParaphraseGPT" -> paraphraseGPTRequestBody;
            case "TheophanyGPT" -> theophanyGPTRequestBody;
            case "IntratextualGPT" -> intratextualGPTRequestBody;
            case "AbstractGPT" -> abstractGPTRequestBody;
            default -> requestBody;
        };

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiURL))
                    .timeout(Duration.ofMinutes(5))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(selectedRequestBody))
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
                        SwingUtilities.invokeLater(() -> responseArea.append(content));
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

