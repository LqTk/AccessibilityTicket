package com.org.tickes.eventbus;

public class StopServiceEvent {
    public boolean stop;

    public StopServiceEvent(boolean stop) {
        this.stop = stop;
    }
}
