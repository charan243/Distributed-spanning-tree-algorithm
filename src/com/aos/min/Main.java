package com.aos.min;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Integer NodeId = Integer.parseInt(args[0]);
		HashMap<Integer, ArrayList<String>> location = getLocation(args[1]);
		ArrayList<Integer> neighbors = getNeighbors(args[2]);
		Integer rootNode = Integer.parseInt(args[3]);
		String config_file = args[4];
		String path = args[5];
		long explore_delay = Long.parseLong(args[6])*1000;
		Core c = new Core(NodeId, location, neighbors, rootNode, explore_delay);
		c.initiator();

		/*		
		System.out.println("NodeId: "+NodeId);
		System.out.println("location: "+location);
		System.out.println("neighbors: "+neighbors);
		System.out.println("rootNode: "+rootNode);
		System.out.println("config_file: "+config_file);
		System.out.println("path: "+path);
		 */		
		//output to file
		toFile(NodeId,c.parent,c.childern,config_file,path);
	}


	public static void toFile(Integer NodeId,int parent,
			ArrayList<Integer> childern,String config_file,String path) {

		String fileName = path+"/"+config_file+"-"+NodeId+".out";
		File file = new File(fileName);
		Writer fileWriter = null;
		BufferedWriter bufferedWriter = null;
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			fileWriter = new FileWriter(file);
			bufferedWriter = new BufferedWriter(fileWriter);
			String line = (parent==-1)?"*":String.valueOf(parent);
			line += System.getProperty("line.separator");
			if(!childern.isEmpty()) {
				String line_2 = childern.toString().replace(",", " ")
						.replace("[", "")
						.replace("]", "").trim();
				line += line_2;
			} else {
				line +="*";
			}
			line += System.getProperty("line.separator");
			bufferedWriter.write(line);
		} catch (IOException e) {
			System.err.println("Error writing the file : "+fileName);
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null && fileWriter != null) {
				try {
					bufferedWriter.close();
					fileWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}


	public static HashMap<Integer, ArrayList<String>> getLocation(String str) {
		String a[] = str.split("#");
		HashMap<Integer, ArrayList<String>> map = new HashMap<Integer, ArrayList<String>>();
		for(int i=0;i<a.length;i++) {
			//System.out.print(a[i]+",");
			String b[] = a[i].split(" ");
			ArrayList<String> arr = new ArrayList<String>();
			arr.add(b[1]);
			arr.add(b[2]);
			map.put(Integer.parseInt(b[0]),arr);
		}
		return map;
	}

	public static ArrayList<Integer> getNeighbors(String str) {
		String a[] = str.split(" ");
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for(int i=0;i<a.length;i++) {
			arr.add(Integer.parseInt(a[i]));
		}
		return arr;
	}
}
