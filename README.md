# FontTweaks 0.4.1

Minecraft 26.3 / Fabric / client-only

Unifontの字形をGUIスケールに対応した画面px単位のドットへ変換します。ピクセル化と文字サイズは独立しています。美咲への自動置換は行いません。

## 0.4.0からの修正

0.4.0の白／透明の多数決は細い線を消してしまうため、廃止しました。ブロック内の線の面積を透明度として保持し、少数の画素も消さずに描画します。文字サイズ、送り幅、字形の描画範囲は維持します。粗いドットへの変換なので元の線の形状を完全に保持する機能ではありません。

「1ドット：1／2／3画面px」は文字倍率100%の画面上の大きさです。GUI3で3を選ぶと1ドット3画面pxです。GUIスケールが変わっても、設定した画面px数に合わせて再計算します。元の16画素高のUnifontはGUI3では24画面px高なので、3画面px指定で高さ方向は8ブロックになります。端のブロックは字形の範囲内に収めます。

ピクセル化ON/OFFで文字の倍率は変更しません。文字サイズ設定はON/OFFどちらでも使用できます。独立した文字倍率を変更すると文字とドットが一緒に拡大縮小します（例：GUI3・3画面px・文字倍率150%ならドットは4.5画面px）。チャット本体の倍率や特殊な描画変換も同様です。

変更時とGUIスケール変更時には、キャッシュ済みの字形へ反映するためリソースを再読み込みします。Bitmap/TTFフォントは加工せず、Unifont（Unihex）だけを対象にします。

旧設定のpixelPerfectModeはpixelationEnabledへ、pixelsPerFontDotはpixelBlockSizeへ読み替えます。0.4.0のpixelBlockSizeも引き継ぎますが、単位は元画像pxから画面pxへ変わっています。元のサイズで比較するには倍率を100%に戻してください。

元の美咲フォント画像と権利表示を保持しています。assets/minecraft/font/default.jsonは同梱しません。

## 検証とビルド

JDK25 / Gradle9.7.1: `gradle clean build`、`python scripts/verify_font_assets.py`。

単体テストでは細線の保持、GUI1/2/3/4とドット1/2/3の組合せ、画像端、文字倍率と効果の独立性、設定の移行を確認します。ActionsでビルドとJAR内の元フォント画像・権利表示の一致も検証します。

Minecraft実機での表示品質・切り替え・Mixin適用は別途確認が必要です。添付報告と同じGUI3・倍率100%で3画面pxを確認し、次に1/2画面pxとON/OFFを比較してください。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
