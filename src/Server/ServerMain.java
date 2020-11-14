package Server;

import Resources.UserInfo;
import Server.Controller.ServerInterfaceController;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server class used to start serversocket and JavaFX interface.
 */
public class ServerMain extends Application
{
    /**
     * Storing online users list. The list is protected by "Collections.synchronizedList" because it is used by many threads.
     */
    private static List<UserInfo> online_Users = Collections.synchronizedList(new ArrayList<UserInfo>());

    /**
     * Main creates serverSocket with the given port, starts a thread that allows clients to connect and runs the JavaFX interface.
     * @param args startup parameters
     */
    public static void main(String[] args)
    {
        try
        {
            ServerSocket Server = new ServerSocket(5100);
            System.out.println("Server is running");
            Thread server_Thread = new ServerThread(Server, online_Users);
            server_Thread.start();

            launch(args);
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Method that loads the fxml file and retrieves the settings from the assigned controller.
     * Gets the list of online users which will be refreshed in controller thread.
     * @param primaryStage main interface window.
     * @throws Exception exception that occurs when a fxml file is loaded incorrectly.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Server/Interface/ServerInterface.fxml"));
        AnchorPane anchorPane = loader.load();
        primaryStage.setTitle("Server files");

        ServerInterfaceController sceneControl = loader.getController();
        sceneControl.getOnlineUsersList(online_Users);

        Scene scene = new Scene(anchorPane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
