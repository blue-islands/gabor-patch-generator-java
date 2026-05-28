import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        try {
            Config cfg = Config.parse(args);
            Random random = cfg.seed == null ? new SecureRandom() : new Random(cfg.seed);

            GaborPatchGenerator patchGenerator = new GaborPatchGenerator();
            if ("single".equals(cfg.mode)) {
                generateSingle(cfg, random, patchGenerator);
            } else if ("training".equals(cfg.mode)) {
                generateTraining(cfg, random, patchGenerator);
            } else {
                throw new IllegalArgumentException("Unsupported mode: " + cfg.mode);
            }
            System.out.println("Done. mode=" + cfg.mode + " output=" + cfg.outputDir.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Use --help to see available options.");
            System.exit(1);
        }
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

    private static void generateSingle(Config cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "single");
        ensureDir(dir);
        for (int i = 0; i < cfg.count; i++) {
            GaborPatchParams p = randomParams(random, cfg.cellSize);
            File out = new File(dir, String.format("gabor_%03d.png", i + 1));
            generator.saveImage(p, out);
        }
    }

    private static void generateTraining(Config cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "training");
        ensureDir(dir);
        TrainingImageGenerator training = new TrainingImageGenerator(generator);
        for (int i = 0; i < cfg.count; i++) {
            File setDir = new File(dir, String.format("set_%03d", i + 1));
            training.generateSamePairSet(setDir, cfg.gridRows, cfg.gridCols, cfg.cellSize, random, cfg.seed, i + 1);
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

    private static class Config {
        String mode = "single";
        int count = 1;
        File outputDir = new File("output");
        Long seed = null;
        int gridRows = 3;
        int gridCols = 3;
        int cellSize = 128;

        static Config parse(String[] args) {
            Map<String, String> kv = new HashMap<>();
            for (int i = 0; i < args.length; i++) {
                String a = args[i];
                if ("--help".equals(a) || "-h".equals(a)) {
                    printHelpAndExit();
                }
                if (!a.startsWith("--") || i + 1 >= args.length) {
                    throw new IllegalArgumentException("Invalid argument: " + a);
                }
                kv.put(a.substring(2), args[++i]);
            }

            Config c = new Config();
            if (kv.containsKey("mode")) c.mode = kv.get("mode");
            if (kv.containsKey("count")) c.count = Integer.parseInt(kv.get("count"));
            if (kv.containsKey("output")) c.outputDir = new File(kv.get("output"));
            if (kv.containsKey("seed")) c.seed = Long.parseLong(kv.get("seed"));
            if (kv.containsKey("gridRows")) c.gridRows = Integer.parseInt(kv.get("gridRows"));
            if (kv.containsKey("gridCols")) c.gridCols = Integer.parseInt(kv.get("gridCols"));
            if (kv.containsKey("cellSize")) c.cellSize = Integer.parseInt(kv.get("cellSize"));

            if (c.count < 1) throw new IllegalArgumentException("count must be >= 1");
            if (c.gridRows < 1 || c.gridCols < 1) throw new IllegalArgumentException("gridRows/gridCols must be >= 1");
            if (c.cellSize < 32) throw new IllegalArgumentException("cellSize must be >= 32");
            return c;
        }

        static void printHelpAndExit() {
            System.out.println("Usage:");
            System.out.println("  --mode single|training --count N --output DIR [--seed S] [--gridRows R --gridCols C --cellSize PX]");
            System.exit(0);
        }
    }
}
