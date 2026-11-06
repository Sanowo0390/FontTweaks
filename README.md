# FontTweaks

**Minecraft Java Edition 26.3 / Fabric / client-only**

日本語の美咲ゴシック第2を内蔵し、GUIスケールを**3に保ったまま、8×8ドットフォントの1ドットを画面上のちょうど2×2ピクセル**で表示できるクライアントModです。

## 主な機能

- **MisakiGothic2forMC (26.3) 内蔵** — 作者KabanFriends氏の日本語ピクセルフォントを別リソースパックなしで使えます。
- **GUIスケール3・1ドット2画面px** — 標準の8×8ドットフォントはGUIスケール3で1ドット3画面px。文字の拡大率を正確に2/3に合わせ、座標も実画面ピクセル単位に揃えます。
- ドットの大きさは **1 / 2 / 3画面px** から選択可能。**デフォルト2px**。
- **ピクセル固定モード** はオン・オフ可能（デフォルトはオン）。GUIスケール3でのみ適用されます。ほかのGUIスケールでは従来のFontTweaks倍率を使います。
- 全体、画面、HUD、ボタン、チャットの文字サイズと、折り返し文章・ツールチップの行間を個別に設定できます。
- 倍率はスライダーと数値入力を併用し、`0.6666666666666667` のような小数も設定ファイルに保持します。
- チャットはMinecraft本体の描画倍率も変更するため、文字と背景・折り返し・クリック範囲の整合性を保ちます。
- Mod Menu経由の日本語/英語設定画面、`config/fonttweaks.json` への自動保存、初期化ボタン。

**重要:** GUI3でピクセル固定をオンにしている間は、厳密な1ドット2pxを守るため、各文字サイズの数値設定よりも固定モードが優先されます。文字サイズを独自に指定するときは固定モードをオフにしてください。FontTweaksの設定画面自体は操作しやすいように固定モードの対象から除外しています。

## 使い方

1. Fabric 26.3、対応版のFabric API、Mod Menuを導入します。
2. GitHub Actionsから**fonttweaks-0.3.0+26.3.jar**（通常のMod JAR）を取得し、`mods` フォルダーに入れます。追加の美咲ゴシック用リソースパックは不要です。
3. MinecraftのGUIスケールを`3`にします。
4. Mod Menu → FontTweaks → 「GUI3 ドット補正」をオン、「1ドット：2画面px」を選択します。
5. ほかの倍率や行間は必要に応じて調整してください。ピクセル固定中、GUI3の文字倍率は2pxが優先です。

**旧版の「文字を2×2／3×3で潰す」処理は削除しました。** 文字をぼかしたり、字形を粗く2値化したりしません。美咲ゴシック第2の元の8×8字形を使います。

## 制約と互換性

- 画面上の**8px高のビットマップフォント**の各ドットを整数倍で描くことを目的にしています。MisakiGothic2forMCがカバーしない字（Unifont/Unihex等の別の元解像度の字形）は必ずしも2px/ドットになりません。
- 独自TTF、リソースパックによる別フォント、特殊なシェーダー/GUI Modなどは異なる見え方になる場合があります。
- フォントだけの倍率補正なので、ボタンそのものの縦横サイズは変わりません。
- JARのコンパイル・自動テスト・内蔵アセットのZIP検証はGitHub Actionsで実施していますが、**Minecraft実機上での表示品質は未検証**です。
- FabricのModリソースパック優先順位によっては、ほかのフォント変更リソースパックと競合します。

## Build / 開発者向け

Requires JDK 25 / Gradle 9.7.1. Run `gradle clean build`.

During the build, Gradle downloads the exact official 26.3 resource-pack archive from Modrinth, verifies its pinned SHA-256 and bundles the **font JSON and bitmap PNG** directly into the mod JAR. The ZIP is downloaded only for builds, not when players launch Minecraft.

Upstream resource-pack ZIP SHA-256:
`a09b2f0ea1e911fe4dd83fac39953a447a69c42fecc544d9af723a84954eee8d`

The bundled assets include `assets/minecraft/font/default.json`, `assets/misakigothic2/font/shift_jis.json`, and `assets/misakigothic2/textures/font/shift_jis.png`.

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
