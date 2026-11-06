# FontTweaks — Minecraft 26.3 / Fabric

A **client-only** experimental Fabric mod for independently adjusting screen/HUD text scale, word-wrapped text line spacing, and item tooltip text row spacing.

## Status

GitHub Actions build has succeeded for the original prototype. Subsequent commits must pass their own workflow build; in-game runtime behavior remains unverified.

## New in 0.2.0

- **Button labels:** independent button-text scale (50–200%). The button graphics and clickable bounds remain unchanged.
- **Chat:** independent chat scale multiplier (50–200%). It multiplies vanilla Chat Scale and the global FontTweaks multiplier. Chat background, wrapping and hit detection use this combined native chat scale; HUD scale does not multiply chat.
- **Unifont pixel alignment (optional):** for GUI scale **3**, text runs with CJK / kana / Hangul characters use **1 physical screen pixel** per approximately 16-pixel-tall Unifont source dot (small, equivalent to 0.6666666666666667 of the default GUI text size) or **2 physical pixels** per source dot (large, equivalent to 1.3333333333333333). Text origins are also rounded to the physical pixel grid.
- **Precision:** every numeric scale setting accepts decimal input with double-precision storage.
- **Config:** `buttonScale`, `chatScale`, `unifontPixelPerfect` and `unifontPixelsPerDot` are new JSON keys. Old files migrate automatically by defaulting new fields.

The Unifont fix is an opt-in pixel-grid **size/alignment correction**, not a rewrite of font textures or a nearest-neighbor shader. The CJK character-range detection does not distinguish resource-pack glyph providers. If a line mixes English and Japanese, its entire run is scaled consistently to avoid splitting individual glyph advance positions. It does not change non-CJK strings, and it is inactive at GUI scales other than 3.

## Features

- Global text scale: 50–200% (default 100%)
- Extra screen/UI text scale: 50–200% (default 100%)
- Extra HUD text scale: 50–200% (default 100%)
- Word-wrapped text line spacing: 50–200% (default 100%)
- Item tooltip text line spacing: 50–200% (default 100%)
- Sliders support 0.001 increments; number fields permit precise decimal entry such as `0.6666666666666667` (= two thirds, about 66.67%).\n- Values use double-precision in config; graphical rendering still uses floats.\n- Mod Menu settings screen, Japanese and English labels, JSON config and reset
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
