package com.github.jojo2357.rendering.typeface;

import com.github.jojo2357.util.Texture;

public class FontCharacter {
    private final char stringRep;
    private final Colors color;
    private final Texture image;

    public FontCharacter(char strRepresentation, Colors color) {
        this.stringRep = strRepresentation;
        this.color = color;
        this.image = new Texture("FontAssets/" + strRepresentation + "_" + color.name);
    }

    public char getStringRep() {
        return stringRep;
    }

    public Colors getColor() {
        return color;
    }

    public Texture getImage() {
        return image;
    }
}
