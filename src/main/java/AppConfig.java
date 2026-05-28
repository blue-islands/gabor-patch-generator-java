import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
    public final String mode;
    public final int setCount;
    public final String outputDir;
    public final Long seed;
    public final int gridRows;
    public final int gridCols;

    private AppConfig(String mode, int setCount, String outputDir, Long seed, int gridRows, int gridCols) {
        this.mode = mode;
        this.setCount = setCount;
        this.outputDir = outputDir;
        this.seed = seed;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
    }

    public static AppConfig load(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            return load(in);
        }
    }

    public static AppConfig loadFromClasspath(String resourceName) throws IOException {
        InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resourceName);
        if (in == null) throw new IOException("Resource not found in classpath: " + resourceName);
        try (InputStream closable = in) {
            return load(closable);
        }
    }

    private static AppConfig load(InputStream in) throws IOException {
        Properties props = new Properties();
        props.load(in);

        String mode = props.getProperty("mode", "training").trim();
        int setCount = Integer.parseInt(props.getProperty("set.count", "1"));
        String outputDir = props.getProperty("output.dir", "output").trim();
        String seedRaw = props.getProperty("seed");
        Long seed = (seedRaw == null || seedRaw.isBlank()) ? null : Long.parseLong(seedRaw.trim());
        int rows = Integer.parseInt(props.getProperty("grid.rows", "6"));
        int cols = Integer.parseInt(props.getProperty("grid.cols", "10"));

        if (setCount < 1) throw new IllegalArgumentException("set.count must be >= 1");
        if (rows < 1 || cols < 1) throw new IllegalArgumentException("grid.rows and grid.cols must be >= 1");
        if ("training".equals(mode) && ((rows * cols) % 2 != 0)) {
            throw new IllegalArgumentException("For training mode, grid.rows * grid.cols must be even (pair layout)");
        }

        return new AppConfig(mode, setCount, outputDir, seed, rows, cols);
    }
}
