package com.github.jojo2357.events;

public enum EventTypes {

    EmptyEvent("empty", 0),
    MouseInputEvent("mouse_input", 1),
    TickEvent("tick", 2),
    RenderEvent("render", 3),;

    private final String name;
    private final int id;

    EventTypes(String name, int id){
        this.name = name;
        this.id = id;
    }

    public String getName(){
        return this.name;
    }

    public int getId(){
        return this.id;
    }
}
