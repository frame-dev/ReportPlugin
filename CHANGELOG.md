# Changelog

All notable changes to this project will be documented in this file.

---

## Initialization

- Initial commit (reset history)
- Add delete_unnecessary_files.sh to .gitignore

---

## Architecture & Refactoring

- Refactor database initialization in ReportCommand and ReportPlugin to pass plugin instance
- Refactor package structure and update main class reference in plugin.yml
- Refactor database connection handling and improve error logging; add ReportAPI for report management
- Refactor ConfigUtils constructor to remove unnecessary plugin dependency
- Refactor database handling to use switch expression; enhance Report class with MongoDB support and additional constructors
- Refactor ConfigUtils to use record type; improve whitespace consistency and suppress unused warnings in various classes
- Refactor ReportCommand to improve command handling; streamline report logic and enhance logging on plugin disable
- Refactor configuration and command classes for improved readability and maintainability
- Refactor report deletion methods to return success status across database helpers
- Refactor history file handling in FileSystemHelper and enhance report update feedback in ReportGUI
- Refactor ReportGUI to dynamically calculate inventory size and streamline button addition

---

## Database & Storage

- Implement database abstraction with MySQL and SQLite support
- Add MongoDB support for report management; refactor database handling and update configuration
- Add FileSystemHelper for report storage; enhance Database class to support filesystem
- Update database configuration to support filesystem; modify Database class to initialize with filesystem type
- Enhance FileSystemHelper with improved error handling; ensure reports directory creation and logging for file operations
- Enhance FileSystemHelper with improved error handling and report management; add delete functionality in ReportGUI and streamline report retrieval logic
- Implement database connection methods across database helpers and enhance logging during plugin initialization

---

## Configuration

- Enhance database connection handling with improved logging; add database type initialization in Database class
- Enhance configuration management by adding comments for database settings; implement database type retrieval and additional report management methods in Database class
- Enhance configuration management by adding default values and comments for new settings in ConfigUtils; update version in plugin.yml and improve documentation in Database class
- Enhance configuration management by adding report settings with limits for reports per player and reporter in ConfigUtils

---

## Features & Commands

- Add ReportDataCommand to view report data and refactor database usage in commands
- Add teleport command for reported players; update version in plugin.yml and enhance command permissions
- Add report deletion command and update related documentation
- Enhance report management by adding server-name configuration; implement report counting functionality across database helpers and update report data display in GUI
- Enhance report management by adding server-address configuration; implement report counting and retrieval methods in ReportAPI
- Add update history management for reports with database integration

---

## GUI & User Interface

- Enhance ReportGUI with improved error handling and report management; add delete functionality in ReportGUI and streamline report retrieval logic
- Enhance ReportDataCommand and ReportGUI with additional report details
- Enhance DatabaseHelper and ReportGUI with additional methods for report management; implement kick and ban functionalities with user prompts
- Refactor ReportGUI to dynamically calculate inventory size and streamline button addition

---

## Discord & Integration

- Enhance Discord integration by adding webhook configurations for report creation and updates; implement dynamic message content in ReportCommand and ReportGUI

---

## Documentation

- Update README and refactor report handling; improve logging and configuration details
- Enhance documentation by adding constructor comments for Database, FileSystemHelper, MongoDBHelper, MySQLHelper, SQLiteHelper, and Report classes; improve clarity on parameter usage and initialization
- Update plugin.yml and README.md for improved command naming and descriptions; enhance ReportDataCommand and ReportGUI with additional report details
- Enhance ReportPlugin and SQLiteHelper with additional comments for clarity; document singleton initialization and database operations
- Update README.md to include new /reporttp command for teleporting to report locations and add related permissions
- Update README.md to format the /report-data command description for consistency

---
