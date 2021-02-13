package com.github.jojo2357.rendering.typeface;

import com.github.jojo2357.rendering.ScreenManager;
import com.github.jojo2357.util.Point;

public class TextRenderer {
    public static void render(String charSequenceToPrint, Point renderStart, int lineWidth, Colors color) {
        charSequenceToPrint = charSequenceToPrint.toUpperCase();
        Point currentSpot = renderStart.copy();
        int onThisLine = 0;
        charSequenceToPrint += ' ';
        for (int i = 0; i < charSequenceToPrint.length(); i++) {
            char renderChar = charSequenceToPrint.charAt(i);
            if (renderChar != ' ') {
                onThisLine++;
                ScreenManager.renderTextureWithOverlay(JojoFont.getCharacter(renderChar, Colors.WHITE).getImage(), currentSpot, 2.1f, color);
                //ScreenManager.renderTexture(JojoFont.getCharacter(renderChar, color).getImage(), currentSpot, 2.1f);
                currentSpot.stepX(18);
            }else {
                if (charSequenceToPrint.indexOf(' ', i + 1) - i + onThisLine >= lineWidth){
                    currentSpot = new Point(renderStart.getX(), currentSpot.getY() + 40);
                    onThisLine = 0;
                }else
                    currentSpot.stepX(18);
            }
        }
    }

    public static void render(String charSequenceToPrint, Point renderStart, int lineWidth, Colors...colors) {
        int colodex = 0;
        charSequenceToPrint = charSequenceToPrint.toUpperCase();
        Point currentSpot = renderStart.copy();
        int onThisLine = 0;
        charSequenceToPrint += ' ';
        for (int i = 0; i < charSequenceToPrint.length(); i++) {
            char renderChar = charSequenceToPrint.charAt(i);
            if (renderChar == '\b')
                colodex++;
            else if (renderChar != ' ') {
                onThisLine++;
                ScreenManager.renderTextureWithOverlay(JojoFont.getCharacter(renderChar, colors[colodex]).getImage(), currentSpot, 2.1f, colors[colodex]);
                currentSpot.stepX(18);
            }else {
                if (charSequenceToPrint.indexOf(' ', i + 1) - i + onThisLine >= lineWidth){
                    currentSpot = new Point(renderStart.getX(), currentSpot.getY() + 40);
                    onThisLine = 0;
                }else
                    currentSpot.stepX(18);
            }
        }
    }
}
