package cn.wsjiu.twoEasy.util;

public class DensityUtils {
    public static float scale = 1;

    public static int dpToPx(int dp) {
        return (int)(dp * scale + 0.5);
    }
}
