package Client;

import Resources.UserInfo;

import java.io.*;

/**
 * Created by Mateusz Szymański on 09.06.2020.
 * Server thread uses to receiving information from the server.
 */
public class ClientSendFile extends Thread
{
    /**
     * Storing the UserInfo object of the current user.
     */
    final UserInfo client_Info;

    /**
     * Storing the file name to be sent
     */
    final String file_Name;

    /**
     * This is a constructor to initialize server send file thread.
     * @param client an initial the UserInfo object of the current user.
     * @param name an initial the file name to be sent
     */
    public ClientSendFile(UserInfo client, String name)
    {
        this.client_Info = client;
        this.file_Name = name;
    }

    /**
     * First, all OutStream is blocked using a semaphore.
     * Then information is sent to the client that the file will be sent, and then the file name and size are given.
     * They are created
     * - FileInputStream
     * - BufferedInputStream
     * - OutputStream
     * The entire file is sent with them.
     * Finally all OutputStream will be unblocked.
     */
    @Override
    public void run()
    {
        try
        {
            client_Info.getBlockSend().acquire();
            System.out.println("Lock Output in Send File");
            DataOutputStream data_Send = new DataOutputStream(client_Info.getUserSocket().getOutputStream());
            data_Send.writeUTF("send_File");
            data_Send.flush();
            data_Send.writeUTF(file_Name);
            data_Send.flush();
            File myFile = new File(client_Info.getDir() + "\\" + file_Name);
            data_Send.writeInt((int) myFile.length());
            data_Send.flush();

            byte[] myByteArray = new byte[(int) myFile.length()];
            FileInputStream file_Send = new FileInputStream(myFile);
            BufferedInputStream buffer_Send = new BufferedInputStream(file_Send);
            buffer_Send.read(myByteArray, 0, myByteArray.length);
            OutputStream stream_Send = client_Info.getUserSocket().getOutputStream();

            System.out.println("Sending " + file_Name + " (" + myByteArray.length + " bytes) to Client " + client_Info.getUserName());
            stream_Send.write(myByteArray, 0, myByteArray.length);
            stream_Send.flush();
            System.out.println("Sent " + file_Name + " (" + myByteArray.length + " bytes) to Client " + client_Info.getUserName());
            System.out.println("Unlock Output in Send File");
            client_Info.getBlockSend().release();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
