public enum EnvelopeType {
    GAUSSIAN,
    LINEAR,
    COS,
    HANN,
    HAMMING,
    CIRCLE,
    NONE;

    public static EnvelopeType random(java.util.Random random) {
        EnvelopeType[] values = values();
        return values[random.nextInt(values.length)];
    }
}
