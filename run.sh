#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "$0")" && pwd)"
gradle_version="9.6.0"
gradle_home="$project_dir/.gradle-bootstrap/gradle-$gradle_version"

if command -v gradle >/dev/null 2>&1; then
    gradle_command="$(command -v gradle)"
else
    gradle_command="$gradle_home/bin/gradle"
fi

if [[ ! -x "$gradle_command" ]]; then
    archive="$project_dir/.gradle-bootstrap/gradle.zip"
    mkdir -p "$project_dir/.gradle-bootstrap"
    curl --fail --location --output "$archive" \
        "https://services.gradle.org/distributions/gradle-$gradle_version-bin.zip"
    unzip -q "$archive" -d "$project_dir/.gradle-bootstrap"
fi

if [[ "${1:-}" == "test" ]]; then
    exec "$gradle_command" --project-dir "$project_dir" test
fi

quoted_arguments=""
for argument in "$@"; do
    printf -v quoted '%q' "$argument"
    quoted_arguments+="${quoted_arguments:+ }$quoted"
done
exec "$gradle_command" --project-dir "$project_dir" run --args="$quoted_arguments"
