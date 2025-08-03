#!/bin/bash
# Debezium connector init script for MySQL 'events' table

sleep 10

set -e

CONNECTOR_NAME="mysql-events-connector"
DEBEZIUM_URL="http://debezium:8083/connectors"

curl -v $DEBEZIUM_URL \
  -H "Content-Type: application/json" \
  -d '{
    "name": "mysql-events-connector",
    "config": {
      "connector.class": "io.debezium.connector.mysql.MySqlConnector",
      "database.hostname": "mysql",
      "database.port": "3306",
      "database.user": "root",
      "database.password": "root",
      "database.server.id": "223344",
      "database.server.name": "esdemo",
      "database.include.list": "es_demo",
      "schema.history.internal.kafka.topic": "schema-changes.events",
      "schema.history.internal.kafka.bootstrap.servers": "kafka:9092",
      "table.include.list": "es_demo.events",
      "database.history.kafka.bootstrap.servers": "kafka:9092",
      "database.history.kafka.topic": "schema-changes.events",
      "topic.prefix": "esdemo",
      "include.schema.changes": "false",
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",
      "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter"
    }
  }'

echo "Debezium connector for 'events' table initialized."