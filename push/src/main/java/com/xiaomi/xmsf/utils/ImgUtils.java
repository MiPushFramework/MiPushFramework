package com.xiaomi.xmsf.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Code implements port from
 * https://www.cnblogs.com/Imageshop/p/3307308.html
 * AND
 * http://imagej.net/Auto_Threshold
 */
public class ImgUtils {

    public static Bitmap convertToTransparentAndWhite(Bitmap bmp) {
        int calculateThreshold = calculateThreshold(bmp);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        int whiteCnt = 0;
        int tsCnt = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int dot = pixels[width * i + j];
                int red = ((dot & 0x00FF0000) >> 16);
                int green = ((dot & 0x0000FF00) >> 8);
                int blue = (dot & 0x000000FF);
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);

                if (gray > calculateThreshold) {
                    pixels[width * i + j] = Color.TRANSPARENT;
                    tsCnt++;
                } else {
                    pixels[width * i + j] = Color.WHITE;
                    whiteCnt++;
                }
            }
        }

        if (whiteCnt > tsCnt) {
            //revert WHITE and TRANSPARENT
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    int dot = pixels[width * i + j];
                    if (dot == Color.WHITE) {
                        pixels[width * i + j] = Color.TRANSPARENT;
                    } else {
                        pixels[width * i + j] = Color.WHITE;
                    }
                }
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        return newBmp;
    }

    public static void getGreyHistogram(Bitmap bitmap, int[] histogram) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                int dot = bitmap.getPixel(x, y);
                int red = ((dot & 0x00FF0000) >> 16);
                int green = ((dot & 0x0000FF00) >> 8);
                int blue = (dot & 0x000000FF);
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                histogram[gray]++;
            }
        }
    }

    private static boolean IsDimodal(double[] histogram)       // 检测直方图是否为双峰的
    {
        // 对直方图的峰进行计数，只有峰数位2才为双峰
        int Count = 0;
        for (int Y = 1; Y < 255; Y++) {
            if (histogram[Y - 1] < histogram[Y] && histogram[Y + 1] < histogram[Y]) {
                Count++;
                if (Count > 2) return false;
            }
        }
        if (Count == 2)
            return true;
        else
            return false;
    }


    public static int calculateThreshold(Bitmap bitmap) {
        //TODO choose the best algorithm
//        return calculateThresholdByOSTU(bitmap);
        ArrayList<Integer> thresholds = new ArrayList<>();
        thresholds.add(calculateThresholdByOSTU(bitmap));
        thresholds.add(calculateThresholdByMinimum(bitmap));
//        thresholds.add(calculateThresholdByPTile(bitmap));
        thresholds.add(calculateThresholdByMean(bitmap));
        Collections.sort(thresholds);
        return thresholds.get(thresholds.size() - 1);
    }


    public static int calculateThresholdByPTile(Bitmap bitmap) {
        int[] histogram = new int[256];
        getGreyHistogram(bitmap, histogram);

        int Tile = 50;
        int Y, Amount = 0, Sum = 0;
        for (Y = 0; Y < 256; Y++) Amount += histogram[Y];
        for (Y = 0; Y < 256; Y++) {
            Sum = Sum + histogram[Y];
            if (Sum >= Amount * Tile / 100) return Y;
        }
        return -1;
    }

    public static int calculateThresholdByOSTU(Bitmap bitmap) {
        int[] histogram = new int[256];
        getGreyHistogram(bitmap, histogram);

        int total = bitmap.getWidth() * bitmap.getHeight();
        double sum = 0;
        for (int i = 0; i < 256; i++)
            sum += i * histogram[i];

        double sumB = 0;
        int wB = 0;
        int wF = 0;

        double varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;
            wF = total - wB;

            if (wF == 0) break;

            sumB += (double) (i * histogram[i]);
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;

            double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = i;
            }
        }

        return threshold;
    }

    public static int calculateThresholdByMinimum(Bitmap bitmap) {
        int[] histogram = new int[256];
        getGreyHistogram(bitmap, histogram);
        int Y, Iter = 0;
        double[] HistGramC = new double[256];           // 基于精度问题，一定要用浮点数来处理，否则得不到正确的结果
        double[] HistGramCC = new double[256];          // 求均值的过程会破坏前面的数据，因此需要两份数据
        for (Y = 0; Y < 256; Y++) {
            HistGramC[Y] = histogram[Y];
            HistGramCC[Y] = histogram[Y];
        }

        // 通过三点求均值来平滑直方图
        while (!IsDimodal(HistGramCC)) {                        // 判断是否已经是双峰的图像了
            HistGramCC[0] = (HistGramC[0] + HistGramC[0] + HistGramC[1]) / 3;                 // 第一点
            for (Y = 1; Y < 255; Y++)
                HistGramCC[Y] = (HistGramC[Y - 1] + HistGramC[Y] + HistGramC[Y + 1]) / 3;     // 中间的点
            HistGramCC[255] = (HistGramC[254] + HistGramC[255] + HistGramC[255]) / 3;         // 最后一点
            System.arraycopy(HistGramCC, 0, HistGramC, 0, 256);
            Iter++;
            if (Iter >= 1000)
                return -1;                                                   // 直方图无法平滑为双峰的，返回错误代码
        }
        // 阈值极为两峰之间的最小值
        boolean Peakfound = false;
        for (Y = 1; Y < 255; Y++) {
            if (HistGramCC[Y - 1] < HistGramCC[Y] && HistGramCC[Y + 1] < HistGramCC[Y])
                Peakfound = true;
            if (Peakfound && HistGramCC[Y - 1] >= HistGramCC[Y] && HistGramCC[Y + 1] >= HistGramCC[Y])
                return Y - 1;
        }
        return -1;
    }


    public static int calculateThresholdByMean(Bitmap bitmap) {
        int[] histogram = new int[256];
        getGreyHistogram(bitmap, histogram);
        int Sum = 0, Amount = 0;
        for (int Y = 0; Y < 256; Y++) {
            Amount += histogram[Y];
            Sum += Y * histogram[Y];
        }
        return Sum / Amount;
    }

}
