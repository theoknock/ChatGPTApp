import java.util.List;
import java.util.Map;

public class Schemas {
    public static final Map<String, Object> BasicSchema = Map.of(
            "name", "response_name",
            "strict", true,
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "psalm_number", Map.of("type", "integer"),
                            "response", Map.of("type", "string")
                    ),
                    "required", List.of("psalm_number", "response"),
                    "additionalProperties", false
            )
    );

    public static final Map<String, Object> AbstractSchema = Map.of(
            "name", "AbstractSchema",
            "strict", true,
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "psalm_number", Map.of(
                                    "type", "integer",
                                    "description", "The Psalm number for which the abstract is generated."
                            ),
                            "sections", Map.of(
                                    "type", "array",
                                    "items", Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "heading", Map.of(
                                                            "type", "string",
                                                            "description", "The name of the section, representing different aspects of the Psalm."
                                                    ),
                                                    "body", Map.of(
                                                            "type", "string",
                                                            "description", "The content generated for this section of the Psalm."
                                                    )
                                            ),
                                            "required", List.of("heading", "body"),
                                            "additionalProperties", false
                                    ),
                                    "description", "Array of sections, each containing a heading and a body."
                            )
                    ),
                    "required", List.of("psalm_number", "sections"),
                    "additionalProperties", false
            )
    );

    public static final Map<String, Object> ParaphraseSchema = Map.of(
            "name", "ParaphraseSchema",
            "strict", true,
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "psalm_number", Map.of("type", "integer"),
                            "verses", Map.of(
                                    "type", "array",
                                    "items", Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "verse", Map.of("type", "integer"),
                                                    "original", Map.of("type", "string"),
                                                    "simplified", Map.of("type", "string"),
                                                    "amplified", Map.of("type", "string"),
                                                    "contextual", Map.of("type", "string")
                                            ),
                                            "required", List.of("verse", "original", "simplified", "amplified", "contextual"),
                                            "additionalProperties", false
                                    )
                            )
                    ),
                    "required", List.of("psalm_number", "verses"),
                    "additionalProperties", false
            )
    );

    public static final Map<String, Object> IntratextualSchema = Map.of(
            "name", "IntratextualSchema",
            "strict", true,
            "schema", Map.of(
                    "type", "object",
                    "properties", Map.of(
                            "psalm_number", Map.of(
                                    "type", "integer",
                                    "description", "The Psalm number for which the synoptic analysis is generated."
                            ),
                            "summary", Map.of(
                                    "type", "string",
                                    "description", "A summary of the overall meaning and purpose of the psalm, connecting its message to Christian faith and God's purpose for humanity."
                            ),
                            "thematic_groups", Map.of(
                                    "type", "array",
                                    "items", Map.of(
                                            "type", "object",
                                            "properties", Map.of(
                                                    "summary_heading", Map.of(
                                                            "type", "string",
                                                            "description", "A concise, one-line summary heading describing the thematic relationship between the verses in the group."
                                                    ),
                                                    "subheading", Map.of(
                                                            "type", "string",
                                                            "description", "A one-sentence run-in subheading that describes the main point or central idea presented by the group of verses, followed by scripture references."
                                                    ),
                                                    "expounded_paragraph", Map.of(
                                                            "type", "string",
                                                            "description", "A 5-6 sentence paragraph that further explains the meaning of the thematic group, expounding on the main point described by the subheading."
                                                    ),
                                                    "focus", Map.of(
                                                            "type", "array",
                                                            "items", Map.of(
                                                                    "type", "object",
                                                                    "properties", Map.of(
                                                                            "question", Map.of(
                                                                                    "type", "string",
                                                                                    "description", "A focus question that directs attention to the most significant aspects of the main point."
                                                                            ),
                                                                            "answer", Map.of(
                                                                                    "type", "string",
                                                                                    "description", "The answer to the focus question."
                                                                            )
                                                                    ),
                                                                    "required", List.of("question", "answer"),
                                                                    "additionalProperties", false
                                                            ),
                                                            "description", "A section with at least 2-3 focus questions and answers highlighting the key aspect(s) of the thematic group."
                                                    ),
                                                    "reflect", Map.of(
                                                            "type", "string",
                                                            "description", "A set of reflection questions or suggestions that encourage readers to reflect on the applicability of the theme in their life, asking them to recount past experiences and relate them to the main point."
                                                    )
                                            ),
                                            "required", List.of("summary_heading", "subheading", "expounded_paragraph", "focus", "reflect"),
                                            "additionalProperties", false
                                    ),
                                    "description", "A list of thematic groups, each with its own heading, subheading, paragraph, focus questions, and reflection section."
                            ),
                            "closing_summary", Map.of(
                                    "type", "string",
                                    "description", "A closing summary that summarizes the overall meaning and purpose of the psalm, connecting it to Christian faith and God's purpose for humanity."
                            )
                    ),
                    "required", List.of("psalm_number", "summary", "thematic_groups", "closing_summary"),
                    "additionalProperties", false
            )
    );
}