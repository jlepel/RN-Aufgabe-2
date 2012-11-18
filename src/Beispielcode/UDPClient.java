/*
 * UDPClient.java
 *
 * Version 2.0
 * Vorlesung Rechnernetze HAW Hamburg
 * Autor: M. H�bner (nach Kurose/Ross)
 * Zweck: UDP-Client Beispielcode:
 *        UDP-Socket erzeugen, einen vom Benutzer eingegebenen
 *        String in ein UDP-Paket einpacken und an den UDP-Server senden, 
 *        den String in Gro�buchstaben empfangen und ausgeben
 */
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPClient {

  public final int SERVER_PORT = 9876;
  public static final int BUFFER_SIZE = 1024;

  private DatagramSocket clientSocket;  // UDP-Socketklasse
  private InetAddress serverIpAddress;  // IP-Adresse des Zielservers

  private boolean serviceRequested = true;

  /* Client starten. Ende, wenn quit eingegeben wurde */
  public void startJob() {
    Scanner inFromUser;

    String sentence;
    String modifiedSentence;

    /* Ab Java 7: try-with-resources mit automat. close benutzen! */
    try {
      /* UDP-Socket erzeugen (kein Verbindungsaufbau!)
       * Socket wird an irgendeinen freien (Quell-)Port gebunden, da kein Port angegeben */
      clientSocket = new DatagramSocket();
      serverIpAddress = InetAddress.getByName("localhost"); // Zieladresse

      /* Konsolenstream (Standardeingabe) initialisieren */
      inFromUser = new Scanner(System.in);
      
      while (serviceRequested) {
        System.out.println("ENTER UDP-DATA: ");
        /* String vom Benutzer (Konsoleneingabe) holen */
        sentence = inFromUser.nextLine();
        
        /* Sende den String als UDP-Paket zum Server */
        writeToServer(sentence);

        /* Modifizierten String vom Server empfangen */
        modifiedSentence = readFromServer();

        /* Test, ob Client beendet werden soll */
        if (modifiedSentence.indexOf("QUIT") > -1) {
          serviceRequested = false;
        }
        
      }
      /* Socket schlie�en (freigeben)*/
      clientSocket.close();
    } catch (IOException e) {
      System.err.println(e.toString());
      System.exit(1);
    }
    System.out.println("UDP Client stopped!");
  }
  private void writeToServer(String sendString) {
    /* Sende den String als UDP-Paket zum Server */
    try {
        /* String in Byte-Array umwandeln */
        byte[] sendData = sendString.getBytes();
        /* Paket erzeugen */
        DatagramPacket sendPacket =
          new DatagramPacket(
            sendData,
            sendData.length,
            serverIpAddress,
            SERVER_PORT);
        /* Senden des Pakets */
        clientSocket.send(sendPacket);
    } catch (IOException e) {
      System.err.println(e.toString());
      serviceRequested = false;
    }
    System.out.println("UDP Client has sent the message: " + sendString);
  }

  private String readFromServer() {
    /* Liefere den n�chsten String vom Server */
    String receiveString = "";

    try {
        /* Paket f�r den Empfang erzeugen */
        byte[] receiveData = new byte[BUFFER_SIZE];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, BUFFER_SIZE);

        /* Warte auf Empfang des Antwort-Pakets auf dem eigenen Port */
        clientSocket.receive(receivePacket);

        /* Paket wurde empfangen --> auspacken und Inhalt anzeigen */
        receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());
    } catch (IOException e) {
      System.err.println("Connection aborted by server!");
      serviceRequested = false;
    }
    System.out.println("UDP Client got from Server: " + receiveString);
    return receiveString;
  }

  public static void main(String[] args) {
    UDPClient myClient = new UDPClient();
    myClient.startJob();
  }
}
