package com.github.jojo2357.util;

import com.github.jojo2357.events.EventManager;
import com.github.jojo2357.events.events.MouseInputEvent;

public class HelperFunctions {
    public static Point clickToMapLocation(MouseInputEvent event){
        return event.getPosition().copy().multiply(2).subtract(EventManager.map.getMapLocation()).multiply(1f/EventManager.map.getZoomFactor());
    }
}
