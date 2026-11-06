# FontTweaks 0.4.2

Minecraft 26.3 / Fabric / client-only

標準英字のドット幅と設定した画面px数が一致する場合、美咲ゴシック第2を自動選択します。

- 標準のビットマップ英字はGUI1単位が1ドットです。GUI3なら文字倍率100%で1ドット3画面pxなので、設定3で一致します。GUI2なら設定2です。
- ピクセル化ON・GUIスケールと画面px数が同じ・Unicodeフォント強制OFFのときだけ、自動で美咲を使用します。設定画面の下部にも判定を表示します。
- これは標準ビットマップ英字を基準にした判定です。外部リソースパックの任意の英字フォントを画像解析する機能ではありません。
- 不一致の場合は標準フォントを使い、Unifont部分にドット加工を適用します。OFFでは自動選択も画像加工も停止します。文字サイズ設定は独立して利用できます。

## ぼやけへの対処

0.4.1の半透明ブロックを廃止し、完全な白／透明だけで画像を生成します。細線を消さないため、ブロックに線が含まれれば白にする方式です。ドットを粗くすると線が太くなったり隣の線とつながったりする可能性があります。

描画開始位置は実画面のピクセル境界へ揃えます。文字サイズ100%の標準的な描画で、1/2/3画面pxのドットを指定します。別途倍率を変更したり、他Modの描画変換が入ったりするとドットも拡大縮小するため、任意の倍率で完全な整数ドットになる保証はありません。

GUIスケール・ピクセル化ON/OFF・画面px設定の変更時にはリソースを再読み込みします。文字幅計算と描画に同じフォント参照先を使います。美咲は元の画像・JSONを維持し、標準フォント上書きファイルassets/minecraft/font/default.jsonは同梱しません。

既存の文字倍率・画面px設定は保持します。GUI3で英字と同じドット幅にするには、倍率100%・ピクセル化ON・3画面pxで比較してください。Unicodeフォント強制はOFFにします。

## 検証とビルド

JDK25 / Gradle9.7.1: `gradle clean build`、`python scripts/verify_font_assets.py`。

単体テストでは英字グリッド一致・不一致、OFF、Unicode強制、フォント参照先の復帰、白／透明以外の画素を生成しないこと、GUIスケールごとのドット数、描画範囲の保持を確認します。Actionsでビルドと元フォント・権利表示の保持を検証します。

Minecraft実機での見た目・切り替え動作は別途確認が必要です。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
