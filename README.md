# FontTweaks — Minecraft 26.3 / Fabric

A **client-only** experimental Fabric mod for independently adjusting screen/HUD text scale, word-wrapped text line spacing, and item tooltip text row spacing.

## Status

GitHub Actions build has succeeded for the original prototype. Subsequent commits must pass their own workflow build; in-game runtime behavior remains unverified.

## New in 0.2.1 — actual font bitmap pixelation

The previous 0.2.0 **GUI-scale-3 pixel alignment/size adjustment has been removed**, because it changed the appearance and size of a text run rather than making the glyph itself blockier.

- **Unifont glyph pixelation** is a separate, optional font effect. It changes the **source glyph bitmap data** before the glyph is uploaded to Minecraft's font texture atlas. It does not adjust GUI scale, font size, character advance or layout.
- Settings: `pixelGlyphEnabled` (default `false`) and `pixelGlyphBlockSize` (default `2`, switch between `2×2`, `3×3` and `4×4` source texel blocks).
- The algorithm thresholds glyph pixels within each source block and fills that block with a single solid glyph color/alpha. This creates genuinely block-shaped letter strokes rather than stretching or repositioning the font.
- Any GUI scale (including scale 3) may use the effect. Turning it on/off or changing strength triggers a **resource reload**, rebuilding cached glyph atlas textures.
- Only **vanilla Unihex/Unifont glyphs** currently go through the pixelation hook. Other providers (bitmap, TTF, sprite fonts and resource-pack replacements) are not modified by this effect. Existing text size and line spacing controls work as before.
- Extremely coarse pixelation can obscure detailed kanji; start with `2×2` blocks.
- The old JSON config keys `unifontPixelPerfect` and `unifontPixelsPerDot` are retired. They are ignored and disappear the next time config is saved.

## Previous 0.2.0 features

- Button text scaling and independent chat scaling, without changing their surrounding UI widgets.
- Decimal values such as `0.6666666666666667` remain supported by all numeric controls.

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
- Chat uses the native chat zoom multiplied by global/chat-specific settings; it does not inherit the HUD multiplier.
- Mixin targets need runtime testing against Minecraft 26.3; compatibility with GUI overhaul mods is not assured.

## Build

Requires **JDK 25**, **Gradle 9.7.1** and internet access to download Fabric dependencies.

Run: gradle clean build

GitHub Actions runs on each push and can produce a downloadable JAR artifact in the workflow run if successful. Do not use any JAR until compilation and Minecraft runtime behavior have been confirmed.

## Install

Build the project and put the non-sources JAR in your Fabric 26.3 instance's mods folder. Install compatible Fabric Loader, Fabric API and Mod Menu for settings access. This is a client-only mod, not a dedicated-server plugin.

## 26.3-specific approach

Minecraft 26.1+ uses the unobfuscated/no-remap Fabric setup. Minecraft 26.3 moved GUI text extraction into GuiGraphicsExtractor; older DrawContext or GuiGraphics Mixins cannot be copied over directly.
