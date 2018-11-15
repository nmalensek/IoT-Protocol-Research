package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.CountDownLatch;

public class MqttSubscriber extends MqttNode implements Runnable {
    private long receivedMessages = 0;
    private boolean running = true;

    public MqttSubscriber(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
    }

    public void subscribe(CountDownLatch counter) throws MqttException {
        client.subscribe(topic, (topic1, message) -> {
//           String payloadData = new String(message.getPayload());
//           long receivedTime = System.currentTimeMillis();
//            System.out.println(receivedTime + "-" + payloadData + "\t" +
//                    "|" + "\t" + "Latency: " + (receivedTime - Long.parseLong(payloadData.split("-")[0])));
            receivedMessages++;
            counter.countDown();
        });
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while(running) {
            try {
                Thread.sleep(100);
                System.out.println("Received " + receivedMessages + " messages.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
