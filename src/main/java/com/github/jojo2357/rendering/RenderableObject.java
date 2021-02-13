package com.github.jojo2357.rendering;

import com.github.jojo2357.events.EventManager;
import com.github.jojo2357.events.GameTimes;
import com.github.jojo2357.util.Dimensions;
import com.github.jojo2357.util.Point;
import com.github.jojo2357.util.Texture;
import org.lwjgl.opengl.GL;

public abstract class RenderableObject implements IRecievesEvent{
    protected Texture image;
    protected Dimensions imageSize;
    protected boolean isVisible = true;
    protected double rotation = 0; //degrees
    protected boolean canRotate = false;

    public RenderableObject(String filename) {
        GL.createCapabilities();
        this.image = new Texture(filename);
        this.imageSize = new Dimensions(image.getWidth(), image.getHeight());
        this.registerToRender();
    }

    protected void render(Point location, Dimensions dimensions) {
        if (EventManager.currentPhase == GameTimes.SECOND_RENDER)
            ScreenManager.renderTexture(this.image, location, 1, this.rotation, dimensions);
    }

    protected void render(Point location) {
        if (EventManager.currentPhase == GameTimes.SECOND_RENDER)
            ScreenManager.renderTexture(this.image, location, 1, this.rotation, this.imageSize);
    }

    public void render() {
        if (EventManager.currentPhase == GameTimes.SECOND_RENDER)
            this.render(new Point(0, 0));
    }

    protected void registerToRender() {
        EventManager.addRenderingObject(this);
    }

    public void disable(boolean visible) {
        this.isVisible = visible;
    }

    public boolean getVisibility() {
        return this.isVisible;
    }
}
