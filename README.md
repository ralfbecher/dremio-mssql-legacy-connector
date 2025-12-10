# Dremio MSSQL Legacy ARP Connector

## Overview
This is a community based Microsoft SQL Server Legacy Dremio connector made using the ARP framework. It is designed to work with older SQL Server versions using legacy JDBC drivers (Microsoft JDBC 6.4 or jTDS). Check [Dremio Hub](https://github.com/dremio-hub) for more examples and [ARP Docs](https://github.com/dremio-hub/dremio-sqllite-connector#arp-file-format) for documentation.

## What is Dremio?

Dremio delivers lightning fast query speed and a self-service semantic layer operating directly against your data lake storage and other sources. No moving data to proprietary data warehouses or creating cubes, aggregation tables and BI extracts. Just flexibility and control for Data Architects, and self-service for Data Consumers.

## Usage

### Required Parameters

- Host
- Port (default: 1433)
- Username
- Password

### Optional Parameters

- Database name
- SSL encryption toggle
- Extra JDBC parameters

## Supported JDBC Drivers

This connector supports two driver options:

1. **Microsoft JDBC 6.4** (`com.microsoft.sqlserver.jdbc.SQLServerDriver`) - For relatively recent SQL Server versions
2. **jTDS** (`net.sourceforge.jtds.jdbc.Driver`) - For very old SQL Server instances

To switch drivers, modify the `DRIVER` constant in `MssqlLegacyConf.java`.

## Development

## Building and Installation

0. Change the pom's dremio.version to suit your Dremio's version.
   `<version.dremio>25.2.0-202410241428100111-a963b970</version.dremio>`
1. In root directory with the pom.xml file run `mvn clean install -DskipTests`. If you want to run the tests, add the JDBC jar to your local maven repo along with environment variables that are required.
2. Take the resulting .jar file from the target folder and put it in the `<DREMIO_HOME>/jars` folder in Dremio
3. Download the appropriate JDBC driver:
   - Microsoft JDBC: [Maven Repository](https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)
   - jTDS: [Maven Repository](https://mvnrepository.com/artifact/net.sourceforge.jtds/jtds)

   Place the driver JAR in the `<DREMIO_HOME>/jars/3rdparty` folder
4. Restart Dremio

## Changes

- Initial release for Dremio version 25.2.0
- Support for legacy Microsoft SQL Server connections
- Dual driver support (Microsoft JDBC 6.4 and jTDS)
