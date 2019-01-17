#!/usr/bin/env bash
curl -s -XPOST http://localhost:8083/connectors -H 'Content-Type: application/json' -d @./config.json | jq .
