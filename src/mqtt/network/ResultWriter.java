package mqtt.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResultWriter extends Thread {
    private String writeDirectory = "../results/";
    private ConcurrentLinkedQueue<String> resultQueue = new ConcurrentLinkedQueue<>();
    private boolean running = true;
    private long startTime;
    private MqttSubscriber parent;

    public ResultWriter(MqttSubscriber parent) {
        this.parent = parent;
        startTime = System.currentTimeMillis();
        File dir = new File(writeDirectory);
        dir.mkdirs();
    }

    public void writeResults(String results) {
        try (FileOutputStream stream = new FileOutputStream(writeDirectory + "MQTT_results" + startTime,
                true)) {
            stream.write((results + "\n").getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        boolean slept = false;
        while (running) {
            String element = resultQueue.poll();
            if (element != null) {
                writeResults(element);
            }
            if (!parent.isRunning() && resultQueue.isEmpty()) {
                if (slept) {
                    running = false;
                }
                try {
                    Thread.sleep(100);
                    slept = true;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ConcurrentLinkedQueue<String> getResultQueue() {
        return resultQueue;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
