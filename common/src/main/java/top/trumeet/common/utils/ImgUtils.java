package top.trumeet.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Code implements port from
 * https://www.cnblogs.com/Imageshop/p/3307308.html
 * AND
 * http://imagej.net/Auto_Threshold
 *
 * @author zts
 */
public class ImgUtils {

    public static Bitmap convertToTransparentAndWhite(Bitmap bitmap) {
        int calculateThreshold = calculateThreshold(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int whiteCnt = 0;
        int tsCnt = 0;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int dot = getPixel(width, pixels, i, j);
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
                    int dot = getPixel(width, pixels, i, j);
                    if (dot == Color.WHITE) {
                        pixels[width * i + j] = Color.TRANSPARENT;
                    } else {
                        pixels[width * i + j] = Color.WHITE;
                    }
                }
            }
        }

        //todo use bwareaopen
        filterWhitePoint(width, height, pixels, 3);

        //corner
        int B = width / 8;

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //shift
                if ((i > B) && (i < (height - B))) {
                    continue;
                }
                if ((j > B) && (j < (width - B))) {
                    continue;
                }

                int x = (i > (height / 2) && ((height - i) < B)) ? (i - (height - 2 * B)) : i;
                int y = (j > (width / 2) && ((width - j) < B)) ? (j - (width - 2 * B)) : j;

                if (((x - B) * (x - B) + (y - B) * (y - B)) > B * B) {
                    pixels[width * i + j] = Color.TRANSPARENT;
                }
            }
        }


        int top = 0;
        int left = 0;
        int right = 0;
        int bottom = 0;

        for (int h = 0; h < bitmap.getHeight(); h++) {
            boolean holdBlackPix = false;
            for (int w = 0; w < bitmap.getWidth(); w++) {
                if (getPixel(width, pixels, h, w) != Color.TRANSPARENT) {
                    holdBlackPix = true;
                    break;
                }
            }

            if (holdBlackPix) {
                break;
            }
            top++;
        }

        for (int w = 0; w < bitmap.getWidth(); w++) {
            boolean holdBlackPix = false;
            for (int h = 0; h < bitmap.getHeight(); h++) {
                if (getPixel(width, pixels, h, w) != Color.TRANSPARENT) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            left++;
        }

        for (int w = bitmap.getWidth() - 1; w >= 0; w--) {
            boolean holdBlackPix = false;
            for (int h = 0; h < bitmap.getHeight(); h++) {
                if (getPixel(width, pixels, h, w) != Color.TRANSPARENT) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            right++;
        }

        for (int h = bitmap.getHeight() - 1; h >= 0; h--) {
            boolean holdBlackPix = false;
            for (int w = 0; w < bitmap.getWidth(); w++) {
                if (getPixel(width, pixels, h, w) != Color.TRANSPARENT) {
                    holdBlackPix = true;
                    break;
                }
            }
            if (holdBlackPix) {
                break;
            }
            bottom++;
        }

        int diff = (bottom + top) - (left + right);
        if (diff > 0) {
            bottom -= (diff / 2);
            top -= (diff / 2);

            bottom = bottom < 0 ? 0 : bottom;
            top = top < 0 ? 0 : top;

        } else if (diff < 0) {
            left += (diff / 2);
            right += (diff / 2);
            left = left < 0 ? 0 : left;
            right = right < 0 ? 0 : right;
        }


        int cropHeight = bitmap.getHeight() - bottom - top;
        int cropWidth = bitmap.getWidth() - left - right;

        int padding = (cropHeight + cropWidth) / 16;

        int[] newPix = new int[cropWidth * cropHeight];

        int i = 0;
        for (int h = top; h < top + cropHeight; h++) {
            for (int w = left; w < left + cropWidth; w++) {
                newPix[i++] = getPixel(width, pixels, h, w);
            }
        }

        Bitmap newBmp = Bitmap.createBitmap(cropWidth + padding * 2, cropHeight + padding * 2, Bitmap.Config.ARGB_8888);
        newBmp.setPixels(newPix, 0, cropWidth, padding, padding, cropWidth, cropHeight);
        return newBmp;
    }

    private static void filterWhitePoint(int width, int height, int[] pixels, int exThre) {
        for (int i = 1; i < height - 1; i++) {
            for (int j = 1; j < width - 1; j++) {
                int[] dots = new int[]{getPixel(width, pixels, i - 1, j - 1),
                        getPixel(width, pixels, i - 1, j),
                        getPixel(width, pixels, i - 1, j + 1),
                        getPixel(width, pixels, i, j - 1),
//                        getPixel(width, pixels, i, j),
                        getPixel(width, pixels, i, j + 1),
                        getPixel(width, pixels, i + 1, j - 1),
                        getPixel(width, pixels, i + 1, j),
                        getPixel(width, pixels, i + 1, j + 1)};

                int whCnt = 0;
                int trCnt = 0;

                for (int dot : dots) {
                    if (dot == Color.WHITE) {
                        whCnt++;
                    } else {
                        trCnt++;
                    }
                }

                if (trCnt > (dots.length - exThre)) {
                    pixels[width * i + j] = Color.TRANSPARENT;
                }
            }
        }
    }

    private static int getPixel(int width, int[] pixels, int i, int j) {
        return pixels[width * i + j];
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

    private static boolean IsDimodal(double[] histogram) {
        // 对直方图的峰进行计数，只有峰数位2才为双峰
        int Count = 0;
        for (int Y = 1; Y < 255; Y++) {
            if (histogram[Y - 1] < histogram[Y] && histogram[Y + 1] < histogram[Y]) {
                Count++;
                if (Count > 2) {
                    return false;
                }
            }
        }
        return Count == 2;
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
        for (Y = 0; Y < 256; Y++) {
            Amount += histogram[Y];
        }
        for (Y = 0; Y < 256; Y++) {
            Sum = Sum + histogram[Y];
            if (Sum >= Amount * Tile / 100) {
                return Y;
            }
        }
        return -1;
    }

    public static int calculateThresholdByOSTU(Bitmap bitmap) {
        int[] histogram = new int[256];
        getGreyHistogram(bitmap, histogram);

        int total = bitmap.getWidth() * bitmap.getHeight();
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        int wF = 0;

        double varMax = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) {
                continue;
            }
            wF = total - wB;

            if (wF == 0) {
                break;
            }

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
            for (Y = 1; Y < 255; Y++) {
                HistGramCC[Y] = (HistGramC[Y - 1] + HistGramC[Y] + HistGramC[Y + 1]) / 3;     // 中间的点
            }
            HistGramCC[255] = (HistGramC[254] + HistGramC[255] + HistGramC[255]) / 3;         // 最后一点
            System.arraycopy(HistGramCC, 0, HistGramC, 0, 256);
            Iter++;
            if (Iter >= 1000) {
                return -1;                                                   // 直方图无法平滑为双峰的，返回错误代码
            }
        }
        // 阈值极为两峰之间的最小值
        boolean Peakfound = false;
        for (Y = 1; Y < 255; Y++) {
            if (HistGramCC[Y - 1] < HistGramCC[Y] && HistGramCC[Y + 1] < HistGramCC[Y]) {
                Peakfound = true;
            }
            if (Peakfound && HistGramCC[Y - 1] >= HistGramCC[Y] && HistGramCC[Y + 1] >= HistGramCC[Y]) {
                return Y - 1;
            }
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


    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            int w = (drawable.getIntrinsicWidth() <= 0) ? 1 : drawable.getIntrinsicWidth();
            int h = (drawable.getIntrinsicHeight() <= 0) ? 1 : drawable.getIntrinsicHeight();
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

}
