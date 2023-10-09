package solar.forecast.mqqt.client;

import org.eclipse.paho.client.mqttv3.MqttException;

public class App {

    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "Client";

        try {
            MqttClientHandler mqttClientHandler = new MqttClientHandler(broker, clientId);

            MessageCallback callback = (topic, message) -> {
                System.out.println("Diagram xyz");
            };

            mqttClientHandler.connectAndSubscribe("topic/client", callback);

            // Send a message to the server - CommandLine input
            String message = "Moltkestraße&30&Karlsruhe&Baden-Württemberg&76133&DE";
            mqttClientHandler.publishMessage("topic/server", message);

            // Wait for a reasonable time for the response
            mqttClientHandler.waitForResponse(10, java.util.concurrent.TimeUnit.SECONDS);

            // Disconnect the client
            mqttClientHandler.disconnect();

        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();
        }
    }
}
