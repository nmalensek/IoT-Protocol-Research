package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.CountDownLatch;

public class MqttSubscriber extends MqttNode {

    public MqttSubscriber(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
    }

    public void subscribe(CountDownLatch counter) throws MqttException {
        client.subscribe(topic, (topic1, message) -> {
           String payloadData = new String(message.getPayload());
           Long receivedTime = System.currentTimeMillis();
            System.out.println(receivedTime + "-" + payloadData + "\t" +
                    "|" + "\t" + "Latency: " + (receivedTime - Long.parseLong(payloadData.split("-")[0])));
            counter.countDown();
        });
    }
}
