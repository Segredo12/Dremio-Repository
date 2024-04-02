
### Sample README

A custom ARP Connector for Dremio to work with OpenSearch

### How to start
1. Update the ```version.dremio``` propery to the installed Dremio version in the ```pom.xml``` file
2. Build the project with ```maven clean install```
3. Copy the compiled jar file in the target folder to the ```$DREMIO_HOME/jars``` directory
4. Include the OpenSearch JDBC driver in the ```$DREMIO_HOME/jars/3rdparty``` directory
5. Restart Dremio with ```$DREMIO_HOME/bin/dremio restart```
6. Add data source in Dremio with OpenSearchLocal connector

### Notes

Anonymous Authentication must be disabled on the OpenSearch configurations. Otherwise Dremio will ignore any credentials and proceed connection with the anonymous account.
File in openseach 2.11: ```opensearch/config/opensearch-security/config.yml```
```yml
  http:
    anonymous_auth_enabled: false
```

### More Information

Dremio Custom ARP Connectors: https://www.dremio.com/resources/tutorials/how-to-create-an-arp-connector/
Template SQLite Connector: https://github.com/dremio-hub/dremio-sqllite-connector
OpenSearch JDBC Driver: https://github.com/opensearch-project/sql-jdbc