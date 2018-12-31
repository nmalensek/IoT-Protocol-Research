# IoT-Protocol-Research (MQTT research)

The PublisherBuilder class creates the specified number of MqttPublisher objects,
and instructs them to either begin publishing immediately or wait until all
publishers have been constructed.

Command line arguments:
[connection string] [QoS level] [number of publishers]
[delay before publishers start sending messages]
[how often publishers send messages (in ms)]
[whether publishers wait to publish until all are constructed]
[total process duration (in ms)]

The SubscriberBuilder class creates a subscriber that subscribes to all
available topics through the wildcard operator ("#"). This process contains
a thread that writes out the number of messages received and latency per one-second
window.

Command line arguments:
[connection string] [process duration]
