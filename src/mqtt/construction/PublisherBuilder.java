package mqtt.construction;

import mqtt.network.MqttPublisher;
import mqtt.util.ProcessTimer;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class PublisherBuilder implements Builder {
    private CountDownLatch waitLatch;
    private ArrayList<MqttPublisher> publishers = new ArrayList<>();
    private static final String TOPIC = "group1/tempAndHumidity";
    private String connectionUri;
    private int numPublishers;
    private int waitTime;
    private int messageFrequency;
    private int totalMessages;
    private int qosLevel;
    private long processDuration;
    private ProcessTimer timer;
    private boolean allAtOnce;

    public PublisherBuilder(String connectionUri, int qosLevel, int numPublishers,
                            int waitTime, int messageFrequency, boolean allAtOnce, long processDuration) {
        this.connectionUri = connectionUri;
        this.qosLevel = qosLevel;
        this.numPublishers = numPublishers;
        this.waitTime = waitTime;
        this.messageFrequency = messageFrequency;
        this.processDuration = processDuration;
        this.allAtOnce = allAtOnce;
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
            if (!allAtOnce) { new Thread(publisher).start(); }
            if (publishers.size() % 10 == 0) {
                System.out.println("Built " + publishers.size() + " publishers of 100");
            }
        }
        timer = new ProcessTimer(processDuration, this);
        timer.start();
    }

    @Override
    public void start() {
        for (MqttPublisher p : publishers) {
            new Thread(p).start();
            waitLatch.countDown();
        }
    }

    @Override
    public void stop() {
        for (MqttPublisher p : publishers) {
            p.setRunning(false);
//            p.stopPublishing();
            totalMessages += p.getCounter();
        }
        System.out.println("Publishers sent " + totalMessages + " messages.");
    }

    public static void main(String[] args) throws MqttException {

        if (args.length < 4) {
            System.out.println("Usage: [connection string] [QoS level] [number of publishers] [delay before publishers" +
                    "start sending messages] [how often publishers send messages (in ms)] " +
                    "[whether publishers wait to publish until all are constructed] [total process duration (in ms)]");
            System.exit(0);
        }

        String connection = args[0];
        int setQos = Integer.parseInt(args[1]);
        int numPublishers = Integer.parseInt(args[2]);
        int startupTime = Integer.parseInt(args[3]);
        int messageFrequency = Integer.parseInt(args[4]);
        boolean allAtOnce = Boolean.parseBoolean(args[5]);
        long processDuration = Long.parseLong(args[6]);

        PublisherBuilder publisherBuilder = new PublisherBuilder(connection, setQos, numPublishers, startupTime,
                messageFrequency, allAtOnce, processDuration);

        System.out.println("Building Publisher(s)...");
        publisherBuilder.buildPublishers();
        System.out.println("Publisher(s) built.");
        System.out.println("Starting publishers...");
        if (allAtOnce) {
            publisherBuilder.start();
        }
        System.out.println("Publishing...");
    }

}
