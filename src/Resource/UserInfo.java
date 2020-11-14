package Resources;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Semaphore;

/**
 * Created by Mateusz Szymański on 09.06.2020
 * User class uses to storing every user information
 */
public class UserInfo implements Serializable
{
    /**
     * Name of a online user
     */
    private String user_Name;
    /**
     * Storing path to server or user folder
     */
    private String dir;
    /**
     * Storing user sockets
     */
    private Socket user_Socket;
    /**
     * Storing user semaphore blocking OutputStream
     */
    private Semaphore block_Send;

    /**
     * This is a constructor to initialize user object
     * @param name an initial user name
     * @param dir an initial server or user dir
     * @param so an initial user socket
     * @param send an initial user semaphore for blocking OutputStream
     */
    public UserInfo(String name, String dir, Socket so, Semaphore send)
    {
        this.user_Name = name;
        this.dir = dir;
        this.user_Socket = so;
        this.block_Send = send;
    }

    /**
     * Get user name
     * @return user name
     */
    public String getUserName()
    {
        return this.user_Name;
    }

    /**
     * Get server or user folder
     * @return server or user folder path
     */
    public String getDir()
    {
        return this.dir;
    }

    /**
     * Get user socket
     * @return user socket
     */
    public Socket getUserSocket()
    {
        return this.user_Socket;
    }

    /**
     * Get user semaphore
     * @return user semaphore for blocking OutputStream
     */
    public Semaphore getBlockSend()
    {
        return this.block_Send;
    }

    /**
     * It creates the "Path" variable and checks if it can create a folder in the given location.
     * If so creates a new folder for the server or user,
     * if it doesn't return information that the folder exists
     */
    public void createDir()
    {
        try
        {
            Path path = Paths.get(this.dir);
            if(!Files.exists(path))
            {
                Files.createDirectories(path);
                System.out.println("Directory " + this.dir + " created");
            }
            else
            {
                System.out.println("Directory " + this.dir + " already exists");
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
