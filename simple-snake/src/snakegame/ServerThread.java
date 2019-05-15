package snakegame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tnc
 */
public class ServerThread implements Runnable {
    
    private final Snake snake;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    
    public ServerThread(Snake snake, int port) {
        this.snake = snake;
        try {    
            serverSocket = new ServerSocket(port);
            clientSocket = serverSocket.accept();
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        try {
            while(true) {
                String res = "";
                res = in.readLine();
                System.out.print("Going ");

                if(res.equals("n")) {
                    snake.goNorth();
                    System.out.println("north");
                } else if(res.equals("w")) {
                    snake.goWest();
                    System.out.println("west");
                } else if(res.equals("s")) {
                    snake.goSouth();
                    System.out.println("south");
                } else if(res.equals("e")) {
                    snake.goEast();
                    System.out.println("east");
                } else {
                    break;
                }
            }
            
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException ex) {
                Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }   
    }
}
