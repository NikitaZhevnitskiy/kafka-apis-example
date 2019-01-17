#!/usr/bin/env bash
curl -s http://localhost:8083/connectors/sink-postgres-word/status | jq .