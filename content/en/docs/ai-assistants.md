---
title: For AI assistants
description: Pantrix writes its own documents for coding assistants from its sources at every build: an MCP server, a skill, llms.txt and the whole library as JSON. An assistant asks for what it needs and gets the library as it is today, not a guess from Material.
section: Getting started
---

## What an assistant is given {#documents}

:::table The documents
- `com.pantrixui:mcp`: The MCP server: one jar that runs by itself and answers an assistant's questions over stdio
- `pantrix-ui` skill: The rules code written with Pantrix keeps, Material's names and Pantrix's, and how the parts fit
- `llms.txt`, `llms-full.txt`: The index of the documents, and all of them in one file
- `pantrix-ui.json`: The library as data: every component, its parameters, its examples and its tags
:::

## Set it up {#setup}

:::steps
## Publish the library and the server {#publish toc="The server"}

The server's jar is published with the library, as `com.pantrixui:mcp` with the classifier `all`. Each machine keeps where it stands in a variable of its own:

```bash ~/.zshrc
export PANTRIX_MCP_JAR=$HOME/.m2/repository/com/pantrixui/mcp/0.1.0/mcp-0.1.0-all.jar
```

## Give the server to the assistant {#server toc="The assistant"}

:::tabs
### Claude Code

```bash
claude mcp add pantrix-ui -- java -jar "$PANTRIX_MCP_JAR"
```

### A project's .mcp.json

```json .mcp.json
{
  "mcpServers": {
    "pantrix-ui": {
      "command": "java",
      "args": ["-jar", "${PANTRIX_MCP_JAR}"]
    }
  }
}
```
:::

## Install the skill {#skill toc="The skill" optional}

The skill says what to do; the server says what the library is today. The two together are best.

```bash
java -jar "$PANTRIX_MCP_JAR" skill .claude/skills
```
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
**The server needs a JDK 17 or newer.** The client starts the jar itself and speaks to it over stdio: nothing but the protocol is written to its standard output.
:::

## Where next {#next}

:::cards
- [Teaching Pantrix to an assistant](/docs/guides/ai): How the documents are made, and what stops the build.
- [Installation](/docs/installation): The library's own dependency lines.
- [detekt rules in your app](/docs/guides/detekt): The rules a machine keeps after the assistant.
- [Changelog](/changelog): What `get_changelog` gives.
:::
