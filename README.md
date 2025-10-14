# ReportPlugin

A simple Spigot plugin for reporting a Player, with Discord webhook integration.

[![GitHub issues](https://img.shields.io/github/issues/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/issues)
[![GitHub forks](https://img.shields.io/github/forks/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/network/members)
[![GitHub stars](https://img.shields.io/github/stars/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/stargazers)

## Features

- Players can report others with a specified reason.
- All reports are securely stored in a database.
- Staff can view and update reports via commands or an intuitive GUI.
- Reports are automatically sent to a Discord channel using a webhook.
- Fully configurable through the `config.yml` file.
- Permission-based access control for all features.
- View detailed information about each player report.
- Supports MySQL, SQLite, PostgreSQL, H2, MongoDB, and file-based storage.
- Instantly teleport to the reporter's location.
- Delete reports directly from the interface.
- View the complete update history of each report.

## Supported Databases

- MySQL
- SQLite
- PostgreSQL
- H2
- MongoDB
- File-based storage

## Installation

1. Build the plugin using Maven:
    ```bash
    mvn clean package
    ```
2. Place the generated `ReportPlugin-[VERSION]-SNAPSHOT.jar` in your server's `plugins` folder.
3. Start your server to generate the default config.
4. Edit `config.yml` to set your Discord webhook URL and other settings.
5. Reload or restart your server.

## Commands

- `/report <player> [reason]`  
  Report a player for a specific reason.

- `/reports-list`  
  List all reports.

- `/report-gui`  
  Open the report management GUI.

- `/report-data`  
  View detailed information about Player data and if they are reported.

- `/reporttp <player> <reportID>`  
  Teleport to the location of a specific report.

- `/report-delete <reportID>`  
  Delete a specific report.

- `/report-updatehistory <reportID>`  
  View the update history of a specific report.

- `/report-clearupdatehistory <reportID>`  
  Clear the update history of a specific report.

## Permissions

- `reportplugin.report` — Use the `/report` command.
- `reportplugin.list` — Use the `/reports-list` command.
- `reportplugin.gui` — Use the `/reportgui` command.
- `reportplugin.reportdata` — Use the `/report-data` command.
- `reportplugin.report.notify` — Receive notifications for new reports.
- `reportplugin.reporttp` — Use the `/reporttp` command.
- `reportplugin.reportdelete` - Use the `report-delete <reportID>` command.
- `reportplugin.updatehistory` - Use the `report-updatehistory <reportID>` command.
- `reportplugin.clearupdatehistory` - Use the `report-clearupdatehistory <reportID>` command.

## Discord Webhook Features

- Sends a notification to a specified Discord channel whenever a player is reported.
- Includes details such as the reported player's name, the reporter's name, the reason for the report, and the timestamp.
- Customizable message format through the configuration file.
- Supports rich embeds for better presentation of report details.
- Handles webhook failures gracefully with retry mechanisms.
- Supports multiple webhooks for different types of reports.
- Option to include player UUIDs and IP addresses in the Discord message for better identification.

## Configuration

Edit the `discord` section in `config.yml` to set your webhook URL.

## Future Plans

- Add more customization options for Discord messages.
- Implement additional storage options.
- Enhance the GUI with more features.
- Add localization support for multiple languages.
- BungeeCord support.

## Changelogs

See the [CHANGELOG.md](CHANGELOG.md) file for detailed changes in each version.

## API

An API is available for developers to interact with the plugin's functionalities. Documentation will be provided in
future updates.

## License

Created by FrameDev. Do not modify without consent.