package pers.tiger.bgll;

import java.util.List;

public class Community {
	//community id
	private int id;
	//member of community
	private List<Node> nodeList;
	//interation times
	private int pass;
	//last pass
	private List<Integer> sonList;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Node> getNodeList() {
		return nodeList;
	}
	public void setNodeList(List<Node> nodeList) {
		this.nodeList = nodeList;
	}
	public int getPass() {
		return pass;
	}
	public void setPass(int pass) {
		this.pass = pass;
	}
	public List<Integer> getSonList() {
		return sonList;
	}
	public void setSonList(List<Integer> sonList) {
		this.sonList = sonList;
	}
	
	
}
