#!/usr/bin/env bash
set -euo pipefail

# Quan trọng: profile local để môi trường dev chạy đồng nhất.
mvn spring-boot:run -Dspring-boot.run.profiles=local
