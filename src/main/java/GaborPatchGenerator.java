import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GaborPatchGenerator {

    public enum Envelope {
        GAUSSIAN,
        LINEAR,
        COS,
        HANN,
        HAMMING,
        CIRCLE,
        NONE
    }

    public static void main(String[] args) throws IOException {
        // サンプル実行
        generateGabor(
                45.0,                       // orientation (degrees)
                256,                        // image size (pixels)
                Envelope.GAUSSIAN,          // envelope
                32.0,                       // std dev
                0.05,                       // spatial frequency
                0.0,                        // phase
                new int[]{255, 255, 255},   // background color RGB
                new int[]{0, 0, 0},         // color1 RGB
                new int[]{255, 255, 255},   // color2 RGB
                new File("gabor.png")       // output file
        );

        System.out.println("画像を生成しました: gabor.png");
    }

    /**
     * Gabor patch を生成して PNG 保存する
     *
     * @param orientDeg orientation in degrees
     * @param size      image size in pixels
     * @param env       envelope
     * @param std       standard deviation (for gaussian)
     * @param freq      spatial frequency
     * @param phase     phase
     * @param color0    background RGB. いずれかが負なら透明背景
     * @param color1    foreground color 1 RGB
     * @param color2    foreground color 2 RGB
     * @param outFile   output PNG file
     */
    public static void generateGabor(
            double orientDeg,
            int size,
            Envelope env,
            double std,
            double freq,
            double phase,
            int[] color0,
            int[] color1,
            int[] color2,
            File outFile
    ) throws IOException {

        double orient = Math.toRadians(orientDeg);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int rx = 0; rx < size; rx++) {
            for (int ry = 0; ry < size; ry++) {

                // 中心からの相対座標
                double dx = rx - 0.5 * size;
                double dy = ry - 0.5 * size;

                // 回転座標
                double t = Math.atan2(dy, dx) + orient;
                double r = Math.sqrt(dx * dx + dy * dy);
                double x = r * Math.cos(t);
                double y = r * Math.sin(t);

                // 縞パターンの振幅（0～1）
                double amp = 0.5 + 0.5 * Math.cos(2.0 * Math.PI * (x * freq + phase));

                // envelope係数（0～1）
                double f;
                switch (env) {
                    case GAUSSIAN:
                        f = Math.exp(-0.5 * Math.pow(x / std, 2) - 0.5 * Math.pow(y / std, 2));
                        break;

                    case LINEAR:
                        f = Math.max(0.0, ((0.5 * size) - r) / (0.5 * size));
                        break;

                    case COS:
                        if (r > size / 2.0) {
                            f = 0.0;
                        } else {
                            f = Math.cos((Math.PI * (r + size / 2.0)) / (size - 1.0) - Math.PI / 2.0);
                        }
                        break;

                    case HANN:
                        if (r > size / 2.0) {
                            f = 0.0;
                        } else {
                            f = 0.5 * (1.0 - Math.cos((2.0 * Math.PI * (r + size / 2.0)) / (size - 1.0)));
                        }
                        break;

                    case HAMMING:
                        if (r > size / 2.0) {
                            f = 0.0;
                        } else {
                            f = 0.54 - 0.46 * Math.cos((2.0 * Math.PI * (r + size / 2.0)) / (size - 1.0));
                        }
                        break;

                    case CIRCLE:
                        f = (r > 0.5 * size) ? 0.0 : 1.0;
                        break;

                    case NONE:
                    default:
                        f = 1.0;
                        break;
                }

                // 2色の縞模様を補間
                double rr = color1[0] * amp + color2[0] * (1.0 - amp);
                double gg = color1[1] * amp + color2[1] * (1.0 - amp);
                double bb = color1[2] * amp + color2[2] * (1.0 - amp);

                int a;
                int rOut;
                int gOut;
                int bOut;

                // 背景色に負値が含まれていたら透明背景扱い
                if (color0[0] < 0 || color0[1] < 0 || color0[2] < 0) {
                    a = clamp255((int) Math.round(255.0 * f));
                    rOut = clamp255((int) Math.round(rr));
                    gOut = clamp255((int) Math.round(gg));
                    bOut = clamp255((int) Math.round(bb));
                } else {
                    rr = rr * f + color0[0] * (1.0 - f);
                    gg = gg * f + color0[1] * (1.0 - f);
                    bb = bb * f + color0[2] * (1.0 - f);

                    a = 255;
                    rOut = clamp255((int) Math.round(rr));
                    gOut = clamp255((int) Math.round(gg));
                    bOut = clamp255((int) Math.round(bb));
                }

                int argb = ((a & 0xff) << 24)
                        | ((rOut & 0xff) << 16)
                        | ((gOut & 0xff) << 8)
                        | (bOut & 0xff);

                image.setRGB(rx, ry, argb);
            }
        }

        ImageIO.write(image, "png", outFile);
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
