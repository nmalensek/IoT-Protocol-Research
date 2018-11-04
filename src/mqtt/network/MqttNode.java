package mqtt.network;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttNode {

    String connectionString;
    String topic;
    MqttClient client;
    String Id;

    public MqttNode(String connectionString, String topic) throws MqttException {
        this.connectionString = connectionString;
        this.topic = topic;
        this.Id = MqttClient.generateClientId();
        client = new MqttClient(connectionString, Id, null);
    }

    //if using setCleanSession(false), then do not set persistence to null above
    public void connectToBroker() throws MqttException {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        client.connect(options);
    }
}
