package Server;

import Resources.UserInfo;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread used to connecting clients.
 */
public class ServerThread extends Thread
{
    /**
     * Storing online users list.
     */
    final List<UserInfo> online_Users;

    /**
     * Storing user sockets.
     */
    final ServerSocket server;

    /**
     * Storing path to main server folder.
     */
    final static String dir = "C:\\Projekt\\ServerFiles";

    /**
     * This is a constructor to initialize server thread.
     * @param server an initial ServerSocket.
     * @param list_Users an initial list of UserInfo object (online users list).
     */
    public ServerThread(ServerSocket server, List<UserInfo> list_Users)
    {
        this.server = server;
        this.online_Users = list_Users;
    }

    /**
     * First of all creates a thread responsible for sending to all connected users a list of users.
     * A loop is being started that waits for client connection, the server receives the username, creates a folder and object for it.
     * After that the server is waiting for logging in.
     * If the login is correct, the object will be added to the list and threads responsible for
     * checking files, sending files and receiving information from the client will be started for it.
     * If the login is incorrect, the wrong name will be sent back and the server will be waiting for the new name.
     */
    @Override
    public void run()
    {
        Thread server_List_Sender = new ServerListSender(online_Users);
        server_List_Sender.start();

        while(true)
        {
            try
            {
                System.out.println("Waiting for the client...");
                Socket client = server.accept();
                System.out.println("Connection from " + client.getLocalPort() + client.getLocalAddress());

                DataInputStream data_Receive = new DataInputStream(client.getInputStream());
                DataOutputStream data_Send = new DataOutputStream(client.getOutputStream());

                String user_Name = data_Receive.readUTF();
                String server_Dir = dir + "\\" + user_Name.toUpperCase();
                UserInfo new_User;

                String message = data_Receive.readUTF();
                while(true)
                {
                    if(message.equals(user_Name))
                    {
                        System.out.println("Correct username");
                        Semaphore block_Send = new Semaphore(1);

                        new_User = new UserInfo(user_Name, server_Dir, client, block_Send);
                        this.online_Users.add(new_User);
                        data_Send.writeUTF("OK");
                        break;
                    }
                    else if(message.equals("Exit"))
                    {
                        client.close();
                        System.exit(0);
                    }

                    System.out.println("Username " + message + " not found");
                    data_Send.writeUTF("BAD");
                    data_Send.flush();
                    message = data_Receive.readUTF();
                }
                List<String> last_Operation = Collections.synchronizedList(new ArrayList<String>());
                Thread server_Files_Check = new ServerFilesCheck(new_User, last_Operation);
                server_Files_Check.start();
                Thread server_Receive = new ServerReceive(online_Users, new_User, last_Operation);
                server_Receive.start();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}