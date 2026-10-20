#!/usr/bin/env bash
# Regenerates docs/assets/*.svg from the diagram sources.
# Needs node; mermaid-cli is fetched by npx.
set -euo pipefail
cd "$(dirname "$0")"
for diagram in *.mmd; do
	name="${diagram%.mmd}"
	for theme in light dark; do
		npx -y @mermaid-js/mermaid-cli@11 \
			-i "$diagram" -o "../assets/$name-$theme.svg" \
			-c "theme-$theme.json" -b transparent
	done
done
