package Client.Controller;

import Resources.UserInfo;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * public class used to control the JavaFX interface.
 */
public class ClientInterfaceController implements Initializable
{
    /**
     * Storing the online users list
     */
    private static List<String> online_users;

    /**
     * Storing the actual operation list
     */
    private static List<String> operation;

    /**
     * Storing the UserInfo object of the current user.
     */
    private static UserInfo client_Info;

    /**
     * Storing the user name which the file is to be sent.
     */
    private static String user_Name;

    /**
     * Storing the file name which will be sent.
     */
    private static String file_Name;

    /**
     * JavaFX ListView uses to display user files.
     */
    @FXML
    private ListView listViewID1 = new ListView();

    /**
     * JavaFX ListView uses to display online users.
     */
    @FXML
    private ListView listViewID2 = new ListView();

    /**
     * JavaFX ListView uses to display actual operation.
     */
    @FXML
    private ListView listViewID3 = new ListView();

    /**
     * Gets the online users list from the "ClientrMain.java" class.
     * @param list list an initial the online users list.
     */
    public void getOnlineUsersList(List<String> list)
    {
        online_users = list;
    }

    /**
     * Gets the actual operation list from the "ServerMain.java" class.
     * @param list an initial the actual operation list.
     */
    public void getOperationList(List<String> list)
    {
        operation = list;
    }

    /**
     * Gets the UserInfo object of the current user from the "ServerMain.java" class.
     * @param Client an initial the UserInfo object of the current user.
     */
    public void getUserInfo(UserInfo Client)
    {
        client_Info = Client;
    }

    /**
     * Called to initialize a controller after its root element has been completely processed.
     * This initialize creating thread to refresh the users list, thread to refresh the user files,
     * thread to take to refresh the actual operation list, thread to save the selected file and user.
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        Thread refresh_List_User = new Thread(this::refreshUsersList);
        refresh_List_User.setDaemon(true);
        refresh_List_User.start();

        Thread init_Action = new Thread(this::refreshUserFiles);
        init_Action.setDaemon(true);
        init_Action.start();

        Thread operation_List = new Thread(this::refreshOperationList);
        operation_List.setDaemon(true);
        operation_List.start();

        Thread get_Name = new Thread(this::getUserName);
        get_Name.setDaemon(true);
        get_Name.start();

        Thread get_File = new Thread(this::getFileName);
        get_File.setDaemon(true);
        get_File.start();
    }

    /**
     * It is a thread that refreshes the user list
     */
    public void refreshUsersList()
    {
        ArrayList<String> listOfUserName = new ArrayList<>();
        while (true)
        {
            Platform.runLater(() -> {
                listOfUserName.clear();
                for (String x : online_users) {
                    listOfUserName.add(x);
                }
                ObservableList<String> uList = FXCollections.observableArrayList(listOfUserName);
                listViewID2.setItems(uList);
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
     * It is a thread that refreshed the file list of user.
     */
    public void refreshUserFiles()
    {
        ArrayList<String> listOfFile = new ArrayList<>();
        while (true)
        {
            Platform.runLater(() -> {
                listOfFile.clear();
                File Dir = new File(client_Info.getDir());
                File[] listOfFiles = Dir.listFiles();
                for(int i = 0; i<listOfFiles.length; i++)
                {
                    listOfFile.add(listOfFiles[i].getName());
                }
                ObservableList<String> uList = FXCollections.observableArrayList(listOfFile);
                listViewID1.setItems(uList);
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
     * It is a thread that refreshed the actual operation list.
     */
    public void refreshOperationList()
    {
        ArrayList<String> listOfOperation = new ArrayList<>();
        while (true)
        {
            Platform.runLater(() -> {
                listOfOperation.clear();
                if(operation.size() > 0)
                {
                    listOfOperation.add(operation.get(0));
                    operation.remove(0);
                }
                else
                {
                    listOfOperation.add("Waiting...");
                }

                ObservableList<String> uList = FXCollections.observableArrayList(listOfOperation);
                listViewID3.setItems(uList);
            });
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * It is a thread that set user name which the file is to be sent.
     */
    public void getUserName()
    {
        while(true)
        {
            Platform.runLater(() -> {
                listViewID2.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        if (newValue != null)
                        {
                            user_Name = newValue;
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

    /**
     * It is a thread that set file name which will be sent.
     */
    public void getFileName()
    {
        while (true)
        {
            Platform.runLater(() -> {
                listViewID1.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>()
                {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
                    {
                        if (newValue != null)
                        {
                            file_Name = newValue;
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

    /**
     * It is a mouseEvent that send information about the end of operation to the server.
     * @param mouseEvent An event which indicates that a mouse action occurred in a component.
     */
    public void mouseClicked(MouseEvent mouseEvent)
    {
        try
        {
            client_Info.getBlockSend().acquire();
            DataOutputStream data_Send = new DataOutputStream(client_Info.getUserSocket().getOutputStream());

            data_Send.writeUTF("exit_Client");
            data_Send.flush();
            client_Info.getBlockSend().release();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * It is a mouseEvent that send information about user name and file name to be shared.
     * @param mouseEvent An event which indicates that a mouse action occurred in a component.
     */
    public void ClickedShare(MouseEvent mouseEvent)
    {
        if(user_Name != null & file_Name != null)
        {
            try
            {
                client_Info.getBlockSend().acquire();
                DataOutputStream data_Send = new DataOutputStream(client_Info.getUserSocket().getOutputStream());
                data_Send.writeUTF("share_File");
                data_Send.flush();
                data_Send.writeUTF(user_Name);
                data_Send.flush();
                data_Send.writeUTF(file_Name);
                data_Send.flush();
                client_Info.getBlockSend().release();
            }
            catch (IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
