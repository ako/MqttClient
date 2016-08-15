# Mendix MQTT connector

Mendix module to send and receive [MQTT][1] messages. This module uses the [Eclipse Paho][3] library. 
Tested with [AWS IoT][5], [The Things network][4] and [mosquitto][2] brokers.

## Usage

Main java actions:

 * MqttPublish - publish a message to specified topic
 * MqttSubscribe - subscribe to a topic. Required you to specify a microflow which will be called upon receiving
   a message. This microflow should have two string parameters: Topic and Payload.
 * MqttUnsubscribe - unsubscribe from topic

  ![MQTT Microflow actions toolbox][9]

## Usage with Amazon AWS IoT
 
You need to register your app as a Thing on AWS IoT. Download the generated certificates and store them in your resources 
folder of your app. When subscribing or publishing a message specify their location reletive to the resources folder.

## Usage with TTN

For TTN you can leave the certifice info empty, instead provide username and password.

Microflow to subscribe to an MQTT topic:

 ![MQTT subscribe to topic][10]

Configuration of subscribe for TTN:

 ![][11]

Microflow to handled messages received:

 ![][12]

## Development

Java dependencies are managed using Apache Ivy. There are two configuration:
* export - this is used to make sure only the required jars for the connector module are in userlib
* default - this downloads all dependencies required to run the project

Before you export the connector module run runivy-export.cmd to ensure you have the correct set of libraries to be
included in the connector mpk.

## Version history

* 0.9 - 2016-08-13 - initial release
  * Implementation
  * Fix for subscribing to multiple topics with different microflows
  * Fix to ensure unique client id

 [1]: http://mqtt.org/
 [2]: http://mosquitto.org/
 [3]: http://www.eclipse.org/paho/
 [4]: http://thethingsnetwork.org/
 [5]: https://aws.amazon.com/iot/
 [6]: https://staging.thethingsnetwork.org/wiki/Backend/Connect/Application
 [7]: https://staging.thethingsnetwork.org/wiki/Backend/Security
 [8]: https://staging.thethingsnetwork.org/wiki/Backend/ttnctl/QuickStart
 [9]: docs/images/mqtt-toolbox.png
 [10]: docs/images/mqtt-subscribe-action.png
 [11]: docs/images/mqtt-subscribe-ttn.png
 [12]: docs/images/mqtt-ttn-on-message-mf.png
