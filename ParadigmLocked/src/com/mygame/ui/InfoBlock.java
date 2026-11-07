package com.mygame.ui;

import com.mygame.engine.core.Img;
// One content block (heading, text, image or gap) in How to Play / Credits.
public final class InfoBlock {
    public enum Kind { HEADING, SUBHEADING, BODY, IMAGE, GAP }

    public final Kind kind;
    public final String text;
    public final Img image;
    public final int height;

    private InfoBlock(Kind kind, String text, Img image, int height) {
        this.kind = kind;
        this.text = text;
        this.image = image;
        this.height = height;
    }

    public static InfoBlock heading(String text) {
        return new InfoBlock(Kind.HEADING, text, null, 0);
    }

    public static InfoBlock subheading(String text) {
        return new InfoBlock(Kind.SUBHEADING, text, null, 0);
    }

    public static InfoBlock body(String text) {
        return new InfoBlock(Kind.BODY, text, null, 0);
    }

    public static InfoBlock image(Img image, int height, String caption) {
        return new InfoBlock(Kind.IMAGE, caption, image, height);
    }

    public static InfoBlock gap(int height) {
        return new InfoBlock(Kind.GAP, null, null, height);
    }
}
