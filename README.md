# gabor-patch-generator-java

添付イメージのような、**グレースケールのガボールパッチ一覧画像**（神経衰弱向けペア配置）を生成する Java 17 ツールです。

## ポイント
- カラフル出力ではなく、黒〜白 + 明るいグレー背景で生成
- `training` モードは **rows × cols の全セル**を使用
- **指定するのは縦横数のみ**（`grid.rows`, `grid.cols`）
- パッチサイズは自動計算（画像全体サイズに収まるように調整）
- Jar に `config.properties` を梱包済み

## ビルド
```bash
mvn package
```

## 実行
### 同梱設定で実行
```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar
```

### 外部設定で実行
```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar --config /path/to/config.properties
```

## config.properties
```properties
mode=training
set.count=1
output.dir=output
seed=
grid.rows=7
grid.cols=11
```

- `training` モードでは `grid.rows * grid.cols` は偶数にしてください（2枚1組のため）。

## 出力
```text
output/
  training/
    set_001/
      puzzle.png
      answer.json
      patch_001.png ...
```
