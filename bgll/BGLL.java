package pers.tiger.bgll;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sun.awt.windows.ThemeReader;

public class BGLL {
	//double of total weight in the network 
	public float M;

	//excute bgll,stop when there is not emerge
	public void excuteBGLL(String path) throws SQLException, IOException{
		//storage the result 
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		Graph graph = new Graph();
		//intial the network 
		Map<Integer, Node> nodeMap = graph.InitialGraph();
		//the total weight of network 
		this.M = graph.getM();
		//flag of if stoping the interation
		boolean flag = true;
		int start = nodeMap.size();
		int pass = 1;

		while(flag){
			List<Community> comList = startBGLL(nodeMap, pass);
			int end = comList.size();
			if(end!=start){
				commnityMap.put(pass, comList);
				System.out.println("this is the" + pass + " 's interation：");
				printCom(comList);
				nodeMap = getNewNodeMap(comList);
				start = end;
			}else{
				flag = false;
			}
			pass++;

		}
		System.out.println("start print the result :");
		printCommunity(commnityMap,path);
	}

	public void excuteBGLL(String inputPath, String outPath) throws NumberFormatException, IOException{
		//storage the result
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		//intial the network
		Graph graph = new Graph(inputPath);
		Map<Integer, Node> nodeMap = graph.getNodeMap();
		//the total weight of network 
		this.M = graph.getM();
		//flag of if stoping the interation
		boolean flag = true;
		int start = nodeMap.size();
		int pass = 1;

		while(flag){
			List<Community> comList = startBGLL(nodeMap, pass);
			int end = comList.size();
			if(end!=start){
				commnityMap.put(pass, comList);
				System.out.println("this is the" + pass + " 's interation：");
				printCom(comList);
				nodeMap = getNewNodeMap(comList);
				start = end;
			}else{
				flag = false;
			}
			pass++;

		}
		System.out.println("start print the result :");
		printCommunity(commnityMap,outPath);
	}

	//excute the bgll ,set the interation times
	public void excuteBGLL(int pass,String path) throws SQLException, IOException{
		//storage the result
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		Graph graph = new Graph();
		//intial the network
		Map<Integer, Node> nodeMap = graph.InitialGraph();
		//the total weight of network 
		this.M = graph.getM();
		for(int i = 1;i <= pass;i++){
			List<Community> comList = startBGLL(nodeMap, i);
			commnityMap.put(i, comList);
			System.out.println("this is the" + i + " 's interation：");
			printCom(comList);
			nodeMap = getNewNodeMap(comList);
		}
		System.out.println("start print the result :");
		printCommunity(commnityMap,path);

	}

	//phase1，let every node be a community and add the node to the  community which get the maximum modularity
	private List<Community> startBGLL(Map<Integer, Node> nodeMap,int pass){
		//flag of stop interation,if there has no node remove,stop the interation
		boolean flag = false;
		List<Community> comList = new LinkedList<>();
		int comid = 0;
		for(Node node:nodeMap.values()){
			Community community = new Community();
			List<Node> nodeList = new ArrayList<>();
			nodeList.add(node);
			community.setId(comid);
			community.setPass(pass);
			community.setNodeList(nodeList);
			comList.add(community);
			comid++;
		}
		while(flag == false){
			Map<Integer, Integer> validateMap = getValidation(comList);
			for(Community com:comList){
				List<Node> nodeInCom = com.getNodeList();
				//community id of node
				int cid = com.getId();
				//record the delete node,node's id,the community which to remove
				Iterator<Node> it1 = nodeInCom.iterator();
				while(it1.hasNext()){
					Node node = it1.next();
					int maxNeighbor = maxQNeighbor(node, comList);
					if(maxNeighbor > 0 && maxNeighbor!=cid){
						//System.out.println(node.getNodeId() + " move from " + cid + " to " + maxNeighbor);
						List<Node> targetList = comList.get(maxNeighbor).getNodeList();
						targetList.add(node);
						it1.remove();
					}else{
						continue;
					}
				}
			}
			int newId = 0;
			for(Iterator<Community> it = comList.iterator();it.hasNext();){
				Community com = it.next();
				if(com.getNodeList().size() == 0){
					it.remove();
				}else{
					com.setId(newId++);
				}
			}
			flag = judge(validateMap, comList);
		}

		return comList;
	}
	//the community id of the node's neighbor node which get the maximum modularity
	private int maxQNeighbor(Node node,List<Community> comList){
		int maxNeighbor = -1;
		double maxQ = Double.MIN_VALUE;
		Map<Integer, Float> neighborMap = node.getNeighborMap();
		for(int neiborId:neighborMap.keySet()){
			if(neiborId == node.getNodeId()){
				continue;
			}
			int comId = nodeCom(neiborId, comList);
			Community community = comList.get(comId);
			List<Node> nodeList = community.getNodeList();
			float in = calComIn(nodeList);
			float tot = calComTot(nodeList);
			float ki = calNodeWeight(node);
			float ki_in = calIIn(node, nodeList);
			double deltaQ = ((in + 2* ki_in) / M - Math.pow(((tot + ki) / M), 2)) 
					- (in / M - Math.pow((tot / M), 2) - Math.pow((ki / M), 2));
			int compare = Double.compare(deltaQ, 0);
			if(compare > 0 && maxQ < deltaQ){
				maxQ = deltaQ;
				maxNeighbor = comId;
			}
		}
		return maxNeighbor;
	}
	//calculate the community id of the node
	private int nodeCom(int id,List<Community> comList){
		int comId = -1;
		for(int i = 0;i < comList.size();i++){
			Community community = comList.get(i);
			List<Node> nodeInCom = community.getNodeList();
			for(Node n:nodeInCom){
				if(n.getNodeId() == id){
					comId = i;
					break;
				}
			}
		}
		return comId;
	}

	//abstract community to node
	private Map<Integer, Node> getNewNodeMap(List<Community> comList){
		Map<Integer, Node> newMap = new HashMap<>();
		for(Community com:comList){
			int comId = com.getId();
			Node node = new Node(comId);
			List<Node> nodeList = com.getNodeList();
			float selfWeight = calComIn(nodeList);
			Map<Integer, Float> thisNodeMap = node.getNeighborMap();
			thisNodeMap.put(comId, selfWeight);
			for(Node nodeN:nodeList){
				Map<Integer, Float> neiborMap = nodeN.getNeighborMap();
				for(int key:neiborMap.keySet()){
					int keyComId = nodeCom(key, comList);
					if(keyComId != comId){
						if(thisNodeMap.containsKey(keyComId)){
							float weight = thisNodeMap.get(keyComId) + neiborMap.get(key);
							thisNodeMap.put(keyComId, weight);
						}else{
							thisNodeMap.put(keyComId, neiborMap.get(key));
						}
					}
				}
			}
			newMap.put(comId, node);
		}
		return newMap;
	}

	//in the sum of the weights of the links inside community C
	private float calComIn(List<Node> nodeList){
		float sumIn = 0;
		//存放社区内节点ID
		List<Integer> idList = new ArrayList<>();
		for(Node node:nodeList){
			idList.add(node.getNodeId());
		}
		//遍历社区内节点的边
		for(Node node:nodeList){
			Map<Integer, Float> neighborMap = node.getNeighborMap();
			for(int nid:neighborMap.keySet()){
				float weight = neighborMap.get(nid);
				if(idList.contains(nid)){
					sumIn += weight;
				}
			}
		}
		return sumIn;
	}

	//tot, the sum of the weights of the links incident to nodes in C
	private float calComTot(List<Node> nodeList){
		float sumTot = 0;
		for(Node node:nodeList){
			Map<Integer, Float> neighborMap = node.getNeighborMap();
			for(float weight:neighborMap.values()){
				sumTot += weight;
			}
		}
		return sumTot;
	}

	//Ki, the sum of the weights of the links incident to node i
	private float calNodeWeight(Node node){
		float Sumweight = 0;
		Map<Integer, Float> neighborMap = node.getNeighborMap();
		for(float weight:neighborMap.values()){
			Sumweight += weight;
		}
		return Sumweight;
	}

	//Ki,in, the sum of the weights of the links from i to nodes in C
	private float calIIn(Node node,List<Node> nodeList){
		float sumIin = 0;
		List<Integer> idList = new ArrayList<>();
		for(Node nodeInC:nodeList){
			idList.add(nodeInC.getNodeId());
		}
		Map<Integer, Float> neighborMap = node.getNeighborMap();
		for(int nid:neighborMap.keySet()){
			float weight = neighborMap.get(nid);
			if(idList.contains(nid)){
				sumIin += weight;
			}
		}
		return sumIin;

	}

	//print result
	private void printCom(List<Community> comList){
		for(Community com:comList){
			int comid = com.getId();
			List<Node> nodeList = com.getNodeList();
			int nodeNum = nodeList.size();
			System.out.println("community" + comid + " size= " + nodeNum);
			String str = "";
			for(Node node:nodeList){
				str+= node.getNodeId() + ",";
			}
			System.out.println(str);
		}
	}
	private Map<Integer, Integer> getValidation(List<Community> comList){
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		for(Community com:comList){
			List<Node> nodeList = com.getNodeList();
			int comId = com.getId();
			for(Node node:nodeList){
				int nodeId = node.getNodeId();
				map.put(nodeId, comId);
			}
		}
		return map;
	}
	//judge if two result is the same
	private boolean judge(Map<Integer, Integer> map,List<Community> comList){
		boolean flag = true;
		for(Community com:comList){
			List<Node> nodeList = com.getNodeList();
			int comId = com.getId();
			for(Node node:nodeList){
				int nodeId = node.getNodeId();
				int lastId = map.get(nodeId);
				if(comId != lastId){
					flag = false;
					break;
				}
			}
		}
		return flag;
	}

	private void printGraph(Map<Integer, Node> nodeMap){
		for(int nodeid:nodeMap.keySet()){
			System.out.println("node-" + nodeid + " weight= " + nodeMap.get(nodeid).getWeight() + "'s neighbor:");
			Map<Integer, Float> neighbor = nodeMap.get(nodeid).getNeighborMap();
			for(int id:neighbor.keySet()){
				System.out.println(nodeid + "-" + id + ": " + neighbor.get(id));
			}
		}
	}

	private void printCommunity(Map<Integer, List<Community>> commnityMap,String path) throws IOException{
		int pass = commnityMap.size();
		List<Node> printList = new ArrayList<>();
		List<Community> comList = commnityMap.get(1);
		for(Community com:comList){
			int comId = com.getId();
			for(Node node:com.getNodeList()){
				int[] bgllList = new int[pass];
				bgllList[0] = comId;
				node.setList_BGLL(bgllList);
				printList.add(node);
			}
		}
		if(pass > 1){
			for(int i=2;i<=commnityMap.size();i++){
				List<Community> tempComList = commnityMap.get(i);
				System.out.println("tempComList = " + tempComList.size());
				Map<Integer, Integer> nodeComMap = new HashMap<>();
				for(Community com:tempComList){
					int comId = com.getId();
					for(Node node:com.getNodeList()){
						nodeComMap.put(node.getNodeId(), comId);
					}
				}
				for(Node node:printList){
					int[] A = node.getList_BGLL();
					int lowId = A[i-2];
					int highId = nodeComMap.get(lowId);
					A[i-1] = highId;
				}
			}
		}
		FileWriter fw = new FileWriter(new File(path));
		BufferedWriter writer = new BufferedWriter(fw);
		String outStr = "";
		for(Node node:printList){
			int id = node.getNodeId();
			String name = node.getHostName();
			int label = node.getLabel();
			String str = id + "," + name + "," + label;
			int[] A = node.getList_BGLL();
			for(int i:A){
				str += "," + i;
			}
			outStr += str + "\r\n";
		}
		writer.write(outStr);
		writer.close();
	}
	public void test() throws SQLException{
		Graph graph = new Graph();
		Map<Integer, Node> nodemap = graph.InitialGraph();
		Node node0 = nodemap.get(0);
		Node node1 = nodemap.get(1);
		Node node2 = nodemap.get(2);
		Node node4 = nodemap.get(4);
		Node node5 = nodemap.get(5);
		List<Node> nodeList = new ArrayList<>();
		nodeList.add(node0);
		nodeList.add(node1);
		nodeList.add(node2);
		nodeList.add(node4);
		nodeList.add(node5);
		Node node7 = nodemap.get(7);
		float kiin = calComIn(nodeList);
		System.out.println(kiin);

	}


}
