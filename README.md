# ReportPlugin

A simple Spigot plugin for reporting issues and feedback, with Discord webhook integration.

## Features

- Players can report other players with a reason.
- Reports are stored in a database.
- Staff can view and update reports via commands or a GUI.
- Reports are sent to a Discord channel via webhook.

## Installation

1. Build the plugin using Maven:
    ```bash
    mvn clean package
    ```
2. Place the generated `ReportPlugin-1.0-SNAPSHOT.jar` in your server's `plugins` folder.
3. Start your server to generate the default config.
4. Edit `config.yml` to set your Discord webhook URL and other settings.
5. Reload or restart your server.

## Commands

- `/report <player> [reason]`  
  Report a player for a specific reason.

- `/reportlist`  
  List all reports.

- `/reportgui`  
  Open the report management GUI.

## Permissions

- `reportplugin.report` — Use the `/report` command.
- `reportplugin.list` — Use the `/reportlist` command.
- `reportplugin.gui` — Use the `/reportgui` command.
- `reportplugin.report.notify` — Receive notifications for new reports.

## Configuration

Edit the `discord` section in `config.yml` to set your webhook URL.

Messages will be changed to make it configurable in the future.

## License

Created by FrameDev. Do not modify without consent.