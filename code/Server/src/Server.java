import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.Thread;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
enum States{LOGIN,INTERACTION,ERRORPASS,EXIT;}

public class Server extends Thread {
    
    class Connection extends Thread {

        Socket socket;
        ObjectInputStream input;
        ObjectOutputStream output;
        
        public Connection(Socket s) throws ClassNotFoundException {
            socket = s;
            try {
                System.out.println("open input stream");
                input = new ObjectInputStream(socket.getInputStream());
                state = States.LOGIN;
                System.out.println("open output stream");
                output = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("open output stream ok");
                output.writeObject("Please enter your password:");
                int counter=0;
                while(counter<3){
                    output.flush();
                    counter+=1;
                    String cliinput =(String)input.readObject();
                    System.out.println("Client is entering: "+cliinput);
                    if (cliinput.equals("0000")){
                        System.out.println("Connenct successfully!");
                        output.writeObject("Login successfully!\nPlease enter your command:");
                        state = States.INTERACTION;
                        break;
                    }
                    else{
                        if(counter<=2)output.writeObject("Wrong Password...Please try again:");
                        else output.writeObject("Wrong Password...");
                        state = States.ERRORPASS;
                    }
                }
                if(counter==3) {
                    output.writeObject("You have no chance.Disconnected...");
                    state = States.EXIT;
                    close();
                }
            } catch (IOException x) {
                x.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                output.flush();
                while (state == States.INTERACTION) {
                    String received= (String) input.readObject();  //blocking
                    System.out.println("receiving "  + received);
                    if (received.equals("exit") || received.equals("quit")) {
                        state = States.EXIT;
                        close();
                        return;
                    }
		    /*String tokens[] = received.split(" ");
		    if(tokens[0].equals("dir")) {
			File dir = tokens.length > 1 ? new File(tokens[1]):new File(".");
			StringBuilder s = new StringBuilder();
			for(File file : dir.listFiles()) {
				 s.append("\n" +file.getName()); ;
			}
			System.out.println(s.toString());
			String message = "--------requested directory information ------\n"+ s.toString();
			output.writeObject(message);
		    }
		    else if(tokens[0].equals("del")) {
			output.writeObject("return directory information as follows");
		    }*/
                    Runtime runtime = Runtime.getRuntime();
                    Process process=runtime.exec(received);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    StringBuffer sb=new StringBuffer();
                    while ((line = reader.readLine()) != null) {
                        sb.append("\n"+line); 
                        System.out.println(line);
                    }
                    output.writeObject(sb.toString());
                    //output.writeObject(command + " !!!!");
                }
            } catch (ClassNotFoundException x) {
                x.printStackTrace();
            } catch (IOException x) {
                System.out.println(x);
                close();
            }
        }
        public void close() {
            try {
                System.out.println("close connection...");
                input.close();
                output.close();
                socket.close();
                removeConnection(this);
            } catch (IOException x) {
                x.printStackTrace();
            }
        }
    }
    States state;
    int counter=0; 
    int port;
    int maxConnections;
    ServerSocket serverSocket;
    List<Connection> connections;
    public Server(int p, int c) {
        port = p;
        maxConnections = c;
        connections =  Collections.synchronizedList(new LinkedList());
        try {
            serverSocket = new ServerSocket(port, maxConnections);
        } catch (IOException x) {
            x.printStackTrace();
        }
    }


    void removeConnection(Connection connection) {
        connections.remove(connection);
    }
    @Override
    public void run() {
        while (true) {
            try {
                // connection Socket
                System.out.println("wait for connections...");
                Socket connSocket = serverSocket.accept();
                System.out.println("create connection socket");
                Connection connection = new Connection(connSocket);
                System.out.println("maxConnections=" + maxConnections);
                if(connections.size() < maxConnections) {
                    connections.add(connection);
                    connection.start();
                }
            } catch (IOException x) {
                x.printStackTrace();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    public static void main(String args[]) {
        Server server = new Server(1234, 100);
        server.start();
    }
}

