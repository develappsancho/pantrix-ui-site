---
name: commit
description: Proposes the commit for finished work in Pantrix UI's site, as one subject line in the repository's [TYPE][Summary] style (DEV, FIX, TEST, DOCS, CI, RELEASE) plus the files that belong in it, without staging or committing, since the owner commits. Use when asked for a commit message ("commit mesajı öner", "commit mesajı ne olsun", "tek commit mesajı") or when finished work is handed over.
allowed-tools: Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git show:*)
---

# Commit proposal

The owner commits in this repository. This skill prepares what they need for it: the checks the change needs, the
message, and the files that belong in it. It never changes the index or the history, and in the working tree it writes
only what the checks in 4 rewrite and the changelog lines 4 asks for.

## Never

- Run `git add`, `git commit`, `git mv`, `git rm`, `git restore`, `git reset`, `git stash`, `git checkout` or any other
  git command that changes the index, the working tree or the history. Only read-only git: `status`, `diff`, `log`,
  `show`. A file is moved with plain `mv`.
- Add a body or trailers. The history has neither: no `Co-Authored-By`, no `Signed-off-by`.

## 1. Read the state

```bash
git log -10 --format='%h %s'
git status --short --untracked-files=all
git diff --stat
git diff --cached --stat
```

- The owner commits mid-work, so what `git log` already has is done. Propose only for what is left, and say so when
  nothing is.
- The index can hold the owner's own staged work that has nothing to do with this change. A plain `git commit` would
  take it along: say so, and give the commit by path (see 5).
- Read the diff of every file you propose (`git diff -- <path>`, or the file itself when it is untracked). Propose only
  what you have seen.

## 2. Choose the files

In: the files of the finished work, and only those. They include what its checks rewrote (see 4): the pages'
screenshots under `app/shared/screenshots/jvm/` recorded again for it, the deploy guide's pictures under
`docs/deploy/images/` drawn again for it, the guide pages under `content/*/docs/guides/` written again from the
library, and `CHANGELOG.md` and `CHANGELOG.tr.md` when the change has a line there (see 4).

Out, each one named in the answer with the reason:
- scratch files, temporary probe tests and debug output;
- build outputs, and anything under `build/`;
- a picture or a screenshot drawn again with no change a person can see, which only its pixels' rounding moved: keep
  the committed one (`git show HEAD:<path> > <path>`);
- changes that are not part of the work handed over, such as the owner's own edits in the working tree.

## 3. Write the message

One line, `[TYPE][Summary]`: two bracketed parts with nothing between them, and no full stop.

**TYPE**, by what the commit is for:

| Type | For | Tests and docs of the same change |
| :--- | :--- | :--- |
| `DEV` | New or changed behaviour: pages, features, refactors, build logic, dependencies, tooling, putting the site up | ride along |
| `FIX` | Behaviour that was wrong and is now right: a bug, a crash, a wrong colour | ride along |
| `TEST` | Only tests: test sources and helpers, screenshots, and the setup they run on | — |
| `DOCS` | Only documentation: `docs/`, the root files, guides, and comment or KDoc changes on their own | — |
| `CI` | Only what the CI service runs: its workflows, and scripts only CI calls | — |
| `RELEASE` | Preparing or marking a deploy: the changelog's date, the library's version | — |

- A commit that mixes takes the type of its main purpose: a fix with its regression test is `FIX`, a page with its
  tests and docs is `DEV`.
- Every hand-over is one commit with one message, as the owner commits it: never suggest a split. When it holds several
  purposes, it takes the type of the main one.

**Summary**
- English, imperative, starting with a capitalised verb: `Add`, `Update`, `Remove`, `Use`, `Take`, `Show`, `Draw`.
- Do not repeat the type: `[FIX][Fix …]` says it twice. Say what is right now instead.
- Say what changed, not how the work went: `[FIX][Draw the sidebar's scroll bar in the dark theme]`, not
  `[FIX][Address review comments]`.
- Name pages, parts and APIs by their names: `Add the theme builder`, `Take the site's address from site.url`.
- Several changes go in the one line, each a summary of its own that starts with a capitalised verb, listed with
  commas and `and` before the last. It stays one line, since there is no body to overflow into.
- Aim for 72 characters or fewer for a single change. A hand-over of several changes runs as long as their list needs:
  the length is never a reason to split it.

Examples from the history:

```
[DEV][Add the Spring Boot server with the MCP endpoint]
[FIX][Open llms.txt from the page for coding assistants' "Read llms.txt"]
[TEST][Add the pages' Roborazzi screenshots at a desktop's, a tablet's and a phone's size, light and dark]
[DOCS][Add a picture of each architecture diagram and the script that draws them, and Fold the diagrams' Mermaid sources under them]
[DEV][Take the site's address from site.url and SITE_URL, Show the address the web app is opened at and Write SITE_URL into the site's files as the stack starts]
```

## 4. Before handing it over

The tests, detekt and `lintKotlin`, the screenshots and the local deploy are yours to run. CONTRIBUTING.md holds the
rules of these checks; where this skill and it part, CONTRIBUTING.md is right.

- When Kotlin changed, run `./gradlew detekt lintKotlin` (`formatKotlin` fixes the layout). It forbids trailing commas.
  Test source sets shared by several targets, such as `commonTest`, get no detekt task: hold lines there to 120
  columns by hand.
- Run the tests the change can affect, on each target it can affect: `./gradlew jvmTest iosSimulatorArm64Test
  wasmJsBrowserTest`, or a module's own (`:feature:ai:jvmTest`). A change to common code affects all three.
- A screen's previews: a new screen has four in a `…ScreenPreviews.kt` beside it; `SitePreviewsTest`
  (`:app:shared:jvmTest`) fails while one is missing, and draws them all.
- The server: `./gradlew :server:test` when `server/` changed.
- The page's loader and the web app in Chrome: `./gradlew :app:webApp:loaderTest` when `site.js` changed, and
  `browserTest` when `site.js`, the web app or a path it leads along changed.
- The desktop app's package: `./gradlew :app:desktopApp:smokeTest` when its packaging or runtime changed.
- The main paths on a device: `./gradlew :app:androidApp:maestroTest` and `./gradlew :app:shared:maestroIosTest` when
  the shell, the header, the menu, the search or a page they lead to changed.
- A page's look: `./gradlew :app:shared:verifyRoborazziJvm` (about six minutes). When the change is meant, record it
  again with `recordRoborazziJvm`, look at the dark images too, and propose the rewritten images with the change.
- Putting it up: `deploy/deploy.sh local` when `deploy/`, the server or the web build changed. It runs the stack at
  `http://localhost:4004` and `deploy/check.sh` on it. The owner's other containers run in the same Docker: touch only
  the stack `pantrix-ui-site`.
- A diagram's source in `docs/deploy/ARCHITECTURE_GUIDE.md` or its English: `node docs/deploy/draw-diagrams.mjs`, and
  look at the pictures it drew.
- A guide in `docs/` or a file at the root changed: its other language changes with it, in the same shape.
- A change a visitor can see (a new or changed page, a change in behaviour or look, a fix they would notice) has a line
  under `Unreleased` in `CHANGELOG.md`, in the section of its kind, and the same line in Turkish in `CHANGELOG.tr.md`.
  CONTRIBUTING.md says what counts and how a line is written. When the lines are missing, write them and propose both
  files with the change. Build logic, tests and documentation alone have no line.
- The site's address is never written by hand: `site.url`, `{{site}}` in a page, `SiteAddress` in the app.
- Say what was verified (which tests, on which targets) and what was not.

## 5. Answer

In the language the owner writes in; the message itself stays English.

1. The one message, alone in a fenced block, ready to copy.
2. The files: modified, new (untracked, so they need `git add`) and deleted.
3. What was left out, and why.
4. Warnings: unrelated staged entries, what the checks in 4 rewrote and why, and anything not run or not verified.

Give the commit as two commands, one `git add` for the new files and one `git commit`, whatever the hand-over holds. The
commit goes by path: it commits only the paths named, with their deletions, and leaves the rest of the index as it is:

```bash
git add <new files>
git commit -m "[TYPE][…]" -- <every file in the commit>
```
