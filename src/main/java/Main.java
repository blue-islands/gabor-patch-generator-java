import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            Path configPath = parseConfigPath(args);
            AppConfig cfg = AppConfig.load(configPath);
            Random random = cfg.seed == null ? new SecureRandom() : new Random(cfg.seed);

            GaborPatchGenerator patchGenerator = new GaborPatchGenerator();
            if ("single".equals(cfg.mode)) {
                generateSingle(cfg, random, patchGenerator);
            } else if ("training".equals(cfg.mode)) {
                generateTraining(cfg, random, patchGenerator);
            } else {
                throw new IllegalArgumentException("Unsupported mode: " + cfg.mode);
            }

            System.out.println("Done. mode=" + cfg.mode + " config=" + configPath);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Usage: java -cp out Main --config config.properties");
            System.exit(1);
        }
    }

    private static Path parseConfigPath(String[] args) {
        if (args.length == 2 && "--config".equals(args[0])) {
            return Path.of(args[1]);
        }
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            System.out.println("Usage: java -cp out Main --config config.properties");
            System.exit(0);
        }
        throw new IllegalArgumentException("--config is required");
    }

    static GaborPatchParams randomParams(Random random, int size) {
        double orientation = random.nextDouble() * 180.0;
        double freq = 0.02 + random.nextDouble() * 0.12;
        double phase = random.nextDouble();
        EnvelopeType envelope = EnvelopeType.random(random);
        double std = Math.max(4.0, size * (0.10 + random.nextDouble() * 0.20));
        double contrast = 0.4 + random.nextDouble() * 0.6;

        Color bg = randomPastel(random);
        Color fg = randomColor(random);
        Color stripe = randomColor(random);

        return new GaborPatchParams(orientation, freq, phase, envelope, std, fg, stripe, bg, size, contrast);
    }

    private static void generateSingle(AppConfig cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "single");
        ensureDir(dir);
        for (int i = 0; i < cfg.setCount; i++) {
            GaborPatchParams p = randomParams(random, cfg.cellSize);
            File out = new File(dir, String.format("gabor_%03d.png", i + 1));
            generator.saveImage(p, out);
        }
    }

    private static void generateTraining(AppConfig cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "training");
        ensureDir(dir);
        TrainingImageGenerator training = new TrainingImageGenerator(generator);
        for (int i = 0; i < cfg.setCount; i++) {
            File setDir = new File(dir, String.format("set_%03d", i + 1));
            training.generateMemoryPairSet(setDir, cfg.gridRows, cfg.gridCols, cfg.trainingImageCount, cfg.cellSize, random, cfg.seed, i + 1);
        }
    }

    private static void ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to create output directory: " + dir.getAbsolutePath());
        }
    }

    private static Color randomColor(Random random) {
        return new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    private static Color randomPastel(Random random) {
        int base = 180;
        return new Color(base + random.nextInt(76), base + random.nextInt(76), base + random.nextInt(76));
    }
}
