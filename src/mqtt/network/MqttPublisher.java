package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqttPublisher extends MqttNode {

    private TempAndHumiditySensor sensor;
    private Long sensorIdCounter = 1L;
    private ScheduledExecutorService executorService;

    public MqttPublisher(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
    }

    public void addSensor() {
        sensor = new TempAndHumiditySensor(client, topic, sensorIdCounter);
    }

    public void startPublishing(int delay, int period, TimeUnit interval) {
        System.out.println("Starting to publish");
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
           try {
                sensor.call();
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
        }, delay, period, interval);
    }

    public void stopPublishing() {
        System.out.println("Stopping publishing...");
        executorService.shutdown();
    }
}
