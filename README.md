# gabor-patch-generator-java

Java 17 向けのガボールパッチ画像生成ツールです。  
**Jar実行**で、神経衰弱のような「2枚1組ペア探し」問題画像を生成できます。

## 概要
- `training` モード: 1枚の `puzzle.png` にガボールパッチをランダム配置
- 各模様は **2枚1組** で出現（memory pair）
- 配置枚数は `training.image.count` で指定（偶数必須）
- `answer.json` に各ペアの正解位置を保存
- `seed` 指定で再現可能（未指定なら毎回ランダム）

## プロパティファイルについて
- デフォルト設定ファイル `config.properties` は **Jar に梱包**されます（`src/main/resources/config.properties`）。
- 引数なしで実行した場合、Jar内の `config.properties` を読み込みます。
- `--config` を指定した場合は外部ファイルを優先します。

## ビルド（Jar作成）
```bash
mvn package
```

生成物:
- `target/gabor-patch-generator-java-1.0.0.jar`

## 実行
### 1) Jar内の設定で実行
```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar
```

### 2) 外部プロパティを指定して実行
```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar --config /path/to/config.properties
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
- `output/training/set_001` 〜 `set_005` を生成
- 各セットに `puzzle.png` と `answer.json` を出力

## 補足
このツールは視覚トレーニング用・研究用・実験用の画像生成を目的としています。
