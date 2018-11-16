package mqtt.construction;

import mqtt.network.MqttPublisher;
import mqtt.network.MqttSubscriber;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class NetworkBuilder {

    private CountDownLatch messageLimit = new CountDownLatch(1000);
    private CountDownLatch waitLatch;
    private static final String TOPIC = "group1/tempAndHumidity";
    private ArrayList<MqttPublisher> publishers = new ArrayList<>();
    private ArrayList<MqttSubscriber> subscribers = new ArrayList<>();
    private String connectionUri;
    private int numPublishers;
    private int waitTime;
    private int messageFrequency;
    private int totalMessages;
    private int qosLevel;

    public NetworkBuilder(String connectionUri, int qosLevel, int numPublishers,
                          int waitTime, int messageFrequency, boolean allAtOnce) {
        this.connectionUri = connectionUri;
        this.qosLevel = qosLevel;
        this.numPublishers = numPublishers;
        this.waitTime = waitTime;
        this.messageFrequency = messageFrequency;
        if (allAtOnce) {
            this.waitLatch = new CountDownLatch(numPublishers);
        } else {
            this.waitLatch = new CountDownLatch(0);
        }
    }

    private void buildPublishers() throws MqttException {
        for (int i = 0; i < numPublishers; i++) {
            MqttPublisher publisher = new MqttPublisher(connectionUri, TOPIC);
            publisher.addSensor(qosLevel);
            publisher.connectToBroker();
            publisher.setSettings(waitTime, messageFrequency, waitLatch);
            publishers.add(publisher);
        }
    }

    private void startPublishers() {
        for (MqttPublisher p : publishers) {
            new Thread(p).start();
            waitLatch.countDown();
        }
    }

    private void buildAndStartSubscribers() throws MqttException {
        MqttSubscriber subscriber = new MqttSubscriber(connectionUri, TOPIC);
        subscriber.connectToBroker();
        subscribers.add(subscriber);
        subscriber.subscribe(messageLimit);
        new Thread(subscriber).start();
    }

    private void stopPublishers() {
        for (MqttPublisher p : publishers) {
            p.setRunning(false);
//            p.stopPublishing();
            totalMessages += p.getCounter();
        }
        System.out.println("Publishers sent " + totalMessages + " messages.");
    }

    private void stopSubscribers() {
        long receivedMessages = 0;
        long receivedLatency = 0;
        for (MqttSubscriber s : subscribers) {
            s.setRunning(false);
            receivedMessages += s.getReceivedMessages();
            receivedLatency += s.getTotalLatency();
        }
        System.out.println("Subscribers received " + receivedMessages +
                " total messages with an average latency of " + receivedLatency/receivedMessages + " ms");
    }

    public static void main(String[] args) throws MqttException, InterruptedException {

        if (args.length < 4) {
            System.out.println("Usage: [connection string] [QoS level] [number of publishers] [delay before publishers" +
                    "start sending messages] [how often publishers send messages (in ms)]");
            System.exit(0);
        }

        String connection = args[0];
        int setQos = Integer.parseInt(args[1]);
        int numPublishers = Integer.parseInt(args[2]);
        int startupTime = Integer.parseInt(args[3]);
        int messageFrequency = Integer.parseInt(args[4]);
        boolean allAtOnce = Boolean.parseBoolean(args[5]);

        NetworkBuilder networkBuilder = new NetworkBuilder(connection, setQos, numPublishers, startupTime,
                messageFrequency, allAtOnce);
        System.out.println("Building Publisher(s)...");
        networkBuilder.buildPublishers();
        System.out.println("Publisher(s) built.");
        System.out.println("Building Subscriber(s)...");
        networkBuilder.buildAndStartSubscribers();
        System.out.println("Subscriber(s) built.");
        System.out.println("Starting publishers...");
        networkBuilder.startPublishers();
        System.out.println("Publishing...");
        networkBuilder.messageLimit.await();
        networkBuilder.stopPublishers();
        networkBuilder.stopSubscribers();
    }
}
