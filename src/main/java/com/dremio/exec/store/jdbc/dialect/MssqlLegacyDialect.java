package com.dremio.exec.store.jdbc.dialect;

import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.exec.store.jdbc.dialect.arp.ArpYaml;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWriter;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.type.SqlTypeName;

/**
 * Custom dialect for MSSQL Legacy that extends ArpDialect to handle
 * SQL Server-specific syntax, particularly Unicode string literals.
 *
 * SQL Server uses N'...' for Unicode strings instead of the ANSI SQL
 * u&'...' syntax that Calcite generates by default.
 */
public class MssqlLegacyDialect extends ArpDialect {

  public MssqlLegacyDialect(ArpYaml yaml) {
    super(yaml);
  }

  @Override
  public boolean supportsCharSet() {
    // Return false to prevent Calcite from generating _UTF16'...' or u&'...' syntax
    // This makes Calcite treat strings as simple literals
    return false;
  }

  @Override
  public void quoteStringLiteral(StringBuilder buf, String charsetName, String val) {
    // For SQL Server, use N'...' prefix for Unicode strings
    if (charsetName != null || hasNonAscii(val)) {
      buf.append("N'");
      buf.append(val.replace("'", "''"));
      buf.append("'");
    } else {
      // For ASCII-only strings, use standard quoting
      buf.append("'");
      buf.append(val.replace("'", "''"));
      buf.append("'");
    }
  }

  /**
   * Check if a string contains non-ASCII characters.
   */
  private static boolean hasNonAscii(String str) {
    if (str == null) {
      return false;
    }
    for (int i = 0; i < str.length(); i++) {
      if (str.charAt(i) > 127) {
        return true;
      }
    }
    return false;
  }
}
