package com.github.jojo2357.events;

import com.github.jojo2357.events.events.*;
import com.github.jojo2357.rendering.RenderableObject;
import com.github.jojo2357.rendering.ScreenManager;
import com.github.jojo2357.rendering.IRecievesEvent;
import com.github.jojo2357.util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventManager {
    private static final List<EventBase> events = new ArrayList<EventBase>();
    private static final HashMap<EventTypes, List<IRecievesEvent>> registeredListeners = new HashMap<>();

    public static GameTimes currentPhase = GameTimes.WAITING;
    private static final TickEvent staticTickEvent = new TickEvent();

    public static boolean configured = false;

    public static void init() {
        for (EventTypes event : EventTypes.values())
            registeredListeners.put(event, new ArrayList<IRecievesEvent>());
        configured = true;
    }

    public static <T extends EventBase> boolean notify(T event) {
        //System.out.println(event.getEventType().getName() + " gotten!");
        if (event instanceof RenderEvent) {
            boolean toClose = ScreenManager.tick();
            ScreenManager.drawBox(new Point(10,10), new Point(30, 30), 255, 255, 255);
            //ScreenManager.drawLine(new Point(100, 100), new Point(200, 200), 255, 255, 255);
            for (GameTimes gameTime : GameTimes.values()) {
                if (gameTime.name().contains("RENDER")) {
                    currentPhase = gameTime;
                    //MiscRenderer.render();
                    events.add(event);
                    EventManager.sendEvents();
                }
            }
            ScreenManager.finishRender();
            currentPhase = GameTimes.WAITING;
            return toClose;
        }else
            events.add(event);
        return false;
    }

    public static <T extends IRecievesEvent> void addListeningObject(T listeningObject, EventTypes eventToListenFor) {
        if (!configured)
            init();
        registeredListeners.get(eventToListenFor).add(listeningObject);
    }

    public static <T extends IRecievesEvent> void addListeningObject(T listeningObject, EventBase eventToListenFor) {
        registeredListeners.get(eventToListenFor.getEventType()).add(listeningObject);
    }

    public static void sendEvents() {
        int maxLoops = 10 * events.size();
        int loopsMade = 0;
        boolean terminateLoops = false;
        while (events.size() > 0) {
            /*if (events.get(0).getEventType() != EventTypes.RenderEvent) {*/
                allFors:
                {// cant use for...each because events.remove(0); crashes it
                    for (int eventLooper = 0; eventLooper < EventPriorities.values().length; eventLooper++) {
                        EventPriorities prio = EventPriorities.values()[eventLooper];
                        for (int objectLooper = 0; objectLooper < registeredListeners.get(events.get(0).getEventType()).size(); objectLooper++) {
                            IRecievesEvent target = registeredListeners.get(events.get(0).getEventType()).get(objectLooper);
                            if (target.getPrio(events.get(0).getEventType()) == prio && target.notify(events.get(0))) {
                                break allFors;
                            }
                        }
                    }
                }
            /*} else {*/
                /*for (IRecievesEvent obj : registeredListeners.get(EventTypes.RenderEvent)) {
                    if (obj instanceof RenderableObject)
                        ((RenderableObject)obj).render();
                }*/
            /*}*/
            events.remove(0);
            loopsMade++;
            if (loopsMade > maxLoops)// debug because reasons
                throw new RuntimeException("Infinite loop?");
        }
    }

    public static void sendTickEvent() {
        if (!configured)
            EventManager.init();
        currentPhase = GameTimes.TICK;
        for (IRecievesEvent room : registeredListeners.get(EventTypes.TickEvent))
            room.notify(staticTickEvent);
    }

    public static <T extends RenderableObject> boolean addRenderingObject(T object) {
        if (!configured)
            init();
        if (registeredListeners.get(EventTypes.RenderEvent).contains(object))
            return true;
        EventManager.registeredListeners.get(EventTypes.RenderEvent).add(object);
        return false;
    }
}
