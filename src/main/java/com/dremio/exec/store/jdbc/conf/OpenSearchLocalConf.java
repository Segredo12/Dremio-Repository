/*
 * Copyright (C) 2017-2018 Dremio Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dremio.exec.store.jdbc.conf;

import java.util.Properties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import com.dremio.exec.catalog.conf.DisplayMetadata;
import com.dremio.exec.catalog.conf.NotMetadataImpacting;
import com.dremio.exec.catalog.conf.Secret;
import com.dremio.exec.catalog.conf.SourceType;
import com.dremio.exec.store.jdbc.CloseableDataSource;
import com.dremio.exec.store.jdbc.DataSources;
import com.dremio.exec.store.jdbc.JdbcPluginConfig;
import com.dremio.exec.store.jdbc.dialect.arp.ArpDialect;
import com.dremio.options.OptionManager;
import com.dremio.services.credentials.CredentialsService;
import com.google.common.annotations.VisibleForTesting;

import io.protostuff.Tag;

/**
 * Configuration for OpenSearch source.
 * Was named OpenSearchLocalConf because Dremio already has a OpeanSearchConf class
 */
@SourceType(value = "OPENSEARCHLOCAL", label = "OpenSearchLocal", uiConfig = "opensearchlocal-layout.json", externalQuerySupported = true)
public class OpenSearchLocalConf extends AbstractArpConf<OpenSearchLocalConf> {
  private static final String ARP_FILENAME = "arp/implementation/opensearchlocal-arp.yaml";
  private static final ArpDialect ARP_DIALECT =
      AbstractArpConf.loadArpFile(ARP_FILENAME, (ArpDialect::new));
  private static final String DRIVER = "org.opensearch.jdbc.Driver";

  @NotBlank
  @Tag(1)
  @DisplayMetadata(label = "Hostname")
  public String hostname;

  @Tag(2)
  @Min(1)
  @Max(65535)
  @DisplayMetadata(label = "Port")
  public int port = 9200;

  @Tag(3)
  @DisplayMetadata(label = "Record fetch size")
  @NotMetadataImpacting
  public int fetchSize = 0;

  @Tag(4)
  @DisplayMetadata(label = "Username")
  public String username;

  @Tag(5)
  @Secret
  @DisplayMetadata(label = "Password")
  public String password;

  @Tag(6)
  @DisplayMetadata(label = "Use SSL")
  public boolean useSSL = true;

  @Tag(7)
  @DisplayMetadata(label = "Trust Self Signed Certificates")
  public boolean trustSelfSigned = false;

  @Tag(8)
  @DisplayMetadata(label = "Hostname Verification")
  public boolean hostnameVerification = true;

  @Tag(9)
  @DisplayMetadata(label = "Maximum idle connections")
  @NotMetadataImpacting
  public int maxIdleConns = 8;

  @Tag(10)
  @DisplayMetadata(label = "Connection idle time (s)")
  @NotMetadataImpacting
  public int idleTimeSec = 60;

  @VisibleForTesting
  public String toJdbcConnectionString() {
    //only host and port, other settings added as properties in newDataSource
    return String.format("jdbc:opensearch://%s:%d ", hostname, port);
  }

  @Override
  @VisibleForTesting
  public JdbcPluginConfig buildPluginConfig(
          JdbcPluginConfig.Builder configBuilder,
          CredentialsService credentialsService,
          OptionManager optionManager
  ) {
    return configBuilder.withDialect(getDialect())
            .withFetchSize(fetchSize)
            .withDatasourceFactory(this::newDataSource)
            .build();
  }

  private CloseableDataSource newDataSource() {
    Properties properties = new Properties();
    properties.put("useSSL", useSSL);
    properties.put("trustSelfSigned", trustSelfSigned);
    properties.put("hostnameVerification", hostnameVerification);
    // connector authentication will break on null values
    if(username!=null && !username.trim().isEmpty() && password!=null && !password.trim().isEmpty()) {
      properties.put("user", username);
      properties.put("password", password);
    }
    return DataSources.newGenericConnectionPoolDataSource(DRIVER,
      toJdbcConnectionString(), null, null, properties, DataSources.CommitMode.DRIVER_SPECIFIED_COMMIT_MODE,
            maxIdleConns, idleTimeSec);
  }

  @Override
  public ArpDialect getDialect() {
    return ARP_DIALECT;
  }

  @VisibleForTesting
  public static ArpDialect getDialectSingleton() {
    return ARP_DIALECT;
  }
}
