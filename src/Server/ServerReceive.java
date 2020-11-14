package Server;

import Resources.UserInfo;

import java.io.*;
import java.nio.file.Files;
import java.util.List;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to receiving information from the client.
 */
public class ServerReceive extends Thread
{

    /**
     * Storing the online users list.
     */
    final List<UserInfo> online_users;

    /**
     * Storing the UserInfo object of the current user.
     */
    final UserInfo client_Info;

    /**
     * Storing the last operation list where is the information about the last operation performed (send file or delete file).
     */
    final List<String> last_Operation;

    /**
     * This is a constructor to initialize server receive thread.
     * @param list1 an initial the online users list.
     * @param client an initial the UserInfo object of the current user.
     * @param list2 an initial the last operation list
     */
    public ServerReceive (List<UserInfo> list1, UserInfo client, List<String> list2)
    {
        this.online_users = list1;
        this.client_Info = client;
        this.last_Operation = list2;
    }

    /**
     * The thread is waiting for a message that indicates what data will be received.
     * If the message is "share_File", the user name and file name are received,
     * and then two paths are created to the current user folder and the given user folder to send the file.
     *
     * If the message is "send_File", the file name is received, its size is then created,
     * - InputStream
     * - FileOutputStream
     * - BufferedOutputStream
     * Using them, the file is sent to the indicated folder.
     *
     * If the message is "delete_File, gets the file name and removes it from the user's folder on the server.
     *
     * If the message is "exit_Client", removes the user from the online user list and ends the thread.
     */
    @Override
    public void run()
    {
        while(true)
        {
            try
            {
                Thread.sleep(3000);
                DataInputStream data_Receive = new DataInputStream(client_Info.getUserSocket().getInputStream());

                String type = data_Receive.readUTF();
                if(type.equals("share_File"))
                {
                    String user_Name = data_Receive.readUTF();
                    String file_Name = data_Receive.readUTF();

                    File src = new File(client_Info.getDir() + "\\" + file_Name);
                    File dest = null;

                    for(UserInfo x : online_users)
                    {
                        if(x.getUserName().equals(user_Name))
                        {
                            dest = new File(x.getDir() + "\\" + file_Name);
                            break;
                        }
                    }

                    Files.copy(src.toPath(), dest.toPath());
                }
                else if(type.equals("send_File"))
                {
                    String fileName = data_Receive.readUTF();
                    int fileSize = data_Receive.readInt();

                    byte[] myByteArray = new byte[fileSize];
                    InputStream stream_Receive = client_Info.getUserSocket().getInputStream();
                    FileOutputStream file_Receive = new FileOutputStream(client_Info.getDir() + "\\" + fileName);
                    BufferedOutputStream buffer_Receive = new BufferedOutputStream(file_Receive);

                    int bytesRead = stream_Receive.read(myByteArray, 0, myByteArray.length);
                    int current = bytesRead;
                    do {
                        bytesRead = stream_Receive.read(myByteArray, current, (myByteArray.length-current));
                        if(bytesRead >= 0) current += bytesRead;
                    }while(bytesRead == -1);

                    buffer_Receive.write(myByteArray, 0, current);
                    buffer_Receive.flush();

                    last_Operation.add("send");
                    last_Operation.add(fileName);
                    System.out.println("File " + fileName + " downloaded (" + current + " bytes) read from Server");
                }
                else if(type.equals("delete_File"))
                {
                    String file_Name = data_Receive.readUTF();
                    File delete_File = new File(client_Info.getDir() + "\\" + file_Name);

                    delete_File.delete();
                }
                else if(type.equals("exit_Client"))
                {
                    for(int i = 0; i < online_users.size(); i++)
                    {
                        if(online_users.get(i).getUserName().equals(client_Info.getUserName()))
                        {
                            System.out.println(online_users.get(i).getUserName() + "'s leaves");
                            online_users.remove(i);
                            return;
                        }
                    }

                }
            }
            catch(IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
