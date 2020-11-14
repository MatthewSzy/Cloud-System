package Server;

import Resources.UserInfo;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to sending to all connected users a list of users.
 */
public class ServerListSender extends Thread
{
    /**
     * Storing the online users list.
     */
    final List<UserInfo> online_Users;

    /**
     * This is a constructor to initialize server list sender.
     * @param list an initial list of UserInfo object (online users list).
     */
    public ServerListSender(List<UserInfo> list)
    {
        this.online_Users = list;
    }

    /**
     * First of all thread checks if the number of users has changed.
     * If the number has changed, it sends a new list to each customer using the Socket located in the "UserInfo" object.
     * If the number has not changed, the thread waits a certain time and re-checks the number of users in the list.
     */
    @Override
    public void run()
    {
        int old_Users_Number = 0;
        while(true)
        {
            int new_Users_Number = online_Users.size();
            if(old_Users_Number != new_Users_Number)
            {
                try
                {
                    for(int i = 0; i < online_Users.size(); i++)
                    {
                        online_Users.get(i).getBlockSend().acquire();
                        DataOutputStream data_Send = new DataOutputStream(online_Users.get(i).getUserSocket().getOutputStream());
                        data_Send.writeUTF("send_List");
                        data_Send.flush();
                        data_Send.writeInt(online_Users.size());
                        data_Send.flush();

                        for(int j = 0; j < online_Users.size(); j++)
                        {
                            data_Send.writeUTF(online_Users.get(j).getUserName());
                            data_Send.flush();
                        }

                        online_Users.get(i).getBlockSend().release();
                    }
                }
                catch(IOException | InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            old_Users_Number = new_Users_Number;
            try {
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
