# gabor-patch-generator-java

[日本語](#日本語) | [English](#english)

---

## 日本語

### 概要

`gabor-patch-generator-java` は、**グレースケールのガボールパッチ**を Java 17 で生成するツールです。単体のパッチ画像に加えて、神経衰弱・タッチゲーム・印刷教材などに使いやすい、同じ模様を 2 枚ずつ含む一覧画像と答え合わせ用 JSON を出力できます。

ガボールパッチは、ガボール変換に由来する縞模様で、角度・周波数・位相・コントラストなどの違いによって見た目が変わります。本ツールでは、黒〜白の縞を明るいグレー背景に配置し、視覚認知トレーニングやゲーム素材として扱いやすい画像セットを自動生成します。

> 注意: 本ツールは画像生成ツールです。視力回復などの医学的効果を保証するものではありません。長時間の利用で目が疲れる場合があるため、休憩を取りながら使用してください。

### 何が作れるか

- **ガボールパッチの一覧画像**: 指定した行数・列数のグリッドに、ランダムなガボールパッチを配置します。
- **ペア配置のトレーニング画像**: `training` モードでは、同じ模様を 2 枚 1 組で配置するため、神経衰弱や同じ模様探しに使えます。
- **答え合わせ用 JSON**: 各セルのペア ID とパラメータを出力するため、ゲーム実装や検証に利用できます。
- **再現可能な画像セット**: `seed` を指定すると、同じ条件で同じ画像セットを再生成できます。

### 想定用途

- ガボールパッチを使った神経衰弱ゲームの素材作成
- 同じ模様を探してタップする視覚認知ゲームの問題画像作成
- 紙に印刷して使うトレーニングシートや教材の作成
- ガボールパッチ生成ロジックの検証・サンプル出力
- Java アプリケーションや Web アプリに組み込むための画像素材生成

### 特長

- カラフル出力ではなく、黒〜白 + 明るいグレー背景で生成
- `training` モードは **rows × cols の全セル**を使用
- **指定するのは縦横数のみ**（`grid.rows`, `grid.cols`）
- パッチサイズは自動計算（画像全体サイズに収まるように調整）
- Jar に `config.properties` を梱包済み
- タイムスタンプ付きの `puzzle_*.png` と `answer_*.json` を出力

### 使い方

#### ビルド

```bash
mvn package
```

#### 同梱設定で実行

```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar
```

#### 外部設定で実行

```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar --config /path/to/config.properties
```

### 設定例: `config.properties`

```properties
mode=training
set.count=1
output.dir=output
seed=
grid.rows=7
grid.cols=11
```

- `mode=training` は、ペア入りの一覧画像を生成します。
- `set.count` は、生成するセット数です。
- `output.dir` は、出力先ディレクトリです。
- `seed` を空にすると毎回ランダム、数値を入れると再現可能な出力になります。
- `training` モードでは `grid.rows * grid.cols` は偶数にしてください（2 枚 1 組のため）。

### 出力

```text
output/
  training/
    set_001/
      puzzle_yyyymmddhhmmss.png
      answer_yyyymmddhhmmss.json
      (work/ は自動削除)
```

### 遊び方の例

1. `puzzle_*.png` を画面に表示、または印刷します。
2. 同じ模様に見えるガボールパッチのペアを探します。
3. ゲーム化する場合は、カードを裏返しにして 2 枚ずつ選ぶ神経衰弱形式にできます。
4. 答え合わせやアプリ実装では、`answer_*.json` の `pairId` を利用します。
5. 目が疲れないよう、短時間で区切って遊ぶことをおすすめします。

---

## English

### Overview

`gabor-patch-generator-java` is a Java 17 tool for generating **grayscale Gabor patch** images. It can create individual patches as well as grid-based training sheets that contain two copies of each pattern, plus an answer JSON file for checking pairs or wiring the output into a game.

A Gabor patch is a striped visual pattern derived from the Gabor transform. Its appearance changes according to parameters such as orientation, frequency, phase, and contrast. This tool generates black-to-white stripe patterns on a light gray background, making the output easy to use as visual-cognition training material or game assets.

> Note: This project is an image-generation tool. It does not guarantee medical benefits such as eyesight improvement. Because extended use may cause eye fatigue, use the generated images with breaks.

### What this tool generates

- **Gabor patch grid images**: Randomized Gabor patches arranged in a user-defined row/column grid.
- **Paired training sheets**: In `training` mode, each pattern appears exactly twice, making the sheet suitable for memory matching or “find the same pattern” games.
- **Answer JSON files**: Each cell includes pair IDs and generation parameters, which can be used for validation or game implementation.
- **Reproducible sets**: Set `seed` to regenerate the same image set under the same conditions.

### Use cases

- Creating assets for a Gabor patch memory matching game
- Building tap-based visual search games where players find identical patterns
- Printing worksheets or training sheets
- Testing and inspecting Gabor patch generation logic
- Generating image assets for Java or web applications

### Features

- Generates grayscale images rather than colorful output
- Uses a black-to-white patch on a light gray background
- `training` mode fills **all rows × cols cells**
- Only the grid dimensions are required (`grid.rows`, `grid.cols`)
- Patch size is calculated automatically to fit the full output image
- Bundles `config.properties` inside the Jar
- Writes timestamped `puzzle_*.png` and `answer_*.json` files

### Usage

#### Build

```bash
mvn package
```

#### Run with the bundled configuration

```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar
```

#### Run with an external configuration

```bash
java -jar target/gabor-patch-generator-java-1.0.0.jar --config /path/to/config.properties
```

### Example `config.properties`

```properties
mode=training
set.count=1
output.dir=output
seed=
grid.rows=7
grid.cols=11
```

- `mode=training` generates a paired grid image.
- `set.count` controls how many sets are generated.
- `output.dir` controls where files are written.
- Leave `seed` empty for random output, or set a number for reproducible output.
- In `training` mode, `grid.rows * grid.cols` must be even because each pattern is generated as a pair.

### Output

```text
output/
  training/
    set_001/
      puzzle_yyyymmddhhmmss.png
      answer_yyyymmddhhmmss.json
      (work/ is removed automatically)
```

### Example ways to play

1. Display `puzzle_*.png` on a screen or print it.
2. Find pairs of Gabor patches that look identical.
3. To make a game, hide the patches as cards and let players flip two cards at a time, like a memory matching game.
4. Use `pairId` in `answer_*.json` to check answers or implement game logic.
5. Keep sessions short and take breaks to avoid eye fatigue.
