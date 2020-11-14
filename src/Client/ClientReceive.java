package Client;

import Resources.UserInfo;

import java.io.*;
import java.util.List;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to receiving information from the client.
 */
public class ClientReceive extends Thread
{
    /**
     * Storing the UserInfo object of the current user.
     */
    final UserInfo client_Info;

    /**
     * Storing the online users list.
     */
    final List<String> online_users;

    /**
     * Storing the last operation list where is the information about the last operation performed (send file or delete file).
     */
    final List<String> last_Operation;

    /**
     * Storing the operation list where is the information about the actual operation made by threads.
     */
    final List<String> operation_List;

    /**
     * This is a constructor to initialize client receive thread.
     * @param client an initial the UserInfo object of the current user.
     * @param list1 an initial the online users list.
     * @param list2 an initial the last operation list
     * @param list3 an initial the actual operation list
     */
    public ClientReceive(UserInfo client, List<String> list1, List<String> list2, List<String> list3)
    {
        this.client_Info = client;
        this.online_users = list1;
        this.last_Operation = list2;
        this.operation_List = list3;
    }

    /**
     * The thread is waiting for a message that indicates what data will be received.
     * If the message is "send_list", starts receiving new user lists.
     * If the message is "send_File", the file name is received, its size is then created,
     * - InputStream
     * - FileOutputStream
     * - BufferedOutputStream
     * Using them, the file is sent to the indicated folder.
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
                if(type.equals("send_List"))
                {
                    operation_List.add("Receiving a list of users...");
                    int number = data_Receive.readInt();
                    online_users.clear();
                    for(int i = 0; i < number; i++)
                    {
                        online_users.add(data_Receive.readUTF());
                    }
                }
                else if(type.equals("send_File"))
                {
                    operation_List.add("Receiving a file...");
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
            }
            catch(IOException | InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
