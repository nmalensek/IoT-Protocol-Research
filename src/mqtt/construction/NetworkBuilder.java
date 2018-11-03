package mqtt.construction;

import mqtt.network.MqttPublisher;
import mqtt.network.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NetworkBuilder {

    private CountDownLatch messageLimit = new CountDownLatch(100);
    private static final String CONNECTION = "tcp://localhost:1883";
    private static final String TOPIC = "group1/tempAndHumidity";
    private ArrayList<MqttPublisher> publishers = new ArrayList<>();
    private ArrayList<MqttSubscriber> subscribers = new ArrayList<>();

    private void buildAndStartPublishers() throws MqttException {
        MqttPublisher publisher = new MqttPublisher(CONNECTION, TOPIC);
        publisher.addSensor();
        publisher.connectToBroker();
        MqttPublisher publisher2 = new MqttPublisher(CONNECTION, TOPIC);
        publisher2.addSensor();
        publisher2.connectToBroker();
        publishers.add(publisher);
        publishers.add(publisher2);
        publisher.startPublishing(5, 1, TimeUnit.MILLISECONDS);
        publisher2.startPublishing(5, 1, TimeUnit.MILLISECONDS);
    }

    private void buildAndStartSubscribers() throws MqttException {
        MqttSubscriber subscriber = new MqttSubscriber(CONNECTION, TOPIC);
        subscriber.connectToBroker();
        subscribers.add(subscriber);
        subscriber.subscribe(messageLimit);
    }

    private void stopPublishers() {
        for (MqttPublisher p : publishers) {
            p.stopPublishing();
        }
    }

    public static void main(String[] args) throws MqttException, InterruptedException {
        NetworkBuilder networkBuilder = new NetworkBuilder();
        System.out.println("Building Publisher(s)...");
        networkBuilder.buildAndStartPublishers();
        System.out.println("Publisher(s) built.");
        System.out.println("Building Subscriber(s)...");
        networkBuilder.buildAndStartSubscribers();
        System.out.println("Subscriber(s) built.");
        networkBuilder.messageLimit.await();
        networkBuilder.stopPublishers();

    }
}
