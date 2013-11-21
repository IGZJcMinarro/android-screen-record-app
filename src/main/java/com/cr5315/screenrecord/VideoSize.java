package com.cr5315.screenrecord;

/**
 * Created by Ben on 11/16/13.
 */
public class VideoSize {
    private int height, width;

    public static final String divider = "~";

    public VideoSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public VideoSize(String videoSize) {
        int start = videoSize.indexOf(divider);
        this.width = Integer.parseInt(videoSize.substring(0, start));
        this.height = Integer.parseInt(videoSize.substring(start + 1));
    }

    public String toString() {
        return String.valueOf(width) + divider + String.valueOf(height);
    }

    public String asString() {
        return String.valueOf(width) + 'x' + String.valueOf(height);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }
}
