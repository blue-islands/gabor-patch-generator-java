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
import java.util.List;
import java.util.Random;

public class TrainingImageGenerator {
    private final GaborPatchGenerator patchGenerator;

    public TrainingImageGenerator(GaborPatchGenerator patchGenerator) {
        this.patchGenerator = patchGenerator;
    }

    public void generateSamePairSet(File setDir, int rows, int cols, int cellSize, Random random, Long seed, int setIndex) throws IOException {
        if (!setDir.exists() && !setDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + setDir.getAbsolutePath());
        }

        int total = rows * cols;
        if (total < 2) {
            throw new IllegalArgumentException("rows * cols must be >= 2");
        }

        int first = random.nextInt(total);
        int second;
        do {
            second = random.nextInt(total);
        } while (second == first);

        GaborPatchParams shared = Main.randomParams(random, cellSize);
        List<GaborPatchParams> paramsList = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            if (i == first || i == second) {
                paramsList.add(cloneParams(shared));
            } else {
                paramsList.add(Main.randomParams(random, cellSize));
            }
        }

        List<BufferedImage> images = new ArrayList<>();
        for (int i = 0; i < total; i++) {
            BufferedImage img = patchGenerator.generateImage(paramsList.get(i));
            images.add(img);
            File patchFile = new File(setDir, String.format("patch_%03d.png", i + 1));
            ImageIO.write(img, "png", patchFile);
        }

        BufferedImage puzzle = composeGrid(images, rows, cols, cellSize);
        ImageIO.write(puzzle, "png", new File(setDir, "puzzle.png"));

        List<Integer> correct = new ArrayList<>();
        correct.add(first);
        correct.add(second);
        Collections.sort(correct);

        Files.writeString(
                new File(setDir, "answer.json").toPath(),
                buildAnswerJson("same_pair", seed, rows, cols, setIndex, correct, paramsList),
                StandardCharsets.UTF_8
        );
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

        for (int i = 0; i < images.size(); i++) {
            int r = i / cols;
            int c = i % cols;
            int x = margin + c * (cellSize + gap);
            int y = margin + r * (cellSize + gap);
            g.drawImage(images.get(i), x, y, null);
            g.setColor(new Color(90, 90, 90));
            g.drawRect(x, y, cellSize, cellSize);
            g.drawString(String.valueOf(i), x + 4, y + 16);
        }
        g.dispose();
        return canvas;
    }

    private static String buildAnswerJson(String mode, Long seed, int rows, int cols, int setIndex,
                                          List<Integer> correctPositions, List<GaborPatchParams> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"mode\": \"").append(mode).append("\",\n");
        sb.append("  \"seed\": ").append(seed == null ? "null" : seed).append(",\n");
        sb.append("  \"setIndex\": ").append(setIndex).append(",\n");
        sb.append("  \"rows\": ").append(rows).append(",\n");
        sb.append("  \"cols\": ").append(cols).append(",\n");
        sb.append("  \"correctPositions\": [").append(correctPositions.get(0)).append(", ").append(correctPositions.get(1)).append("],\n");
        sb.append("  \"patches\": [\n");
        for (int i = 0; i < params.size(); i++) {
            GaborPatchParams p = params.get(i);
            sb.append("    {\"index\": ").append(i)
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
