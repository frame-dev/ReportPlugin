 # ReportPlugin

A simple Spigot plugin for reporting a Player, with Discord webhook integration.

[![GitHub issues](https://img.shields.io/github/issues/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/issues)
[![GitHub forks](https://img.shields.io/github/forks/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/network/members)
[![GitHub stars](https://img.shields.io/github/stars/frame-dev/ReportPlugin.svg)](https://github.com/frame-dev/ReportPlugin/stargazers)

## Features

- Players can report other players with a reason.
- Reports are stored in a database.
- Staff can view and update reports via commands or a GUI.
- Reports are sent to a Discord channel via webhook.
- Configurable settings via `config.yml`.
- Permission-based access control.
- Detailed player report data viewing.
- Supports MySQL, SQLite, MongoDB and File Storage.

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

## Permissions

- `reportplugin.report` — Use the `/report` command.
- `reportplugin.list` — Use the `/reports-list` command.
- `reportplugin.gui` — Use the `/reportgui` command.
- `reportplugin.reportdata` — Use the `/report-data` command.
- `reportplugin.report.notify` — Receive notifications for new reports.
- `reportplugin.reporttp` — Use the `/reporttp` command.

## Configuration

Edit the `discord` section in `config.yml` to set your webhook URL.

### Example `config.yml`

```yaml
useDiscordWebhook: false
database: filesystem
discord:
  create:
    webhook-url: YOUR_WEBHOOK_URL_HERE
    username: ReportBot
    avatar-url: https://example.com/avatar.png
    content: New report received!
    embed:
      title: New Report
      description: '**Reported Player:** %ReportedPlayer%\n**Reporter:** %Reporter%\n**Reason:**
        %Reason%\n**Server:** %ServerName%\n**Location:** %Location%\n**World:** %WorldName%'
      url: https://example.com
      footer:
        text: 'Report ID: %ReporterID%'
        icon-url: https://example.com/footer-icon.png
      image:
        url: https://example.com/image.png
      thumbnail:
        url: https://example.com/thumbnail.png
  update:
    webhook-url: YOUR_WEBHOOK_URL_HERE
    username: ReportBot
    avatar-url: https://example.com/avatar.png
    content: Report updated!
    embed:
      title: Report Updated
      description: '**Reported Player:** %ReportedPlayer%\n**Reporter:** %Reporter%\n**Reason:**
        %Reason%\n**Status:** %Status%\n**Additional Info:** %AdditionalInfo%\n**Resolution
        Comment:** %ResolutionComment%\n**Server:** %ServerName%\n**Location:** %Location%\n**World:**
        %WorldName%'
      url: https://example.com
      footer:
        text: 'Report ID: %ReporterID%'
        icon-url: https://example.com/footer-icon.png
      image:
        url: https://example.com/image.png
      thumbnail:
        url: https://example.com/thumbnail.png
mysql:
  host: localhost
  port: 3306
  database: reports
  username: yourUsername
  password: yourPassword
sqlite:
  file: reports.db
  path: database
mongodb:
  host: localhost
  port: 27017
  database: spigotTestDB
  username: yourUsername
  password: yourPassword
# This name will be displayed in the Discord webhook and saved in the database.
server-name: Localhost Server
# This address will be displayed in the Discord webhook and saved in the database.
server-address: localhost
```

## License

Created by FrameDev. Do not modify without consent.