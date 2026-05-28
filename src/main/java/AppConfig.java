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
    public final int cellSize;
    public final int gridRows;
    public final int gridCols;
    public final int trainingImageCount;

    private AppConfig(String mode, int setCount, String outputDir, Long seed, int cellSize,
                      int gridRows, int gridCols, int trainingImageCount) {
        this.mode = mode;
        this.setCount = setCount;
        this.outputDir = outputDir;
        this.seed = seed;
        this.cellSize = cellSize;
        this.gridRows = gridRows;
        this.gridCols = gridCols;
        this.trainingImageCount = trainingImageCount;
    }

    public static AppConfig load(Path path) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        }

        String mode = props.getProperty("mode", "training").trim();
        int setCount = Integer.parseInt(props.getProperty("set.count", "1"));
        String outputDir = props.getProperty("output.dir", "output").trim();
        String seedRaw = props.getProperty("seed");
        Long seed = (seedRaw == null || seedRaw.isBlank()) ? null : Long.parseLong(seedRaw.trim());

        int cellSize = Integer.parseInt(props.getProperty("cell.size", "128"));
        int rows = Integer.parseInt(props.getProperty("grid.rows", "3"));
        int cols = Integer.parseInt(props.getProperty("grid.cols", "4"));
        int trainingImageCount = Integer.parseInt(props.getProperty("training.image.count", String.valueOf(rows * cols)));

        if (setCount < 1) throw new IllegalArgumentException("set.count must be >= 1");
        if (cellSize < 32) throw new IllegalArgumentException("cell.size must be >= 32");
        if (rows < 1 || cols < 1) throw new IllegalArgumentException("grid.rows and grid.cols must be >= 1");
        if (trainingImageCount < 2 || trainingImageCount % 2 != 0) {
            throw new IllegalArgumentException("training.image.count must be an even number >= 2");
        }
        if (rows * cols < trainingImageCount) {
            throw new IllegalArgumentException("grid.rows * grid.cols must be >= training.image.count");
        }

        return new AppConfig(mode, setCount, outputDir, seed, cellSize, rows, cols, trainingImageCount);
    }
}
