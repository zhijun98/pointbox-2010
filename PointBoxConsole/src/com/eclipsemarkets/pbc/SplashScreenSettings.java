/**
 * Eclipse Market Solutions LLC
 */

package com.eclipsemarkets.pbc;

/**
 *
 * @author Zhijun Zhang
 */
public class SplashScreenSettings {
    private int width;
    private int height;
    private int progess_line_pos_y;
    private int text_pos_x;
    private int text_pos_y;

    public SplashScreenSettings(int width, int height, int progess_line_pos_y, int text_pos_x, int text_pos_y) {
        this.width = width;
        this.height = height;
        this.progess_line_pos_y = progess_line_pos_y;
        this.text_pos_x = text_pos_x;
        this.text_pos_y = text_pos_y;
    }

    public int getHeight() {
        return height;
    }

    public int getProgess_line_pos_y() {
        return progess_line_pos_y;
    }

    public int getText_pos_x() {
        return text_pos_x;
    }

    public int getText_pos_y() {
        return text_pos_y;
    }

    public int getWidth() {
        return width;
    }
}
