# ReportPlugin

A Spigot plugin for handling player reports with commands, a moderation GUI, update history, and optional Discord webhook notifications.

## Features

- Create reports with `/report <player> [reason]`
- Browse unresolved reports in a GUI
- View report details, update status, and store moderation history
- Use multi-state report statuses: `open`, `in progress`, `resolved`, `rejected`, and `punished`
- Filter and sort the moderation GUI by active/closed state, newest, oldest, reported player, reporter, or status
- Teleport to the reported location or to the reporter
- Kick players or apply permanent and temporary bans directly from the GUI
- Prevent duplicate report spam from the same reporter against the same target within a configurable time window
- Attach staff notes and an evidence URL such as a screenshot or video link to each report
- Show a configurable help page with `/report-help`
- Send create, update, and resolve events to Discord webhooks
- Use MySQL, SQLite, PostgreSQL, H2, MongoDB, or file-based storage

## Supported Storage Backends

- `mysql`
- `sqlite`
- `postgresql`
- `h2`
- `mongodb`
- `jsonfilesystem`
- `yamlfilesystem`
- `textfilesystem`

Default: `jsonfilesystem`

## Build

```bash
mvn clean package
```

The built jar will be created in `target/`.

## Installation

1. Build the plugin with Maven.
2. Put the generated jar into your server `plugins/` folder.
3. Start the server once to generate `config.yml` and `messages.yml`.
4. Adjust the configuration for your storage backend and Discord setup.
5. Reload with `/reportplugin reload` or restart the server.

## Commands

- `/report <player> [reason]` - Create a report.
- `/reports-list` - List unresolved reports.
- `/report-gui [reportId]` - Open the report management GUI.
- `/report-data` - Show report data for online players.
- `/report-help` - Show the help page.
- `/reporttp <player|reportId> [reportId]` - Teleport to a report location.
- `/report-delete <reportId>` - Delete a report.
- `/report-updatehistory <reportId>` - Show report update history.
- `/report-clearupdatehistory <reportId>` - Clear a report's update history.
- `/reportplugin reload` - Reload the plugin configuration.

From the GUI ban flow, you can enter durations like:

- `30m`
- `12h`
- `7d`
- `2w`
- `1d12h`
- `perm`

## Permissions

- `reportplugin.report` - Use `/report`
- `reportplugin.list` - Use `/reports-list`
- `reportplugin.gui` - Use `/report-gui`
- `reportplugin.reportdata` - Use `/report-data`
- `reportplugin.help` - Use `/report-help`
- `reportplugin.reporttp` - Use `/reporttp`
- `reportplugin.reportdelete` - Use `/report-delete`
- `reportplugin.updatehistory` - Use `/report-updatehistory`
- `reportplugin.clearupdatehistory` - Use `/report-clearupdatehistory`
- `reportplugin.reload` - Use `/reportplugin reload`
- `reportplugin.report.notify` - Receive staff notifications for report activity

## Configuration

Important top-level options in `config.yml`:

- `database` selects the active storage backend.
- `server-name` and `server-address` are stored with each report.
- `report-settings.max-reports-per-player` limits reports against one player.
- `report-settings.max-reports-per-reporter` limits how many reports one player can create.
- `report-settings.duplicate-window-seconds` blocks repeat reports from the same reporter against the same target for a short cooldown window.
- `notify.*` controls in-game staff notifications.
- `discord.notify.*` controls which Discord webhook events are sent.
- `useDiscordWebhook` enables or disables Discord integration entirely.

## Moderation Flow

- Selecting a report in `/report-gui` unlocks actions like teleport, delete, update, kick, and ban.
- The update flow can change the reason, staff notes, evidence URL, resolution comment, and report status.
- The GUI lets staff cycle between active-only, closed-only, and all reports, and also cycle through multiple sort modes.
- Kick and ban actions automatically mark the report as `punished`.
- The ban flow now asks for both a reason and a duration.
- Use `perm`, `perma`, or `permanent` for a permanent ban.
- Use values like `30m`, `12h`, `7d`, `2w`, or combined values like `1d12h` for temporary bans.

## Messages

User-facing text is configurable in `messages.yml`.

- `messages.*` contains common command and GUI feedback.
- `gui.*` contains GUI titles and lore text.
- `help.lines` defines the output for `/report-help`.
- Ban prompts and timed-ban feedback are also configured in `messages.yml`.

## API

`ReportAPI` can be used by other plugins to create, update, resolve, and inspect reports.

## Changelog

Project history is tracked in [CHANGELOG.md](CHANGELOG.md).
