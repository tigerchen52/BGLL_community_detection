package pers.tiger.bgll;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pers.tiger.dao.DBconnection;

public class Graph {
	private float M;
	Map<Integer, Node> nodeMap;
	public Graph(String inputPath) throws NumberFormatException, IOException{
		nodeMap = InitialGraph(inputPath);
	}
	
	public Graph(){
		
	}
	public Map<Integer, Node> getNodeMap() {
		return nodeMap;
	}
	public void setNodeMap(Map<Integer, Node> nodeMap) {
		this.nodeMap = nodeMap;
	}
	public Map<Integer, Node> InitialGraph() throws SQLException{
		float sumWeight = 0;
		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		String sql = "select b.node1,b.node2,b.weight,a.com_id,c.com_id from karate_nodes as a, "
				+ "karate_edges as b,karate_nodes as c where b.node1 = a.node and b.node2 = c.node";
		DBconnection dbc = new DBconnection();
		Connection conn = dbc.getConnection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()){
			int nodeA = rs.getInt(1);
			int nodeB = rs.getInt(2);
			float weight = rs.getFloat(3);
//			String hostName1 = rs.getString(4);
//			String hostName2 = rs.getString(5);
			int lable1 = rs.getInt(4);
			int label2 = rs.getInt(5);
			sumWeight += weight;
			
			//nodeA
			if(nodeMap.containsKey(nodeA)){
				Node node = nodeMap.get(nodeA);
				node.getNeighborMap().put(nodeB, weight);
			}else{
				Node node = new Node(nodeA);
//				node.setHostName(hostName1);
				node.setLabel(lable1);
				node.getNeighborMap().put(nodeB, weight);
				nodeMap.put(nodeA, node);
			}
			//nodeB
			if(nodeMap.containsKey(nodeB)){
				Node node = nodeMap.get(nodeB);
				node.getNeighborMap().put(nodeA, weight);
			}else{
				Node node = new Node(nodeB);
//				node.setHostName(hostName2);
				node.setLabel(label2);
				node.getNeighborMap().put(nodeA, weight);
				nodeMap.put(nodeB, node);
			}
		}
		dbc.closeConnection(rs, st, conn);
		setM(2*sumWeight);
		return nodeMap;
	}
	public Map<Integer, Node> InitialGraph(String inputPath) throws NumberFormatException, IOException{
		float sumWeight = 0;
		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		FileReader fr = new FileReader(new File(inputPath));
		BufferedReader reader = new BufferedReader(fr);
		String str = null;
		while((str = reader.readLine()) != null){
			String line[] = str.split(",");
			int nodeA = Integer.parseInt(line[0]);
			int nodeB = Integer.parseInt(line[1]);
			float weight = 0;
			if(line.length > 2){
				weight = Float.parseFloat(line[2]);
			}else{
				 weight = 1;
			}
//			String hostName1 = rs.getString(4);
//			String hostName2 = rs.getString(5);
			//int lable1 = rs.getInt(4);
			//int label2 = rs.getInt(5);
			sumWeight += weight;
			
			//nodeA
			if(nodeMap.containsKey(nodeA)){
				Node node = nodeMap.get(nodeA);
				node.getNeighborMap().put(nodeB, weight);
			}else{
				Node node = new Node(nodeA);
//				node.setHostName(hostName1);
				//node.setLabel(lable1);
				node.getNeighborMap().put(nodeB, weight);
				nodeMap.put(nodeA, node);
			}
			//nodeB
			if(nodeMap.containsKey(nodeB)){
				Node node = nodeMap.get(nodeB);
				node.getNeighborMap().put(nodeA, weight);
			}else{
				Node node = new Node(nodeB);
//				node.setHostName(hostName2);
				//node.setLabel(label2);
				node.getNeighborMap().put(nodeA, weight);
				nodeMap.put(nodeB, node);
			}
		}
		reader.close();
		setM(2*sumWeight);
		this.nodeMap = nodeMap;
		return nodeMap;
	}
	
	public Map<Integer, Node> InitialGraph(List<Edge> edgeList){
		float sumWeight = 0;
		Map<Integer, Node> nodeMap = new HashMap<Integer, Node>();
		for(Edge edge:edgeList){
			int nodeA = edge.getServerId();
			int nodeB = edge.getClientId();
			float weight = edge.getWeight();
			
			sumWeight += weight;
			//nodeA
			if(nodeMap.containsKey(nodeA)){
				Node node = nodeMap.get(nodeA);
				node.getNeighborMap().put(nodeB, weight);
			}else{
				Node node = new Node(nodeA);
				node.getNeighborMap().put(nodeB, weight);
				nodeMap.put(nodeA, node);
			}
			//nodeB
			if(nodeMap.containsKey(nodeB)){
				Node node = nodeMap.get(nodeB);
				node.getNeighborMap().put(nodeA, weight);
			}else{
				Node node = new Node(nodeB);
				node.getNeighborMap().put(nodeA, weight);
				nodeMap.put(nodeB, node);
			}
		}
		setM(2*sumWeight);
		this.nodeMap = nodeMap;
		return nodeMap;
	}
	List<Edge> getEdge(String sql) throws SQLException{
		List<Edge> edgeList = new ArrayList<>();
		DBconnection dbc = new DBconnection();
		Connection conn = dbc.getConnection();
		Statement st = conn.createStatement();
		ResultSet rs = st.executeQuery(sql);
		while(rs.next()){
			int nodeA = rs.getInt(1);
			int nodeB = rs.getInt(2);
			float weight = rs.getFloat(3);
			Edge edge = new Edge();
			edge.setServerId(nodeA);
			edge.setClientId(nodeB);
			edge.setWeight(weight);
			edgeList.add(edge);
		}
		return edgeList;
	}
	
	public void printGraph(Map<Integer, Node> nodeMap){
		for(int nodeid:nodeMap.keySet()){
			System.out.println("node-" + nodeid  + "'s neighbor:");
			Map<Integer, Float> neighbor = nodeMap.get(nodeid).getNeighborMap();
			for(int id:neighbor.keySet()){
				System.out.println(nodeid + "-" + id + ": " + neighbor.get(id));
			}
		}
	}
	public float getM() {
		return M;
	}
	public void setM(float m) {
		M = m;
	}
	
}
