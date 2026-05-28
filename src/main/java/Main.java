import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Main {
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static void main(String[] args) {
        try {
            Path configPath = parseConfigPath(args);
            AppConfig cfg = (configPath == null)
                    ? AppConfig.loadFromClasspath("config.properties")
                    : AppConfig.load(configPath);
            Random random = cfg.seed == null ? new SecureRandom() : new Random(cfg.seed);

            GaborPatchGenerator patchGenerator = new GaborPatchGenerator();
            if ("single".equals(cfg.mode)) {
                generateSingle(cfg, random, patchGenerator);
            } else if ("training".equals(cfg.mode)) {
                generateTraining(cfg, random, patchGenerator);
            } else {
                throw new IllegalArgumentException("Unsupported mode: " + cfg.mode);
            }

            System.out.println("Done. mode=" + cfg.mode + " config=" + (configPath == null ? "classpath:config.properties" : configPath));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println("Usage: java -jar target/gabor-patch-generator-java-1.0.0.jar [--config path/to/config.properties]");
            System.exit(1);
        }
    }

    private static Path parseConfigPath(String[] args) {
        if (args.length == 0) return null;
        if (args.length == 2 && "--config".equals(args[0])) return Path.of(args[1]);
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            System.out.println("Usage: java -jar target/gabor-patch-generator-java-1.0.0.jar [--config path/to/config.properties]");
            System.exit(0);
        }
        throw new IllegalArgumentException("Invalid arguments. Use --help for usage.");
    }

    static GaborPatchParams randomParams(Random random, int size) {
        double orientation = random.nextDouble() * 180.0;
        double freq = 0.035 + random.nextDouble() * 0.045;
        double phase = random.nextDouble();
        EnvelopeType envelope = EnvelopeType.GAUSSIAN;
        double std = size * (0.12 + random.nextDouble() * 0.10);
        double contrast = 0.85 + random.nextDouble() * 0.15;

        Color bg = new Color(232, 232, 232);
        Color fg = new Color(10, 10, 10);
        Color stripe = new Color(250, 250, 250);

        return new GaborPatchParams(orientation, freq, phase, envelope, std, fg, stripe, bg, size, contrast);
    }

    private static void generateSingle(AppConfig cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "single");
        ensureDir(dir);
        int size = TrainingImageGenerator.autoCellSize(cfg.gridRows, cfg.gridCols);
        for (int i = 0; i < cfg.setCount; i++) {
            String ts = LocalDateTime.now().format(TS_FORMAT);
            generator.saveImage(randomParams(random, size), new File(dir, String.format("gabor_%s_%03d.png", ts, i + 1)));
        }
    }

    private static void generateTraining(AppConfig cfg, Random random, GaborPatchGenerator generator) throws IOException {
        File dir = new File(cfg.outputDir, "training");
        ensureDir(dir);
        TrainingImageGenerator training = new TrainingImageGenerator(generator);
        for (int i = 0; i < cfg.setCount; i++) {
            String ts = LocalDateTime.now().format(TS_FORMAT);
            File setDir = new File(dir, String.format("set_%03d_%s", i + 1, ts));
            training.generateMemoryPairSet(setDir, cfg.gridRows, cfg.gridCols, random, cfg.seed, i + 1, ts);
        }
    }

    private static void ensureDir(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Failed to create output directory: " + dir.getAbsolutePath());
    }
}
