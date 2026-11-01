package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The version of the server's jar the setups name. */
private const val JAR_VERSION = LibraryFacts.VERSION

/** The server's jar by its name alone. */
private const val JAR = "mcp-$JAR_VERSION-all.jar"

/** The server's jar where Maven puts it on a machine. */
private const val MAVEN_JAR = "~/.m2/repository/com/pantrixui/mcp/$JAR_VERSION/$JAR"

/** The words of the page's setup, in each language and each size: its steps and each assistant's lines. */
internal object SetupSay {
    val SetupTitle = BySize(Words("Set it up in a minute", "Bir dakikada kur"), phone = Words("Set it up", "Kur"))
    val SetupLead = BySize(
        Words(
            "The server runs on JDK 17 or newer, and the client starts it itself over stdio. The skill goes where " +
                "the assistant reads its skills.",
            "Sunucu JDK 17 ya da üstünde çalışır ve istemci onu stdio üzerinden kendisi başlatır. Beceri, " +
                "asistanın becerilerini okuduğu yere gider."
        ),
        tablet = Words(
            "JDK 17 or newer; the client starts the server itself over stdio.",
            "JDK 17 ya da üstü; istemci sunucuyu stdio üzerinden kendisi başlatır."
        ),
        phone = Words(
            "JDK 17 or newer; the client starts the server over stdio.",
            "JDK 17 ya da üstü; istemci sunucuyu stdio üzerinden başlatır."
        )
    )

    /** The steps of the setup, a desktop's alone: words, then a name of the code where one ends them. */
    val Steps = listOf(
        Words("Take the jar, ", "Jar'ı al, ") to JAR,
        Words("Name it in your assistant's settings.", "Asistanının ayarlarında ona yer ver.") to null,
        Words(
            "Write the skill into the project, so the team gets it through git.",
            "Beceriyi projeye yaz, ekip git'ten alsın."
        ) to null,
        Words(
            "Ask for a screen: the assistant loads the skill and calls the tools.",
            "Bir ekran iste: asistan beceriyi yükler ve araçları çağırır."
        ) to null
    )
    val Fewer = Words("Fewer tools, fewer wrong turns. ", "Daha az araç, daha az yanlış dönüş. ")
    val FewerEnd = Words(" opens the server with those two alone.", " sunucuyu yalnızca o ikisiyle açar.")

    /** The assistants' setups; a phone leaves out the last. */
    val Clients = listOf(
        Client(
            BySize(Words("Claude Code", "Claude Code")),
            BySize(
                Words(
                    "Then /mcp lists pantrix-ui as connected, and /skills lists the skill.",
                    "Sonra /mcp pantrix-ui'yi bağlı gösterir, /skills de beceriyi."
                ),
                tablet = Words(
                    "Then /mcp lists pantrix-ui, and /skills lists the skill.",
                    "Sonra /mcp pantrix-ui'yi, /skills de beceriyi gösterir."
                ),
                phone = Words("/mcp and /skills then list pantrix-ui.", "Sonra /mcp ve /skills pantrix-ui'yi gösterir.")
            ),
            BySize(
                listOf(
                    SetupLine(comment = Words("# The server", "# Sunucu")),
                    SetupLine("claude mcp add pantrix-ui -- java -jar $MAVEN_JAR"),
                    SetupLine(),
                    SetupLine(comment = Words("# The skill, into the project", "# Beceri, projeye")),
                    SetupLine("java -jar $JAR skill .claude/skills")
                ),
                tablet = listOf(
                    SetupLine(comment = Words("# The server", "# Sunucu")),
                    SetupLine("claude mcp add pantrix-ui -- java -jar $JAR"),
                    SetupLine(),
                    SetupLine(comment = Words("# The skill, into the project", "# Beceri, projeye")),
                    SetupLine("java -jar $JAR skill .claude/skills")
                ),
                phone = listOf(
                    SetupLine("claude mcp add pantrix-ui \\"),
                    SetupLine("  -- java -jar $JAR"),
                    SetupLine(),
                    SetupLine(comment = Words("# the skill, into the project", "# beceri, projeye")),
                    SetupLine("java -jar $JAR \\"),
                    SetupLine("  skill .claude/skills")
                )
            )
        ),
        Client(
            BySize(
                Words("Claude Desktop, Cursor", "Claude Desktop, Cursor"),
                tablet = Words("Desktop, Cursor", "Desktop, Cursor")
            ),
            BySize(
                Words(
                    "Any client that reads an mcpServers field takes the same entry.",
                    "mcpServers alanını okuyan her istemci aynı girdiyi alır."
                ),
                tablet = Words("Any client that reads an mcpServers field.", "mcpServers alanını okuyan her istemci."),
                phone = Words("Any client with an mcpServers field.", "mcpServers alanı olan her istemci.")
            ),
            BySize(
                listOf(
                    SetupLine("{"),
                    SetupLine("  \"mcpServers\": {"),
                    SetupLine("    \"pantrix-ui\": {", kind = TokenKind.StringLiteral),
                    SetupLine("      \"command\": \"java\","),
                    SetupLine("      \"args\": [\"-jar\", \"/path/$JAR\"]"),
                    SetupLine("    }"),
                    SetupLine("  }"),
                    SetupLine("}")
                ),
                tablet = listOf(
                    SetupLine("{ \"mcpServers\": {"),
                    SetupLine("    \"pantrix-ui\": {", kind = TokenKind.StringLiteral),
                    SetupLine("      \"command\": \"java\","),
                    SetupLine("      \"args\": [\"-jar\", \"/path/$JAR\"] } } }")
                ),
                phone = listOf(
                    SetupLine("{ \"mcpServers\": {"),
                    SetupLine("  \"pantrix-ui\": {", kind = TokenKind.StringLiteral),
                    SetupLine("    \"command\": \"java\","),
                    SetupLine("    \"args\": [\"-jar\","),
                    SetupLine("      \"/path/$JAR\"]"),
                    SetupLine("} } }")
                )
            )
        ),
        Client(
            BySize(Words("A team’s repository", "Bir ekibin deposu"), tablet = Words("A team", "Bir ekip")),
            BySize(
                Words(
                    "The file goes into git, the jar’s path does not: each machine sets PANTRIX_MCP_JAR.",
                    "Dosya git'e girer, jar'ın yolu girmez: her makine PANTRIX_MCP_JAR'ı kendisi tanımlar."
                ),
                tablet = Words(
                    "The file goes into git; each machine sets PANTRIX_MCP_JAR.",
                    "Dosya git'e girer; her makine PANTRIX_MCP_JAR'ı tanımlar."
                ),
                phone = Words("Each machine sets PANTRIX_MCP_JAR.", "Her makine PANTRIX_MCP_JAR'ı tanımlar.")
            ),
            BySize(
                listOf(
                    SetupLine(comment = Words("# .mcp.json at the app’s root", "# Uygulamanın kökünde .mcp.json")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"command\": \"java\", \"args\": [\"-jar\", \"\${PANTRIX_MCP_JAR}\"] } } }"),
                    SetupLine(),
                    SetupLine(comment = Words("# in each machine’s shell profile", "# her makinenin kabuk profilinde")),
                    SetupLine("export PANTRIX_MCP_JAR=\$HOME${MAVEN_JAR.removePrefix("~")}")
                ),
                tablet = listOf(
                    SetupLine(comment = Words("# .mcp.json at the app’s root", "# Uygulamanın kökünde .mcp.json")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"command\": \"java\", \"args\": [\"-jar\", \"\${PANTRIX_MCP_JAR}\"] } } }")
                ),
                phone = listOf(
                    SetupLine(comment = Words("# .mcp.json, in git", "# .mcp.json, git'te")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("  \"command\": \"java\","),
                    SetupLine("  \"args\": [\"-jar\", \"\${PANTRIX_MCP_JAR}\"]"),
                    SetupLine("} } }")
                )
            )
        ),
        Client(
            BySize(Words("Without a server", "Sunucusuz"), tablet = Words("No server", "Sunucusuz")),
            BySize(
                Words(
                    "An assistant that reads neither MCP nor skills is given llms.txt.",
                    "Ne MCP'yi ne becerileri okuyan bir asistana llms.txt verilir."
                ),
                tablet = Words(
                    "Give an assistant that reads neither llms.txt.",
                    "İkisini de okumayan bir asistana llms.txt'yi ver."
                )
            ),
            BySize(
                listOf(
                    SetupLine(
                        comment = Words(
                            "# Every document, as a site serves them",
                            "# Bir sitenin sunduğu gibi bütün belgeler"
                        )
                    ),
                    SetupLine("java -jar $JAR docs ./pantrix-docs"),
                    SetupLine(),
                    SetupLine(comment = Words("# Then give the assistant", "# Sonra asistana ver")),
                    SetupLine("pantrix-docs/llms.txt", kind = TokenKind.Keyword)
                ),
                tablet = listOf(
                    SetupLine("java -jar $JAR docs ./pantrix-docs"),
                    SetupLine(comment = Words("# then: pantrix-docs/llms.txt", "# sonra: pantrix-docs/llms.txt"))
                )
            ),
            onPhone = false
        )
    )
}
