package mqtt.construction;

import mqtt.network.MqttSubscriber;
import mqtt.util.ProcessTimer;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.ArrayList;

public class SubscriberBuilder implements Builder {

//    private static final String TOPIC = "group1/tempAndHumidity";
    private static final String TOPIC = "#"; //wildcard, subscribe to all topics

    private ProcessTimer timer;
    private ArrayList<MqttSubscriber> subscribers = new ArrayList<>();
    private String connectionUri;
    private long duration;

    public SubscriberBuilder(String connectionUri, long duration) {
        this.connectionUri = connectionUri;
        this.duration = duration;
    }


    @Override
    public void start() {
        try {
            MqttSubscriber subscriber = new MqttSubscriber(connectionUri, TOPIC);
            subscriber.connectToBroker();
            subscribers.add(subscriber);
            subscriber.subscribe();
            new Thread(subscriber).start();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        timer = new ProcessTimer(duration, this);
        timer.start();
    }

    @Override
    public void stop() {
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

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: [connection string] [process duration]");
            System.exit(0);
        }

        String connection = args[0];
        long runTime = Long.parseLong(args[1]);

        SubscriberBuilder subscriberBuilder = new SubscriberBuilder(connection, runTime);
        System.out.println("Building Subscriber(s)...");
        subscriberBuilder.start();
        System.out.println("Subscriber(s) built.");
    }
}
