package com.aos.min;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import com.aos.min.ClientThread;
import com.aos.min.Message;

public class Core {
	static public Integer SEARCH_MSG_TYPE = 0;
	static public Integer ACK_MSG_TYPE = 1;
	static public Integer NAK_MSG_TYPE = 2;
	static public Integer PHASE_MSG_TYPE = 3;
	static public Integer PHASE_ACK_MSG_TYPE = 4;
	static public Integer PHASE_NAK_MSG_TYPE = 5;
	static public Integer TERMINATE_MSG_TYPE = 6;

	private Integer nodeId;
	private HashMap<Integer, ArrayList<String>> location;
	private ArrayList<Integer> neighbors;
	private Integer rootNode;
	public int parent;
	public ArrayList<Integer> childern;
	public long explore_delay;

	public Core(Integer nodeId, HashMap<Integer, ArrayList<String>> location, ArrayList<Integer> neighbors,
			Integer rootNode,long explore_delay) {
		super();
		this.nodeId = nodeId;
		this.location = location;
		this.neighbors = neighbors;
		this.rootNode = rootNode;
		this.parent = -1;
		this.childern = new ArrayList<Integer>();
		this.explore_delay = explore_delay;
	}

	public void initiator() {
		if(rootNode == nodeId) {
			// send explore messages to all neighbors
			Thread sendSearch = new Thread(new ClientThread(new ArrayList<Integer>(neighbors), location, SEARCH_MSG_TYPE, nodeId, explore_delay,-1));
			sendSearch.start();
			int port = Integer.parseInt(location.get(nodeId).get(1));
			listenForMessagesAsRoot(port);
		} else {
			int port = Integer.parseInt(location.get(nodeId).get(1));
			listenForMessagesAsNonRoot(port);
		}

	}



	public void listenForMessagesAsRoot(int port) {
		ServerSocket serverSock = null;
		try
		{
			ArrayList<Integer> tmp_neighbors = new ArrayList<Integer>(neighbors);
			serverSock = new ServerSocket(port);
			int tmp_size = tmp_neighbors.size();
			int tmp = 0,i = 1;
			while(!tmp_neighbors.isEmpty()) {
				Socket sock = serverSock.accept();				
				ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
				Message recMessage = (Message) inStream.readObject();
				inStream.close();
				Integer recMessageType = recMessage.getMessageType();
				if( ACK_MSG_TYPE.equals(recMessageType) || PHASE_ACK_MSG_TYPE.equals(recMessageType) ) {
					tmp++;
				}
				if( NAK_MSG_TYPE.equals(recMessageType) || PHASE_NAK_MSG_TYPE.equals(recMessageType) ) {
					tmp++;
					tmp_neighbors.remove(recMessage.getID());
				}
				if( (tmp == tmp_size) && (!tmp_neighbors.isEmpty())) {
					//send phase message
					Thread sendPhase = new Thread(new ClientThread(new ArrayList<Integer>(tmp_neighbors), location, PHASE_MSG_TYPE, nodeId, 0, i));
					sendPhase.start();					
					tmp_size = tmp_neighbors.size();
					tmp = 0;
					i++;
				}
			}
			childern = neighbors;
			//System.out.println("Sending Terminate to "+childern);
			if(!childern.isEmpty()) {
				Thread sendTerminate = new Thread(new ClientThread(new ArrayList<Integer>(childern), location, TERMINATE_MSG_TYPE, nodeId, 0, -1));
				sendTerminate.start();				
			}
		}
		catch(IOException ex)
		{
			System.out.println("port "+port+" already in use NodeId "+nodeId);
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  finally {
			if (serverSock != null) {
				try {
					serverSock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//		System.out.println("Parent: "+parent);
		//		System.out.println("Childern: "+neighbors);
	}	
	public void listenForMessagesAsNonRoot(int port) {
		ServerSocket serverSock = null;
		try
		{
			serverSock = new ServerSocket(port);
			while( true )
			{
				Socket sock = serverSock.accept();				
				ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
				Message recMessage = (Message) inStream.readObject();
				inStream.close();
				int recMessageType = recMessage.getMessageType();
				if( TERMINATE_MSG_TYPE.equals(recMessageType) ) {
					//				System.out.println("Received Terminate from "+parent);
					//				System.out.println("Sending Terminate to "+childern);
					if(!childern.isEmpty()) {
						Thread sendTerminate = new Thread(new ClientThread(new ArrayList<Integer>(childern), location, TERMINATE_MSG_TYPE, nodeId, 0, -1));
						sendTerminate.start();						
					}
					break;
				}
				if( SEARCH_MSG_TYPE.equals(recMessageType) ) {
					if(parent == -1) {
						parent = recMessage.getID();
						neighbors.remove(recMessage.getID());
						//send ACK
						ArrayList<Integer> arr = new ArrayList<Integer>();
						arr.add(parent);
						//				System.out.println("Sending ACK to NodeID: "+recMessage.getID());
						Thread sendAck = new Thread(new ClientThread(arr, location, ACK_MSG_TYPE, nodeId, 0,-1));
						sendAck.start();
					} else {
						ArrayList<Integer> arr = new ArrayList<Integer>();
						arr.add(recMessage.getID());
						//				System.out.println("Sending NAK to NodeID: "+recMessage.getID());
						Thread sendNak = new Thread(new ClientThread(arr, location, NAK_MSG_TYPE, nodeId, 0,-1));
						sendNak.start();
					}
				}
				if( PHASE_MSG_TYPE.equals(recMessageType) ) {
					int phase_value = recMessage.phase - 1;
					//			System.out.println("Got Phase message from "+recMessage.getID()+" with value: "+recMessage.phase);
					if( phase_value == 0 ) {
						//send search message to neighbors
						//				System.out.println("Sending Search message for "+neighbors);
						if(!neighbors.isEmpty()) {
							Thread sendSearch = new Thread(new ClientThread(new ArrayList<Integer>(neighbors), location, SEARCH_MSG_TYPE, nodeId, explore_delay,-1));
							sendSearch.start();							
						}
						listenForAckNak(serverSock);
						childern = new ArrayList<Integer>(neighbors);
						//				System.out.println("Childern: "+childern);
					} else {
						//send phase message to discovered neighbors
						//				System.out.println("Sending Phase message for "+neighbors);
						if(!neighbors.isEmpty()) {
							Thread sendPhase = new Thread(new ClientThread(new ArrayList<Integer>(neighbors), location, PHASE_MSG_TYPE, nodeId, explore_delay,phase_value));
							sendPhase.start();							
						}
						listenForAckNak(serverSock);
					}
				}

			}

		}
		catch(IOException ex)
		{
			System.out.println("port "+port+" already in use NodeId "+nodeId);
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}  finally {
			if (serverSock != null) {
				try {
					serverSock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		//	System.out.println("parent: "+parent);
		//	System.out.println("Childern: "+childern);
	}


	public void listenForAckNak(ServerSocket serverSock) throws IOException, ClassNotFoundException {
		int tmp_nak = 0;
		int tmp_ack = 0;
		int tmp_size = neighbors.size();
		while((tmp_ack + tmp_nak) != tmp_size) {
			Socket sock = serverSock.accept();				
			ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
			Message recMessage = (Message) inStream.readObject();
			inStream.close();
			int recMessageType = recMessage.getMessageType();
			if( ACK_MSG_TYPE.equals(recMessageType) || PHASE_ACK_MSG_TYPE.equals(recMessageType) ) {
				tmp_ack++;
			}
			if( NAK_MSG_TYPE.equals(recMessageType) || PHASE_NAK_MSG_TYPE.equals(recMessageType) ) {
				tmp_nak++;
				neighbors.remove(recMessage.getID());
			}
			if( SEARCH_MSG_TYPE.equals(recMessageType) ) {
				ArrayList<Integer> arr = new ArrayList<Integer>();
				arr.add(recMessage.getID());
				//		System.out.println("Sending NAK for NodeID: "+recMessage.getID());
				Thread sendNak = new Thread(new ClientThread(arr, location, NAK_MSG_TYPE, nodeId, 0,-1));
				sendNak.start();
			}
		}
		if( tmp_ack >= 1) {
			//Send Phase ACK to parent
			ArrayList<Integer> arr = new ArrayList<Integer>();
			arr.add(parent);
			//	System.out.println("Sending Phase ACK to NodeID: "+parent);
			Thread sendPhaseAck = new Thread(new ClientThread(arr, location, PHASE_ACK_MSG_TYPE, nodeId, 0,-1));
			sendPhaseAck.start();

		} else {
			//Send Phase NAK to parent
			ArrayList<Integer> arr = new ArrayList<Integer>();
			arr.add(parent);
			//	System.out.println("Sending Phase NAK to NodeID: "+parent);
			Thread sendPhaseNak = new Thread(new ClientThread(arr, location, PHASE_NAK_MSG_TYPE, nodeId, 0,-1));
			sendPhaseNak.start();
		}
	}
}
