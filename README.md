# FontTweaks

**Minecraft Java Edition 26.3 / Fabric / client-only — v0.3.2**

## ON / OFFとドット設定

- **ON（美咲）**：内蔵の美咲ゴシック第2を標準フォントの参照先として使用します。
- **OFF（標準）**：FontTweaksのフォント置換・倍率・行間変更を停止します。保存した調整値は保持します。
- **ON・GUIスケール3**：1ドットを **1 / 2 / 3画面px** から選択します。初期値は2px。倍率スライダーは無効になり、ドット指定が優先されます。
- **ON・GUI3以外**：美咲フォントを使用し、倍率スライダーが有効になります。ドット指定は無効です。
- OFF中は倍率・行間・ドット指定の操作を無効にします。
- 既存の設定キー `pixelPerfectMode` をON/OFFとして引き継ぎます。

Mod Menu → FontTweaksで設定します。GUI3のまま2pxにしたい場合は、ONと「1ドット：2画面px」を選んでください。設定変更は即時保存されます。

## v0.3.2の修正

旧版は `assets/minecraft/font/default.json` を無条件で同梱し、OFFでも美咲が残っていました。新しいJARではそのファイルを同梱せず、同じ内容を `assets/fonttweaks/font/misaki.json` に配置します。元のフォントJSON・PNGと権利表示は維持します。

`Font.getGlyphSource` の参照先をキャッシュ検索前に切り替えるため、文字幅計算と描画が同じフォントを使います。両方のフォントを通常のリソース読み込みで用意するため、切り替え時のリロードや再起動は不要な実装です。チャットは切り替え時に再折り返しします。明示的な別フォント指定は変更しません。

OFFは現在のリソースパック構成の標準フォントへ戻します。別途フォント変更パックを有効にしている場合、そのパックまでは解除しません。バニラ字形との比較時は外部フォントパックを無効にしてください。

## 導入と確認

Fabric 26.3、対応Fabric API、Mod Menuを導入し、旧FontTweaks JARを `fonttweaks-0.3.2+26.3.jar` に置き換えます。追加の美咲リソースパックは不要です。

実機確認項目（自動テストとは別途確認が必要）:

1. 外部フォントパックなし・GUI3でON/OFFを繰り返し、日本語の字形が美咲／標準に戻るか確認。
2. ONで1/2/3pxを切り替え、設定画面のサンプル、メニュー、HUD、チャットを確認。
3. 全体倍率に2/3などを保存した状態でも、OFFで標準倍率に戻るか確認。
4. ON/OFFそれぞれで再起動、リソース再読み込み、初期化を確認。
5. GUI2/4では美咲のON/OFFが動き、ドット指定が無効になるか確認。

整数ドット補正の対象は8px高のビットマップ字形です。フォールバック字形や他Modによる特殊描画の表示品質は実機確認が必要です。設定画面の操作部分は倍率変更から除外し、サンプル行にドット指定を適用します。

## Build

JDK 25 / Gradle 9.7.1: `gradle clean build`、続けて `python scripts/verify_font_assets.py`。

ビルド時のみ、既存の固定URLからMisakiGothic2forMC 26.3を取得します。起動時のダウンロードはありません。

ZIP SHA-256: `a09b2f0ea1e911fe4dd83fac39953a447a69c42fecc544d9af723a84954eee8d`

Actionsはビルド・単体テストに加え、完成JARに標準フォントの上書きが存在しないこと、元のフォント画像・JSON・権利表示とのバイト一致を検証します。ビルド成功は実機での表示確認を意味しません。

## Licenses / 権利表示

- **FontTweaks:** Copyright (c) 2026 Sanowo — MIT.
- **MisakiGothic2forMC:** Copyright (c) KabanFriends — MIT. [公式配布ページ](https://modrinth.com/resourcepack/misakigothic2formc) / [作者ページ](https://kaban.sh/minecraft/misaki2/)
- **美咲ゴシック第2:** Copyright (C) 2002-2019 Num Kadoma (門真なむ)。商用・非商用で利用・改変・再配布が許可されています。[原作者公式ページ](https://littlelimit.net/misaki.htm)

Bundled font attribution and full MIT notice are in [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) and included under `META-INF/` in the built JAR.
