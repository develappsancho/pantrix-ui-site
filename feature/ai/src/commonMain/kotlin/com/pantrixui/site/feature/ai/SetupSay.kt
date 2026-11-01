package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The version of the server's jar the setups name: the latest release's, attached to it. */
private const val JAR_VERSION = LibraryFacts.RELEASE

/** The server's jar by its name alone. */
private const val JAR = "mcp-$JAR_VERSION-all.jar"

/** Where the site serves the server: at the address the site is opened at. */
private val ADDRESS: String
    get() = SiteAddress.mcp

/** The address with no scheme, as a sentence names it. */
private val SHORT_ADDRESS: String
    get() = ADDRESS.substringAfter("://")

/** The words of the page's setup, in each language and each size: its steps and each assistant's lines. */
internal object SetupSay {
    val SetupTitle = BySize(Words("Set it up in a minute", "Bir dakikada kur"), phone = Words("Set it up", "Kur"))
    val SetupLead = BySize(
        Words(
            "The server answers at $SHORT_ADDRESS, so an assistant needs nothing but the address. Its jar runs it on " +
                "your own machine instead, on JDK 17 or newer.",
            "Sunucu $SHORT_ADDRESS adresinde cevap verir; bir asistana adresten başkası gerekmez. Jar'ı onu kendi " +
                "makinende de çalıştırır, JDK 17 ya da üstünde."
        ),
        tablet = Words(
            "An assistant needs only the address; the jar runs the server on your machine.",
            "Asistana yalnızca adres gerekir; jar sunucuyu kendi makinende çalıştırır."
        ),
        phone = Words(
            "The address is all an assistant needs.",
            "Bir asistana adres yeter."
        )
    )

    /** The steps of the setup, a desktop's alone: words, then a name of the code where one ends them. */
    val Steps = listOf(
        Words("Give your assistant the address, ", "Asistanına adresi ver: ") to SHORT_ADDRESS,
        Words("Or take the jar from a release, ", "Ya da jar'ı bir sürümden al: ") to JAR,
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
    val FewerEnd = Words(
        " opens the jar's server with those two alone.",
        " jar'ın sunucusunu yalnızca o ikisiyle açar."
    )

    /** The assistants' setups; a phone leaves out the last. */
    val Clients = listOf(
        Client(
            BySize(Words("Claude Code", "Claude Code")),
            BySize(
                Words(
                    "Then /mcp lists pantrix-ui as connected, and /skills lists the plugin's skill.",
                    "Sonra /mcp pantrix-ui'yi bağlı gösterir, /skills de eklentinin becerisini."
                ),
                tablet = Words(
                    "Then /mcp lists pantrix-ui, and /skills lists the skill.",
                    "Sonra /mcp pantrix-ui'yi, /skills de beceriyi gösterir."
                ),
                phone = Words("/mcp and /skills then list pantrix-ui.", "Sonra /mcp ve /skills pantrix-ui'yi gösterir.")
            ),
            BySize(
                // The plugin brings the server at the site's address and the skill: the server added by its address
                // as well would be listed twice.
                listOf(
                    SetupLine(
                        comment = Words(
                            "# The server and the skill, with Pantrix UI's plugin",
                            "# Sunucu ve beceri, Pantrix UI'ın eklentisiyle"
                        )
                    ),
                    SetupLine("claude plugin marketplace add ${LibraryFacts.PACKAGES_REPOSITORY}"),
                    SetupLine("claude plugin install pantrix-ui@pantrix-ui")
                ),
                tablet = listOf(
                    SetupLine(comment = Words("# Server and skill, with the plugin", "# Sunucu ve beceri, eklentiyle")),
                    SetupLine("claude plugin marketplace add \\"),
                    SetupLine("  ${LibraryFacts.PACKAGES_REPOSITORY}"),
                    SetupLine("claude plugin install pantrix-ui@pantrix-ui")
                ),
                phone = listOf(
                    SetupLine(comment = Words("# the server and the skill", "# sunucu ve beceri")),
                    SetupLine("claude plugin marketplace add \\"),
                    SetupLine("  ${LibraryFacts.PACKAGES_REPOSITORY}"),
                    SetupLine("claude plugin install \\"),
                    SetupLine("  pantrix-ui@pantrix-ui")
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
                    "Any client that reads an mcpServers field runs the jar, attached to each release.",
                    "mcpServers alanını okuyan her istemci, her sürüme ekli jar'ı çalıştırır."
                ),
                tablet = Words(
                    "Any client with an mcpServers field; the jar is on each release.",
                    "mcpServers alanı olan her istemci; jar her sürüme eklidir."
                ),
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
                    "Both go into git, and no machine needs the jar to use the server.",
                    "İkisi de git'e girer; sunucuyu kullanmak için hiçbir makineye jar gerekmez."
                ),
                tablet = Words(
                    "Both go into git; no machine needs the jar.",
                    "İkisi de git'e girer; hiçbir makineye jar gerekmez."
                ),
                phone = Words("The file goes into git.", "Dosya git'e girer.")
            ),
            BySize(
                listOf(
                    SetupLine(comment = Words("# .mcp.json at the app’s root", "# Uygulamanın kökünde .mcp.json")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"type\": \"http\", \"url\": \"$ADDRESS\" } } }"),
                    SetupLine(),
                    SetupLine(comment = Words("# The skill, into the project", "# Beceri, projeye")),
                    SetupLine("java -jar $JAR skill .claude/skills")
                ),
                tablet = listOf(
                    SetupLine(comment = Words("# .mcp.json at the app’s root", "# Uygulamanın kökünde .mcp.json")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"type\": \"http\", \"url\": \"$ADDRESS\" } } }")
                ),
                phone = listOf(
                    SetupLine(comment = Words("# .mcp.json, in git", "# .mcp.json, git'te")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("  \"type\": \"http\","),
                    SetupLine("  \"url\": \"$ADDRESS\""),
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
                            "# Every document, its links leading to this site",
                            "# Bütün belgeler, bağlantıları bu siteye giden"
                        )
                    ),
                    SetupLine(LLMS_ADDRESS, kind = TokenKind.Keyword),
                    SetupLine(),
                    SetupLine(
                        comment = Words("# Or on your machine, from the jar", "# Ya da kendi makinende, jar'dan")
                    ),
                    SetupLine("java -jar $JAR docs ./pantrix-docs")
                ),
                tablet = listOf(
                    SetupLine(LLMS_ADDRESS, kind = TokenKind.Keyword),
                    SetupLine(
                        comment = Words(
                            "# or: java -jar … docs ./pantrix-docs",
                            "# ya da: java -jar … docs ./pantrix-docs"
                        )
                    )
                )
            ),
            onPhone = false
        )
    )
}
