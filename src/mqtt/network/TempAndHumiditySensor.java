package mqtt.network;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Random;
import java.util.concurrent.Callable;

public class TempAndHumiditySensor implements Callable<Boolean>, Sensor {
    private String publisherId;
    private IMqttClient client;
    private Random random = new Random();
    private StringBuilder builder = new StringBuilder();
    private String topic;
    private int qosLevel;

    public TempAndHumiditySensor(IMqttClient client, String initialTopic, String publisherId, int qosLevel) {
        this.client = client;
        this.topic = initialTopic;
        this.publisherId = publisherId;
        this.qosLevel = qosLevel;
    }

    @Override
    public Boolean call() throws Exception {

        if (!client.isConnected()) {
            System.out.println("Client is not connected.");
            return false;
        }

        try {
            MqttMessage message = new MqttMessage(prepareMessage());
            message.setQos(qosLevel);
//            message.setRetained(true);

            client.publish(topic, message);
            return true;
        } catch (MqttException e) {
            System.out.println("Could not send message");
            return false;
        }
    }

    private byte[] prepareMessage() {
        builder.append(System.currentTimeMillis());
        builder.append("-");
        builder.append(publisherId);
        builder.append("-");
        builder.append(readTemperature(70, 6));
        builder.append("-");
        builder.append(readHumidity(50, 10));

        byte[] payload = builder.toString().getBytes();
        builder.setLength(0);

        return payload;
    }

    private String readTemperature(int initial, int changeBound) {
        return String.format("T:%.2fF", takeReading(initial, changeBound, 8));
    }

    private String readHumidity(int initial, int changeBound) {
        return String.format("H:%.2f%%", takeReading(initial, changeBound, 5));
    }

    private double takeReading(int initial, int changeBound, int stability) {
        int plusMinus = random.nextInt(21) % stability == 0 ? 1 : -1;
        double change = random.nextInt(changeBound) + random.nextDouble();

        return initial + (change * plusMinus);
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public Long getId() {
        return null;
    }
}
