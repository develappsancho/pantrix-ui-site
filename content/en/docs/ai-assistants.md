---
title: For AI assistants
description: Pantrix writes its own documents for coding assistants from its sources at every build: an MCP server, a skill, llms.txt and the whole library as JSON. An assistant asks for what it needs and gets the library as it is today, not a guess from Material.
section: Getting started
---

## What an assistant is given {#documents}

:::table The documents
- `{{site}}/mcp`: The MCP server, which this site serves over HTTP: an assistant is given its address alone
- `mcp-0.2.0-all.jar`: The same server as one jar that runs by itself, attached to each release, for a machine of your own
- `pantrix-ui` skill: The rules code written with Pantrix keeps, Material's names and Pantrix's, and how the parts fit
- `llms.txt`, `llms-full.txt`: The index of the documents, and all of them in one file, at this site's root
- `pantrix-ui.json`: The library as data: every component, its parameters, its examples and its tags
:::

## Set it up {#setup}

:::steps
## Give the assistant the server {#server toc="The server"}

The server answers at `{{site}}/mcp`, so the assistant needs nothing but its address.

:::tabs
### Claude Code

```bash
claude mcp add -t http pantrix-ui {{site}}/mcp
```

### A project's .mcp.json

```json .mcp.json
{
  "mcpServers": {
    "pantrix-ui": {
      "type": "http",
      "url": "{{site}}/mcp"
    }
  }
}
```
:::

## Install the skill {#skill toc="The skill" optional}

The skill says what to do; the server says what the library is today. The two together are best. In Claude Code, Pantrix UI's plugin brings the skill, and the server at this site's address too, from the repository the library is distributed from: with it, the step above is not needed.

```bash
claude plugin marketplace add develappsancho/pantrix-ui-cmp-package
claude plugin install pantrix-ui@pantrix-ui
```

## Or run the server on your machine {#jar toc="The jar" optional}

The jar is attached to [each release](https://github.com/develappsancho/pantrix-ui-cmp-package/releases), and after `./gradlew publishToMavenLocal` in the library's repository it stands in the local Maven repository too: `~/.m2/repository/com/pantrixui/mcp/<version>/`. It runs on JDK 17 or newer. It writes the skill into a project too, which the team then gets through git, and which any assistant that reads skills takes:

```bash
curl -fL --create-dirs -o ~/.pantrix/mcp-0.2.0-all.jar https://github.com/develappsancho/pantrix-ui-cmp-package/releases/download/v0.2.0/mcp-0.2.0-all.jar
claude mcp add pantrix-ui -- java -jar ~/.pantrix/mcp-0.2.0-all.jar
java -jar ~/.pantrix/mcp-0.2.0-all.jar skill .claude/skills
```
:::

## Where the files are {#files}

:::table Where to find them
| What | Where |
| :--- | :--- |
| The MCP server | `{{site}}/mcp`, `mcp-0.2.0-all.jar` on [the release](https://github.com/develappsancho/pantrix-ui-cmp-package/releases/tag/v0.2.0), or the local Maven repository after a local publish |
| The skill | Claude Code's plugin `pantrix-ui@pantrix-ui`, `pantrix-ui-skill-0.2.0.zip` on the release, or the jar's `skill` command |
| `llms.txt` | [{{site}}/llms.txt]({{site}}/llms.txt): its links lead to this site's pages, and what has no page is under `/llms/` |
| Every document, on your machine | `java -jar mcp-0.2.0-all.jar docs <folder>` |
:::

## What it can ask {#tools}

:::table The server's tools
- `search_components`, `get_component`: A component by what it is for, and its page with examples that compile
- `get_component_metadata`: Its import, its artifact, its tags and every parameter
- `search_api`, `get_api`: Any declaration, by its name or by what it does
- `get_example`, `list_examples`: One example's code with its imports
- `search_documentation`, `get_documentation_page`: The pages, the guides and the skill's references
- `get_changelog`: What changed, version by version
- `get_rules`: The skill itself, for an assistant that has not installed it
:::

:::callout
**The jar needs a JDK 17 or newer; the address needs nothing.** A client starts the jar itself and speaks to it over stdio, and nothing but the protocol is written to its standard output. Over HTTP each request is answered on its own, with no session.
:::

## Where next {#next}

:::cards
- [Teaching Pantrix to an assistant](/docs/guides/ai): How the documents are made, and what stops the build.
- [Installation](/docs/installation): The library's own dependency lines.
- [detekt rules in your app](/docs/guides/detekt): The rules a machine keeps after the assistant.
- [Changelog](/changelog): What `get_changelog` gives.
:::
