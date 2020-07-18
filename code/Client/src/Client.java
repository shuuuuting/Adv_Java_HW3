import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {

    String serverName;
    int port;
    Socket socket;
    ObjectInputStream input;
    ObjectOutputStream output;

    public Client(String name, int p) throws ClassNotFoundException {
        serverName = name;
        port = p;
        try {
            socket = new Socket(InetAddress.getByName(serverName), port);
            System.out.println("create client socket ok");
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("create output stream ok");
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());
            System.out.println("create input stream ok");
            String msg = (String)input.readObject();
            System.out.println(msg);
            int counter=0;
            while(counter<3){
                counter+=1;
                Scanner scanner = new Scanner(System.in);
                String password = scanner.nextLine();
                output.writeObject(password);
                output.flush();
                String message = (String)input.readObject();
                System.out.println(message);
            }
            String notification = (String)input.readObject();
            System.out.println(notification);
        } catch (UnknownHostException x) {
            x.printStackTrace();
        } catch (IOException x) {
            x.printStackTrace();
        }
    }

    public void exec() {
        Scanner scanner = new Scanner(System.in);
        //System.out.println("scanner=" + scanner); // server ready
        String command;

        do {
            command = scanner.nextLine();
            //System.out.println(command);
            //System.out.flush();
            try {
                output.writeObject(command);
                output.flush();
                String message = (String)input.readObject();
                System.out.print(message);

            } catch (ClassNotFoundException x) {
                x.printStackTrace();
                break;
            } catch (IOException x) {
                x.printStackTrace();
                break;
            }
        } while (!command.equals("quit") && !command.equals("exit"));

    }

    public static void main(String args[]) throws ClassNotFoundException {
        Client client = new Client("localhost", 1234);
        client.exec();
        //Scanner scanner = new Scanner(System.in);
        //String command = scanner.nextLine();
        //System.out.println(command);
    }
}