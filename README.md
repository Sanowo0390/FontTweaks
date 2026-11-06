# FontTweaks 0.4.5

Minecraft 26.3 / Fabric / client-only

## ピクセル加工

- ピクセル加工ON/OFFと文字サイズ・行間は独立しています。
- 1ドットは1〜4画面pxから選択できます。表示px数は文字倍率100%時の基準です。別途文字倍率を変更するとドットも拡大縮小します。
- 標準ビットマップ英字とドット幅が一致する場合、美咲ゴシック第2を自動選択します。GUI3なら3画面px、GUI4なら4画面pxです（加工ON・Unicodeフォント強制OFF）。外部フォント画像を解析する判定ではありません。
- 不一致ならUnifontを加工します。GUI4・2画面pxは元のUnifontのドット幅と同じため、見た目が変わらない組合せです。
- 自動GUIでも実効倍率を使い、設定画面の下部に実効GUI倍率と適用状態を表示します。
- ON/OFF、ドット指定、GUI倍率の変更時はリソースを再読み込みします。

## 0.4.5：隙間を保護

ユーザーの「別々の線の間の隙間を残して、くっつくのを防ぎたい」という指定に合わせ、元のドットより粗い設定で、加工前にある細い空白を保護する後処理を追加しました。

元の白い画素は中心が入る1ブロックへ割り当てます。その後、元画像で上下または左右の線に挟まれていた空白が埋まった場所を候補にし、元の細い直線に当たらず、消した後も周囲8近傍の線がつながる場合だけ、ブロックを空白へ戻します。画素を追加して隙間を埋める処理ではありません。

線が切れる補正、細線の削除、孤立ドットや端点の削除は避けます。これにより、すべての隙間が必ず戻るわけではありません。指定ドット幅で線と隙間の両方を維持できない箇所は、無理に削除しません。全日本語字形での可読性改善は実機確認が必要です。

GUI3・1画面pxは隙間保護の後処理の対象外とし、良好と報告された0.4.4の処理を維持しています。文字全体のサイズ・送り幅、美咲の元画像、ライセンス表示も維持します。

「1px」はマス目の幅であり、線幅を必ず1pxにする意味ではありません。白いマスが隣接すれば線は2px以上になります。今回は一律に線を細くする機能ではなく、元の空白を安全に戻す処理です。

## 導入と確認

旧JARをfonttweaks-0.4.5+26.3.jarへ置き換えます。既存設定は保持します。GUI3・文字倍率100%・2画面pxで線間の隙間を比較し、1画面pxの表示も確認してください。Minecraft実機での確認は未実施です。

## Build

JDK25 / Gradle9.7.1: `gradle clean build`、`python scripts/verify_font_assets.py`。

単体テストでは埋まった隙間の復元、細い直線の保持、GUI3・1画面pxの既存画像との一致、GUI倍率とドット寸法、フォント選択、設定の保持を確認します。Actionsでビルドと元フォント・権利表示の一致を検証します。標準フォントを無条件に上書きするassets/minecraft/font/default.jsonは同梱しません。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
