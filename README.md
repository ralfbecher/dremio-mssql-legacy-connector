# Dremio MSSQL Legacy ARP Connector

## Overview
This is a community based Microsoft SQL Server Legacy Dremio connector made using the ARP framework. It is designed to work with older SQL Server versions (2008, 2012, etc.) that only support TLSv1 or have other compatibility issues with modern JDBC drivers. Check [Dremio Hub](https://github.com/dremio-hub) for more examples and [ARP Docs](https://github.com/dremio-hub/dremio-sqllite-connector#arp-file-format) for documentation.

## What is Dremio?

Dremio delivers lightning fast query speed and a self-service semantic layer operating directly against your data lake storage and other sources. No moving data to proprietary data warehouses or creating cubes, aggregation tables and BI extracts. Just flexibility and control for Data Architects, and self-service for Data Consumers.

## JDBC Driver

This connector uses the **jTDS driver** which provides better compatibility with legacy SQL Servers:
- Supports TLSv1 (required for older SQL Servers)
- Works with SQL Server 2000, 2005, 2008, 2012, and newer
- No TLS version conflicts with modern Java

**Required:** Download [jTDS 1.3.1](https://sourceforge.net/projects/jtds/files/jtds/1.3.1/) and place `jtds-1.3.1.jar` in `<DREMIO_HOME>/jars/3rdparty/`

## Usage

### Required Parameters

- Host
- Port (default: 1433)
- Username
- Password

### Optional Parameters

- Database name
- SSL encryption toggle (uses jTDS `ssl=require`)
- Extra JDBC parameters (e.g., `instance=SQLEXPRESS`)

### Extra JDBC Parameters Examples

For named instances:
```
instance=SQLEXPRESS
```

For Windows Authentication (requires additional setup):
```
domain=MYDOMAIN
```

## Building and Installation

1. Change the pom's dremio.version to suit your Dremio's version:
   ```xml
   <version.dremio>25.2.0-202410241428100111-a963b970</version.dremio>
   ```

2. Build the connector:
   ```bash
   mvn clean install -DskipTests
   ```

3. Copy files to Dremio:
   ```bash
   cp target/dremio-mssql-legacy-plugin-*.jar <DREMIO_HOME>/jars/
   cp jtds-1.3.1.jar <DREMIO_HOME>/jars/3rdparty/
   ```

4. Restart Dremio

## Changes

- Switch to jTDS driver as default for better legacy SQL Server compatibility
- Added custom dialect for SQL Server Unicode string literals (N'...' syntax)
- Fixed ARP YAML format for Dremio 25.x compatibility
- Initial release for Dremio version 25.2.0
