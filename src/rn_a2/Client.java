/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rn_a2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abe235
 */
public class Client {
    //TCP-Verbindung aufbauen zum chat server
    //server namen und hostnamen mittteilen; hostname via Socket
    //regelmäßig liste vom server abfragen

    public static final int CLIENT_PORT = 50001;
    private String name;
    ;
    private Socket TCPSocket; // TCP-Standard-Socketklasse
    private String toServer;
    private DataOutputStream output; // Ausgabestream zum Server
    private BufferedReader input; // Eingabestream vom Server
    Map<String, String> allActiveUser;
    private DatagramSocket UDPSocket;
    private boolean serviceRequested = true;
    public static final int BUFFER_SIZE = 1024;
    private ICallback callback;

    //zum testen, weil ohne startdialog
    public Client() throws UnknownHostException {
        //this.TCPHost = TCPSocket.getInetAddress().getHostName();
        this.name = "test";
        allActiveUser = new HashMap<String, String>();
    }

    public Client(String name, String adr) throws SocketException {
        this.name = name;
        this.toServer = adr;
        this.UDPSocket = new DatagramSocket(CLIENT_PORT);
        allActiveUser = new HashMap<String, String>();
    }

    public void setCallback(ICallback c) {
        this.callback = c;
    }

    public String getName() {
        return name;
    }

    public List<String> getUsers() {
        return new ArrayList<String>() {
            {
                for (String key : allActiveUser.keySet()) {
                    add(key);
                }
            }
        };
    }

    public void disconnectFromServer() throws IOException {
        writeToServer("BYE");
        System.out.println(readFromServer());
        try {
            TCPSocket.close();
        } catch (IOException e) {
        }
    }

    public boolean connectToServer() throws IOException {
        TCPSocket = new Socket(toServer, TCPServer.SERVER_PORT);
        input = new BufferedReader(new InputStreamReader(
                TCPSocket.getInputStream()));
        output = new DataOutputStream(TCPSocket.getOutputStream());
        writeToServer("NEW " + getName());
        String inputString = readFromServer();
        if (inputString.equals("OK")) {
            readFromOther();
            return true;
        }
        if (inputString.startsWith("ERROR")) {
            TCPSocket.close();
        }
        return false;
    }

    public void updateUserlist() throws IOException {
        writeToServer("INFO");
        String inputString = readFromServer();
        if (inputString.startsWith("ERROR")) {
            TCPSocket.close();
        }
        String[] userList = inputString.split(" ");
        for (int i = 0; i < Integer.parseInt(userList[1]); i++) {
            allActiveUser.put(userList[i * 2 + 3], userList[i * 2 + 2]);
        }
    }

    private String readFromServer() throws IOException {
        return input.readLine();
    }

    private void writeToServer(String request) throws IOException {
        output.writeBytes(request + '\n');
    }

   

    public void writeToOther(final String sendString) {
        //Thread B -> den anderen senden
        new Thread(new Runnable() {
            @Override
            public void run() {
           
                byte[] sendData = sendString.getBytes();
                for (Map.Entry<String, String> elem : allActiveUser.entrySet()) {
                    try {
                        //if(!elem.getKey().equals(getName())){
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(elem.getValue()), CLIENT_PORT);
                        UDPSocket.send(sendPacket);
                        //}
                    } catch (IOException e) {
                        System.err.println(e.toString());
                        serviceRequested = false;
                        UDPSocket.close();
                        System.exit(1);
                    }
                }

            }
        }).start();
    }

    public void readFromOther() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    String receiveString = "";
                    String chatName = "";
                    try {
                        byte[] receiveData = new byte[BUFFER_SIZE];
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, BUFFER_SIZE);
                        UDPSocket.receive(receivePacket);

                        for (Map.Entry<String, String> elem : allActiveUser.entrySet()) {
                            String tmp = receivePacket.getAddress().getHostName();
                            System.out.println("paket " + tmp);
                            if (tmp.equals(elem.getValue())) {
                                System.out.println("paket " + tmp);
                                System.out.println("key" + elem.getKey());
                                chatName = elem.getKey();
                            }
                        }
                        receiveString = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    } catch (IOException e) {
                        System.err.println("Connection aborted by server!");
                        serviceRequested = false;
                    }

                    callback.call(chatName + ": " + receiveString);

                }
            }
        }).start();
    }
}
