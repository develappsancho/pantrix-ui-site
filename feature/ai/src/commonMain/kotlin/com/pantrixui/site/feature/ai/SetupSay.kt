package com.pantrixui.site.feature.ai

import com.pantrixui.site.core.designsystem.BySize
import com.pantrixui.site.core.model.LibraryFacts
import com.pantrixui.site.core.model.SiteAddress
import com.pantrixui.site.core.model.TokenKind
import com.pantrixui.site.core.model.Words

/** The server's jar by its name alone: the latest release's, attached to it. */
private const val JAR = LibraryFacts.MCP_JAR

/** The skill's zip by its name alone, attached to the same release. */
private const val SKILL_ZIP = LibraryFacts.SKILL_ZIP

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
            "The server answers at $SHORT_ADDRESS, so an assistant needs nothing but the address. Its jar, among the " +
                "files below, runs it on your own machine instead, on JDK 17 or newer.",
            "Sunucu $SHORT_ADDRESS adresinde cevap verir; bir asistana adresten başkası gerekmez. Aşağıdaki " +
                "dosyalardaki jar'ı onu kendi makinende de çalıştırır, JDK 17 ya da üstünde."
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

    /** The steps of the setup, a desktop's alone. */
    val Steps = listOf(
        Step(Words("Give your assistant the address, ", "Asistanına adresi ver: "), SHORT_ADDRESS),
        Step(
            Words("Or download the jar from the release, ", "Ya da jar'ı sürümden indir: "),
            JAR,
            LibraryFacts.releaseFile(JAR)
        ),
        Step(
            Words(
                "Unzip the skill into the project's .claude/skills, so the team gets it through git: ",
                "Beceriyi projenin .claude/skills klasörüne aç, ekip git'ten alsın: "
            ),
            SKILL_ZIP,
            LibraryFacts.releaseFile(SKILL_ZIP)
        ),
        Step(
            Words(
                "Ask for a screen: the assistant loads the skill and calls the tools.",
                "Bir ekran iste: asistan beceriyi yükler ve araçları çağırır."
            )
        )
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
                    "A client that takes an address, as Cursor, needs nothing else; one that starts a command, as " +
                        "Claude Desktop's file, runs the jar from the files below.",
                    "Adres alan bir istemciye, Cursor gibi, başka bir şey gerekmez; komut başlatan bir istemci, " +
                        "Claude Desktop'ın dosyası gibi, aşağıdaki dosyalardaki jar'ı çalıştırır."
                ),
                tablet = Words(
                    "Cursor takes the address; a command runs the jar, among the files below.",
                    "Cursor adresi alır; bir komut jar'ı çalıştırır, aşağıdaki dosyalarda."
                ),
                phone = Words("Cursor takes the address; the jar is below.", "Cursor adresi alır; jar aşağıda.")
            ),
            BySize(
                listOf(
                    SetupLine(comment = Words("# The address, as Cursor takes it", "# Adres, Cursor'un aldığı gibi")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"url\": \"$ADDRESS\" } } }"),
                    SetupLine(),
                    SetupLine(
                        comment = Words(
                            "# Or the jar, as Claude Desktop starts it",
                            "# Ya da jar, Claude Desktop'ın başlattığı gibi"
                        )
                    ),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"command\": \"java\","),
                    SetupLine("    \"args\": [\"-jar\", \"/path/$JAR\"] } } }")
                ),
                tablet = listOf(
                    SetupLine(comment = Words("# The address", "# Adres")),
                    SetupLine("{ \"mcpServers\": { \"pantrix-ui\": {"),
                    SetupLine("    \"url\": \"$ADDRESS\" } } }"),
                    SetupLine(comment = Words("# Or the jar", "# Ya da jar")),
                    SetupLine("    \"command\": \"java\","),
                    SetupLine("    \"args\": [\"-jar\", \"/path/$JAR\"]")
                ),
                phone = listOf(
                    SetupLine("{ \"mcpServers\": {"),
                    SetupLine("  \"pantrix-ui\": {", kind = TokenKind.StringLiteral),
                    SetupLine("    \"url\":"),
                    SetupLine("      \"$ADDRESS\""),
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
                    SetupLine(comment = Words("# The skill, from the release's zip", "# Beceri, sürümün zip'inden")),
                    SetupLine("unzip $SKILL_ZIP -d .claude/skills")
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
