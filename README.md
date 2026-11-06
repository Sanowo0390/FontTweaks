# FontTweaks 0.5.0

Minecraft 26.3 / Fabric / client-only

## フォントと除外文字

Mod Menu → FontTweaks → 「フォント・除外文字」を開きます。

- **リソースパック適用：ON** — 内蔵のPixelMplus12 Regularを使用します。ピクセル加工がOFFでも適用できます。初期値はOFFです。
- **英字フォントを置き換える：ON/OFF** — フォント置換に半角英数字・記号とラテン文字を含めるか選びます。OFFが初期値で、その文字は元のフォントを使います。日本語の適用には影響しません。
- **除外する文字（ブラックリスト）** — 例として「！？★」を入力すると、その3文字はフォント置換とUnifontのピクセル加工から除外します。文字は消さず、元のフォントで表示します。区切り記号や正規表現ではなく、入力された各文字が対象です。空欄なら除外なしです。補助文字・絵文字もコードポイント単位で扱います。
- 「完了」またはEscで戻ると、変更があればリソースを再読み込みし、キャッシュ済みの文字にも反映します。設定はconfig/fonttweaks.jsonへ保存します。
- 初期化は「フォント・除外文字」ページに移しました。文字倍率・行間・加工・フォントと除外文字をすべて初期値へ戻します。

適用の優先順は、明示的なカスタムフォントと除外文字の保持 → 英字OFFの保持 → PixelMplus12 → 従来の美咲自動選択 → 標準フォントです。PixelMplus未収録の文字は標準フォントへフォールバックします。英字OFFはフォントの置換範囲を指定する項目で、Unifont加工からも外したい文字はブラックリストへ登録します。

## PixelMplus12 Regular

作者の公式TTFを改変せず同梱します。固定コミットd89b95f04eeb95bf98bb09cb9234a2f99d186df5からビルド時だけ取得し、SHA-256を検証します。起動時のダウンロードはありません。

SHA-256: `02f19467ea7cc235cc06c570b7f6c3b0a12a6f682bc8d74c43f2d323d97bcd12`

独立したfonttweaks:pixelmplus12の名前で読み込み、文字の描画と幅計算の両方で選択します。標準フォントを無条件に上書きするassets/minecraft/font/default.jsonは同梱しません。

TTFの設定はsize 8、oversample 3です。アウトラインの実機での見え方、ベースライン、混在時の行間・文字幅は未検証です。PixelMplus使用中のドット感はPixelMplusの字形とレンダリングによるもので、Unifont用の画面px加工設定では変化しません。フォントを切り替えるため、字幅・折り返しが変わる場合があります。

## ピクセル加工

加工ON/OFFと文字サイズ・行間は独立しています。1ドットは1〜4画面pxから選びます。表示px数は文字倍率100%時の基準です。倍率を変更するとドットも拡大縮小します。

PixelMplus適用OFFでは、標準ビットマップ英字とドット幅が一致する場合、美咲を自動選択します。GUI3なら3画面px、GUI4なら4画面pxです（加工ON・Unicodeフォント強制OFF）。外部フォント画像を解析する判定ではありません。英字OFF・ブラックリストはこの美咲選択にも適用します。

不一致ならUnifontを加工します。元の画素の中心を1つの出力ブロックに割り当て、元の細い空白を戻せる箇所では隙間保護を行います。すべての隙間を維持できる保証はありません。GUI3・1画面pxの加工方式は0.4.4と同じです。GUI4・2画面pxは元のUnifontと同じドット幅です。

## Build / 検証

JDK25 / Gradle9.7.1: `gradle clean build`、`python scripts/verify_font_assets.py`。

単体テストでフォント選択、英字の範囲、ブラックリスト優先、補助文字、設定の往復、除外文字のアトラス描画時の加工抑止、既存の隙間保護を確認します。Actionsでは元の美咲データ、PixelMplusの正確なハッシュ、権利表示を配布JARから検証します。

Minecraft実機での混在表示、設定画面の配置、Mixin適用とリロード後の挙動は別途確認が必要です。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.

- **PixelMplus12 Regular:** Copyright (C) 2013 itouhiro / Copyright (C) 2002-2013 M+ FONTS PROJECT — M+ FONT LICENSE。公式: https://github.com/itouhiro/PixelMplus 。ライセンス全文はTHIRD_PARTY_NOTICES.mdとJAR内META-INF/に同梱します。
