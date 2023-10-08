package solar.forecast.mqqt.client;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.concurrent.CountDownLatch;

public class MqttClientHandler {

  private MqttClient client;
  private CountDownLatch latch;
  private MessageCallback messageCallback;

  public MqttClientHandler(String broker, String clientId) throws MqttException {
    MemoryPersistence persistence = new MemoryPersistence();
    this.client = new MqttClient(broker, clientId, persistence);
  }

  public void connectAndSubscribe(String topic, MessageCallback callback) throws MqttException {
    MqttConnectOptions connOpts = new MqttConnectOptions();
    connOpts.setCleanSession(true);

    System.out.println("Connecting to broker: " + client.getServerURI());
    client.connect(connOpts);
    System.out.println("Connected");

    latch = new CountDownLatch(1);

    // Subscribe to the response from the server
    client.subscribe(topic, (t, responseMessage) -> {
      String receivedResponse = new String(responseMessage.getPayload());
      System.out.println("Received response from server: " + receivedResponse);

      // Release the latch to allow the program to exit
      latch.countDown();

      // Call the custom callback
      if (messageCallback != null) {
        messageCallback.handleMessage(t, responseMessage);
      }
    });

    this.messageCallback = callback;
  }

  public void publishMessage(String topic, String message) throws MqttException {
    MqttMessage mqttMessage = new MqttMessage(message.getBytes());
    mqttMessage.setQos(2);

    System.out.println("Publishing message to server: " + message);
    client.publish(topic, mqttMessage);
  }

  public void waitForResponse(long timeout, java.util.concurrent.TimeUnit unit) throws InterruptedException {
    if (!latch.await(timeout, unit)) {
      System.out.println("No response received from the server in the last " + timeout + " " + unit.toString());
    }
  }

  public void disconnect() {
    try {
      client.disconnect();
      System.out.println("Disconnected");
    } catch (MqttException e) {
      e.printStackTrace();
    }
  }
}
