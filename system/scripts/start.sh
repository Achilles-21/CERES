#!/usr/bin/env bash
set -euo pipefail
system_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
component="${1:-}"
case "$component" in
  frontend) component_dir=frontend ;;
  java) component_dir=backend-system ;;
  model) component_dir=backend-model ;;
  slidegen) component_dir=backend-model ;;
  slide-frontend) component_dir=backend-model/slidegen/frontend ;;
  medrag) component_dir=backend-model/module_RAG/medrag ;;
  *) echo "Usage: $0 {frontend|java|model|slidegen|slide-frontend|medrag}" >&2; exit 2 ;;
esac
# Local env files are operator-owned shell assignments; quote values where needed.
for env_file in "$system_dir/.env" "$system_dir/$component_dir/.env"; do
  if [[ -f "$env_file" ]]; then
    set -a
    source "$env_file"
    set +a
  fi
done
cd "$system_dir/$component_dir"
case "$component" in
  frontend) exec pnpm dev ;;
  java)
    missing=()
    for name in CERES_DB_URL CERES_DB_USER CERES_DB_PASSWORD CERES_REDIS_PASSWORD CERES_JWT_SECRET CERES_MINIO_ACCESS_KEY CERES_MINIO_SECRET_KEY CERES_VOD_SECRET_ID CERES_VOD_SECRET_KEY; do
      if [[ -z "${!name:-}" ]]; then missing+=("$name"); fi
    done
    if (( ${#missing[@]} )); then
      printf 'Missing required environment variable: %s\n' "${missing[@]}" >&2
      exit 1
    fi
    jar_file=ceres-admin/target/ceres-admin.jar
    if [[ ! -f "$jar_file" ]]; then
      echo "Build first: cd system/backend-system && mvn package" >&2
      exit 1
    fi
    exec java -jar "$jar_file" ;;
  model) exec bash ceres.sh ;;
  slidegen) exec "${PYTHON_BIN:-python}" -m uvicorn slidegen.api.server:app --host "${CERES_HOST:-127.0.0.1}" --port "${CERES_SLIDEGEN_PORT:-8081}" ;;
  slide-frontend) exec npm run dev ;;
  medrag) exec "${PYTHON_BIN:-python}" -m uvicorn medrag_service:app --host "${CERES_HOST:-127.0.0.1}" --port "${CERES_MEDRAG_PORT:-8001}" ;;
esac
