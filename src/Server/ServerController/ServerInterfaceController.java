package Server.Controller;

import Resources.UserInfo;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * public class used to control the JavaFX interface.
 */
public class ServerInterfaceController implements Initializable
{
    /**
     * Storing online users list
     */
    private static List<UserInfo> online_users;

    /**
     * Gets a list from the "ServerMain.java" class.
     * @param list an initial
     */
    public void getOnlineUsersList(List<UserInfo> list)
    {
        online_users = list;
    }

    /**
     * JavaFX ListView uses to display online users.
     */
    @FXML
    private ListView listViewID = new ListView();

    /**
     * JavaFX ListView uses to display user files.
     */
    @FXML
    private ListView listViewID2 = new ListView();

    /**
     * Called to initialize a controller after its root element has been completely processed.
     * This initialize creating thread to refresh the users list and thread to refresh the user files.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Thread refresh_List_User = new Thread(this::refreshUsersList);
        refresh_List_User.setDaemon(true);
        refresh_List_User.start();

        Thread init_Action = new Thread(this::setFileView);
        init_Action.setDaemon(true);
        init_Action.start();
    }

    /**
     * It is a thread that refreshes the user list
     */
    public void refreshUsersList()
    {
        ArrayList<String> listOfUsers = new ArrayList<>();
        while (true)
        {
            Platform.runLater(() -> {
                listOfUsers.clear();
                for (UserInfo x : online_users)
                {
                    listOfUsers.add(x.getUserName());
                }
                ObservableList<String> uList = FXCollections.observableArrayList(listOfUsers);
                listViewID.setItems(uList);
            });

            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * It is a thread that set the file list of the selected user.
     */
    public void setFileView()
    {
        ArrayList<String> listOfFile = new ArrayList<>();
        while(true)
        {
            Platform.runLater(() -> {
                listViewID.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        if(newValue != null)
                        {
                            listOfFile.clear();
                            for(UserInfo x : online_users)
                            {
                                if(x.getUserName().equals(newValue))
                                {
                                    File Dir = new File(x.getDir());
                                    File[] listOfFiles = Dir.listFiles();
                                    for(int i = 0; i<listOfFiles.length; i++)
                                    {
                                        listOfFile.add(listOfFiles[i].getName());
                                    }
                                    ObservableList<String> uList = FXCollections.observableArrayList(listOfFile);
                                    listViewID2.setItems(uList);
                                    break;
                                }
                            }
                        }
                    }
                });
            });

            try
            {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
