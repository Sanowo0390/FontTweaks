# FontTweaks 0.4.0

Minecraft 26.3 / Fabric / client-only

Unifontの元の字形を粗いドットにするピクセル化と、文字サイズを独立して設定します。美咲へのフォント置換は行いません。

- ピクセル化ON/OFF：Unifont（Unihex）のビットマップだけを加工します。文字幅、高さ、送り幅、描画倍率は変更しません。
- 粗さ1：元のドットのまま。粗さ2／3：元画像の2×2／3×3画素を同じ白／透明のブロックにまとめます。半分以上が白なら白、残りは透明にします。端のブロックは元画像の範囲で処理します。
- 粗さは画面上のpx数ではありません。GUIスケールに依存しない元画像の画素数です。
- 文字サイズ・行間：ピクセル化のON/OFFにかかわらず調整可能です。GUI3の強制倍率補正は廃止しました。
- 切り替え・粗さ変更時は、キャッシュ済みの字形にも反映するためMinecraftのリソースを再読み込みします。再起動は不要な実装です。
- Bitmap/TTFフォントはこの効果の対象外です。Unifontを使う日本語などが対象です。美咲と同じ字形を生成する機能ではありません。

旧設定のpixelPerfectModeはpixelationEnabledへ、pixelsPerFontDotはpixelBlockSizeへ読み替えます。既存の文字倍率・行間は保持します。元のサイズで効果だけを比較する場合は文字倍率を100%にしてください。初期化でも倍率100%・粗さ2・ピクセル化ONになります。

元の美咲フォント画像と権利表示は同梱したまま保持しますが、標準フォントへの自動適用は行いません。assets/minecraft/font/default.jsonは同梱しません。

## 検証とビルド

JDK25 / Gradle9.7.1: `gradle clean build`、`python scripts/verify_font_assets.py`。

単体テストではブロック処理、端の処理、バッファ範囲、文字サイズと効果の独立性、旧設定の読み替えを確認します。ActionsでビルドとJAR内の元フォント画像・権利表示の一致も検証します。

実機の表示品質とMixin適用は別途確認が必要です。GUI3・倍率100%でON/OFFと粗さ1/2/3を比較し、次にONのまま倍率を変更してください。粗くすることで細線が欠けたり線がつながったりする場合があるため、可読性は実機確認が必要です。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
