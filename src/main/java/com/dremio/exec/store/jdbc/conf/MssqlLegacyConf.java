package com.dremio.exec.store.jdbc.conf;

import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.DataSources;
import com.dremio.services.credentials.CredentialsService;
import com.dremio.options.OptionManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotBlank;

import io.protostuff.Tag;

/**
 * Configuration for MSSQL Legacy sources.
 *
 * This is an ARP-based connector designed to work with old SQL Server
 * versions using a legacy JDBC driver (e.g. Microsoft 6.4 or jTDS).
 */
@SourceType(value = "MSSQL_LEGACY", label = "MSSQL Legacy")
public class MssqlLegacyConf extends AbstractArpConf<MssqlLegacyConf> {

  private static final String ARP_FILENAME = "arp/implementation/mssql-legacy-arp.yaml";

  // Dialect loaded from ARP YAML
  private static final ArpDialect ARP_DIALECT =
      AbstractArpConf.loadArpFile(ARP_FILENAME, (ArpDialect::new));

  /**
   * Choose ONE of these driver classes, depending on which JAR
   * you actually put into /opt/dremio/jars/3rdparty:
   *
   * 1) Microsoft JDBC 6.4:
   *    DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
   *
   * 2) jTDS (good for very old SQL Server):
   *    DRIVER = "net.sourceforge.jtds.jdbc.Driver";
   */
  private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
  // private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";

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
  public ArpDialect getDialect() {
    return ARP_DIALECT;
  }

  @Override
  @JsonIgnore
  public JdbcPluginConfig buildPluginConfig(
      JdbcPluginConfig.Builder configBuilder,
      CredentialsService credentialsService,
      OptionManager optionManager) {
    // Build JDBC URL depending on driver choice
    String url;

    if (DRIVER.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
      // Microsoft legacy driver
      // Note: for very old servers you may want to allow TLSv1 here:
      // ;encrypt=true;trustServerCertificate=true;sslProtocol=TLSv1
      StringBuilder sb = new StringBuilder();
      sb.append("jdbc:sqlserver://").append(host).append(":").append(port).append(";");
      if (database != null && !database.isEmpty()) {
        sb.append("databaseName=").append(database).append(";");
      }
      if (useSsl) {
        sb.append("encrypt=true;trustServerCertificate=true;");
        // sb.append("sslProtocol=TLSv1;"); // uncomment if your driver supports it
      } else {
        sb.append("encrypt=false;");
      }
      if (extraParams != null && !extraParams.isEmpty()) {
        sb.append(extraParams);
      }
      url = sb.toString();
    } else {
      // jTDS URL style
      StringBuilder sb = new StringBuilder();
      sb.append("jdbc:jtds:sqlserver://").append(host).append(":").append(port);
      if (database != null && !database.isEmpty()) {
        sb.append("/").append(database);
      }
      if (useSsl) {
        // jTDS SSL flag
        sb.append(";ssl=require");
      }
      if (extraParams != null && !extraParams.isEmpty()) {
        sb.append(";").append(extraParams);
      }
      url = sb.toString();
    }

    return configBuilder
        .withDialect(getDialect())
        .withDatasourceFactory(() -> DataSources.newGenericConnectionPoolDataSource(
            DRIVER, url, username, password, null,
            DataSources.CommitMode.DRIVER_SPECIFIED_COMMIT_MODE, 8, 60000L))
        .clearHiddenSchemas() // optional, depends on your DB
        .build();
  }

}