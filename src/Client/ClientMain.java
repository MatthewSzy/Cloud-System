package Client;

import Client.Controller.ClientInterfaceController;
import Resources.UserInfo;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Client class used to connecting client to server and starting JavaFX interface.
 */
public class ClientMain extends Application
{
    /**
     * Storing online users list. The list is protected by "Collections.synchronizedList" because it is used by many threads.
     */
    private static List<String> online_users = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Storing the operation list where is the information about the actual operation made by threads.
     */
    private static List<String> operation_List = Collections.synchronizedList(new ArrayList<String>());

    /**
     * Storing the UserInfo object of the current user.
     */
    private static UserInfo client_Data;

    /**
     * Main creates client socket with the given host and port, after that the client sends its name to the server which creates a folder for it.
     * The UserInfo object is create and the user is asked to send a login (his username).
     * If the login is correct,threads responsible for checking files, sending files and receiving information from the server will be started for it.
     * Finally the JavaFX window opens.
     * @param args startup parameters
     */
    public static void main(String[] args)
    {
        try
        {
            System.out.println("Connecting to Server...");
            Socket client = new Socket("localhost", 5100);
            System.out.println("Connected to Server");

            BufferedReader input_message = new BufferedReader(new InputStreamReader(System.in));
            DataOutputStream data_Send = new DataOutputStream(client.getOutputStream());
            DataInputStream data_Receive = new DataInputStream(client.getInputStream());

            data_Send.writeUTF(args[0]);
            Semaphore block_Send = new Semaphore(1);

            client_Data = new UserInfo(args[0], args[1], client, block_Send);
            client_Data.createDir();

            while(true)
            {
                System.out.println("Input your username: ");
                String login = input_message.readLine();
                data_Send.writeUTF(login);
                data_Send.flush();

                if(login.equals("Exit"))
                {
                    client.close();
                    System.exit(0);
                }
                login = data_Receive.readUTF();

                if(login.equals("OK")) break;
                else if(login.equals("BAD"))
                {
                    System.out.println("Incorrect username");
                }
            }

            System.out.println("Welcome " + client_Data.getUserName() + "!");

            List<String> last_Operation = Collections.synchronizedList(new ArrayList<String>());

            Thread client_Files_Check = new ClientFilesCheck(client_Data, last_Operation, operation_List);
            client_Files_Check.start();

            Thread client_Receive = new ClientReceive(client_Data, online_users, last_Operation, operation_List);
            client_Receive.start();
            launch(args);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method that loads the fxml file and retrieves the settings from the assigned controller.
     * Gets the list of online users, operation list and the UserInfo object of the current user which will be refreshed in controller thread.
     * @param primaryStage main interface window
     * @throws Exception exception that occurs when a fxml file is loaded incorrectly
     */
    @Override
    public void start(Stage primaryStage) throws Exception
    {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client/Interface/ClientInterface.fxml"));
        AnchorPane anchorPane = loader.load();
        primaryStage.setTitle(client_Data.getUserName() + "'s files");

        ClientInterfaceController sceneControl = loader.getController();
        sceneControl.getOnlineUsersList(online_users);
        sceneControl.getOperationList(operation_List);
        sceneControl.getUserInfo(client_Data);

        Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
