package solar.forecast.mqqt.client;

import org.eclipse.paho.client.mqttv3.MqttException;
import java.util.Scanner;

public class App {
    static String strasse, hausnummer, stadt, plz, bundesland, land, kwp;

    public static void main(String[] args) {
        clearScreen();
        AdresseEingabe();
        String broker = "tcp://localhost:1883";
        String clientId = "Client";

        try {
            MqttClientHandler mqttClientHandler = new MqttClientHandler(broker, clientId);

            MessageCallback callback = (topic, message) -> {
                // System.out.println("Diagram xyz");
            };

            mqttClientHandler.connectAndSubscribe("topic/client", callback);

            // Send a message to the server - CommandLine input
            // String message = "Moltkestraße&30&Karlsruhe&Baden-Württemberg&76133&DE";
            String message = String.format("%s&%s&%s&%s&%s&%s&%s", strasse, hausnummer, stadt, bundesland, plz, land,
                    kwp);

            mqttClientHandler.publishMessage("topic/server", message);

            // Wait for a reasonable time for the response
            mqttClientHandler.waitForResponse(10, java.util.concurrent.TimeUnit.SECONDS);

            // Disconnect the client
            mqttClientHandler.disconnect();

        } catch (MqttException | InterruptedException me) {
            me.printStackTrace();
        }
    }

    public static void clearScreen() {

        System.out.print("\033[H\033[2J");

        System.out.flush();

    }

    public static void AdresseEingabe() {
        // Ein Scanner-Objekt erstellen, um die Eingabe des Benutzers zu lesen
        Scanner scanner = new Scanner(System.in);

        // Den Benutzer nach seiner Straße fragen
        System.out.print("Bitte geben Sie Ihre Straße ein: ");
        strasse = scanner.nextLine();

        // Den Benutzer nach seiner Hausnummer fragen
        System.out.print("Bitte geben Sie Ihre Hausnummer ein: ");
        hausnummer = scanner.nextLine();

        // Den Benutzer nach seiner Postleitzahl fragen
        System.out.print("Bitte geben Sie Ihre Postleitzahl ein: ");
        plz = scanner.nextLine();

        // Den Benutzer nach seiner Stadt fragen
        System.out.print("Bitte geben Sie Ihre Stadt ein: ");
        stadt = scanner.nextLine();

        System.out.print("Bitte geben Sie Ihr Bundesland ein: ");
        bundesland = scanner.nextLine();

        // Den Benutzer nach seinem Land fragen
        System.out.print("Bitte geben Sie Ihr Länderkürzel ein (z.B. DE): ");
        land = scanner.nextLine();

        // Den Benutzer nach kwp der Photovoltaikanlage fragen
        System.out.print("Bitte geben Sie die Maximalleistung in KWP Ihrer Photovoltaikanlage ein (z.B. 3.4): ");
        kwp = scanner.nextLine();

        // Den Scanner schließen, um Ressourcen freizugeben
        scanner.close();

        System.out.println();
    }

}
