package com.github.jojo2357;

import com.github.jojo2357.events.EventBase;
import com.github.jojo2357.events.EventManager;
import com.github.jojo2357.events.events.RenderEvent;
import com.github.jojo2357.rendering.RenderableObject;
import com.github.jojo2357.rendering.ScreenManager;
import com.github.jojo2357.rendering.typeface.JojoFont;
import com.github.jojo2357.util.Point;

import java.util.Arrays;

public class Main {
    public static double fps = 60;
    public static double frameLength = (1000.0 / fps);

    public static void main(String[] args) {
        long[] lastTimes = new long[100];
        int index = 0;
        int totalSkips = 0;
        ScreenManager.init();
        JojoFont.init();
        long timeIn = System.currentTimeMillis();
        long last;
        int loops = 0;
        RenderableObject obj = new RenderableObject("FontAssets/A_white") {
            @Override
            public <T extends EventBase> boolean notify(T event) {
                render(new Point(100, 100));
                return false;
            }
        };
        do {
            last = System.currentTimeMillis();
            //loops++;
            EventManager.sendTickEvent();
            EventManager.sendEvents();
            if (EventManager.notify(new RenderEvent())) break;
            try {
                if ((long) (frameLength - (System.currentTimeMillis() - last)) > 0) {
                    Thread.sleep((long) (frameLength - (System.currentTimeMillis() - last)));
                } else {
                    System.out.println("SKIPPED FRAME (" + (frameLength - (System.currentTimeMillis() - last)) + ") " + (++totalSkips));
                }
            } catch (Exception e) {
                System.out.println("FAILED FRAME " + (long) (frameLength - (System.currentTimeMillis() - last)));
            }
            //fps logger
            /*lastTimes[index] = System.currentTimeMillis() - last;
            if (++index == 100){
                int sum = 0;
                for (long q : lastTimes)
                    sum += q;
                index = 0;
                System.out.println("100 frames in " + sum + "ms averaging " + 100000/sum + "fps");
            }*/
        } while (true);
    }
}
