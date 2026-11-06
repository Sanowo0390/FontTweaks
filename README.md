# FontTweaks — Minecraft 26.3 / Fabric

A **client-only** experimental Fabric mod for independently adjusting screen/HUD text scale, word-wrapped text line spacing, and item tooltip text row spacing.

## Status

Source package / **unbuilt prototype**. Minecraft 26.3 class signatures were cross-checked against published API references, but a full Gradle compilation and actual Minecraft runtime test have **not** been completed. Do not place the source ZIP in the mods directory; it is not a JAR.

## Features

- Global text scale: 50–200% (default 100%)
- Extra screen/UI text scale: 50–200% (default 100%)
- Extra HUD text scale: 50–200% (default 100%)
- Word-wrapped text line spacing: 75–200% (default 100%)
- Item tooltip text line spacing: 75–200% (default 100%)
- Mod Menu settings screen, Japanese and English labels, JSON config and reset
- Config path: config/fonttweaks.json

## Limitations

- Glyph rendering changes, but button hitboxes and layout containers do not change. Large text may overlap or clip.
- Not every tooltip, other mod's custom text rendering or rendering path will respond to scaling. Tooltip text and background geometry may not align.
- Wrapped line spacing covers the GuiGraphicsExtractor textWithWordWrap path. Chat scale and spacing have native Minecraft settings.
- Chat/HUD text may share the HUD multiplier, in addition to vanilla chat scale.
- Mixin targets need runtime testing against Minecraft 26.3; compatibility with GUI overhaul mods is not assured.

## Build

Requires **JDK 25**, **Gradle 9.7.1** and internet access to download Fabric dependencies.

Run: gradle clean build

GitHub Actions runs on each push and can produce a downloadable JAR artifact in the workflow run if successful. Do not use any JAR until compilation and Minecraft runtime behavior have been confirmed.

## Install

Build the project and put the non-sources JAR in your Fabric 26.3 instance's mods folder. Install compatible Fabric Loader, Fabric API and Mod Menu for settings access. This is a client-only mod, not a dedicated-server plugin.

## 26.3-specific approach

Minecraft 26.1+ uses the unobfuscated/no-remap Fabric setup. Minecraft 26.3 moved GUI text extraction into GuiGraphicsExtractor; older DrawContext or GuiGraphics Mixins cannot be copied over directly.
