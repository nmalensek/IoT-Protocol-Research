package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MqttPublisher extends MqttNode implements Runnable {

    private TempAndHumiditySensor sensor;
    private int counter = 0;
    private int delay;
    private long interval;
    private CountDownLatch wait;
    private boolean running = true;

    public MqttPublisher(String connectionString, String topic) throws MqttException {
        super(connectionString, topic);
    }

    public void addSensor() {
        sensor = new TempAndHumiditySensor(client, topic, Id);
    }

    public void setSettings(int delay, long interval, CountDownLatch wait) {
        this.delay = delay;
        this.interval = interval;
        this.wait = wait;
    }

    public void stopPublishing() {
        System.out.println("Stopping publishing...");
        System.out.println("Publisher " + Id + " sent " + counter + " messages.");
    }

    public int getCounter() { return counter; }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        try {
            wait.await();
            Thread.sleep(delay);
            while(running) {
                sensor.call();
                counter++;
                Thread.sleep(interval);
            }
        } catch (InterruptedException ie) {

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
