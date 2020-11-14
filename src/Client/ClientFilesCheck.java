package Client;

import Resources.UserInfo;

import java.io.*;
import java.util.List;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to checking user files in local folder and sending the file to a server folder.
 */
public class ClientFilesCheck extends Thread
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
     * Storing the operation list where is the information about the actual operation made by threads.
     */
    final List<String> operation_List;

    /**
     * This is a constructor to initialize client files check thread.
     * @param client an initial the UserInfo object of the current user.
     * @param list an initial the last operation list.
     * @param list2 an initial the actual operation list.
     */
    public ClientFilesCheck(UserInfo client, List<String> list, List<String> list2)
    {
        this.client_Info = client;
        this.last_Operation = list;
        this.operation_List = list2;
    }

    /**
     * First, the client sends its list of files so that the server can check if it needs to send new files from the server to the local folder.
     * A loop is being started that checks for changes to the local user folder.
     * If a difference is found, first the list of operations is checked to see if any file has been previously sent from the server folder.
     * If not the file is sent to a server folder.
     */
    @Override
    public void run()
    {
        File user_Dir_Files = new File(client_Info.getDir());
        File[] user_Files = user_Dir_Files.listFiles();

        operation_List.add("Check files..");
        try
        {
            DataOutputStream data_Send = new DataOutputStream(client_Info.getUserSocket().getOutputStream());

            data_Send.writeInt(user_Files.length);
            data_Send.flush();
            for(int i = 0; i < user_Files.length; i++)
            {
                data_Send.writeUTF(user_Files[i].getName());
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

                operation_List.add("Check files...");
                if(new_Dir_Files.length > old_Dir_Files.length)
                {
                    for(int i = 0; i < new_Dir_Files.length; i++)
                    {
                        boolean check = false;
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
                                operation_List.add("Send file...");
                                Thread send_File = new ClientSendFile(client_Info, new_Dir_Files[i].getName());
                                send_File.start();
                            }
                        }
                    }
                }
                else if(new_Dir_Files.length < old_Dir_Files.length)
                {
                    for(int i = 0; i < old_Dir_Files.length; i++)
                    {
                        boolean check = false;
                        for(int j = 0; j < new_Dir_Files.length; j++)
                        {
                            if(old_Dir_Files[i].getName().equals(new_Dir_Files[j].getName()))
                            {
                                check = true;
                                break;
                            }
                        }
                        if(check == false)
                        {
                            client_Info.getBlockSend().acquire();
                            DataOutputStream data_Send = new DataOutputStream(client_Info.getUserSocket().getOutputStream());

                            data_Send.writeUTF("delete_File");
                            data_Send.flush();
                            data_Send.writeUTF(old_Dir_Files[i].getName());
                            client_Info.getBlockSend().release();
                        }
                    }
                }
                old_Dir = new_Dir;
                old_Dir_Files = old_Dir.listFiles();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
