package jp.livlog.gaborpatch;

import java.awt.Color;

/**
 * Parameters for generating a single Gabor patch image.
 */
public class GaborPatchParams {
    public double orientationDeg;
    public double frequency;
    public double phase;
    public EnvelopeType envelopeType;
    public double standardDeviation;
    public Color foregroundColor;
    public Color stripeColor;
    public Color backgroundColor;
    public int size;
    public double contrast;

    public GaborPatchParams(double orientationDeg,
                            double frequency,
                            double phase,
                            EnvelopeType envelopeType,
                            double standardDeviation,
                            Color foregroundColor,
                            Color stripeColor,
                            Color backgroundColor,
                            int size,
                            double contrast) {
        this.orientationDeg = orientationDeg;
        this.frequency = frequency;
        this.phase = phase;
        this.envelopeType = envelopeType;
        this.standardDeviation = standardDeviation;
        this.foregroundColor = foregroundColor;
        this.stripeColor = stripeColor;
        this.backgroundColor = backgroundColor;
        this.size = size;
        this.contrast = contrast;
    }
}
