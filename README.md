# FontTweaks 0.6.0

Minecraft 26.3 / Fabric / client-only

## 0.6.0の変更

- 数値項目の右側に「戻す」を追加しました。その項目だけを初期値100%に戻します。ピクセル加工ON/OFF、画面px数、リソースパック適用、フォント選択、英字ON/OFFにも個別の戻すボタンがあります。全体の初期化も残しています。
- 数値入力を変更した後、24bit色をARGB色として使っていた問題を修正しました。正常入力は不透明の白、不正な値は不透明の赤です。
- ブラックリストを追加・個別削除できる一覧にしました。複数文字を追加した場合もUnicodeの1コードポイントごとの行に分け、重複は登録しません。ページ送りに対応します。旧blacklistedCharactersの文字列は個々のblacklistedEntriesへ移行して保存します。
- ブラックリストはフォント置換だけの除外になりました。登録文字でもUnifontのピクセル加工は、加工ONなら適用します。加工の除外に使っていた古い焼き込みコンテキストは削除しました。
- プレビューに「文字サイズ」「行間」の切り替えを追加しました。行間は手計算したサンプルではなく、実際のMultiLineLabel、textWithWordWrap、ClientTextTooltipの経路で2行を描画します。行間の項目を編集してからプレビューを開くと行間の画面を表示します。

## 行間について

「行間全体」は標準の複数行ラベル、折り返し文章、ツールチップ、チャットの共通倍率です。「文章の行間」「説明文の行間」は共通倍率に乗算します。単一行のメニューボタン同士の距離や、他Modが固定座標で並べる一覧項目を移動する設定ではありません。

今回、隔離したFabric/MinecraftをpreLaunchまで起動し、対象クラスへのMixin適用と、実メソッドの行送り変化を確認するテストを追加しました。100%→200%で標準ラベルの行送り9→18、ツールチップ高さ10→20となることを検証します。設定ファイルの保存と再読み込みも通します。通常のゲーム画面や他Modを含めた表示はこのテストに含まれません。「全て変わらない」という実機報告の原因を確定したものではありません。

## フォント・除外文字

Mod Menu → FontTweaks → 「フォント・除外文字」で操作します。

リソースパック適用OFFでは、美咲・PixelMplusのどちらも適用しません。ONならPixelMplus12 Regular、美咲ゴシック第2、自動から選べます。自動は標準英字とドット幅が一致すれば美咲、それ以外はPixelMplusを使います。英字置換OFFなら半角英数字・記号とラテン文字は元のフォントです。

ブラックリストは「追加」で登録し、各行の「削除」で1文字ずつ削除します。登録した文字は非表示にはせず、元のフォントへ戻します。Unifontで描く文字には加工ONならピクセル加工が適用されます。Bitmap/TTFフォントはUnifont加工の対象外です。明示的なカスタムフォントは保持します。

フォントや加工の切り替え時は再読み込みを要求します。読み込み中に変更した内容も、完了後に再反映します。数値の倍率・行間設定は再読み込みなしで参照します。

## ピクセル加工とサイズ

1ドットは1〜4画面px、文字倍率100%時の基準です。サイズ変更とは独立しています。倍率を変更するとドットも拡大縮小します。Unifontの元の画素を1ブロックへ割り当て、細い空白を安全に戻せる箇所では隙間保護を行います。すべての隙間を保持できる保証はありません。GUI3・1画面pxは良好と報告された方式を維持しています。

GUI4・2画面pxは元のUnifontと同じドット幅です。PixelMplus・美咲の字形は既製フォントのため、Unifont用の画面px加工設定では変更しません。文字サイズ・行間は別途調整できます。

## Build / 検証

JDK25 / Gradle9.7.1:

- `gradle clean build`
- `gradle runSmoke -x downloadAssets` — 隔離起動テスト。ゲーム画面を開く前に終了します。
- `python scripts/verify_font_assets.py`

Actionsでも単体テスト、隔離起動テスト、配布JAR検証を行います。smoke用のMod JARはbuild/smokeに生成し、通常配布JARには同梱しません。

PixelMplus12 Regularは公式固定コミットd89b95f04eeb95bf98bb09cb9234a2f99d186df5からビルド時に取得します。SHA-256は02f19467ea7cc235cc06c570b7f6c3b0a12a6f682bc8d74c43f2d323d97bcd12です。元の美咲の画像・JSON・権利表示も維持します。assets/minecraft/font/default.jsonは同梱しません。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.

- **PixelMplus12 Regular:** Copyright (C) 2013 itouhiro / Copyright (C) 2002-2013 M+ FONTS PROJECT — M+ FONT LICENSE。公式: https://github.com/itouhiro/PixelMplus 。ライセンス全文はTHIRD_PARTY_NOTICES.mdとJAR内META-INF/に同梱します。
