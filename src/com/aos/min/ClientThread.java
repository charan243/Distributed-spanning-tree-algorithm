package com.aos.min;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientThread implements Runnable{

	private ArrayList<Integer> dstNodeIds;
	private HashMap<Integer, ArrayList<String>> location;
	private Integer messageType;
	private Integer nodeId;
	private long explore_delay;
	private int phase;

	public ClientThread(ArrayList<Integer> dstNodeIds, HashMap<Integer, ArrayList<String>> location,
			Integer messageType, Integer nodeId, long explore_delay, int phase) {
		super();
		this.dstNodeIds = dstNodeIds;
		this.location = location;
		this.messageType = messageType;
		this.nodeId = nodeId;
		this.explore_delay = explore_delay;
		this.phase = phase;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(messageType.equals(Core.SEARCH_MSG_TYPE)) {
			try {
				Thread.sleep(explore_delay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(Integer dstId : dstNodeIds) {
			ArrayList<String> arr = location.get(dstId);
			String ip = arr.get(0);
			int port = Integer.parseInt(arr.get(1));
			send(ip,port);
		}		
	}

	public void send(String ip,int port)
	{
		Socket clientSocket = null;
		try
		{			
			clientSocket = new Socket(ip,port);
			ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
			Message m = new Message(nodeId, messageType);
			m.phase = phase;
			outputStream.writeObject(m);
			outputStream.flush();
			outputStream.close();
			clientSocket.close();
		}
		catch(IOException ex)
		{
			System.out.println("Got error on client NodeId: "+nodeId);
			ex.printStackTrace();
		}  finally {
			if (clientSocket != null) {
				try {
					clientSocket.close();
				} catch (IOException e) {
					// log error just in case
				}
			}
		}
	}

}