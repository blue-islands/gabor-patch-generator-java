# gabor-patch-generator-java

Java 17 で動作する、視覚トレーニング用・研究用・実験用のガボールパッチ画像セット生成ツールです。  
1枚画像のバッチ生成 (`single`) と、グリッド問題画像生成 (`training`) に対応しています。

## 概要
- `single` モード: ランダムパラメータのガボールパッチを個別 PNG で複数生成
- `training` モード: グリッド状問題画像 (`puzzle.png`) と正解ファイル (`answer.json`) をセットで生成
- `--seed` 指定で再現可能

## ビルド方法
```bash
mvn compile
```

## 実行方法
```bash
mvn exec:java -Dexec.args="--mode single --count 10 --output output"
```

## コマンドライン引数
- `--mode` : `single` または `training`
- `--count` : 生成数（`single` は枚数、`training` はセット数）
- `--output` : 出力先ディレクトリ
- `--seed` : 乱数シード（省略時は毎回ランダム）
- `--gridRows` : `training` 用グリッド行数（デフォルト 3）
- `--gridCols` : `training` 用グリッド列数（デフォルト 3）
- `--cellSize` : 各セルのパッチ画像サイズ（px、デフォルト 128）

`--help` で簡易ヘルプを表示します。

## 出力例
```text
output/
  single/
    gabor_001.png
    gabor_002.png
  training/
    set_001/
      puzzle.png
      answer.json
      patch_001.png
      patch_002.png
      ...
```

## seed を使った再現
同じコマンドに同じ `--seed` を指定すると、同じ画像と同じ正解情報を再生成できます。

例:
```bash
mvn exec:java -Dexec.args="--mode training --count 2 --output output --seed 12345 --gridRows 3 --gridCols 3 --cellSize 128"
```

## サンプル実行
### 単体画像を10枚生成
```bash
mvn exec:java -Dexec.args="--mode single --count 10 --output output --cellSize 128"
```
- `output/single/gabor_001.png` 〜 `gabor_010.png` を生成

### 問題画像を5セット生成
```bash
mvn exec:java -Dexec.args="--mode training --count 5 --output output --gridRows 4 --gridCols 4 --cellSize 128"
```
- `output/training/set_001` 〜 `set_005` を生成
- 各セットに `puzzle.png` と `answer.json` を出力

## 主要クラス
- `Main`: エントリポイント。引数解析とモード分岐を担当
- `GaborPatchParams`: 1枚生成に必要なパラメータを保持
- `EnvelopeType`: エンベロープ種別 enum
- `GaborPatchGenerator`: パッチ生成と PNG 保存
- `TrainingImageGenerator`: グリッド問題画像と正解 JSON の生成
