#!/usr/bin/env bash
set -euo pipefail

# Quan trọng: đây chỉ là ví dụ deploy tối giản cho template ban đầu.
mvn clean package

docker compose --env-file .env.example up --build -d
