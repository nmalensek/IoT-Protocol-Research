package mqtt.util;

import mqtt.construction.Builder;

public class ProcessTimer extends Thread {

    private long duration;
    private Builder parent;

    public ProcessTimer(long duration, Builder parent) {
        this.duration = duration;
        this.parent = parent;
    }

    @Override
    public void run() {
        while(duration > 0) {
            try {
                Thread.sleep(1000);
                duration = duration - 1000;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        parent.stop();
    }
}
