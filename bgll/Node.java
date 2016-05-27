package pers.tiger.bgll;

import java.util.HashMap;
import java.util.Map;

public class Node {
	//initial the node
	public Node(int nodeId){
		this.nodeId = nodeId;
		initialNode();
	}
	//node id
	private int nodeId;
	//neighbor node and it's weight
	private Map<Integer, Float> neighborMap;
	//node's name
	private String hostName;
	//node's label
	private int label;
	//a array of communitu id of every iteration
	private int[] list_BGLL;
	//initial neighbor map
	public void initialNode(){
		neighborMap = new HashMap<Integer, Float>();
	}
	//node's degree
	public int getDegree(){
		return neighborMap.size();
	}
	//node's weight
	public float getWeight(){
		float weight = 0;
		for(int key:neighborMap.keySet()){
			weight += neighborMap.get(key);
		}
		return weight;
	}
	public int getNodeId() {
		return nodeId;
	}
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	public Map<Integer, Float> getNeighborMap() {
		return neighborMap;
	}
	public void setNeighborMap(Map<Integer, Float> neighborMap) {
		this.neighborMap = neighborMap;
	}
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	public int getLabel() {
		return label;
	}
	public void setLabel(int label) {
		this.label = label;
	}
	public int[] getList_BGLL() {
		return list_BGLL;
	}
	public void setList_BGLL(int[] list_BGLL) {
		this.list_BGLL = list_BGLL;
	}
	
	
}
