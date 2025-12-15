package com.dremio.exec.store.jdbc.conf;

import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.exec.store.jdbc.dialect.MssqlLegacyDialect;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.CloseableDataSource;
import com.dremio.services.credentials.CredentialsService;
import com.dremio.options.OptionManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.sql.DataSource;
import javax.validation.constraints.NotBlank;

import org.apache.commons.dbcp2.BasicDataSource;

import io.protostuff.Tag;

/**
 * Configuration for MSSQL Legacy sources.
 *
 * This is an ARP-based connector designed to work with old SQL Server
 * versions using the Microsoft JDBC 6.4 driver.
 */
@SourceType(value = "MSSQL_LEGACY", label = "MSSQL Legacy", uiConfig = "MSSQL-layout.json", externalQuerySupported = true)
public class MssqlLegacyConf extends AbstractArpConf<MssqlLegacyConf> {

  private static final String ARP_FILENAME = "arp/implementation/mssql-legacy-arp.yaml";

  // Dialect loaded from ARP YAML - using custom MssqlLegacyDialect for SQL Server-specific handling
  private static final MssqlLegacyDialect ARP_DIALECT =
      AbstractArpConf.loadArpFile(ARP_FILENAME, (MssqlLegacyDialect::new));

  /**
   * Microsoft JDBC Driver 6.4 for SQL Server.
   *
   * Make sure to put mssql-jdbc-6.4.0.jre8.jar in /opt/dremio/jars/3rdparty/
   * Download from: https://docs.microsoft.com/en-us/sql/connect/jdbc/download-microsoft-jdbc-driver-for-sql-server
   */
  private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

  // ===== UI fields =====

  @NotBlank
  @Tag(1)
  @DisplayMetadata(label = "Host")
  public String host;

  @Tag(2)
  @DisplayMetadata(label = "Port")
  public int port = 1433;

  @Tag(3)
  @DisplayMetadata(label = "Database (optional)")
  public String database;

  @NotBlank
  @Tag(4)
  @DisplayMetadata(label = "Username")
  public String username;

  @NotBlank
  @Tag(5)
  @Secret
  @DisplayMetadata(label = "Password")
  public String password;

  @Tag(6)
  @DisplayMetadata(label = "Use SSL (legacy)")
  public boolean useSsl = false;

  @Tag(7)
  @DisplayMetadata(label = "Extra JDBC parameters (optional)")
  public String extraParams;

  // ===== Required overrides =====

  @Override
  @JsonIgnore
  public MssqlLegacyDialect getDialect() {
    return ARP_DIALECT;
  }

  @Override
  @JsonIgnore
  public JdbcPluginConfig buildPluginConfig(
      JdbcPluginConfig.Builder configBuilder,
      CredentialsService credentialsService,
      OptionManager optionManager) {
    // Build Microsoft JDBC URL
    // Format: jdbc:sqlserver://host:port;databaseName=database;property=value
    StringBuilder sb = new StringBuilder();
    sb.append("jdbc:sqlserver://").append(host).append(":").append(port);
    if (database != null && !database.isEmpty()) {
      sb.append(";databaseName=").append(database);
    }
    if (useSsl) {
      sb.append(";encrypt=true;trustServerCertificate=true");
    }
    if (extraParams != null && !extraParams.isEmpty()) {
      // Ensure params start with semicolon
      if (!extraParams.startsWith(";")) {
        sb.append(";");
      }
      sb.append(extraParams);
    }
    String url = sb.toString();

    return configBuilder
        .withDialect(getDialect())
        .withDatasourceFactory(() -> createDataSource(url))
        .clearHiddenSchemas()
        .build();
  }

  /**
   * Creates a DBCP2 DataSource with connection pooling.
   */
  private CloseableDataSource createDataSource(String url) {
    BasicDataSource ds = new BasicDataSource();
    ds.setDriverClassName(DRIVER);
    ds.setUrl(url);
    ds.setUsername(username);
    ds.setPassword(password);

    // Connection validation
    ds.setValidationQuery("SELECT 1");
    ds.setTestOnBorrow(true);
    ds.setValidationQueryTimeout(30);

    // Connection pool settings
    ds.setMaxTotal(8);
    ds.setMaxIdle(8);
    ds.setMinIdle(0);
    ds.setMaxWaitMillis(60000L);

    return CloseableDataSource.wrap(ds);
  }

}