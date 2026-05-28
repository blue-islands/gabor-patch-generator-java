# gabor-patch-generator-java

Java 17 向けのガボールパッチ画像生成ツールです。  
**プロパティファイルで設定し、神経衰弱のような「2枚1組ペア探し」問題画像を生成**できます。

## 概要
- `training` モード: 1枚の `puzzle.png` にガボールパッチをランダム配置
- 各模様は **2枚1組** で出現（memory pair）
- 配置枚数は `training.image.count` で指定（偶数必須）
- `answer.json` に各ペアの正解位置を保存
- `seed` 指定で再現可能（未指定なら毎回ランダム）

## ビルド
```bash
javac -d out src/main/java/*.java
```
- `output/training/set_001` 〜 `set_005` を生成
- 各セットに `puzzle.png` と `answer.json` を出力

## 実行
```bash
java -cp out Main --config config.properties
```

## 設定ファイル（config.properties）
主な項目:
- `mode=training|single`
- `set.count=2` （training ならセット数、single なら出力枚数）
- `output.dir=output`
- `seed=12345` （空欄ならランダム）
- `cell.size=128`
- `grid.rows=4`
- `grid.cols=4`
- `training.image.count=12` （偶数、かつ `grid.rows*grid.cols` 以下）

## 出力例
```text
output/
  training/
    set_001/
      puzzle.png
      answer.json
      patch_001.png
      patch_002.png
      ...
```

## サンプル
1. `config.properties` の `training.image.count=12` を設定
2. 実行:
   ```bash
   java -cp out Main --config config.properties
   ```
3. 生成物:
   - `output/training/set_001/puzzle.png`
   - `output/training/set_001/answer.json`
   - `output/training/set_001/patch_001.png` ...

## 補足
このツールは視覚トレーニング用・研究用・実験用の画像生成を目的としています。
