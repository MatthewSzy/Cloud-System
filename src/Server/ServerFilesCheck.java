package Server;

import Resources.UserInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to checking user files on server and sending the file to a local folder.
 */
public class ServerFilesCheck extends Thread
{
    /**
     * Storing the UserInfo object of the current user.
     */
    final UserInfo client_Info;

    /**
     * Storing the last operation list where is the information about the last operation performed (send file or delete file).
     */
    final List<String> last_Operation;

    /**
     * This is a constructor to initialize server files check thread.
     * @param client an initial the UserInfo object of the current user.
     * @param list an initial the last operation list.
     */
    public ServerFilesCheck(UserInfo client, List<String> list)
    {
        this.client_Info = client;
        this.last_Operation = list;
    }

    /**
     * First, the client asks the server for new files by the client. He sends his list of files and the server compares it with his list of files.
     * If a file is found that is not in the local folder, it will be sent.
     * A loop is being started that checks for changes to the user folder on the server.
     * If a difference is found, first the list of operations is checked to see if any file has been previously sent from the local folder.
     * If not the file is sent to a local folder.
     */
    @Override
    public void run()
    {
        File server_Dir_Files = new File(client_Info.getDir());
        File[] server_Files = server_Dir_Files.listFiles();
        boolean check;
        ArrayList<String> user_Files = new ArrayList<>();
        try
        {
            DataInputStream data_Receive = new DataInputStream(client_Info.getUserSocket().getInputStream());

            int files_Number = data_Receive.readInt();
            for(int i = 0; i < files_Number; i++)
            {
                user_Files.add(data_Receive.readUTF());
            }

            for(int i = 0; i<server_Files.length; i++)
            {
                check = false;
                for(int j = 0; j<user_Files.size(); j++)
                {
                    if(server_Files[i].getName().equals(user_Files.get(j)))
                    {
                        check = true;
                        break;
                    }
                }
                if(check == false)
                {
                    Thread send_File = new ServerSendFile(client_Info, server_Files[i].getName());
                    send_File.start();
                }
            }
            Thread.sleep(3000);
        }
        catch(IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        last_Operation.clear();
        File old_Dir = new File(client_Info.getDir());
        File[] old_Dir_Files = old_Dir.listFiles();

        while(true)
        {
            try
            {
                Thread.sleep(3000);
                File new_Dir = new File(client_Info.getDir());
                File[] new_Dir_Files = new_Dir.listFiles();

                if(new_Dir_Files.length > old_Dir_Files.length)
                {
                    for(int i = 0; i < new_Dir_Files.length; i++)
                    {
                        check = false;
                        for(int j = 0; j < old_Dir_Files.length; j++)
                        {
                            if(new_Dir_Files[i].getName().equals(old_Dir_Files[j].getName()))
                            {
                                check = true;
                                break;
                            }
                        }
                        if(check == false)
                        {
                            if(last_Operation.size() > 1)
                            {
                                if(last_Operation.get(0).equals("send") && last_Operation.get(1).equals(new_Dir_Files[i].getName()))
                                {
                                    last_Operation.remove(0);
                                    last_Operation.remove(0);
                                    continue;
                                }
                            }
                            else
                            {
                                Thread send_File = new ServerSendFile(client_Info, new_Dir_Files[i].getName());
                                send_File.start();
                            }
                        }
                    }
                }
                old_Dir = new_Dir;
                old_Dir_Files = old_Dir.listFiles();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
