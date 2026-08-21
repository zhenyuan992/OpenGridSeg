package org.opengridseg.detect;

import ij.plugin.filter.GaussianBlur;
import ij.process.FHT;
import ij.process.FloatProcessor;
import org.opengridseg.image.FloatPlane;

public final class OrientationEstimator {
    private OrientationEstimator() {}

    public static double estimateDegrees(FloatPlane source) {
        int maxDimension = Math.max(source.width(), source.height());
        double scale = maxDimension > 1024 ? 1024.0 / maxDimension : 1.0;
        FloatProcessor input = new FloatProcessor(source.width(), source.height(), source.pixels().clone());
        if (scale < 1.0) input = (FloatProcessor) input.resize(
            Math.max(32, (int) Math.round(source.width() * scale)),
            Math.max(32, (int) Math.round(source.height() * scale)), true);
        FloatProcessor background = (FloatProcessor) input.duplicate();
        new GaussianBlur().blurGaussian(background, 45.0 * scale, 45.0 * scale, 0.01);
        int width = input.getWidth(), height = input.getHeight();
        int size = 1;
        while (size < Math.max(width, height)) size <<= 1;
        float[] padded = new float[size * size];
        int xOffset = (size - width) / 2, yOffset = (size - height) / 2;
        double mean = 0;
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
            mean += background.getf(x, y) - input.getf(x, y);
        mean /= width * (double) height;
        for (int y = 0; y < height; y++) {
            double wy = height == 1 ? 1 : 0.5 - 0.5 * Math.cos(2 * Math.PI * y / (height - 1.0));
            for (int x = 0; x < width; x++) {
                double wx = width == 1 ? 1 : 0.5 - 0.5 * Math.cos(2 * Math.PI * x / (width - 1.0));
                padded[(y + yOffset) * size + x + xOffset] = (float) (((background.getf(x, y) - input.getf(x, y)) - mean) * wx * wy);
            }
        }
        FHT fht = new FHT(new FloatProcessor(size, size, padded));
        fht.transform();
        float[] hartley = (float[]) fht.getPixels();
        final double step = 0.25;
        int bins = (int) Math.round(180.0 / step);
        double[] power = new double[bins];
        int[] counts = new int[bins];
        for (int ky = -size / 2; ky < size / 2; ky++) for (int kx = -size / 2; kx < size / 2; kx++) {
            if (kx == 0 && ky == 0) continue;
            double frequency = Math.hypot(kx, ky) / size;
            if (frequency < 0.012 / scale || frequency > 0.080 / scale) continue;
            int ix = kx >= 0 ? kx : size + kx;
            int iy = ky >= 0 ? ky : size + ky;
            int nix = kx <= 0 ? -kx : size - kx;
            int niy = ky <= 0 ? -ky : size - ky;
            double h1 = hartley[iy * size + ix];
            double h2 = hartley[niy * size + nix];
            double p = 0.5 * (h1 * h1 + h2 * h2);
            double angle = Math.toDegrees(Math.atan2(ky, kx));
            angle = ((angle % 180.0) + 180.0) % 180.0;
            int bin = Math.min(bins - 1, (int) Math.floor(angle / step));
            power[bin] += p;
            counts[bin]++;
        }
        for (int i = 0; i < bins; i++) if (counts[i] > 0) power[i] /= counts[i];
        double[] smooth = new double[bins];
        for (int i = 0; i < bins; i++) for (int d = -6; d <= 6; d++) {
            double weight = Math.exp(-0.5 * d * d / (1.5 * 1.5));
            smooth[i] += weight * power[(i + d + bins) % bins];
        }
        int best = 0;
        for (int i = 1; i < bins; i++) if (smooth[i] > smooth[best]) best = i;
        double frequencyAngle = (best + 0.5) * step;
        return wrapAxial(frequencyAngle - 90.0);
    }

    static double wrapAxial(double angle) {
        double value = (angle + 90.0) % 180.0;
        if (value < 0) value += 180.0;
        return value - 90.0;
    }
}
