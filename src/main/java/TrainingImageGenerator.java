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

    public void generateMemoryPairSet(File setDir, int rows, int cols, Random random, Long seed, int setIndex, String timestamp) throws IOException {
        if (!setDir.exists() && !setDir.mkdirs()) throw new IOException("Failed to create directory: " + setDir.getAbsolutePath());

        int imageCount = rows * cols;
        int cellSize = autoCellSize(rows, cols);
        int pairCount = imageCount / 2;

        List<GaborPatchParams> pairs = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) pairs.add(Main.randomParams(random, cellSize));

        List<GaborPatchParams> placed = new ArrayList<>();
        List<Integer> pairIds = new ArrayList<>();
        for (int i = 0; i < pairCount; i++) {
            placed.add(cloneParams(pairs.get(i)));
            placed.add(cloneParams(pairs.get(i)));
            pairIds.add(i);
            pairIds.add(i);
        }
        shuffleInSync(placed, pairIds, random);

        File workDir = new File(setDir, "work");
        if (!workDir.exists() && !workDir.mkdirs()) throw new IOException("Failed to create work directory: " + workDir.getAbsolutePath());

        List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < placed.size(); i++) {
            BufferedImage img = patchGenerator.generateImage(placed.get(i));
            images.add(img);
            ImageIO.write(img, "png", new File(workDir, String.format("patch_%03d.png", i + 1)));
        }

        ImageIO.write(composeGrid(images, rows, cols, cellSize), "png", new File(setDir, "puzzle_" + timestamp + ".png"));
        Files.writeString(new File(setDir, "answer_" + timestamp + ".json").toPath(),
                buildAnswerJson(seed, setIndex, rows, cols, imageCount, cellSize, pairIds, placed),
                StandardCharsets.UTF_8);

        deleteDirectoryRecursively(workDir);
    }

    public static int autoCellSize(int rows, int cols) {
        int margin = 16;
        int gap = 10;
        int targetWidth = 1280;
        int targetHeight = 760;
        int maxByWidth = (targetWidth - margin * 2 - gap * (cols - 1)) / cols;
        int maxByHeight = (targetHeight - margin * 2 - gap * (rows - 1)) / rows;
        int size = Math.min(maxByWidth, maxByHeight);
        return Math.max(56, Math.min(size, 140));
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
        g.setColor(new Color(232, 232, 232));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g.setStroke(new BasicStroke(1f));

        for (int i = 0; i < images.size(); i++) {
            int r = i / cols;
            int c = i % cols;
            int x = margin + c * (cellSize + gap);
            int y = margin + r * (cellSize + gap);
            g.drawImage(images.get(i), x, y, null);
            g.setColor(new Color(170, 170, 170));
            g.drawRect(x, y, cellSize, cellSize);
            g.drawString(String.valueOf(i), x + 4, y + 14);
        }
        g.dispose();
        return canvas;
    }

    private static String buildAnswerJson(Long seed, int setIndex, int rows, int cols, int imageCount, int cellSize,
                                          List<Integer> pairIds, List<GaborPatchParams> params) {
        Map<Integer, List<Integer>> pairPositions = new HashMap<>();
        for (int i = 0; i < pairIds.size(); i++) pairPositions.computeIfAbsent(pairIds.get(i), k -> new ArrayList<>()).add(i);

        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"mode\": \"memory_pairs\",\n");
        sb.append("  \"seed\": ").append(seed == null ? "null" : seed).append(",\n");
        sb.append("  \"setIndex\": ").append(setIndex).append(",\n");
        sb.append("  \"rows\": ").append(rows).append(",\n");
        sb.append("  \"cols\": ").append(cols).append(",\n");
        sb.append("  \"imageCount\": ").append(imageCount).append(",\n");
        sb.append("  \"cellSize\": ").append(cellSize).append(",\n");
        sb.append("  \"pairs\": [\n");

        List<Integer> keys = new ArrayList<>(pairPositions.keySet());
        Collections.sort(keys);
        for (int i = 0; i < keys.size(); i++) {
            int id = keys.get(i);
            List<Integer> pos = pairPositions.get(id);
            sb.append("    {\"pairId\": ").append(id).append(", \"positions\": [").append(pos.get(0)).append(", ").append(pos.get(1)).append("]}");
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
        sb.append("  ]\n}");
        return sb.toString();
    }

    private static void deleteDirectoryRecursively(File dir) throws IOException {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteDirectoryRecursively(child);
                } else if (!child.delete()) {
                    throw new IOException("Failed to delete work file: " + child.getAbsolutePath());
                }
            }
        }
        if (!dir.delete()) {
            throw new IOException("Failed to delete work directory: " + dir.getAbsolutePath());
        }
    }

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.6f", value);
    }

    private static GaborPatchParams cloneParams(GaborPatchParams p) {
        return new GaborPatchParams(p.orientationDeg, p.frequency, p.phase, p.envelopeType, p.standardDeviation,
                p.foregroundColor, p.stripeColor, p.backgroundColor, p.size, p.contrast);
    }
}
