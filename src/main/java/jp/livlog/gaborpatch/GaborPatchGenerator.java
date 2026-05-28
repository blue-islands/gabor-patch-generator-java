package jp.livlog.gaborpatch;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class GaborPatchGenerator {

    public BufferedImage generateImage(GaborPatchParams params) {
        int size = params.size;
        double orient = Math.toRadians(params.orientationDeg);
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        for (int rx = 0; rx < size; rx++) {
            for (int ry = 0; ry < size; ry++) {
                double dx = rx - 0.5 * size;
                double dy = ry - 0.5 * size;

                double t = Math.atan2(dy, dx) + orient;
                double r = Math.sqrt(dx * dx + dy * dy);
                double x = r * Math.cos(t);
                double y = r * Math.sin(t);

                double amp = 0.5 + 0.5 * Math.cos(2.0 * Math.PI * (x * params.frequency + params.phase));
                amp = applyContrast(amp, params.contrast);

                double f = envelopeValue(params.envelopeType, x, y, r, size, params.standardDeviation);

                Color c1 = params.foregroundColor;
                Color c2 = params.stripeColor;
                Color bg = params.backgroundColor;

                double rr = c1.getRed() * amp + c2.getRed() * (1.0 - amp);
                double gg = c1.getGreen() * amp + c2.getGreen() * (1.0 - amp);
                double bb = c1.getBlue() * amp + c2.getBlue() * (1.0 - amp);

                rr = rr * f + bg.getRed() * (1.0 - f);
                gg = gg * f + bg.getGreen() * (1.0 - f);
                bb = bb * f + bg.getBlue() * (1.0 - f);

                int a = 255;
                int rOut = clamp255((int) Math.round(rr));
                int gOut = clamp255((int) Math.round(gg));
                int bOut = clamp255((int) Math.round(bb));

                int argb = ((a & 0xff) << 24)
                        | ((rOut & 0xff) << 16)
                        | ((gOut & 0xff) << 8)
                        | (bOut & 0xff);

                image.setRGB(rx, ry, argb);
            }
        }

        return image;
    }

    public void saveImage(GaborPatchParams params, File outFile) throws IOException {
        BufferedImage image = generateImage(params);
        ImageIO.write(image, "png", outFile);
    }

    private static double applyContrast(double amp, double contrast) {
        double centered = amp - 0.5;
        double scaled = centered * contrast;
        return Math.max(0.0, Math.min(1.0, scaled + 0.5));
    }

    private static double envelopeValue(EnvelopeType env, double x, double y, double r, int size, double std) {
        switch (env) {
            case GAUSSIAN:
                return Math.exp(-0.5 * Math.pow(x / std, 2) - 0.5 * Math.pow(y / std, 2));
            case LINEAR:
                return Math.max(0.0, ((0.5 * size) - r) / (0.5 * size));
            case COS:
                if (r > size / 2.0) {
                    return 0.0;
                }
                return Math.cos((Math.PI * (r + size / 2.0)) / (size - 1.0) - Math.PI / 2.0);
            case HANN:
                if (r > size / 2.0) {
                    return 0.0;
                }
                return 0.5 * (1.0 - Math.cos((2.0 * Math.PI * (r + size / 2.0)) / (size - 1.0)));
            case HAMMING:
                if (r > size / 2.0) {
                    return 0.0;
                }
                return 0.54 - 0.46 * Math.cos((2.0 * Math.PI * (r + size / 2.0)) / (size - 1.0));
            case CIRCLE:
                return (r > 0.5 * size) ? 0.0 : 1.0;
            case NONE:
            default:
                return 1.0;
        }
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
