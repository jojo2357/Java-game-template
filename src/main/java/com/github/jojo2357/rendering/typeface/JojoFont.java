package com.github.jojo2357.rendering.typeface;

public class JojoFont {

    private static final FontCharacter[] fontCharacters = new FontCharacter[37];

    public static void init() {
        loadNumbers();
        loadLetters();
    }

    public static void loadNumbers() {
        for (char asciiValue = 48; asciiValue < 58; asciiValue++) {
            fontCharacters[asciiValue - 48] = new FontCharacter(asciiValue, Colors.WHITE);
        }
        fontCharacters[36] = new FontCharacter('-', Colors.WHITE);
    }

    private static void loadLetters() {
        for (char asciiValue = 65; asciiValue < 91; asciiValue++) {
            fontCharacters[asciiValue - 55] = new FontCharacter(asciiValue, Colors.WHITE);
        }
    }

    public static FontCharacter getCharacter(Colors color, char charRepresentation) {
        return getCharacter(charRepresentation, color);
    }

    public static FontCharacter getCharacter(char charRepresentation, Colors color) {
        if (charRepresentation <= '9' && charRepresentation >= '0')
            if (fontCharacters[charRepresentation - 48] == null)
                throw new IllegalStateException("Probs wrong color. " + charRepresentation + "_" + color.name + " DNE");
            else
                return fontCharacters[charRepresentation - 48];
        if (charRepresentation <= 'Z' && charRepresentation >= 'A')
            if (fontCharacters[charRepresentation - 55] == null)
                throw new IllegalStateException("Probs wrong color. " + charRepresentation + "_" + color.name + " DNE");
            else
                return fontCharacters[charRepresentation - 55];
        if (charRepresentation == '-')
            if (fontCharacters[36] == null)
                throw new IllegalStateException("Probs wrong color. " + charRepresentation + "_" + color.name + " DNE");
            else
                return fontCharacters[36];
        throw new IndexOutOfBoundsException("font type for " + charRepresentation + "_" + color.name + " not found");
    }
}
