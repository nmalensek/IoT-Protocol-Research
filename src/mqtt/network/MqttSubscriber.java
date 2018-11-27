package mqtt.network;

import mqtt.util.ResultWriter;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttSubscriber extends MqttNode implements Runnable {
    private long receivedMessages = 0;
    private boolean running = true;
    private long totalLatency = 0;
    private ResultWriter writer = new ResultWriter(this);

    public MqttSubscriber(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
        writer.start();
    }

    public void subscribe() throws MqttException {
        client.subscribe(topic, (topic1, message) -> {
           String payloadData = new String(message.getPayload());
            receivedMessages++;
            long receivedTime = System.currentTimeMillis();
            totalLatency += (receivedTime - Long.parseLong(payloadData.split("-")[0]));
        });
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public boolean isRunning() {
        return running;
    }

    @Override
    public void run() {
        long previousTotal = 0;
        long previousLatency = 0;
        long snapShot;
        long snapShotLatency;
        long averageLatency;
        while(running) {
            try {
                Thread.sleep(1000);
                snapShot = receivedMessages - previousTotal;
                snapShotLatency = totalLatency - previousLatency;
                previousTotal = receivedMessages;
                previousLatency = totalLatency;
                averageLatency = calculateLatency(snapShot, snapShotLatency);
                System.out.println("Received " + snapShot + " messages this window.");
                System.out.println("Average latency per message this window: " + averageLatency);
                writer.getResultQueue().add(snapShot + "," + snapShotLatency + "," + averageLatency);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long calculateLatency(long messages, long totalMessageLatency) {
        if (messages == 0 ) { return 0; }
        return totalMessageLatency / messages;
    }

    public long getReceivedMessages() {
        return receivedMessages;
    }

    public long getTotalLatency() {
        return totalLatency;
    }
}
//            System.out.println(receivedTime + "-" + payloadData + "\t" +
//                    "|" + "\t" + "Latency: " + (receivedTime - Long.parseLong(payloadData.split("-")[0])));