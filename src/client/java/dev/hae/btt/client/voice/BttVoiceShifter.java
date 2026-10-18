package dev.hae.btt.client.voice;
















public final class BttVoiceShifter {

    private static final int MAX_WINDOW = 960;

    private static final int FADE = MAX_WINDOW / 2;

    private final float[] prevTail = new float[FADE];
    private boolean hasPrev;


    private static float[] stretch(float[] in, float ratio) {
        int n = in.length;
        int len = Math.min(MAX_WINDOW, n);
        if (len < 8) return in.clone();
        int hop = len / 2;
        int outLen = Math.round(n * ratio);
        float[] out = new float[outLen + len];
        float[] weight = new float[outLen + len];
        for (int outPos = 0; outPos < outLen; outPos += hop) {
            int src = Math.min(n - len, Math.round(outPos / ratio));
            if (src < 0) src = 0;
            for (int i = 0; i < len; i++) {
                float w = (float) (0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / (len - 1)));
                out[outPos + i] += in[src + i] * w;
                weight[outPos + i] += w;
            }
        }
        for (int i = 0; i < outLen; i++) {
            if (weight[i] > 1.0e-4f) out[i] /= weight[i];
        }
        float[] trimmed = new float[outLen];
        System.arraycopy(out, 0, trimmed, 0, outLen);
        return trimmed;
    }


    private static float[] resample(float[] src, float ratio, int outLen) {
        float[] out = new float[outLen];
        for (int i = 0; i < outLen; i++) {
            float pos = i * ratio;
            int i0 = (int) pos;
            if (i0 >= src.length - 1) {
                out[i] = src[src.length - 1];
                continue;
            }
            float frac = pos - i0;
            out[i] = src[i0] * (1.0f - frac) + src[i0 + 1] * frac;
        }
        return out;
    }





    public short[] process(short[] in, float ratio) {
        int n = in.length;
        if (n == 0 || ratio <= 1.001f) return in;
        float[] src = new float[n];
        for (int i = 0; i < n; i++) src[i] = in[i];
        float[] stretched = stretch(src, ratio);
        float[] shifted = resample(stretched, ratio, n);

        if (hasPrev) {
            int fade = Math.min(FADE, n);
            for (int i = 0; i < fade; i++) {
                float t = (float) i / fade;
                shifted[i] = prevTail[i] * (1.0f - t) + shifted[i] * t;
            }
        }
        int fade = Math.min(FADE, n);
        System.arraycopy(shifted, n - fade, prevTail, 0, fade);
        hasPrev = true;
        short[] out = new short[n];
        for (int i = 0; i < n; i++) {
            out[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(shifted[i])));
        }
        return out;
    }
}
