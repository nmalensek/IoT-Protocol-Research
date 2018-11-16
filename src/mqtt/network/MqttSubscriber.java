package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class MqttSubscriber extends MqttNode implements Runnable {
    private long receivedMessages = 0;
    private boolean running = true;
    private long totalLatency = 0;

    public MqttSubscriber(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
    }

    public void subscribe(CountDownLatch counter) throws MqttException {
        client.subscribe(topic, (topic1, message) -> {
           String payloadData = new String(message.getPayload());
//            System.out.println(receivedTime + "-" + payloadData + "\t" +
//                    "|" + "\t" + "Latency: " + (receivedTime - Long.parseLong(payloadData.split("-")[0])));
            receivedMessages++;
            long receivedTime = System.currentTimeMillis();
            totalLatency += (receivedTime - Long.parseLong(payloadData.split("-")[0]));
            counter.countDown();
        });
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        long previousTotal = 0;
        long previousLatency = 0;
        long snapShot = 0;
        long snapShotLatency = 0;

        while(running) {
            try {
                Thread.sleep(100);
                snapShot = receivedMessages - previousTotal;
                snapShotLatency = totalLatency - previousLatency;
                previousTotal = receivedMessages;
                previousLatency = totalLatency;
                System.out.println("Received " + snapShot + " messages.");
                System.out.println("Average latency: " + calculateLatency(snapShot, snapShotLatency));
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
