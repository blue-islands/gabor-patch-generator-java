import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TrainingImageGenerator {
    private final GaborPatchGenerator patchGenerator;

    public TrainingImageGenerator(GaborPatchGenerator patchGenerator) {
        this.patchGenerator = patchGenerator;
    }

    public void generateMemoryPairSet(File setDir, int rows, int cols, int imageCount, int cellSize,
                                      Random random, Long seed, int setIndex) throws IOException {
        if (!setDir.exists() && !setDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + setDir.getAbsolutePath());
        }

        int pairCount = imageCount / 2;
        List<GaborPatchParams> pairs = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            pairs.add(Main.randomParams(random, cellSize));
        }

        List<GaborPatchParams> placed = new ArrayList<>();
        List<Integer> pairIds = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            placed.add(cloneParams(pairs.get(i)));
            placed.add(cloneParams(pairs.get(i)));
            pairIds.add(i);
            pairIds.add(i);
        }

        shuffleInSync(placed, pairIds, random);

        List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < placed.size(); i++) {
            BufferedImage img = patchGenerator.generateImage(placed.get(i));
            images.add(img);
            ImageIO.write(img, "png", new File(setDir, String.format("patch_%03d.png", i + 1)));
        }

        BufferedImage puzzle = composeGrid(images, rows, cols, cellSize);
        ImageIO.write(puzzle, "png", new File(setDir, "puzzle.png"));

        Files.writeString(new File(setDir, "answer.json").toPath(),
                buildAnswerJson(seed, setIndex, rows, cols, imageCount, pairIds, placed), StandardCharsets.UTF_8);
    }

    private static void shuffleInSync(List<GaborPatchParams> params, List<Integer> pairIds, Random random) {
        for (int i = params.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Collections.swap(params, i, j);
            Collections.swap(pairIds, i, j);
        }
    }

    private BufferedImage composeGrid(List<BufferedImage> images, int rows, int cols, int cellSize) {
        int margin = 16;
        int gap = 10;
        int width = margin * 2 + cols * cellSize + (cols - 1) * gap;
        int height = margin * 2 + rows * cellSize + (rows - 1) * gap;

        BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = canvas.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(235, 235, 235));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.setStroke(new BasicStroke(2f));

        for (int i = 0; i < rows * cols; i++) {
            int r = i / cols;
            int c = i % cols;
            int x = margin + c * (cellSize + gap);
            int y = margin + r * (cellSize + gap);

            if (i < images.size()) {
                g.drawImage(images.get(i), x, y, null);
            } else {
                g.setColor(new Color(210, 210, 210));
                g.fillRect(x, y, cellSize, cellSize);
            }
            g.setColor(new Color(90, 90, 90));
            g.drawRect(x, y, cellSize, cellSize);
            g.drawString(String.valueOf(i), x + 4, y + 16);
        }

        g.dispose();
        return canvas;
    }

    private static String buildAnswerJson(Long seed, int setIndex, int rows, int cols, int imageCount,
                                          List<Integer> pairIds, List<GaborPatchParams> params) {
        Map<Integer, List<Integer>> pairPositions = new HashMap<>();
        for (int i = 0; i < pairIds.size(); i++) {
            pairPositions.computeIfAbsent(pairIds.get(i), k -> new ArrayList<>()).add(i);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"mode\": \"memory_pairs\",\n");
        sb.append("  \"seed\": ").append(seed == null ? "null" : seed).append(",\n");
        sb.append("  \"setIndex\": ").append(setIndex).append(",\n");
        sb.append("  \"rows\": ").append(rows).append(",\n");
        sb.append("  \"cols\": ").append(cols).append(",\n");
        sb.append("  \"imageCount\": ").append(imageCount).append(",\n");
        sb.append("  \"pairs\": [\n");

        List<Integer> keys = new ArrayList<>(pairPositions.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            int id = keys.get(i);
            List<Integer> pos = pairPositions.get(id);
            sb.append("    {\"pairId\": ").append(id).append(", \"positions\": [")
                    .append(pos.get(0)).append(", ").append(pos.get(1)).append("]}");
            sb.append(i == keys.size() - 1 ? "\n" : ",\n");
        }
        sb.append("  ],\n");
        sb.append("  \"patches\": [\n");
        for (int i = 0; i < params.size(); i++) {
            GaborPatchParams p = params.get(i);
            sb.append("    {\"index\": ").append(i)
                    .append(", \"pairId\": ").append(pairIds.get(i))
                    .append(", \"orientationDeg\": ").append(format(p.orientationDeg))
                    .append(", \"frequency\": ").append(format(p.frequency))
                    .append(", \"phase\": ").append(format(p.phase))
                    .append(", \"envelope\": \"").append(p.envelopeType).append("\"")
                    .append(", \"standardDeviation\": ").append(format(p.standardDeviation))
                    .append(", \"contrast\": ").append(format(p.contrast))
                    .append("}");
            sb.append(i == params.size() - 1 ? "\n" : ",\n");
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.6f", value);
    }

    private static GaborPatchParams cloneParams(GaborPatchParams p) {
        return new GaborPatchParams(
                p.orientationDeg, p.frequency, p.phase, p.envelopeType, p.standardDeviation,
                p.foregroundColor, p.stripeColor, p.backgroundColor, p.size, p.contrast
        );
    }
}
