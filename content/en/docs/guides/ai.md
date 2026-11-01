---
title: Teaching Pantrix to an assistant
description: The documents written so that coding assistants (Claude, Copilot, Cursor…) learn Pantrix from the library's own sources rather than by guessing.
---

Everything said here was seen in tests and builds run in this repository. Section 9 says what was seen where. The server and its documents are published with the library, in its version, as `com.pantrixui:mcp` (section 4).

## 1. The problem {#problem}

An assistant that has not read the library's sources:

*   **Falls back to Material:** it writes `Button` for `PantrixButton` and `MaterialTheme` for `PantrixTheme`. Pantrix has no Material, and the code does not compile.
*   **Makes up parameters:** it gives a name a component does not have (`label =`), leaves out one it has, or gets its type wrong.
*   **Guesses packages:** it writes an import that does not exist, such as `import com.pantrixui.components.Button`. Pantrix's components live in packages of their own, such as `com.pantrixui.components.elements.button`.
*   **Does not know the rules:** that an automation tag is built from `tagKey`, that text goes through Pantrix's own wrappers, that sizes come from tokens.

A document written by hand solves part of this, but goes stale as the library changes. A parameter is renamed, the document keeps the old name, and the assistant writes the old name.

:::sketch ai
:::

## 2. What is written {#what-is-written}

:::table
| File | What | What for |
| :--- | :--- | :--- |
| `llms.txt` | The index, in the format of [llmstxt.org](https://llmstxt.org). What the library is, its artifacts, the tools published with it (the detekt rules and this server), the smallest app and the rules code that uses Pantrix keeps to; a line for each page of the demo under its group (its link, the first sentence of its summary, the component's name); a line for each guide (its link, the first sentence of its summary); the modules' API references; the skill's link. About 31 KB, some 8,000 tokens. | The file an assistant reads first: it says where everything is. |
| `llms-full.txt` | All of it in one file: the same opening, the demo's 114 pages in the order of its menu, the 23 guides, then the API no page documents, by module. It holds no links. About 4.3 MB, some 1.2 million tokens. | A model with a very wide context, or loading into a document index (RAG). |
| `pages/<key>.md` | A page of the demo: its summary, its features, the component's short examples, the compiled examples (with their imports and the code of the helpers they use), and the API the page documents. The key is the page's address on the web: `date-picker`. | Giving an assistant the component of the one screen it is writing. |
| `api/<module>.md` | All of a module's public API, by package: signatures, descriptions, parameters, enum entries, members. | Looking at the whole of a declaration. |
| `guides/<key>.md` | A guide for apps, in English: how a part of the library is used across an app, the problem every Compose Multiplatform app has there, the library's answer, its pitfalls and how it is tested. The key is the guide's name without `_GUIDE`, in small letters: `docs/guide/date-time/DATE_TIME_GUIDE.en.md` is `guides/date-time.md`. Its links lead to the other guides among the documents, and to the repository's other files on GitHub. | Giving an assistant how a whole part of an app is done: its tests, its layout by the window, dates, the clipboard. |
| `pantrix-ui.json` | The library as data: its modules, its pages and their examples, its components, every public declaration and its parameters. About 5 MB. | Tools: the MCP server, a site, a code generator. |
| `pantrix-ui.schema.json` | The JSON Schema (2020-12) of `pantrix-ui.json`. Written by hand: `ai/schema/pantrix-ui.schema.json`. | Telling a tool that reads the JSON what it can rely on. |
| `CHANGELOG.md` | The repository's changelog, as it is. | What changed in a version. |
| `tr/guides/<key>.md`, `tr/CHANGELOG.md` | The Turkish of the guides for apps and of the changelog, in the same form: a guide's links lead to the Turkish of the other guides. The index (`llms.txt`) and the MCP server name the English alone. | A place that reads in Turkish, such as the site's Turkish pages. |
| `skills/pantrix-ui/` | The skill. `SKILL.md`: where to look things up, the library's rules ("Core rules"), Pantrix's counterparts of Material's names, the steps of writing a screen and of reviewing code, the list of the references. `references/`: setup, theming, layout, forms, overlays, charts, languages and platform services, accessibility, tests, Material 3. Written by hand (`ai/skills/pantrix-ui/`); only `references/components.md` is written from the catalogue at every build: every component with its import, what it is for, its keywords, its tags and its page, and the icons. | The skill an assistant installs (section 4). `llms.txt` and the prompts read the rules from it; the MCP server gives the skill itself as well (`get_rules`, `get_documentation_page`, `pantrix://skill`). |
| `files.txt` | The list of every document, a path a line. | A reader that cannot list a jar's resources: the server's commands `docs` and `skill`. |
:::

## 3. Making them {#making-them}

```bash
./gradlew :ai:mcp:generateAiDocs
```

*   **Where:** the files are written to the folder `ai/generated/` at the root of the repository: `ai/generated/llms.txt`, `ai/generated/pages/button.md`…
*   **Not in git:** the folder is in `.gitignore`. Every build writes the files again from the sources.
*   **Not edited by hand:** the task empties the folder and writes it again at every run; a change made by hand is gone at the next build. A change is made to a source (section 5).
*   **What else makes them:** `:ai:mcp:jar`, `:ai:mcp:test` and `./gradlew build` run the task too. The jar carries the files under `pantrix-ui/`.
*   **When they are made again:** where the sources did not change, Gradle takes the result from its cache (`UP-TO-DATE`, `FROM-CACHE`). When a KDoc, a component's annotation, a demo page or its English changes, the task runs again.
*   **Removing them:** `./gradlew clean` removes the folder too.

### For a site

Where the documents are to be served from a site, its address is given, and the links are then absolute:

```bash
./gradlew :ai:mcp:generateAiDocs -Ppantrix.ai.baseUrl=https://site-address
```

*   A trailing `/` is added where it is left out.
*   Without an address the links are relative (`pages/button.md`) and work as long as the files stand beside each other.
*   The `$schema` of `pantrix-ui.json` is written with the address too; without one it is the file's name alone.

## 4. Giving them to an assistant {#giving-them-to-an-assistant}

### The MCP server

An assistant reads the documents best from Pantrix's MCP server: it chooses what to ask, and takes only what it needs. The server is one jar that runs by itself, `mcp-<version>-all.jar`. It is published with the library, as `com.pantrixui:mcp` with the classifier `all`: after `./gradlew publishToMavenLocal` it is in `~/.m2/repository/com/pantrixui/mcp/<version>/` ([the publishing guide](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/publishing/PUBLISHING_GUIDE.md), in Turkish). In this repository it is made with:

```bash
./gradlew :ai:mcp:allJar
```

The jar is written to `ai/mcp/build/libs/mcp-<version>-all.jar`. An assistant's settings name the command that runs it. In Claude Code:

```bash
claude mcp add pantrix-ui -- java -jar /path/mcp-<version>-all.jar
```

In a client that reads its settings from an `mcpServers` field (Claude Desktop, Cursor…):

```json
{
  "mcpServers": {
    "pantrix-ui": {
      "command": "java",
      "args": ["-jar", "/path/mcp-<version>-all.jar"]
    }
  }
}
```

*   **What it runs on:** JDK 17 or newer. The client starts the jar itself and speaks to it over stdio.
*   **Tools:**

    | Tool | What it gives |
    | :--- | :--- |
    | `search_components` | The components found by what they are for (`toast`, `bottom sheet`); every one without a query |
    | `get_component` | A component's page: its features, its examples with their imports, its API |
    | `get_component_metadata` | A component as data: its import, artifact, keywords, tags, every form and its parameters |
    | `search_api`, `get_api` | Finding a declaration by its name or by what it does, and getting the whole of it |
    | `search_icons` | The icons of `PantrixIcons` |
    | `search_documentation`, `get_documentation_page` | Every page, the theme's, the window's and the services' among them; the guides (`guide/testing`, `guide/adaptive-usage`…, or their titles); the skill's references (`skill/forms`, `skill/testing`…, or the link the skill writes: `references/forms.md`), and the skill itself by the key `skill` |
    | `list_examples`, `get_example` | A page's examples, and the whole code of one |
    | `get_changelog` | The changelog, or the part of it of one version |
    | `get_rules` | The skill itself: the rules code written with Pantrix keeps, Pantrix's counterparts of Material's names, the steps of writing and of reviewing a screen, the list of the references. An assistant that has not installed the skill reads it here too |
*   **Resources:** `pantrix://components`, `pantrix://api`, `pantrix://examples`, `pantrix://documentation` (the pages, the guides and the skill's references) and `pantrix://icons` are lists in JSON; `pantrix://llms.txt`, `pantrix://llms-full.txt`, `pantrix://schema` (`pantrix-ui.json`) and `pantrix://skill` (the skill whole: `SKILL.md` and its references) are the documents themselves.
*   **Prompts:** `find_component_for_use_case` (the component for a need, and how it is used), `implement_screen` (a screen, with its rules), `write_ui_test` (a component's test, with its tags and the helpers of `com.pantrixui:testing`). Each carries what the documents say and the rules in itself.
*   **The tool filter:** `java -jar mcp-<version>-all.jar --tools search_components,get_component` opens the server with those tools alone. The fewer tools an assistant chooses between, the less often it chooses wrong.
*   **Commands:** `java -jar mcp-<version>-all.jar docs <folder>` writes every document into the folder as a site would serve them; `skill <folder>` writes the skill into `<folder>/pantrix-ui` (`~/.claude/skills`, for example).
*   **Standard output is the protocol's alone:** while the server runs, nothing but the protocol is written to its standard output, or a client fails on a line it cannot read. The SDK's logger (kotlin-logging) prints a line to the standard output at its first logger; the server turns that off and sends whatever writes to the standard output to the standard error. The protocol's test measures it (section 8).
*   **As a library:** `com.pantrixui:mcp` without the classifier is the server's code and the documents, with its dependencies in its POM. A site that serves the same tools over HTTP takes it, and makes its server with `PantrixMcp.server()`.
*   **The licences:** the jar carries the code of the libraries it runs on. `META-INF/LICENSE` is Pantrix's licence; `META-INF/THIRD-PARTY.md` names each library it bundles with its licence, and at its end each module with its version; the libraries' own licence files are under `META-INF/licenses/`. The build stops where the notice and what the jar bundles part (section 6).

### The skill

The skill teaches an assistant how to work with Pantrix: its rules, the steps of writing a screen, and how the parts of the library fit together. It is in the Agent Skills format: a folder, with `SKILL.md` and its references in it. The assistant learns from the description in `SKILL.md` when to use it, and reads the references as it needs them.

```bash
java -jar mcp-<version>-all.jar skill <folder>
```

*   **Where to:** Claude Code reads the skills in a project's `.claude/skills/` folder and in the user's `~/.claude/skills/`. A skill written into the app's repository reaches the team through git; one written into the user's folder opens in every project. Other assistants read skills from folders of their own; the assistant's documentation names the folder.
*   **With the server or without it:** the skill says to use the server's tools where there is one (`search_components`, `get_component_metadata`…), and the documents the command `docs` writes where there is none. The two together are best: the skill says what to do, the server says what the library is today.
*   **The repository's copy is not installed:** `ai/skills/pantrix-ui/` has no `references/components.md`, which the build writes. What is installed is the jar's, or `ai/generated/skills/pantrix-ui/`.
*   **The version:** a skill belongs to a version of the library. When the library is updated, the skill is written again.

### In another project, from start to end

An app's repository that uses Pantrix takes the skill and the server so. The first step is taken in Pantrix's repository, the second on each machine, the others in the app's.

1.  **The library and the server:** `./gradlew clean publishToMavenLocal` in Pantrix's repository publishes both. The app takes the library from `mavenLocal()`; the dependency lines are in the skill's `references/setup.md`, and the publishing in detail in the [publishing guide](https://github.com/developersancho/pantrix-ui-cmp/blob/main/docs/guide/publishing/PUBLISHING_GUIDE.md) (in Turkish).
2.  **The jar:** the server's jar now stands in the local Maven repository. Each machine keeps where it is in an environment variable of its own, in the shell's profile for example:

    ```bash
    export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/<version>/mcp-<version>-all.jar
    ```
3.  **The skill:** `java -jar "$PANTRIX_MCP_JAR" skill .claude/skills` at the app's root. `.claude/skills/pantrix-ui/` goes into git; the team takes the skill with the repository.
4.  **The server:** a `.mcp.json` at the app's root:

    ```json
    {
      "mcpServers": {
        "pantrix-ui": {
          "command": "java",
          "args": ["-jar", "${PANTRIX_MCP_JAR}"]
        }
      }
    }
    ```

    Claude Code expands `${PANTRIX_MCP_JAR}` with each machine's own variable: the file goes into git, the jar's path does not. An interactive session asks for approval of the repository's server the first time it opens; `claude -p` loads it without asking.
5.  **Trying it:** `claude` is opened in the app's folder. `/skills` lists `pantrix-ui`, and `/mcp` lists `pantrix-ui` as connected. Asked for something like "write a sign-up screen", the assistant loads the skill, calls the server's tools and writes with Pantrix's components (the trial in section 9).
6.  **Keeping it current:** when Pantrix is updated, steps 1–3 are taken again: the library, the jar and the skill come from one version. The skill command writes the folder anew; the change is a commit in the app's repository.
7.  **Other assistants:** clients that read their settings from a `mcpServers` field take the same entry in their own settings file. An assistant that reads skills reads the folder `pantrix-ui` from its own folder of skills. An assistant that reads neither is given `llms.txt` (below).

### Giving the files

:::table
| When | What to give |
| :--- | :--- |
| The assistant is to write a screen or a feature in Pantrix | `llms.txt`. The assistant reads the page or the module's reference it needs. |
| The work is with one component | That component's page: `pages/data-table.md`. The page has the examples' code with its imports, and every parameter of the component with its description. |
| The assistant is to know everything at once | `llms-full.txt`. It is large: for a model with a wide context, or for a document index. |
| A tool is to read Pantrix | `pantrix-ui.json` and its schema (section 7). |
:::

*   **Giving a file:** the file is added to the conversation, or named in the assistant's project rules. In Claude Code a file is named `@ai/generated/llms.txt`, and a `CLAUDE.md` takes a file in with a line `@path`.
*   **In an app's repository:** there is no site yet, so the files come from the server's jar, which holds those of its version: its command `docs` copies them into the app's repository, and they are named from there.

## 5. Where the documents come from {#where-the-documents-come-from}

Every line of the documents comes from a source the build already holds:

:::table
| Source | What goes into the documents |
| :--- | :--- |
| The library's KDoc (English) | The description of every public declaration, parameter, enum entry and member. detekt's rule `UndocumentedParameter` stops the build on a parameter without a description ([the detekt guide](/docs/guides/detekt)). |
| `@PantrixComponent` and `@PantrixExample` | The component's name, its group, its one-sentence description, its keywords and its short examples. |
| The demo's pages (`@DemoPage`, `@DemoExample`) | The page's summary and features, the examples' titles, descriptions and code. |
| The demo's English (`app/shared/src/commonMain/translations/en`) | The English of the pages and the examples, the texts in the examples' code among them. |
| The README | Its first paragraph is the library's summary, its first Kotlin example the smallest app. The build reads the example against the library too (section 6). |
| `ai/skills/pantrix-ui/` | The skill, as it is written by hand. The section "Core rules" of `SKILL.md` is the library's rules: what the skill, `llms.txt`, `get_rules` and the prompts say, the one place the rules are written. |
| The guides' English (`docs/guide/<key>/X_GUIDE.en.md`) | The guides for apps (`guides/<key>.md`). The build reads each against its Turkish first (section 6). |
| The guides' Turkish (`docs/guide/<key>/X_GUIDE.md`) and `CHANGELOG.tr.md` | The documents in Turkish (`tr/guides/<key>.md`, `tr/CHANGELOG.md`): only the guides that have an English too. |
:::

*   **A new component** comes into the documents by itself once it meets CONTRIBUTING's rule for a new component (its annotations, its KDoc, its demo page). There is nothing more to do.
*   **The examples' code** is the demo's compiled code, as the demo shows it, with the helpers of its file it uses. The imports the code needs are written at its top. The few names the code calls of the demo app itself (such as `ownColors()`) are the app's own, and what they are is written under the example.
*   **The components' short examples** (`@PantrixExample`) are not compiled. They take their text from an app's resources (`stringResource(Res.string.save)`), so they read as an app would write them. Since they are not compiled, the guard in section 6 reads them against the library.

## 6. What stops the build {#what-stops-the-build}

Where the documents would tell an assistant something false, or nothing at all, the task stops the build; so does the server's jar, where its notice of what it bundles is not what it bundles. The message says what to fix where:

:::table
| What | Message | Fixing it |
| :--- | :--- | :--- |
| A component no page documents | `ui/…/Gauge.kt:198: no page of the sample app documents PantrixGauge, …` | The component is named in the `api` of its page's `@DemoPage`. |
| A named argument in a `@PantrixExample` that no form of the library has | ``PantrixButton, "Primary action": `tag` is given to PantrixButton, and no form of PantrixButton has a parameter of that name`` | The example's code is written with the component's signature as it is today. |
| A text of the demo without its English | `These texts of the sample app have no English, …` with the list of the missing texts | `./gradlew :app:shared:generateDemoCatalog` is run, and the files under `app/shared/build/reports/demoCatalog/translations/en` are filled and copied to `translations/en` ([the demo catalogue](https://github.com/developersancho/pantrix-ui-cmp/blob/main/CONTRIBUTING.md#the-demo-catalogue)). |
| Two declarations that would get the same id in the documents | `… would share the id … in the documents for coding assistants` | One of the declarations is renamed. |
| A component whose keywords are empty, not written in lower case, or give a word twice | `ui/…/Button.kt:144: the @PantrixComponent of Button gives no keywords. …` | The annotation's `keywords` are given the words someone looking for the component would use that its name does not say: what other design systems and platforms call it, and what it is used for (`toast` and `undo` for a snackbar). Three to eight, in English and in lower case. |
| The skill, or the README's first example, names what the library does not have (`PantrixSlider`), or an entry or a member a type does not have (`ButtonVariant.Primary`) | ``ai/skills/pantrix-ui/SKILL.md:73: `ButtonVariant` has no `Primary` `` | The name is written as the library has it today. |
| A block of Kotlin imports what the library does not have, uses a name of the library it does not import, or gives an argument by a name no form has | ``…/references/overlays.md:55: `PantrixDialog` is the library's, and the code does not import it: `import com.pantrixui.components.overlay.dialog.PantrixDialog` `` | The block is written with its imports and the signature's names as they are today. |
| An artifact that is not published, or a version other than the library's | ``…/references/setup.md:36: `com.pantrixui:components:0.2.0` is not the version the library is published in, 0.1.0-SNAPSHOT`` | The version is the one in `version.properties`: when it is raised, the skill's are raised with it. |
| The skill's form: a name that is not its folder's, a description longer than 1,024 characters, a `SKILL.md` longer than 500 lines; a reference no link of `SKILL.md` leads to, a link that leads to no file of the skill; a reference longer than 100 lines without a `Contents:` line at its top that lists its sections | ``…/references/layout.md:1: a reference of 193 lines lists its sections before the first: `Contents: …` `` | What the message says is written. |
| A `references/components.md` written by hand | `… the generator writes this reference; it is not written by hand` | The file is removed: the generator writes it. |
| A guide outside the folder of its key (`docs/guide/<key>/`); a guide for apps without its English, or whose English has parted from its Turkish: a heading, a table's line, a block of code, a list item or an image the other does not have; a third line that does not link the other language; a link that leads to no file or heading, or from the English to the Turkish of a guide that has its English | `docs/guide/date-time/DATE_TIME_GUIDE.en.md:120: a bullet, where the Turkish has a numbered item, docs/guide/date-time/DATE_TIME_GUIDE.md:118` | The English is written as the Turkish stands, and a part is added to both or taken from both. A guide about the repository's own building, which stays Turkish, is named among `REPOSITORY_GUIDES` in `AiDocsPlugin`. |
| A module the server's jar bundles that the notice `ai/mcp/licenses/THIRD-PARTY.md` does not list, a module it lists that the jar does not bundle, or a licence's text it names that the jar does not hold | ``ai/mcp/licenses/THIRD-PARTY.md: `io.ktor:ktor-sse-jvm` is bundled into the jar, and no list names it: …`` | The library's section of the notice says its licence and lists the module; a text that no bundled jar holds is put beside the notice, under the path it takes after `META-INF/licenses/`. |
:::

*   **What the check leaves out:** in the short examples and in the skill's blocks, the arguments given by name to the library's functions, constructors, a type's members (`Rules.minLength`) and extensions (`form.field`) are checked. The members a class takes from a supertype (`ButtonStyle.override`) and Compose's functions are not: the check does not know their parameters from the sources.
*   **The skill's blocks are not compiled:** their names, imports, arguments and coordinates are checked at every build; their types are not. They were compiled once, on the day they were written (section 9).
*   **Keywords not given at all:** `keywords` has no default in the annotation. A component that does not give it does not compile; generating the documents stops on one that gives an empty list.

## 7. The format {#the-format}

The schema (`ai/schema/pantrix-ui.schema.json`) describes every field of `pantrix-ui.json`. What a tool needs to know:

*   **Id:** a declaration is named by its package and its name: `com.pantrixui.components.elements.button.ButtonVariant`. An extension's type stands before its name (`…button.ButtonVariant.colors`). Functions that would share an id have the types of their parameters in brackets in theirs: `…PantrixButton(String, () -> Unit, …)`. A type inside a type follows its type's id (`…ChartPoint.Kind`).
*   **`kind`:** the type of every parameter stands as written, and beside it one word says what a caller is asked for. The first group are data a text can carry, the second code only a program can give:

    | `kind` | What | Example |
    | :--- | :--- | :--- |
    | `string`, `boolean`, `integer`, `number` | A value written out | `"Save"`, `true`, `3`, `0.5f` |
    | `color`, `dimension` | A colour, a size | `Color(0xFF6750A4)`, `16.dp` |
    | `enum` | An entry of one of the library's enums; the enum is in `ref` | `ButtonVariant.Text` |
    | `type` | A value of another of the library's types; the type is in `ref` | `NavigationItem(…)` |
    | `list`, `map` | A collection; `ref` is the type it holds, where that is the library's | `listOf(…)` |
    | `modifier` | A `Modifier` | `Modifier.padding(8.dp)` |
    | `composable` | A slot the caller draws into | `{ PantrixText("Save") }` |
    | `function` | Any other lambda, a callback | `{ save() }` |
    | `generic` | A type parameter of the declaration | `T` |
    | `external` | Any other type of Compose or Kotlin | `TextStyle`, `ImageVector` |
*   **`ref`:** the id of the parameter's type where that is one of the library's types, or of the type a collection holds where that is.
*   **A parameter with an empty description:** the declaration's own text describes it by its name (`[name]`, as Kotlin's coding conventions suggest). The description is in the declaration's `description`.
*   **`tags`:** the names a component's automation tags start with, as its documentation writes them: `Sheet` and `SheetClose`, for `Sheet_<tagKey>` and `SheetClose_<tagKey>`. The first is the component's own ([the testing guide](/docs/guides/testing)).
*   **`platform`:** `common` for a declaration on every platform, else the name of its source set: `android`, `ios`, `jvm`, `js`, `wasmJs`, `web`.
*   **`keywords`:** the words someone looking for a component would use that its name does not say (`toast` and `undo` for a snackbar). A search reads them with the component's name and description; they are written on the page's line of the component too.

## 8. Checking them {#checking-them}

```bash
./gradlew :ai:mcp:test
```

*   The schema is what the meta-schema of JSON Schema 2020-12 says, and the document is what the schema says; the schema takes a field it does not know, and a required field that is missing, for an error.
*   Every id named somewhere in the document (a page's API, a component's forms and parts, a parameter's `ref`) is in the document.
*   Every page and every module has its Markdown in the jar.
*   `llms.txt` is in the format of llmstxt.org. Every page is named once under its own group, every link points to a file in the jar, and the index stays under 50,000 characters. `llms-full.txt` holds the Markdown of every page and every declaration with no page.
*   **The server:** every tool is tried against the jar's documents (`toast` finds `Snackbar`, `Buton` offers `Button`). The jar is run as an assistant runs it, as a process of its own, and spoken to in JSON-RPC: the lists, a tool, a resource and a prompt. That its standard output holds nothing but the protocol, and that it exits with nought when its input closes, is measured. `get_rules` gives the skill itself and `get_documentation_page` its references, by their keys and by the skill's links; the search finds the references too; `pantrix://skill` is the skill whole. The tool filter, and the refusal of a tool it does not have, are tried too. What the commands `docs` and `skill` write is tried as well; the skill keeps to the Agent Skills format (its name its folder's, a description under 1,024 characters, `SKILL.md` under 500 lines).
*   **The skill:** the command `skill` writes the references too, and every link of the skill leads to one of its files; `references/components.md` names every component. `llms.txt` tells the rules as `SKILL.md` writes them, and links the skill.
*   **The guides:** the jar has the 23 guides for apps in English, each under its key and without the line to its Turkish; the search finds them, and `get_documentation_page` gives them by their keys, their paths and their titles; their links lead to the documents or to the repository's files.
*   **The jar's licences:** `META-INF/LICENSE` is Pantrix's, and no bundled jar's licence stands at the jar's root or in its `META-INF`; the notice lists every module the jar bundles with its version, and every text it names is in the jar.

The generator's own tests run with `./gradlew :build-logic:test`: the types' kinds, the checks of the short examples, of the skill, of the guides' two languages and of the jar's notice, the JSON writer, the reader and its guards, and the Markdown, `llms.txt`, the guides' documents and the index of the components exactly as expected.

### Trying them in a real assistant

That the skill and the server work in an assistant is seen by giving one task in two folders. Both folders hold the skeleton of a small app that depends on Pantrix (`settings.gradle.kts`, `composeApp/build.gradle.kts` with the dependency, an `App.kt` that opens `PantrixTheme`). One is given the skill and the server as the recipe says (section 4), the other neither. Claude Code's headless mode runs the task once and writes every tool it calls:

```bash
claude -p "$(cat task.txt)" --mcp-config .mcp.json --strict-mcp-config \
  --setting-sources project,local --permission-mode acceptEdits --permission-prompts none \
  --allowedTools "mcp__pantrix-ui Skill" --disallowedTools "Bash WebFetch WebSearch" \
  --max-budget-usd 3 --no-session-persistence --output-format stream-json --verbose > session.jsonl
```

*   **This server alone:** `--mcp-config` with `--strict-mcp-config` leaves the machine's other MCP servers out. The folder compared with it is given no `--mcp-config`: it has no server at all.
*   **This project alone:** `--setting-sources project,local` leaves the user's own settings, plugins and hooks out.
*   **Permissions:** files are written inside the folder alone; Bash and the web are off; the server's tools and the skill are on. Whatever would ask for approval is refused by itself (`--permission-prompts none`).
*   **A ceiling:** `--max-budget-usd` caps what a session spends.
*   **Reading it:** in `session.jsonl` a call of `Skill` shows the skill was loaded, and calls of `mcp__pantrix-ui__…` that the server was used; the last line gives the turns, the time and the cost. The screen written is compiled against the library in this repository: with a stand-in for the app's `Res` class, among the JVM tests of a module with no Material.
*   **The task** (`task.txt` of the trial in section 9): "This is a Compose Multiplatform app; its shared code is in composeApp/src/commonMain. Write a sign-up screen in composeApp/src/commonMain/kotlin/com/example/app/SignUpScreen.kt: a composable `SignUpScreen(onSignedUp: () -> Unit)` with fields for the name, the e-mail address and the password, a checkbox to accept the terms, and a button that signs up. The fields are checked, and an error shows under a field that is wrong. In a wide window a short panel about the app stands beside the form; in a narrow one only the form shows. Put every text the screen shows in composeApp/src/commonMain/composeResources/values/strings.xml. Use the UI library this project depends on, and write only these two files."

## 9. What was verified and what was not {#verification}

### Verified

*   **Tests:** the 28 tests of `:ai:mcp` and the 163 of `build-logic` pass.
*   **The server's times:** the process starts and answers `initialize` in some 0.35 seconds; a first call to a tool takes 0.03 to 0.07 seconds, and a search of the documentation that reads every page for the first time some half a second.
*   **Guards:** each was tried by breaking real sources (an example's argument, a page's `api`, an English text); each stopped the build with its message in section 6.
*   **The guides' check:** the guard was broken in twelve ways (a table's lines, a list item's kind, an image, the Kotlin read with its comments, the English's link to a Turkish, a heading a link leads to, the line to the other language, a repository guide given an English, links in code, the line to the Turkish kept, links in blocks, the Turkish of a guide not led to its English); each turned a test red. Every guide's English was read against its Turkish by the guard before the guard was written into the build.
*   **The publication (2026-10-25):** `com.pantrixui:mcp` was published to a scratch Maven repository, not `~/.m2`. Its module metadata has four variants (`apiElements`, `runtimeElements`, `sourcesElements`, `allJarElements`), and its POM the server's four dependencies. In a separate project, `com.pantrixui:mcp` gave the library's jar and its 32 dependencies, and `com.pantrixui:mcp:0.1.0-SNAPSHOT:all` the jar that runs by itself alone.
*   **The licences' check:** the notice was broken in three ways (a module left out, a module added, a text taken away); each stopped the build with its message in section 6. A jar made without leaving the bundled jars' licences out turned the jar's test red.
*   **The skill's check:** the skill was broken in sixteen ways (an enum entry, a member, an argument, an import, a version, an artifact, a link, its name, its contents…); each stopped the build with its message in section 6, its file and its line.
*   **A real assistant (2026-10-25):** the task above was given in two folders, in Claude Code 2.1.294 with Opus 5.5.
    *   **With the skill and the server:** the assistant loaded the skill by itself, read the references `forms` and `layout`, and called the server's `get_component`, `get_documentation_page` and, six times, `get_api`. It wrote the screen with Pantrix alone: fields bound to a form with its rules, the texts from resources, the values from the theme, a `tagKey` on the button, the panel by the window (`isNarrow`, `isShort`, `byWidth`). The code compiled at the first try, with no warning. 22 turns, 81 seconds, $0.85.
    *   **With neither:** the assistant could not see Pantrix's API (its request to read the local Maven repository was refused). It wrote the screen in plain Compose Foundation: `BasicText`, `BasicTextField`, colours written by hand (`Color(0xFF…)`), no tag on any control. The code compiled, since it uses nothing of Pantrix. 11 turns, 80 seconds, $0.53.
*   **The skill's code:** its 17 blocks of Kotlin and the README's example were put among a module's JVM tests and compiled against the library, with no warning. The three entry points of a platform of their own (Android, iOS, the web) and the Gradle blocks cannot be compiled that way; they are the samples' own code.
*   **The demo catalogue:** after the code it shares with the generator was moved out, the old and the new code gave it byte for byte the same: 226 files and the translation reports.
*   **A site's address:** tried with a trailing `/`, without one, and with none given.
*   **The folder and the cache:** `clean` removes the folder; the task comes from the cache when the sources did not change.

### Not verified

*   The trial in a real assistant is one task and one model (Opus 5.5); other tasks, models and assistants were not tried. The screen written was compiled, not drawn.
*   How particular tools (Cursor, Copilot…) read `llms.txt` was not tried.
*   There is no site yet; absolute links were only seen in the files.
*   The guides' English was written by the assistant that wrote these documents; the build reads its shape and its links against the Turkish, and no second reader has read it.
*   The server was run in Claude Code (above); other clients (Cursor, Claude Desktop…) were not tried.
*   The server was not published to the local Maven repository or to GitHub Packages, nor taken from there by a client. The licences' texts were put into the jar as they ask; no lawyer read them.
