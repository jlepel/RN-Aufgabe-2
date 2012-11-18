/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rn_a2;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import rn_a2.ServerThread.ClientContainer;






//vielleicht geht auch vererbung von Server -> irgendwann mal ausproberen
/**
 *
 * @author abe235
 */
public class TCPServer extends Thread {
    
    public static void main(String[] args) {
		
//	MailReader.printMailLine("0LeOC9-1T3BhM3xKH-00qlSl.txt");


		TCPServer server = new TCPServer();	
                System.out.println("start");
                server.run();
		
}

    //wartet auf TCP-Verbindungsanfragen
    public static final int SERVER_PORT = 50000;
    public static final String SERVER_IP = "localhost";
    private static List<ClientContainer> clientList = new ArrayList();


    //TODO: synchronized machen -> T'S FRAGEN DRAUF AB
    public static synchronized List<ClientContainer> getClientList(){
        return clientList;
    }
    
    public static synchronized void addToClientList(ClientContainer client){
        clientList.add(client);
    }
    
    //TODO
    public static synchronized void deleteFromClientList(String hostname){
        for(ClientContainer elem : clientList){
            clientList.remove(elem); if (elem.getHost().equals(hostname));
        }
        
    }
        
        
    public static synchronized int sizeClientList(){
          return clientList.size();
    }
    
    //ChatClient muss daher fur die Anmeldung eine TCP]Verbindung zum Chat]Server
    //aufbauen, dem Chat]Server seinen Chat]Namen mitteilen (den Hostnamen des neuen Clients kann
    //der Chat]Server aus dem Socket ermitteln)
    public void run() {
        ServerSocket welcomeSocket; // TCP-Server-Socketklasse
        Socket connectionSocket; // TCP-Standard-Socketklasse

        int counter = 0; // Z�hlt die erzeugten Bearbeitungs-Threads

        // -------------------------------

        try {
            welcomeSocket = new ServerSocket(SERVER_PORT);
            welcomeSocket.setReuseAddress(true); //zum debuggen!!!!!

            while (true) { // Server laufen IMMER

                connectionSocket = welcomeSocket.accept();

                /* Neuen Arbeits-Thread erzeugen und den Socket �bergeben */
                (new ServerThread(++counter, connectionSocket))
                        .start();
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
}
class ServerThread extends Thread {
    /*
     * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
     * erhaelt
     */

    private int name;
    private Socket socket;
    private boolean loggedIn =false;
    private boolean serviceRequested = true;
    private String hostname;
    private BufferedReader input;
    private DataOutputStream output;

    public ServerThread(int num, Socket sock) {
        this.name = num;
        this.socket = sock;
        hostname = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            input = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            output = new DataOutputStream(socket.getOutputStream());
 		while (!loggedIn) {
			String command = readFromClient();
                        getCommand(command); 
             } 
                while (serviceRequested && loggedIn) {
                    String command = readFromClient();
                    getCommand(command);
                    
                }
            socket.close();
        } catch (IOException e) {
            System.err.println(e.toString());
            System.exit(1);
        }
    }
   

//	private void userLogIn() throws IOException {
    public void getCommand(String command) throws IOException {
        if (command.startsWith("NEW ") && command.length() > 4  && !loggedIn) {
            String args = command.substring(4);
             checkLogin(args);
        } else if (command.equals("INFO") && loggedIn) {
            getInfo();
        } else if (command.equals("BYE") && loggedIn) {
            logout();
        } else {
            writeToClient("ERROR Command not valid");
        }
    }

    public boolean checkName(String name) {
        for (ClientContainer elem : TCPServer.getClientList()) {
            if (elem.getName().equals(name)) {
                return false;
            }
        }
        //sterchen wahrscheinlich unnötig
        return name.matches("[0-9a-zA-Z]*") && name.length() <= 20;
    }

    public void checkLogin(String args) throws IOException {
        if (loggedIn = checkName(args)) {
            ClientContainer c = new ClientContainer(args, socket.getInetAddress().getHostName());
            TCPServer.addToClientList(c);
            writeToClient("OK");
        } else {
            writeToClient("ERROR Name not valid");
        }
    }

    public void getInfo() throws IOException {
        String allUser = "LIST " + TCPServer.sizeClientList();

        for (ClientContainer elem : TCPServer.getClientList()) {
            
            allUser += " " + elem.getHost() + " " + elem.getName();
        }
        writeToClient(allUser);

    }

    public void logout() throws IOException {
        //TODO: fertig stellen
        writeToClient("BYE");
        TCPServer.deleteFromClientList(socket.getInetAddress().getHostName());
        loggedIn = false;
        
    }

    private String readFromClient() throws IOException {
        return input.readLine();

    }

    private void writeToClient(String message) throws IOException {
        output.writeBytes(message + '\r' + '\n');

    }
    
    class ClientContainer {
        private String name;
        private String host;
        
        public ClientContainer(String name, String host){
            this.host = host;
            this.name = name;
        }
        
        public String getName(){
            return name;
        }
        
        public String getHost(){
            return host;
        }
        
        
    }
}
