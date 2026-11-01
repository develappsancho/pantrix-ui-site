# Security Policy

Türkçesi: [SECURITY.tr.md](./SECURITY.tr.md)

## Supported versions

The site as it runs at `pantrixui.com`, and the main branch it is put up from. An earlier deploy gets no fix of its
own: the next deploy carries it.

## Reporting a vulnerability

Do not report a vulnerability in an issue, a pull request or a discussion: everyone can read those.

Report it through GitHub's private vulnerability reporting: the repository's **Security** tab, then **Report a
vulnerability**. Where that is not open to you, report it directly to the maintainer,
[@developersancho](https://github.com/developersancho).

A report says:

- the address or the commit, and the browser, app or platform it shows on;
- what an attacker can do, and what it takes;
- the steps that show it, with a proof of concept if there is one.

## What happens next

- **Within 5 working days:** an answer that the report arrived.
- **Within 30 days:** an assessment and a plan. They say whether it is a vulnerability, how severe it is, and when a fix
  can come.
- **The fix:** it comes with a deploy. The vulnerability is then described in the changelog's `Security` section and in
  a GitHub security advisory.
- **Credit:** the reporter is named for the find, if they wish.

Please keep the vulnerability to yourself until the fix is out.

## What is in scope

- **The server at `/mcp`.** A message it did not expect, large, malformed, or sent from a page of another site, is
  refused: the server must not crash or hang on it, and answers from the library's documents alone.
- **nginx's settings.** Its redirects and headers, and what it serves: never a file outside the site's own folder.
- **The pages,** in the app and in their static HTML: a page must run no script the site did not write.
- **Putting it up.** The images and the scripts that make them: a secret, such as a token, never goes into an image or a
  log.
- **The apps** for Android, iOS and the desktop.

## What is not in scope

- **Pantrix UI itself.** Report a vulnerability in the library to the same maintainer, saying it is the library's.
- **A vulnerability in a dependency.** Report it to that project. When the site exposes it, it is in scope here.
- **The host's own setup:** the server the site runs on, and Coolify and its proxy. Report a vulnerability in Coolify
  or Traefik to those projects.
