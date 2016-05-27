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
		//storage the result 存储BGLL划分结果
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		Graph graph = new Graph();
		//intial the network初始网络
		Map<Integer, Node> nodeMap = graph.InitialGraph();
		//网络总权重
		this.M = graph.getM();
		//标记
		boolean flag = true;
		int start = nodeMap.size();
		int pass = 1;

		while(flag){
			List<Community> comList = startBGLL(nodeMap, pass);
			int end = comList.size();
			if(end!=start){
				commnityMap.put(pass, comList);
				System.out.println("这是第" + pass + "趟划分：");
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
		//存储BGLL划分结果
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		//初始网络
		Graph graph = new Graph(inputPath);
		Map<Integer, Node> nodeMap = graph.getNodeMap();
		//网络总权重
		this.M = graph.getM();
		//标记
		boolean flag = true;
		int start = nodeMap.size();
		int pass = 1;

		while(flag){
			List<Community> comList = startBGLL(nodeMap, pass);
			int end = comList.size();
			if(end!=start){
				commnityMap.put(pass, comList);
				System.out.println("这是第" + pass + "趟划分：");
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

	//执行BGLL划分,通过参数pass控制
	public void excuteBGLL(int pass,String path) throws SQLException, IOException{
		//存储BGLL划分结果
		Map<Integer, List<Community>> commnityMap = new HashMap<>();;
		Graph graph = new Graph();
		//初始网络
		Map<Integer, Node> nodeMap = graph.InitialGraph();
		//网络总权重
		this.M = graph.getM();
		for(int i = 1;i <= pass;i++){
			List<Community> comList = startBGLL(nodeMap, i);
			commnityMap.put(i, comList);
			System.out.println("这是第" + i + "趟划分：");
			printCom(comList);
			nodeMap = getNewNodeMap(comList);
		}
		System.out.println("start print the result :");
		printCommunity(commnityMap,path);

	}

	//phase1，将每个节点看做是一个社区，把节点i加入到模块度增益最大的邻居节点j的社区，如果模块度增益是负的，保持不动
	private List<Community> startBGLL(Map<Integer, Node> nodeMap,int pass){
		//迭代终止标记，如果没有节点移动，flag=0 ,迭代结束
		boolean flag = false;
		List<Community> comList = new LinkedList<>();
		//初始化社区链表
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
		//遍历社区中每个节点，计算其增益最大邻居社区，增益为负什么都不做,当没有节点移动迭代结束
		while(flag == false){
			//获取初始社区Map
			Map<Integer, Integer> validateMap = getValidation(comList);
			for(Community com:comList){
				List<Node> nodeInCom = com.getNodeList();
				//节点i社区id
				int cid = com.getId();
				//记录要删除的节点，节点本身社区id，要加入的社区id
				Iterator<Node> it1 = nodeInCom.iterator();
				while(it1.hasNext()){
					Node node = it1.next();
					//节点j的社区id
					int maxNeighbor = maxQNeighbor(node, comList);
					//有模块度增益最大的邻居节点且所属社区相异。节点i加入节点j所在社区
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
			//记录节点数量为零的社区下标，将其删除
			int newId = 0;
			for(Iterator<Community> it = comList.iterator();it.hasNext();){
				Community com = it.next();
				if(com.getNodeList().size() == 0){
					it.remove();
				}else{
					com.setId(newId++);
				}
			}
			//是否有节点移动
			flag = judge(validateMap, comList);
		}

		return comList;
	}
	//计算节点i邻居中最大增益模块度的邻居所属社团
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
	//计算节点i所在社区
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

	//将划分好社区抽象成节点
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

	//in 计算社区内部边权重之和
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

	//tot 所有与社区C内部节点相关联的边权重之和
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

	//Ki 节点i的权重之和
	private float calNodeWeight(Node node){
		float Sumweight = 0;
		Map<Integer, Float> neighborMap = node.getNeighborMap();
		for(float weight:neighborMap.values()){
			Sumweight += weight;
		}
		return Sumweight;
	}

	//Ki,in 节点i与社区C的内部连边权重之和
	private float calIIn(Node node,List<Node> nodeList){
		float sumIin = 0;
		//存放社区内节点ID
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

	//打印社区划分结果
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
	//记录每次划分节点所属社区
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
	//判断两次划分是否一样
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

	//打印BGLL社区划分结果
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
