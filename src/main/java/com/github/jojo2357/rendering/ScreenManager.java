package com.github.jojo2357.rendering;

import com.github.jojo2357.events.EventManager;
import com.github.jojo2357.events.GameTimes;
import com.github.jojo2357.events.events.MouseInputEvent;
import com.github.jojo2357.rendering.typeface.Colors;
import com.github.jojo2357.util.Dimensions;
import com.github.jojo2357.util.Point;
import com.github.jojo2357.util.Texture;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

public class ScreenManager {
    public static final Dimensions windowSize = new Dimensions(800, 600);
    private static final Point lastPosition = new Point(0, 0);
    private static final double[] x = new double[1];
    private static final double[] y = new double[1];
    public static long window;
    public static Point screenOffset = new Point();
    public static MouseInputEvent lastPostedMouseEvent = new MouseInputEvent(new Point(0, 0), (byte) -1, 0);
    public static MouseInputEvent lastMouseEvent = new MouseInputEvent(new Point(0, 0), (byte) -1, 0);
    private static double rot = 0;
    private static float zoom = 1;
    private static double scrolls = 0;

    public static void init() {

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        window = glfwCreateWindow(windowSize.getWidth(), windowSize.getHeight(), "Desktop boats", NULL, NULL);
        if (window == NULL) throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        glfwSetScrollCallback(window, new GLFWScrollCallback() {
            @Override
            public void invoke(long window, double xoffset, double yoffset) {
                doMouseWheel(yoffset);
            }
        });

        glfwShowWindow(window);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    }

    private static void doMouseWheel(double vel) {
        scrolls += vel;
    }

    public static boolean tick() {
        glfwPollEvents();
        glfwGetCursorPos(window, x, y);
        Point currentMouse = new Point(x[0], y[0]);

        byte mouseButtonActions = (byte) 0;// release is index 0, press 1. left mouse button is actions & 1, right is & 2, middle is & 4

        for (int mouseChecker = 0; mouseChecker <= 2; mouseChecker++) {
            mouseButtonActions |= glfwGetMouseButton(window, mouseChecker) == 1 ? (byte) Math.pow(2, mouseChecker) : (byte) 0;
        }

        lastMouseEvent = (MouseInputEvent)lastPostedMouseEvent.copy();
        MouseInputEvent event = new MouseInputEvent(currentMouse, mouseButtonActions, (int) Math.round(scrolls));
        /*if (scrolls != 0)
            System.out.println((int)Math.round(scrolls));*/
        scrolls = 0;
        if (!event.equals(lastMouseEvent))
            EventManager.notify(event);
        lastPostedMouseEvent = ((MouseInputEvent)event.copy());

        glClear(GL_COLOR_BUFFER_BIT);// clear the framebuffer// swap the color buffers
        return glfwWindowShouldClose(window);
    }

    public static void renderTexture(Texture text, Point point, float sizeFactor, Dimensions dimensions) {
        switch (EventManager.currentPhase) {
            case FIRST_RENDER:
            case SECOND_RENDER:
            case THIRD_RENDER:
                renderTexture(text, point, sizeFactor, 0, dimensions);
                break;
            default:
                throw new IllegalStateException("attempted to render outside of render phase!");
        }
    }

    public static void renderTexture(Texture text, Point point, float sizeFactor, double rotation, Dimensions specialDimensions) {
        if (EventManager.currentPhase != GameTimes.FIRST_RENDER && EventManager.currentPhase != GameTimes.SECOND_RENDER && EventManager.currentPhase != GameTimes.THIRD_RENDER) {
            throw new IllegalStateException("attempted to render outside of render phase!");
        }
        text.bind();
        //point = point.add(new Point(text.dimensions.getWidth()/2, text.dimensions.getHeight()/2));
        double offset = Math.toDegrees(Math.atan(specialDimensions.getHeight() / (double) specialDimensions.getWidth())) - 45;
        glEnable(GL_TEXTURE_2D);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0);
        glVertex2f((float) zoom * convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (-45 + rotation + offset)) + point.getX()), windowSize.getWidth()), zoom * -(float) convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (-45 + rotation + offset)) + point.getY()), windowSize.getHeight()));
        glTexCoord2f(1f, 0);
        glVertex2f((float) zoom * convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (45 + rotation - offset)) + point.getX()), windowSize.getWidth()), zoom * -(float) convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (45 + rotation - offset)) + point.getY()), windowSize.getHeight()));
        glTexCoord2f(1f, -1f);
        glVertex2f((float) zoom * convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (135 + rotation + offset)) + point.getX()), windowSize.getWidth()), zoom * -(float) convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (135 + rotation + offset)) + point.getY()), windowSize.getHeight()));
        glTexCoord2f(0, -1f);
        glVertex2f((float) zoom * convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (225 + rotation - offset)) + point.getX()), windowSize.getWidth()), zoom * -(float) convertToScreenCoord(myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (225 + rotation - offset)) + point.getY()), windowSize.getHeight()));
        glEnd();
        glDisable(GL_TEXTURE_2D);
        /*Point a = new Point((float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (-45 + rotation + offset)) + point.getX() - ScreenManager.windowSize.getWidth()) / ScreenManager.windowSize.getWidth(), -(float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (-45 + rotation + offset)) + point.getY()  - ScreenManager.windowSize.getHeight()) / ScreenManager.windowSize.getHeight());
        Point b = new Point((float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (45 + rotation - offset)) + point.getX()   - ScreenManager.windowSize.getWidth()) / ScreenManager.windowSize.getWidth(), -(float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (45 + rotation - offset)) + point.getY() - ScreenManager.windowSize.getHeight()) / ScreenManager.windowSize.getHeight());
        Point c = new Point((float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (135 + rotation + offset)) + point.getX()  - ScreenManager.windowSize.getWidth()) / ScreenManager.windowSize.getWidth(), -(float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (135 + rotation + offset)) + point.getY()- ScreenManager.windowSize.getHeight()) / ScreenManager.windowSize.getHeight());
        Point d = new Point((float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.sin((Math.PI / 180.0) * (225 + rotation - offset)) + point.getX()  - ScreenManager.windowSize.getWidth()) / ScreenManager.windowSize.getWidth(), -(float) myRounder(sizeFactor * specialDimensions.getDiagonal() * Math.cos((Math.PI / 180.0) * (225 + rotation - offset)) + point.getY() - ScreenManager.windowSize.getHeight()) / ScreenManager.windowSize.getHeight());
        System.out.println(a + " " + b + " " + c + " " + d);*/
    }

    private static float convertToScreenCoord(float pointIn, float dimension) {
        return (pointIn - dimension) / dimension;
    }

    private static float myRounder(double in) {
        if (Math.abs(in) % 1 < 0.01) return (float) Math.floor(in);
        if (Math.abs(in) % 1 > 0.99) return (float) Math.ceil(in);
        return (float) in;
    }

    public static void drawBox(Point topLeft, Point bottomRight, int r, int g, int b) {
        GL11.glColor4f(r, g, b, 255);
        Point[] points = {new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(bottomRight.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(bottomRight.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(bottomRight.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(bottomRight.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight()))};

        glBegin(GL_LINES);
        for (int i = 0; i < points.length - 1; i++) {
            glVertex2f(points[i].getX(), points[i].getY());
            glVertex2f(points[i + 1].getX(), points[i + 1].getY());
        }
        glEnd();
    }

    public static void drawBoxFilled(Point topLeft, Point bottomRight, int r, int g, int b) {
        GL11.glColor4f(r, g, b, 255);
        Point[] points = {new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(bottomRight.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(bottomRight.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(bottomRight.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(bottomRight.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight())), new Point(convertToScreenCoord(topLeft.getX(), ScreenManager.windowSize.getWidth()), -convertToScreenCoord(topLeft.getY(), ScreenManager.windowSize.getHeight()))};

        glBegin(GL_TRIANGLE_FAN);
        for (int i = 0; i < points.length - 1; i++) {
            glVertex2f(points[i].getX(), points[i].getY());
            glVertex2f(points[i + 1].getX(), points[i + 1].getY());
        }
        glEnd();
        GL11.glColor4f(255, 255, 255, 255);
    }

    public static void finishRender() {
        glfwSwapBuffers(window);
    }

    public static void renderTexture(Texture text, Point point) {
        renderTexture(text, point, 1);
    }

    public static void renderTexture(Texture text, Point point, float sizeFactor) {
        renderTexture(text, point, sizeFactor, 0, new Dimensions(text.getWidth(), text.getHeight()));
    }

    public static void drawCircle(Point origin, float radius, int refinement){
        refinement *= radius;
        double twoPiOnRefinement = Math.PI * 2/ refinement;
        glBegin(GL_POINTS);
        for(int i = 0; i <= refinement; i++){ //NUM_PIZZA_SLICES decides how round the circle looks.
            double angle = twoPiOnRefinement * i;
            glVertex2f(convertToScreenCoord(origin.getX() + (float)Math.cos(angle) * radius, windowSize.getWidth()), -convertToScreenCoord(origin.getY() + (float)Math.sin(angle) * radius, windowSize.getHeight()));
        }
        glEnd();
    }

    public static void drawCircle(Point origin, float radius, int refinement, int r, int g, int b, int a){
        GL11.glColor4f(r, g, b, a);
        drawCircle(origin, radius, refinement);
        GL11.glColor4f(255, 255, 255, 255);
    }

    public static void drawLine(Point origin, Point destination){
        glBegin(GL_LINES);
        glVertex2f(convertToScreenCoord(origin.getX(), windowSize.getWidth()), -convertToScreenCoord(origin.getY(), windowSize.getHeight()));
        glVertex2f(convertToScreenCoord(destination.getX(), windowSize.getWidth()), -convertToScreenCoord(destination.getY(), windowSize.getHeight()));
        glEnd();
    }

    public static void drawLine(Point origin, Point destination, int r, int g, int b){
        drawLine(origin, destination, r, g, b, 255);
    }

    public static void drawLine(Point origin, Point destination, int r, int g, int b, int a){
        GL11.glColor4f(r, g, b, a);
        drawLine(origin, destination);
        GL11.glColor4f(255, 255, 255, 255);
    }

    public static void drawGreenBox() {
        ScreenManager.drawBoxFilled(new Point(ScreenManager.windowSize.getWidth() * 2 - 200, ScreenManager.windowSize.getHeight() * 2 - 200), new Point(ScreenManager.windowSize.getWidth() * 2, ScreenManager.windowSize.getHeight() * 2), 0, 255, 0);
    }

    public static void renderTextureWithOverlay(Texture image, Point currentSpot, float size, Colors color) {
        GL11.glColor4f(color.R / 255f, color.G / 255f, color.B / 255f, 255);
        renderTexture(image, currentSpot, size);
        GL11.glColor4f(255, 255, 255, 255);
    }
}
