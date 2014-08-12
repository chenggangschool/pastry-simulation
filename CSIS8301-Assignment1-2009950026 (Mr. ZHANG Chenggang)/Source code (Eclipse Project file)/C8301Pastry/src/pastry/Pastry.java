package pastry;

import java.util.*;

public class Pastry 
{
	private Vector<PastryNode> nodeList;   //the list of all 256 pastry nodes, no matter live or dead
	
	private Vector<PastryNode> liveNodesList;  //added at 18:44 Nov 11th, 2009
	
	private int b;
	private int length;
	private int L;
	private int M;
	
	private int base;
	
	private int protocolID;   //added at 23:37, Nov 11th, 2009
	
//	private Vector<PastryNode> routingPath; //routing path    //commented at 11:15, Nov 11th, 2009
	
	public Pastry(int b, int length, int L, int M)
	{
		//initially, the nodeList should be null, which means that the pastry network contains no pastry node at first.
		this.b = b;
		this.base = (int)Math.pow(2, b);
		this.length = length;
		this.L = L;
		this.M = M;
		this.nodeList = new Vector<PastryNode>( (int) Math.pow( (int)Math.pow(2, this.b), this.length) );
		this.liveNodesList = new Vector<PastryNode>();
		
		PastryNode node = new PastryNode("", L, M, b, length);
		node.setLiveOrNot(false);   //false node
		for(int i = 0; i < this.nodeList.capacity(); i++)
		{
			this.nodeList.addElement(node);
		}
//		this.routingPath = new Vector<PastryNode>();  //commented at 11:15, Nov 11th, 2009
	}
	
	public void setProtocolID(int protocolId)
	{
		this.protocolID = protocolId;
	}
	
	public int getProtocolID()
	{
		return this.protocolID;
	}
	
	public Vector<PastryNode> getLiveNodesList()
	{
		this.liveNodesList.clear();   //added at 22:37 Nov 11th, 2009 to remove bugs!
		for(int i = 0; i < this.nodeList.size(); i++)
		{
			if(this.nodeList.elementAt(i).liveOrNot)
			{
				this.liveNodesList.addElement(this.nodeList.elementAt(i));
			}
		}
		return this.liveNodesList;
	}
	
	public int getLiveNodesNum()
	{
		this.getLiveNodesList();
		return this.liveNodesList.size();
	}
	
	//****************   commented at 11:15, Nov 11th, 2009   *******//
//	public void clearRoutingPath()
//	{
//		this.routingPath.clear();
//	}
//	public Vector<PastryNode> getRoutingPath()
//	{
//		return this.routingPath;
//	}
	//****************   commented at 11:15, Nov 11th, 2009   *******//
	
	public Vector<PastryNode> getNodeList()
	{
		return this.nodeList;
	}
	
	public int getB()
	{
		return this.b;
	}
	public int getLength()
	{
		return this.length;
	}
	
	
	public boolean isValidNodeId(String nodeId)
	{
		if(nodeId.length() != 4)
			return false;
		
		for(int i = 0; i < nodeId.length(); i++)
		{
			char eachChar = nodeId.charAt(i);
			if(!Character.isDigit(eachChar))
			{
				return false;
			}
			else
			{
				if(eachChar - '0' >= 0 && eachChar - '0' <= 3)
				{
				}
				else
				{
					return false;
				}
			}
		}
		return true;
	}
	public boolean nodeIsLive(String nodeId)
	{
		if( this.getNodeList().elementAt(getIndex(nodeId)).liveOrNot)
			return true;
		else
			return false;
	}
	
	/*
	 * @todo: insert the node "nodeIdStr" into the pastry network and re-format the table information 
	 */
	public boolean/*void*/insertNode(String nodeIdStr)
	{
		if(this.nodeList.elementAt(this.getIndex(nodeIdStr)).liveOrNot)
		{
			//we already have this node in the network
			return false;
		}

		PastryNode newNode = new PastryNode(nodeIdStr, this.L, this.M, 2, 4);
		
		String nearestNode = findProximityNearestNode(nodeIdStr);    //the joining/starting node is defined according to the physical distance
		
		if(nearestNode == null)
		{
			//the pastry network is now null and contains no nodes at all
			if(this.protocolID == 0)
			{
				for(int i = 0; i < this.length; i++)
				{
					//set the routing table for the newly inserted node, which is the only node in the pastry network
					newNode.routingTable[i][nodeIdStr.charAt(i) - '0'] = nodeIdStr;
					//set one entry as "SELF" at each line 
				}
			}
			else if(this.protocolID == 1)
			{
				//need to be rewritten
				for(int i = 0; i < this.length; i++)
				{
					//set the routing table for the newly inserted node, which is the only node in the pastry network
					newNode.routingTable[i][nodeIdStr.charAt(i) - '0'] = nodeIdStr;
					//set one entry as "SELF" at each line 
				}
				
				//specially set the L and M set to contain itself
				newNode.neighborhoodSet[0] = nodeIdStr;
				//////newNode.smallerLeafSet[0] = nodeIdStr;        //commented at 12:24, Nov 12th,2009
			}
		}
		else
		{
			//the pastry network already has some node(s)
			PastryNode joiningNode = nodeList.elementAt(getIndex(nearestNode));

			Vector<PastryNode> routingPathWhenFirstInserted = new Vector<PastryNode>();
			joiningNode.route(nodeIdStr, routingPathWhenFirstInserted);   //put the result into routingPathWhenFirstInserted
			//we have found the starting node to join the pastry network
			//Vector<PastryNode> routingPath = thisNode.route(nodeIdStr);
			//It is necessary to maintain a 256-length vector with each node in it, and we can get the PastryNode object
			//once we have the "NodeIdStr", because we can change this NodeIdStr into the index of the 256-length vector. 
			newNode.setRoutingPathForMyself(routingPathWhenFirstInserted);  //added at 13:08 Nov 11th, 2009
			
			//after this function call, routingPath has been changed accordingly.
			
			newNode.constructLSet();
			newNode.constructMSet();
			newNode.constructRTable();
			
			//tell my arrival to the relevant nodes by giving my R, L, M tables to them
			newNode.getNodesUnionList();
			for(int p = 0; p < newNode.nodesUnionList.size(); p++)
			{
				//each node in the union of the new node's R,L,M, whose R,L,M information may need to be updated
				int currentNodeIndex = getIndex((newNode.nodesUnionList).elementAt(p));
				PastryNode currentNode = this.nodeList.elementAt(currentNodeIndex);
				
				/****************    Added to avoid operating on false nodes    ****************/
				if(currentNode.getIdStr().equals("") || currentNode.getLiveOrNot() == false)
				{
					//this node is a false node
//					break;
				}
				/****************    Added to avoid operating on false nodes    ****************/
				
				/****************    Added to avoid operating on myself    ****************/
				else if(currentNode.getIdStr().equals(newNode.getIdStr()))
				{
					//this node is a the new node itself
//					break;
				}
				/****************    Added to avoid operating on myself    ****************/
				else
				{
					currentNode.updateRTable(newNode);
					currentNode.updateLSet(newNode);
					currentNode.updateMSet(newNode);
				}
			}
		}
		this.nodeList.setElementAt(newNode, getIndex(nodeIdStr));  
		return true;
	}
	
	private Vector<String> extractNodesListInMSet(int currentNodeIndex)
	{
		Vector<String> rstVector = new Vector<String>();
		PastryNode currentNode = this.nodeList.elementAt(currentNodeIndex);
		String [] MSet = currentNode.neighborhoodSet;
		for(int j = 0; j < MSet.length; j++)
		{
			if(MSet[j] != null)      //added by chenggang, 0:14 Nov 10th, 2009
			{
				if(!rstVector.contains(MSet[j]))
				{
					rstVector.addElement(MSet[j]);
					//System.out.println("I have inserted element :" + MSet[j]);
				}
			}
		}
		//System.out.println("rstVector.length() = " + rstVector.size());
		//System.out.println("rstVector.elementAt(0) = " + rstVector.elementAt(0));
		return rstVector;
	}
	private Vector<String> mergeNodesList(Vector<String> newNodesList, Vector<String> previousNodesList)
	{
		Vector<String> rstVector = new Vector<String>();
		for(int j = 0; j < newNodesList.size(); j++)
		{
			if(newNodesList.elementAt(j) != null)
			{
				if(!rstVector.contains(newNodesList.elementAt(j)))
				{
					rstVector.addElement(newNodesList.elementAt(j));
				}
			}
		}
		for(int j = 0; j < previousNodesList.size(); j++)
		{
			if(previousNodesList.elementAt(j)!= null)
			{
				if(!rstVector.contains(previousNodesList.elementAt(j)))
				{
					rstVector.addElement(previousNodesList.elementAt(j));
				}
			}
		}
		
		return rstVector;
	}
	private Vector<String> chooseTopMNodes(Vector<String> candidateNodesList, String currentNodeIdStr)
	{
		Vector<String> rstVector = new Vector<String>();
		Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
		orderedNodesList = sort(candidateNodesList, currentNodeIdStr);
		for(int k = 0; k < ( (this.M < orderedNodesList.size()) ? this.M : orderedNodesList.size() ); k++)
		{
			if(orderedNodesList.elementAt(k) != null)
			{
				rstVector.addElement(orderedNodesList.elementAt(k));
			}
			else
				break;
		}
		return rstVector;
	}
	
	private Vector<String> sort(Vector<String> previousNodesList, String currentNodeIdStr)
	{
		//Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
		int size = previousNodesList.size();
		for(int i = 0; i < size - 1; i++)
		{
			for(int j = 0; j < size - 1 - i; j++)
			{
				if( (getPhysicalDistance(previousNodesList.elementAt(j), currentNodeIdStr) > 
				getPhysicalDistance(previousNodesList.elementAt(j + 1), currentNodeIdStr) ) ||
					( (getPhysicalDistance(previousNodesList.elementAt(j), currentNodeIdStr) == 
					getPhysicalDistance(previousNodesList.elementAt(j + 1), currentNodeIdStr)) && 
					getIndex(previousNodesList.elementAt(j)) > getIndex(previousNodesList.elementAt(j + 1)) ) )
				{
					//swap()
					String temp = "";
					temp = previousNodesList.elementAt(j);
					previousNodesList.setElementAt(previousNodesList.elementAt(j + 1), j);
					previousNodesList.setElementAt(temp, j + 1);
				}
			}
		}
		return previousNodesList;
		//return orderedNodesList;
	}
	
	/*
	 * @ todo: to find possible improvement for current node currentNodeIdStr's Routing table's (m, n) entry value "currentEntry"
	 *  	   with potential better entries from the newNodesList
	 * */
	private String getBetterRoutingTableEntry(String currentNodeIdStr, int m, int n, String currentEntry, Vector<String> newNodesList)
	{
		String betterNodeIdStr = null;
		if(currentEntry == null)
		{
			//previous value is null
			for(int i = 0; i < newNodesList.size(); i++)
			{
				//see if this one is a better choice
				String thisNewNodeId = newNodesList.elementAt(i);
				if(thisNewNodeId.startsWith(currentNodeIdStr.substring(0, m)) && thisNewNodeId.charAt(m) == n + '0')
				{
					//this is the prerequisite, and if satisfying that, it is a valid choice, but not necessarily better
					if(currentEntry == null)
					{
						//the first time I find a valid non-null entry
						currentEntry = thisNewNodeId;
					}
					else
					{
						if(getPhysicalDistance(currentNodeIdStr, thisNewNodeId) < getPhysicalDistance(currentNodeIdStr, currentEntry)
								|| ( getPhysicalDistance(currentNodeIdStr, thisNewNodeId) == getPhysicalDistance(currentNodeIdStr, currentEntry)
										&& getIndex(thisNewNodeId) <= getIndex(currentEntry) )
										)
						{
							//find a better one
							currentEntry = thisNewNodeId;
							betterNodeIdStr = thisNewNodeId;
						}
					}
				}
			}
		}
		else
		{
			//previous has a non-null value, but may find a nearer node for this entry from the newNodesList
			for(int i = 0; i < newNodesList.size(); i++)
			{
				//see if this one is a better choice
				String thisNewNodeId = newNodesList.elementAt(i);
				if(thisNewNodeId.startsWith(currentNodeIdStr.substring(0, m)) && thisNewNodeId.charAt(m) == n + '0')
				{
					//this is the prerequisite, and if satisfying that, it is a valid choice, but not necessarily better
					if(getPhysicalDistance(currentNodeIdStr, thisNewNodeId) < getPhysicalDistance(currentNodeIdStr, currentEntry)
							|| ( getPhysicalDistance(currentNodeIdStr, thisNewNodeId) == getPhysicalDistance(currentNodeIdStr, currentEntry)
									&& getIndex(thisNewNodeId) <= getIndex(currentEntry) )
									)
					{
						//find a better one
						currentEntry = thisNewNodeId;
						betterNodeIdStr = thisNewNodeId;
					}
				}
			}
		}
		return betterNodeIdStr;
	}
	
	private String findProximityNearestNode(String nodeIdToInsert)
	{
	        //sort the distance from small to big, the id from small to big
	        int i = 0;
	        int smallestDistance = 10000000;
	        String nearestNodeIdStr = null;
	        int numOfNodes = this.nodeList.size();
	        for(i = 0; i < numOfNodes; i++)
	        {
	        	PastryNode currentNode = this.nodeList.elementAt(i);
	        	if(currentNode.liveOrNot && getPhysicalDistance(nodeIdToInsert, currentNode) < smallestDistance)
	            {
	        		nearestNodeIdStr = currentNode.idStr;
	            }
	        }
	        return nearestNodeIdStr;
	}
	
	private int getPhysicalDistance(String newNodeIdStr, PastryNode node2)
	{
		int locationOfNewNode = getLocationOfNode(newNodeIdStr);
		return Math.abs(locationOfNewNode - node2.getLocation());
	}
	
	public int getPhysicalDistance(String newNodeIdStr, String node2Str)
	{
		int locationOfNewNode = getLocationOfNode(newNodeIdStr);
		int locationOfNode2 = getLocationOfNode(node2Str);
		return Math.abs(locationOfNewNode - locationOfNode2);
	}
	
	public String getStrFromIndex(int nodeIdIndex)
	{
		//nodeIdIndex is from 0 to 255
		//return "1111";
		/**
		 * 十进制转二进制：  用2辗转相除至结果为1,将余数和最后的1从下向上倒序写 就是结果  
		例如302  
		302/2 = 151 余0  
		151/2 = 75 余1  
		75/2 = 37 余1  
		37/2 = 18 余1  
		18/2 = 9 余0  
		9/2 = 4 余1  
		4/2 = 2 余0  
		2/2 = 1 余0  
		故二进制为100101110 
		 * */
		Vector<String> remainer = new Vector<String>();
		int eachResult = nodeIdIndex;

		while(eachResult >= 4)
		{
			remainer.addElement(Integer.toString(eachResult % 4));
			eachResult /= 4;
		}
		remainer.addElement(Integer.toString(eachResult % 4));  
		//Vector<Integer> resultStr = new Vector<Integer>();
		String resultStr = "";
		for(int i = remainer.size() - 1; i >= 0; i--)
		{
			resultStr += remainer.elementAt(i);
		}
		String updatedResultStr = "";
		if(resultStr.length() < 4)
		{
			//need to add several "0" in the front
			for(int j = 0; j < 4 - resultStr.length(); j++)
			{
				updatedResultStr += "0";
			}
			updatedResultStr += resultStr;
		}
		else
		{
			updatedResultStr = resultStr;
		}
		return updatedResultStr;
	}
	
	/*
	 * @todo: return the location of node "nodeId"
	 * */
	public int getLocationOfNode(String nodeIdStr)
	{
		int[] location = {51, 58, 179, 115, 
				          78, 133, 44, 192, 
				          30, 53, 225, 99, 
				          68, 56, 13, 106, 
				          57, 216, 231, 112, 
				          11, 203, 213, 249,
				          223, 222, 255, 29,
				          61, 59, 184, 63,
				          31, 206, 22, 167,
				          50, 2, 88, 77,
				          17, 121, 70, 126,
				          18, 217, 178, 148,
				          122, 60, 136, 93,
				          237, 75, 65, 16,
				          204, 71, 228, 149,
				          28, 143, 120, 74,
				          20, 119, 15, 84,
				          43, 196, 168, 97,
				          246, 211, 146, 92, 
				          200, 243, 24, 0,
				          220, 150, 173, 4,
				          3, 103, 195, 66,
				          55, 64, 197, 82,
				          253, 238, 45, 38,
				          42, 86, 101, 96,
				          191, 9, 10, 49,
				          199, 127, 230, 128,
				          109, 79, 80, 190,
				          40, 25, 100, 39,
				          138, 137, 1, 162,
				          73, 76, 160, 132,
				          226, 5, 239, 46,
				          145, 85, 37, 113,
				          210, 67, 87, 110,
				          34, 36, 205, 111,
				          233, 91, 186, 193,
				          152, 157, 181, 7,
				          26, 242, 219, 180,
				          117, 116, 134, 174,
				          164, 215, 33, 32,
				          218, 69, 139, 185,
				          83, 169, 241, 202,
				          124, 177, 155, 161,
				          252, 229, 54, 114,
				          175, 62, 14, 198,
				          48, 144, 98, 250,
				          153, 165, 189, 209,
				          234, 151, 72, 8,
				          118, 176, 156, 159,
				          94, 140, 194, 187,
				          172, 170, 166, 104, 
				          135, 102, 245, 163,
				          212, 27, 41, 235,
				          47, 147, 183, 221,
				          23, 208, 251, 214,
				          154, 35, 129, 247,
				          248, 123, 141, 171,
				          158, 21, 89, 142,
				          52, 105, 227, 107,
				          244, 224, 232, 108,
				          188, 201, 254, 95,
				          130, 236, 6, 240,
				          81, 131, 182, 12,
				          19, 125, 207, 90};
		int nodeIdIndex = getIndex(nodeIdStr);
		return location[nodeIdIndex];
	}
	
	public int getIndex(String nodeIdStr)
	{
		int sum = 0;
		int i = 0;
		int len = nodeIdStr.length();
		for(i = 0; i < len; i++)
		{
			sum += (nodeIdStr.charAt(i) - '0') * Math.pow(Math.pow(2, this.b), len - 1 - i);
		}
		return sum;
	}
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*private*/public class PastryNode
	{
		private String idStr;
		private int location;
		private boolean liveOrNot;  
		//really need this one? One opinion should be: as long as one pastry node is in the pastry network, it should be live
		private String[] leafSet;
		
		private String[] smallerLeafSet;
		private String[] largerLeafSet;
		
		private String[] neighborhoodSet;
		private String[][] routingTable;
		private Vector<String> nodesUnionList;   //used for rare cases
		
//		private Vector<PastryNode> routingPath;  //added by chenggang at 11:51, Nov 10th, 2009
		//commented at 12:59, Nov 11th, 2009 to solve the bug of the resulted routing path returned by route()
		
		private Vector<PastryNode> routingPathForMyself;   //added by chenggang at 11:47, Nov 11th,2009
		private Vector<String> allAvailableNodes;
		
		private Vector<String> allAvailableNodesForUpdate;  //added by chenggang at 21:56, Nov 10th,2009
		
		private int b;
		private int length;
		
		private int L;
		private int M;
		
		private Vector<String> smallerNodesList;
		private Vector<String> largerNodesList;
		
		public PastryNode(String nodeIdStr, int L, int M, int b, int length)
		{
			this.idStr = nodeIdStr;
			this.location = getLocationOfNode(this.idStr);
			this.liveOrNot = true;
			this.leafSet = new String[L];
			
			int halfL = (int)(L / 2);
			this.smallerLeafSet = new String[halfL];
			this.largerLeafSet = new String[halfL];
			
//			System.out.println("****************");
//			for(int i = 0; i < this.leafSet.length; i++)
//			{
//				System.out.println("leafset[" + i + "] = " + this.leafSet[i]);
//			}
//			System.out.println("****************");
			this.neighborhoodSet = new String[M];
			this.routingTable = new String[length][(int)Math.pow(2, b)];
			this.nodesUnionList = new Vector<String>();
			
//			this.routingPath = new Vector<PastryNode>();
			this.routingPathForMyself = new Vector<PastryNode>();
			this.allAvailableNodes = new Vector<String>();
			
			this.allAvailableNodesForUpdate = new Vector<String>();
			
			this.b = b;
			this.length = length;
			
			this.L = L;
			this.M = M;
			
			smallerNodesList = new Vector<String>();
			largerNodesList = new Vector<String>();
		}
		
		public void setRoutingPathForMyself(Vector<PastryNode> pathForMyself)
		{
			for(int i = 0; i < pathForMyself.size(); i++)
			{
				PastryNode node = pathForMyself.elementAt(i);
				try
				{
					//System.out.println("i = " + i + "; node.id = " + node.getIdStr());
					//this.routingPathForMyself.setElementAt(node, 0);
					this.routingPathForMyself.addElement(node);
				}
				catch(Exception e)
				{
					System.out.println(e.getMessage());
					System.out.println(e.getLocalizedMessage());
				}
			}
		}
		
		public void setLiveOrNot(boolean liveOrNot)
		{
			this.liveOrNot = liveOrNot;
		}
		
		public boolean getLiveOrNot()
		{
			return this.liveOrNot;
		}
		
		public String getIdStr()
		{
			return this.idStr;
		}
		
		public String[] getSmallerLeafSet()
		{
			return this.smallerLeafSet;
		}
		
		public String[] getLargerLeafSet()
		{
			return this.largerLeafSet;
		}
		
		public String[] getNeighborhoodSet()
		{
			return this.neighborhoodSet;
		}
		
		public int getLocation()
		{
			return this.location;
		}
		
		public String[][] getRoutingTable()
		{
			return this.routingTable;
		}
		
		public Vector<PastryNode> getRoutingPathForMyself()
		{
			return this.routingPathForMyself;
		}
		
		public void printRoutingTable()
		{
			//System.out.println("******    The routing table is as follows:   ******");
			System.out.println("Routing table:");
			System.out.println("-----------------------------------------------");
			for(int k = 0; k < this.length; k++)
			{
				for(int m = 0; m < Math.pow(2, this.b); m++)
				{
					if(this.getRoutingTable()[k][m] == null)
					{
						System.out.print( "    " + "   ||   ");
					}
					else
					{
						if(this.getRoutingTable()[k][m].equals(this.idStr))
						{
							System.out.print( "SELF" + "   ||   ");
						}
						else
							System.out.print( this.getRoutingTable()[k][m] + "   ||   ");
					}
				}
				System.out.println();
				System.out.println("-----------------------------------------------");
			}
			//System.out.println("@@@@@@    The routing table    @@@@@@");
		}
		
		public void printLeafSet()
		{
			System.out.print("Leaf set:    ");
			String [] smallerLeafSet = this.getSmallerLeafSet();
			for(int j = 0; j < smallerLeafSet.length; j++)
			{
				if(smallerLeafSet[j] != null)
					System.out.print(smallerLeafSet[j] + "  ");
			}
			
			System.out.print(" || ");
			
			String [] largerLeafSet = this.getLargerLeafSet();
			for(int j = 0; j < largerLeafSet.length; j++)
			{
				if(largerLeafSet[j] != null)
					System.out.print(largerLeafSet[j] + "  ");
			}
			System.out.println();
		}
		
		public void printNeighborSet()
		{
			System.out.print("Neighborhood set:    ");
			String [] neighborhoodSet = this.getNeighborhoodSet();
			for(int j = 0; j < neighborhoodSet.length; j++)
			{
				if(neighborhoodSet[j] != null)
					System.out.print(neighborhoodSet[j] + "  ");
			}
			System.out.println("");
		}
		
		public void printRoutingPathWhenInserted()
		{
			Vector<PastryNode> path = this.getRoutingPathForMyself();
			if(path != null && path.size() != 0)
			{
				System.out.println("This node's routingPath when inserted is:");
				for(int j = 0; j < path.size() - 1; j++)
				{
					System.out.print(path.elementAt(j).getIdStr() + " --> ");
				}
				System.out.println(path.elementAt(path.size() - 1).getIdStr());
			}
		}
		
		public void printNodesUnionList()
		{
			Vector<String> unionNodesList = this.getNodesUnionList();
			if(unionNodesList != null && unionNodesList.size() != 0)
			{
				System.out.println("This node's nodesUnionList is:");
				for(int j = 0; j < unionNodesList.size() - 1; j++)
				{
					System.out.print(unionNodesList.elementAt(j) + " --> ");
				}
				System.out.println(unionNodesList.elementAt(unionNodesList.size() - 1));
			}
		}
		
		/*
		 * @todo: test whether keyIdStr falls among the leaf set of this node.
		 * @return true if among the leaf set
		 * @return false if not
		 * */
		private boolean inLeafSet(String keyIdStr)
		{
			int keyIndex = getIndex(keyIdStr);
			//if(leafSetIsNull())
			if(smallerLeafSetIsNull() && largerLeafSetIsNull())
			{
				return false;
			}
			else
			{
				//at least one set is not null, i.e., the leaf set is not null
				if(keyIndex >= getSmallestLeaf() && keyIndex <= getLargestLeaf())
				{
					return true;
				}
				else
					return false;
			}
		}
//		private boolean leafSetIsNull()
//		{
//			for(int i = 0; i < this.leafSet.length; i++)
//			{
//				if(this.leafSet[i] != null)
//				{
//					return false;
//				}
//			}
//			return true;
//		}
		
		private boolean smallerLeafSetIsNull()
		{
			for(int i = 0; i < this.smallerLeafSet.length; i++)
			{
				if(this.smallerLeafSet[i] != null)
				{
					return false;
				}
			}
			return true;
		}
		
		private boolean largerLeafSetIsNull()
		{
			for(int i = 0; i < this.largerLeafSet.length; i++)
			{
				if(this.largerLeafSet[i] != null)
				{
					return false;
				}
			}
			return true;
		}
		
		private Vector<String> getCompressedLeafSet()
		{
			Vector<String> compressedLeafSet = new Vector<String>(this.L);
			for(int i = 0; i < this.smallerLeafSet.length; i++)
			{
				String node = this.smallerLeafSet[i];
				if(node != null && !compressedLeafSet.contains(node))
				{
					compressedLeafSet.addElement(node);
				}
			}
			for(int i = 0; i < this.largerLeafSet.length; i++)
			{
				String node = this.largerLeafSet[i];
				if(node != null && !compressedLeafSet.contains(node))
				{
					compressedLeafSet.addElement(node);
				}
			}
			return compressedLeafSet;
		}
		
		
//		private int getSmallestLeaf()
//		{
//			//assume that the leaf is already in order(from smallest to largest)
//			int smallestLeaf = 1000000;
//			if(smallerLeafSetIsNull())
//			{
//				return 1000000;
//			}
//			else
//			{
//				//leaf set is not null, should return a positive number representing the smallest node
//				for(int i = 0; i < this.smallerLeafSet.length; i++)
//				//for(int i = 0; i < this.leafSet.length; i++)
//				{
//					if(this.smallerLeafSet[i] == null)   //if(this.leafSet[i].equals(""))
//					{
//						break;
//					}
//					else
//					{
//						int currentIndex = getIndex(this.smallerLeafSet[i]);
//						if( currentIndex < smallestLeaf)
//						{
//							smallestLeaf = currentIndex; 
//						}
//					}
//				}
//				return smallestLeaf;
//			}
//		}
		
		private int getSmallestLeaf()
		{
			//assume that the leaf is already in order(from smallest to largest)
			
			Vector<String> compressedLeafSet = getCompressedLeafSet();
			int smallestLeaf = 1000000;
			
			{
				//leaf set is not null, should return a positive number representing the smallest node
				for(int i = 0; i < compressedLeafSet.size(); i++)
				//for(int i = 0; i < this.leafSet.length; i++)
				{
					if(compressedLeafSet.elementAt(i) == null)   //if(this.leafSet[i].equals(""))
					{
						//break;
					}
					else
					{
						int currentIndex = getIndex(compressedLeafSet.elementAt(i));
						if( currentIndex < smallestLeaf)
						{
							smallestLeaf = currentIndex; 
						}
					}
				}
				return smallestLeaf;
			}
		}
		
//		private int getLargestLeaf()
//		{
//			//assume that the leaf is already in order(from smallest to largest)
//			int largestLeaf = -1;
//			if(largerLeafSetIsNull())
//			{
//				return -1;
//			}
//			else
//			{
//				for(int i = 0; i < this.largerLeafSet.length; i++)
//				{
//					if(this.largerLeafSet[i] == null)   //if(this.leafSet[i].equals(""))
//					{
//						break;
//					}
//					else
//					{
//						int currentIndex = getIndex(this.largerLeafSet[i]);
//						if( currentIndex > largestLeaf)
//						{
//							largestLeaf = currentIndex; 
//						}
//					}
//				}
//				return largestLeaf;
//			}
//		}
		
		private int getLargestLeaf()
		{
			//assume that the leaf is already in order(from smallest to largest)
			
			Vector<String> compressedLeafSet = getCompressedLeafSet();
			int largestLeaf = -1;
			
			{
				//leaf set is not null, should return a positive number representing the smallest node
				for(int i = 0; i < compressedLeafSet.size(); i++)
				//for(int i = 0; i < this.leafSet.length; i++)
				{
					if(compressedLeafSet.elementAt(i) == null)   //if(this.leafSet[i].equals(""))
					{
						//break;
					}
					else
					{
						int currentIndex = getIndex(compressedLeafSet.elementAt(i));
						if( currentIndex > largestLeaf)
						{
							largestLeaf = currentIndex; 
						}
					}
				}
				return largestLeaf;
			}
		}
		
		public Vector<String>/*void*/ getNodesUnionList()
		{
			this.nodesUnionList.clear();  //reset
			
			//nodesUnionList = (nodes in L) U (nodes in R) U (nodes in M);
			//1. get the initial union first, 
			//2. and then sort the nodes from the smallest to the largest
			
			//R table
			for(int i = 0; i < length; i++)
			{
				for(int j = 0; j < Math.pow(2, b); j++)
				{
					String currentNode = this.routingTable[i][j];
					if(currentNode != null)    //if(!currentNode.equals(""))
					{
						//not null
						if(!this.nodesUnionList.contains(currentNode))
						{
							this.nodesUnionList.addElement(currentNode);
						}
					}
				}
			}
			
			//L set
//			for(int j = 0; j < leafSet.length; j++)
//			{
//				String currentNode = leafSet[j];
//				if(currentNode != null)
//				{
//					//not null
//					if(!this.nodesUnionList.contains(currentNode))
//					{
//						this.nodesUnionList.addElement(currentNode);
//					}
//				}
//				else
//					break;
//			}
			
			//smaller L set
			for(int j = 0; j < this.smallerLeafSet.length; j++)
			{
				String currentNode = smallerLeafSet[j];
				if(currentNode != null)
				{
					//not null
					if(!this.nodesUnionList.contains(currentNode))
					{
						this.nodesUnionList.addElement(currentNode);
					}
				}
				else
					break;
			}
			//larger L set
			for(int j = 0; j < this.largerLeafSet.length; j++)
			{
				String currentNode = largerLeafSet[j];
				if(currentNode != null)
				{
					//not null
					if(!this.nodesUnionList.contains(currentNode))
					{
						this.nodesUnionList.addElement(currentNode);
					}
				}
				else
					break;
			}
			
			//M set
			for(int j = 0; j < neighborhoodSet.length; j++)
			{
				String currentNode = neighborhoodSet[j];
				if(currentNode != null)
				{
					//not null
					if(!this.nodesUnionList.contains(currentNode))
					{
						this.nodesUnionList.addElement(currentNode);
					}
				}
				else
					break;
			}
			
			//now we need to re-sort the result nodesList to make it easier to use
			sortNodesUnionList();
			
			return this.nodesUnionList;                       //added by chengang at 12:06, Nov 10th, 2009
		}
		
		private void sortNodesUnionList()
		{
			//就地resort this.nodesUnionList, in the order of "SMALLEST to LARGEST"
		}
		
		/**
		 * If a better node can not be found, this node itself will be returned back
		 * */
		private String findBetterNodeInRareCases(int sharedPrefixLen, String keyIdStr)
		{
			int currentNumericalDistance = Math.abs(getIndex(keyIdStr) - getIndex(this.idStr));  
			//the current node's numerical distance to the object key
			
			String betterNodeIdStr = this.idStr;
			this.getNodesUnionList();                  //added by chenggang at 10:52 Nov 10,2009
			
			for(int i = 0; i < this.nodesUnionList.size(); i++)
			{
				String currentNodeIdStr = this.nodesUnionList.elementAt(i);
				if(sharedPrefixLength(currentNodeIdStr, keyIdStr) >= sharedPrefixLen)
				{
					//|T-D| <= |A-D|
					//String currentNode = this.nodesUnionList.elementAt(i);
					int numericalDistance = Math.abs(getIndex(keyIdStr) - getIndex(currentNodeIdStr));
//					if( numericalDistance < currentNumericalDistance || 
//							(numericalDistance == currentNumericalDistance && getIndex(currentNodeIdStr) <= getIndex(this.idStr) ) )
					if( numericalDistance < currentNumericalDistance ||
							(numericalDistance == currentNumericalDistance && getIndex(currentNodeIdStr) <= getIndex(betterNodeIdStr)))   
						//modified by chenggang at 10:57 of Nov 10th,2009
					{
						betterNodeIdStr = currentNodeIdStr;
						currentNumericalDistance = Math.abs(getIndex(keyIdStr) - getIndex(betterNodeIdStr));  //added by chengang
					}
				}
			}
			return betterNodeIdStr;
		}
		private Vector<String> getAvailableNodesList()
		{
			//potential materials: part 1. O :all nodes along the routing path
			//							2. all nodes in O's R,L,M tables
			//							3. the newly-inserted node "this"
			
			allAvailableNodes.clear();
			
			allAvailableNodes.addElement(this.idStr);  //part 3
			
			//add nodes information along the routing path I got from the joiningNode
			for(int i = 0; i < this.routingPathForMyself.size(); i++)   //changed at 12:01 Nov 11th, 2009
			//for(int i = 0; i < this.routingPath.size(); i++)
			{
				//PastryNode currentNodeOnPath = this.routingPath.elementAt(i);
				PastryNode currentNodeOnPath = this.routingPathForMyself.elementAt(i);
				//part 2
				Vector<String> currentNodeUnionList = currentNodeOnPath.getNodesUnionList();
				for(int j = 0; j < currentNodeUnionList.size(); j++)
				{
					String nodeStr = currentNodeUnionList.elementAt(j);
					if(nodeStr != null && !nodeStr.equals(""))
					{
						if(!allAvailableNodes.contains(nodeStr))
						{
							allAvailableNodes.addElement(nodeStr);
						}
					}
				}
				
				//part 1
				String currentNodeOnPathStr = currentNodeOnPath.getIdStr();
				if(currentNodeOnPathStr != null && !currentNodeOnPathStr.equals(""))
				{
					if(!allAvailableNodes.contains(currentNodeOnPathStr))
					{
						allAvailableNodes.addElement(currentNodeOnPathStr);
					}
				}
			}
			
			return this.allAvailableNodes;
		}
		
		public void constructRTable()
		{
			if(getProtocolID() == 0)
			{
				getAvailableNodesList();
				for(int i = 0; i < this.length; i++)
				{
					for(int j = 0; j < Math.pow(2, this.b); j++)
					{
						//each entry is chosen
						this.routingTable[i][j] = this.getBestEntryFromAvailableNodes(i, j);
					}
				}
			}
			else if(getProtocolID() == 1)
			{
				//new policy is here!
				
				
				//****************     Added at 15:58, Nov 12th,2009           *********//
				//first add "SELF" on each line
				for(int i = 0; i < this.length; i++)
				{
					//set the routing table for the newly inserted node, which is the only node in the pastry network
					this.routingTable[i][this.idStr.charAt(i) - '0'] = this.idStr;
					//set one entry as "SELF" at each line 
				}
				//****************     Added at 15:58, Nov 12th,2009           *********//
				
				//try to use the nodes (along the routing path)'s inforamtion
				Vector<PastryNode> nodesListInRoutingPath = this.getRoutingPathForMyself();
				for(int i = 0; i < nodesListInRoutingPath.size(); i++)
				{
					//makes use of each node's routing table
					PastryNode nodeI = nodesListInRoutingPath.elementAt(i);
					for(int j = 0; j < Math.pow(2, getB()); j++)
					{
						//see whether we can make use of this entry
						String entry = nodeI.routingTable[i][j];
						if(entry.startsWith(this.idStr.substring(0, i)) && entry.charAt(i) == j + '0')
						{
							this.routingTable[i][j] = entry;
						}
					}
				}
			}
		}
		
		private String getBestEntryFromAvailableNodes(int lineNum, int columnNum)
		{
			String bestEntry = null;
			//materials: this.allAvailableNodes
			//output: the best entry choice from the allAvailableNodes for this special entry r[lineNum][columnNum]
			String previousEntry = null;
			
			for(int i = 0; i < this.allAvailableNodes.size(); i++)
			{
				//previous value is null
				{
					//see if this one is a better choice
					String thisNewNodeId = this.allAvailableNodes.elementAt(i);
					if(thisNewNodeId.startsWith(this.idStr.substring(0, lineNum)) && thisNewNodeId.charAt(lineNum) == columnNum + '0')
					{
						//this is the prerequisite, and if satisfying that, it is a valid choice, but not necessarily better
						if(previousEntry == null)
						{
							//the first time I find a valid non-null entry
							previousEntry = thisNewNodeId;
							bestEntry = thisNewNodeId;              //bug, added at 23:13, Nov 10th,2009
						}
						else
						{
							if(getPhysicalDistance(this.idStr, thisNewNodeId) < getPhysicalDistance(this.idStr, previousEntry)
									|| ( getPhysicalDistance(this.idStr, thisNewNodeId) == getPhysicalDistance(this.idStr, previousEntry)
											&& getIndex(thisNewNodeId) <= getIndex(previousEntry) )
											)
							{
								//find a better one
								previousEntry = thisNewNodeId;
								bestEntry = thisNewNodeId;
							}
						}
					}
				}
			}
			return bestEntry;
		}
		
		public void constructLSet()
		{
			if(getProtocolID() == 0)
			{
				getAvailableNodesList();
				
				for(int i = 0; i < this.allAvailableNodes.size(); i++)
				{
					String currentNodeStr = this.allAvailableNodes.elementAt(i);
					if(getIndex(currentNodeStr) < getIndex(this.idStr))
					{
						if(!currentNodeStr.equals("") && !smallerNodesList.contains(currentNodeStr))
						{
							smallerNodesList.addElement(currentNodeStr);
						}
					}
					if(getIndex(currentNodeStr) > getIndex(this.idStr))
					{
						if(!currentNodeStr.equals("") && !largerNodesList.contains(currentNodeStr))
						{
							largerNodesList.addElement(currentNodeStr);
						}
					}
				}
				
				Vector<String> smallerLSet = this.chooseTopHalfNSmallerNodes();
				for(int i = 0; i < ((this.smallerLeafSet.length < smallerLSet.size()) ? this.smallerLeafSet.length : smallerLSet.size() ); i++)
				{
					this.smallerLeafSet[i] = smallerLSet.elementAt(i);
				}
				
				Vector<String> largerLSet = this.chooseTopHalfNLargerNodes();
				for(int i = 0; i < ((this.largerLeafSet.length < largerLSet.size()) ? this.largerLeafSet.length : largerLSet.size() ); i++)
				{
					this.largerLeafSet[i] = largerLSet.elementAt(i);
				}
			}
			else if(getProtocolID() == 1)
			{
				//new policy
				Vector<PastryNode> nodesListInRoutingPath = this.getRoutingPathForMyself();
				PastryNode nodeZ = nodesListInRoutingPath.elementAt(nodesListInRoutingPath.size() - 1);
				//the last node in the routing path, which is numerically nearest to the new node X
				
				Vector<String> nodesInZLeafSet = new Vector<String>();
				for(int i = 0; i < nodeZ.smallerLeafSet.length; i++)
				{
					String str = nodeZ.smallerLeafSet[i];
					if( str != null && !nodesInZLeafSet.contains(str))
					{
						nodesInZLeafSet.addElement(str);
					}
				}
				for(int i = 0; i < nodeZ.largerLeafSet.length; i++)
				{
					String str = nodeZ.largerLeafSet[i];
					if( str != null && !nodesInZLeafSet.contains(str))
					{
						nodesInZLeafSet.addElement(str);
					}
				}
				
				
				for(int i = 0; i < nodesInZLeafSet.size(); i++)
				{
					String currentNodeStr = nodesInZLeafSet.elementAt(i);
					if(getIndex(currentNodeStr) < getIndex(this.idStr))
					{
						if(!currentNodeStr.equals("") && !smallerNodesList.contains(currentNodeStr))
						{
							smallerNodesList.addElement(currentNodeStr);
						}
					}
					if(getIndex(currentNodeStr) > getIndex(this.idStr))
					{
						if(!currentNodeStr.equals("") && !largerNodesList.contains(currentNodeStr))
						{
							largerNodesList.addElement(currentNodeStr);
						}
					}
				}
				
				Vector<String> smallerLSet = this.chooseTopHalfNSmallerNodes();
				for(int i = 0; i < ((this.smallerLeafSet.length < smallerLSet.size()) ? this.smallerLeafSet.length : smallerLSet.size() ); i++)
				{
					this.smallerLeafSet[i] = smallerLSet.elementAt(i);
				}
				
				Vector<String> largerLSet = this.chooseTopHalfNLargerNodes();
				for(int i = 0; i < ((this.largerLeafSet.length < largerLSet.size()) ? this.largerLeafSet.length : largerLSet.size() ); i++)
				{
					this.largerLeafSet[i] = largerLSet.elementAt(i);
				}
			}
		}
		private Vector<String> chooseTopHalfNSmallerNodes()
		{
			//leaf set should not contain itself
			Vector<String> rstVector = new Vector<String>();
			Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
			
			orderedNodesList = sortForLeafSet(this.smallerNodesList);
			for(int k = 0; k < ( (this.smallerLeafSet.length < orderedNodesList.size()) ? this.smallerLeafSet.length : orderedNodesList.size() ); k++)
			{
				if(orderedNodesList.elementAt(k) != null)
				{
					rstVector.addElement(orderedNodesList.elementAt(k));
				}
				else
					break;
			}
			return rstVector;
		}
		
		private Vector<String> chooseTopHalfNLargerNodes()
		{
			//leaf set should not contain itself
			Vector<String> rstVector = new Vector<String>();
			Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
			
			orderedNodesList = sortForLeafSet(this.largerNodesList);
			for(int k = 0; k < ( (this.largerLeafSet.length < orderedNodesList.size()) ? this.largerLeafSet.length : orderedNodesList.size() ); k++)
			{
				if(orderedNodesList.elementAt(k) != null)
				{
					rstVector.addElement(orderedNodesList.elementAt(k));
				}
				else
					break;
			}
			return rstVector;
		}
		
		private int getNumericalDistance(String node1, String node2)
		{
			return Math.abs(getIndex(node1) - getIndex(node2));
		}
		
		private Vector<String> sortForLeafSet(Vector<String> previousNodesList)
		{
			//Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
			int size = previousNodesList.size();
			for(int i = 0; i < size - 1; i++)
			{
				for(int j = 0; j < size - 1 - i; j++)
				{
					if( (getNumericalDistance(previousNodesList.elementAt(j), this.idStr) > 
					getNumericalDistance(previousNodesList.elementAt(j + 1), this.idStr) ) ||
						( (getNumericalDistance(previousNodesList.elementAt(j), this.idStr) == 
							getNumericalDistance(previousNodesList.elementAt(j + 1), this.idStr)) && 
						getIndex(previousNodesList.elementAt(j)) > getIndex(previousNodesList.elementAt(j + 1)) ) )
					{
						//swap()
						String temp = "";
						temp = previousNodesList.elementAt(j);
						previousNodesList.setElementAt(previousNodesList.elementAt(j + 1), j);
						previousNodesList.setElementAt(temp, j + 1);
					}
				}
			}
			return previousNodesList;
			//return orderedNodesList;
		}
		
		public void constructMSet()
		{
			if(getProtocolID() == 0)
			{
				getAvailableNodesList();
				Vector<String> MSet = chooseTopMNodes();
				for(int i = 0; i < ((this.M < MSet.size()) ? this.M : MSet.size() ); i++)
				{
					this.neighborhoodSet[i] = MSet.elementAt(i);
				}
			}
			else if(getProtocolID() == 1)
			{
				//new policy is here
				Vector<PastryNode> nodesListInRoutingPath = this.getRoutingPathForMyself();
				PastryNode nodeA = nodesListInRoutingPath.elementAt(0);
				//the first node in the routing path, physically nearest to the new node X
				//copy A's M set to X
				for(int i = 0; i < nodeA.neighborhoodSet.length; i++)
				{
					this.neighborhoodSet[i] = nodeA.neighborhoodSet[i];
				}
			}
		}
		
		private Vector<String> chooseTopMNodes()
		{
			//neighborhood set should not contain itself
			Vector<String> rstVector = new Vector<String>();
			Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
			Vector<String> availableNodesExceptMyself = new Vector<String>(this.allAvailableNodes.size() - 1);
			
			for(int i = 0 ; i < this.allAvailableNodes.size(); i++)
			{
				//to remove the node itself, so that the neighborhood set and the leaf set will not contain itself
				String node = this.allAvailableNodes.elementAt(i);
				if(!node.equals(this.idStr))
				{
					//not myself
					availableNodesExceptMyself.addElement(node);
				}
			}
			
			orderedNodesList = sort(availableNodesExceptMyself, this.idStr);
			for(int k = 0; k < ( (this.M < orderedNodesList.size()) ? this.M : orderedNodesList.size() ); k++)
			{
				if(orderedNodesList.elementAt(k) != null)
				{
					rstVector.addElement(orderedNodesList.elementAt(k));
				}
				else
					break;
			}
			return rstVector;
		}
		
		
		private Vector<String> getAvailableNodesListForUpdate(PastryNode newNode)
		{
			//potential materials: part 1. Myself
			//							2. all nodes in my own R,L,M tables
			//							3. the newly-inserted node
			//                          4. the newly-inserted node's R,L,M tables
			
			allAvailableNodesForUpdate.clear();
			
			allAvailableNodesForUpdate.addElement(this.idStr);  //part 1
			
			//part 2
			Vector<String> currentNodeUnionList = this.getNodesUnionList();
			for(int j = 0; j < currentNodeUnionList.size(); j++)
			{
				String nodeStr = currentNodeUnionList.elementAt(j);
				if(nodeStr != null && !nodeStr.equals(""))
				{
					if(!allAvailableNodesForUpdate.contains(nodeStr))
					{
						allAvailableNodesForUpdate.addElement(nodeStr);
					}
				}
			}
			if(!allAvailableNodesForUpdate.contains(newNode.getIdStr()))
				allAvailableNodesForUpdate.addElement(newNode.getIdStr());  //part 3
			
			//part 4
			Vector<String> newNodeSUnionList = newNode.getNodesUnionList();
			for(int j = 0; j < newNodeSUnionList.size(); j++)
			{
				String nodeStr = newNodeSUnionList.elementAt(j);
				if(nodeStr != null && !nodeStr.equals(""))
				{
					if(!allAvailableNodesForUpdate.contains(nodeStr))
					{
						allAvailableNodesForUpdate.addElement(nodeStr);
					}
				}
			}
			
			return this.allAvailableNodesForUpdate;
		}
		
		public void updateRTable(PastryNode newNode)
		{
			getAvailableNodesListForUpdate(newNode);
			for(int i = 0; i < this.length; i++)
			{
				for(int j = 0; j < Math.pow(2, this.b); j++)
				{
					//each entry is chosen
					this.routingTable[i][j] = this.getBestEntryFromAvailableNodesForUpdate(i, j);
				}
			}
		}
		
		private String getBestEntryFromAvailableNodesForUpdate(int lineNum, int columnNum)
		{
			String bestEntry = null;
			//materials: this.allAvailableNodesForUpdate
			//output: the best entry choice from the allAvailableNodesForUpdate for this special entry r[lineNum][columnNum]
			String previousEntry = null;
			
			for(int i = 0; i < this.allAvailableNodesForUpdate.size(); i++)
			{
				//previous value is null
				{
					//see if this one is a better choice
					String thisNewNodeId = this.allAvailableNodesForUpdate.elementAt(i);
					if(thisNewNodeId.startsWith(this.idStr.substring(0, lineNum)) && thisNewNodeId.charAt(lineNum) == columnNum + '0')
					{
						//this is the prerequisite, and if satisfying that, it is a valid choice, but not necessarily better
						if(previousEntry == null)
						{
							//the first time I find a valid non-null entry
							previousEntry = thisNewNodeId;
							bestEntry = thisNewNodeId;
						}
						else
						{
							if(getPhysicalDistance(this.idStr, thisNewNodeId) < getPhysicalDistance(this.idStr, previousEntry)
									|| ( getPhysicalDistance(this.idStr, thisNewNodeId) == getPhysicalDistance(this.idStr, previousEntry)
											&& getIndex(thisNewNodeId) <= getIndex(previousEntry) )
											)
							{
								//find a better one
								previousEntry = thisNewNodeId;
								bestEntry = thisNewNodeId;
							}
						}
					}
				}
			}
			return bestEntry;
		}
		
		public void updateLSet(PastryNode newNode)
		{
			getAvailableNodesListForUpdate(newNode);
			
			for(int i = 0; i < this.allAvailableNodesForUpdate.size(); i++)
			{
				String currentNodeStr = this.allAvailableNodesForUpdate.elementAt(i);
				if(getIndex(currentNodeStr) < getIndex(this.idStr))
				{
					if(!currentNodeStr.equals("") && !smallerNodesList.contains(currentNodeStr))
					{
						smallerNodesList.addElement(currentNodeStr);
					}
				}
				if(getIndex(currentNodeStr) > getIndex(this.idStr))
				{
					if(!currentNodeStr.equals("") && !largerNodesList.contains(currentNodeStr))
					{
						largerNodesList.addElement(currentNodeStr);
					}
				}
			}
			
			Vector<String> smallerLSet = this.chooseTopHalfNSmallerNodes();
			for(int i = 0; i < ((this.smallerLeafSet.length < smallerLSet.size()) ? this.smallerLeafSet.length : smallerLSet.size() ); i++)
			{
				this.smallerLeafSet[i] = smallerLSet.elementAt(i);
			}
			
			Vector<String> largerLSet = this.chooseTopHalfNLargerNodes();
			for(int i = 0; i < ((this.largerLeafSet.length < largerLSet.size()) ? this.largerLeafSet.length : largerLSet.size() ); i++)
			{
				this.largerLeafSet[i] = largerLSet.elementAt(i);
			}
		}
		
		public void updateMSet(PastryNode newNode)
		{
			getAvailableNodesListForUpdate(newNode);
			
			Vector<String> MSet = chooseTopMNodesForUpdate();
			for(int i = 0; i < ((this.M < MSet.size()) ? this.M : MSet.size() ); i++)
			{
				this.neighborhoodSet[i] = MSet.elementAt(i);
			}
		}
		
		private Vector<String> chooseTopMNodesForUpdate()
		{
			//neighborhood set should not contain itself
			Vector<String> rstVector = new Vector<String>();
			Vector<String> orderedNodesList = new Vector<String>();    //the sorted nodes list
			Vector<String> availableNodesExceptMyself = new Vector<String>(this.allAvailableNodesForUpdate.size() - 1);
			
			for(int i = 0 ; i < this.allAvailableNodesForUpdate.size(); i++)
			{
				//to remove the node itself, so that the neighborhood set and the leaf set will not contain itself
				String node = this.allAvailableNodesForUpdate.elementAt(i);
				if(!node.equals(this.idStr))
				{
					//not myself
					availableNodesExceptMyself.addElement(node);
				}
			}
			
			orderedNodesList = sort(availableNodesExceptMyself, this.idStr);
			for(int k = 0; k < ( (this.M < orderedNodesList.size()) ? this.M : orderedNodesList.size() ); k++)
			{
				if(orderedNodesList.elementAt(k) != null)
				{
					rstVector.addElement(orderedNodesList.elementAt(k));
				}
				else
					break;
			}
			return rstVector;
		}
		
		public void/*Vector<PastryNode>*/ route(String keyIdStr, Vector<PastryNode> resultRoutingPath)
		{
			//this.routingPath.addElement(this);   //no matter what, should insert this PastryNode into the routing path
			//changed at 12:56, Nov 11th, 2009  found a big bug!
			resultRoutingPath.addElement(this);
			
			//*******   Actually I am looking for myself! Added at 19:26, Nov 11th, 2009  ****//
			if(keyIdStr.equals(this.getIdStr()))
			{
				return;
			}
			//*******   Actually I am looking for myself! Added at 19:26, Nov 11th, 2009  ****//
			
			if(inLeafSet(keyIdStr))
			{
				//the object key is within range of the leaf set
				//leaf set is certainly not null
				//find the numerically nearest node to the keyId
				int smallestDistance = 1000000;
				String nearestLeaf = "3333";    //the nearest leaf node
				
				Vector<String> compressedLeafSet = new Vector<String>(this.L);
				for(int i = 0; i < this.smallerLeafSet.length; i++)
				{
					String node = this.smallerLeafSet[i];
					if(node != null && !compressedLeafSet.contains(node))
					{
						compressedLeafSet.addElement(node);
					}
				}
				for(int i = 0; i < this.largerLeafSet.length; i++)
				{
					String node = this.largerLeafSet[i];
					if(node != null && !compressedLeafSet.contains(node))
					{
						compressedLeafSet.addElement(node);
					}
				}
				
				//*****************    Commented at 23:54, Nov 10th,2009   *******************//
//				for(int i = 0; i < this.leafSet.length; i++)
//				{
//					//go through the leaf set and find the numerically nearest leaf 
//					String currentLeaf = this.leafSet[i];
//					if( currentLeaf != null)
//					{
//						//not null
//						int currentDistance = Math.abs(getIndex(keyIdStr) - getIndex(currentLeaf));
//						//the current leaf's distance to the object key
//						
//						if(currentDistance < smallestDistance || 
//								(currentDistance == smallestDistance && getIndex(currentLeaf) <= getIndex(nearestLeaf) ) )
//						{
//							smallestDistance = currentDistance;
//							nearestLeaf = currentLeaf;
//						}
//					}
//					else
//						break;
//				}
//				//after the for loop, has found the nearest leaf node
				//*****************    Commented at 23:54, Nov 10th,2009   *******************//
				
				for(int i = 0; i < compressedLeafSet.size(); i++)
				{
					//go through the leaf set and find the numerically nearest leaf 
					String currentLeaf = compressedLeafSet.elementAt(i);
					if( currentLeaf != null)
					{
						//not null
						int currentDistance = Math.abs(getIndex(keyIdStr) - getIndex(currentLeaf));
						//the current leaf's distance to the object key
						
						if(currentDistance < smallestDistance || 
								(currentDistance == smallestDistance && getIndex(currentLeaf) <= getIndex(nearestLeaf) ) )
						{
							smallestDistance = currentDistance;
							nearestLeaf = currentLeaf;
						}
					}
					else
						break;
				}
				//after the for loop, has found the nearest leaf node
				
				int nearestLeafId = getIndex(nearestLeaf);
				PastryNode nearestNode = nodeList.elementAt(nearestLeafId);
				
//				if(this.routingPath.size() >= 1)
//				{
//					PastryNode lastNodeInThePath = this.routingPath.elementAt(this.routingPath.size()-1);
//					if(!nearestLeaf.equals(lastNodeInThePath.getIdStr()))
//					{
//						//to avoid repeated node in the routing path
//						this.routingPath.addElement(nearestNode);
//					}
//				}
//				else
//				{
//					//routingPath is null, you can directly insert the leaf node
//					this.routingPath.addElement(nearestNode);
//				}
				
				
				//***************  Added at 15:46, Nov 12th, 2009 to correct the bug that "myself is actually the nearest"  ****************//
				int myNumericalDistance = Math.abs(getIndex(keyIdStr) - getIndex(this.idStr));
				int numericalDistanceOfNearestLeaf = Math.abs(getIndex(keyIdStr) - getIndex(nearestLeaf));
				if(myNumericalDistance < numericalDistanceOfNearestLeaf || 
						(myNumericalDistance == numericalDistanceOfNearestLeaf && getIndex(this.idStr) < getIndex(nearestLeaf)))
				{
					//I myself is the result
					return;
				}
				//***************  Added at 15:46, Nov 12th, 2009 to correct the bug that "myself is actually the nearest"  ****************//
				else
				{
					if(resultRoutingPath.size() >= 1)
					{
						PastryNode lastNodeInThePath = resultRoutingPath.elementAt(resultRoutingPath.size()-1);
						if(!nearestLeaf.equals(lastNodeInThePath.getIdStr()))
						{
							//to avoid repeated node in the routing path
							resultRoutingPath.addElement(nearestNode);
						}
					}
					else
					{
						//routingPath is null, you can directly insert the leaf node
						resultRoutingPath.addElement(nearestNode);
					}
					
					return;             //the route stops here!
					//routingPath.addElement();
				}
			}
			else
			{
				//use the routing table
				int sharedPrefixLen = sharedPrefixLength(this.idStr, keyIdStr);
				
				//*********************  Added at 10:43, Nov 11th,2009 ************************//
				if(sharedPrefixLen == this.length)
				{
					//the keyIdStr is the same as the current routing node, actually I am looking for myself
					//can stop now
					return;
				}
				//*********************  Added at 10:43, Nov 11th,2009 ************************//
				
				String nextNode = this.routingTable[sharedPrefixLen][keyIdStr.charAt(sharedPrefixLen) - '0'];
				if(nextNode != null)
				{
					//*******   Added to avoid deadlock   ********//
					if(nextNode.equals(this.getIdStr()))
					{
						//deadlock will happen, time to stop
						return;
					}
					//*******   Added to avoid deadlock   ********//
				
					//forward to the entry in the routing table
					PastryNode nextPastryNode = nodeList.elementAt(getIndex(nextNode));
					nextPastryNode.route(keyIdStr, resultRoutingPath);
				}
				else
				{
					//rare case
					this.getNodesUnionList();
					String betterNode = this.findBetterNodeInRareCases(sharedPrefixLen, keyIdStr);
				
					//*******   Added to avoid deadlock   ********//
					if(betterNode.equals(this.getIdStr()))
					{
						//can not find a better node, time to stop
						//deadlock will happen, time to stop
						return;
					}
					//*******   Added to avoid deadlock   ********//
					
					PastryNode nextPastryNode = nodeList.elementAt(getIndex(betterNode));
				
					nextPastryNode.route(keyIdStr, resultRoutingPath);
				}
			}
		}
		
		private int sharedPrefixLength(String currentNodeIdStr, String keyIdStr)
		{
			int sharedLen = 0;
			int len1 = currentNodeIdStr.length();
			int len2 = keyIdStr.length();
			if(len1 == len2)
			{
				for(int i = 0; i < len1; i++)
				{
					if(currentNodeIdStr.charAt(i) == keyIdStr.charAt(i))
					{
						sharedLen++;
					}
					else
					{
						break;
						//whenever faced with the first different digit, just return
					}
				}
			}
			return sharedLen;
		}
	}
	
	public static void main1(String args[])
	{
		int numOfTests = 60;//1000;
		
		//Pastry pastry = new Pastry(2, 4, 4, 4);
		
		for(int numOfNodes = 256; numOfNodes <= 256 ; numOfNodes = numOfNodes * 2)
		{
			Pastry pastry = new Pastry(2, 4, 4, 4);
			//each test
			//1. construct the Pastry network by inserting numOfNodes randomly chosen nodes
			Vector<Integer> nodesHasBeenInserted = new Vector<Integer>();
			//to record the nodes which has been inserted into the Pastry network to avoid repetition.			
			for(int i = 0; i < numOfNodes; i++)
			{
				//each time insert a random different node
				int randomNodeIdIndex = (int)(256 * Math.random()); //[0, 1)
				while(nodesHasBeenInserted.contains(randomNodeIdIndex))
				{
					randomNodeIdIndex = (int)(256 * Math.random());
				}
				
				//a new Node Index now
				String randomNodeIdStr = pastry.getStrFromIndex(randomNodeIdIndex);
				pastry.insertNode(randomNodeIdStr);
			}
			
			//2. For each constructed Pastry network, run numOfTests routing test and record the # of hops
			int[] numOfHops = new int[numOfTests];  //used to store the # of hops of each test
			Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
			
			int [] times = new int[10];
			
			int numOfLiveNodes = liveNodesList.size();
			for(int i = 0; i < numOfTests; i++ )
			{
				//each time randomly choose 2 nodes from the Pastry Network
				int randomNodeIdIndex1 = (int)(numOfLiveNodes * Math.random()); //[0, 1)
				int randomNodeIdIndex2 = (int)(numOfLiveNodes * Math.random()); //[0, 1)
				
				while(randomNodeIdIndex2 == randomNodeIdIndex1)
				{
					randomNodeIdIndex2 = (int)(numOfLiveNodes * Math.random());
				}
				
				//now we have 2 different live nodes ID in the Pastry network
				//String randomNodeIdStr1 = pastry.getStrFromIndex(randomNodeIdIndex1);
				String randomNodeIdStr2 = pastry.getStrFromIndex(randomNodeIdIndex2);
				
				PastryNode startingNode = liveNodesList.elementAt(randomNodeIdIndex1);
				Vector<PastryNode> resultRoutingPath = new Vector<PastryNode>();
				startingNode.route(randomNodeIdStr2, resultRoutingPath);
				numOfHops[i] = resultRoutingPath.size();
				times[numOfHops[i]]++;
				//System.out.println("current numOfHops is : " + numOfHops[i]);
			}
			for(int m = 0; m < 10; m++)
			{
				System.out.println( "Hops num: " + m + ", " + times[m] +" times");
			}
			//System.out.println( "numOfNodes = "+ numOfNodes + "; averaged num of hops: =" + sumHops/numOfTests);
			//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
		}
	}
	
	public static void main2(String args[])
	{
		Pastry pastry = new Pastry(2, 4, 4, 4);
		PastryNode node = pastry.new PastryNode("2312", 4, 4, 2, 4);
//		if(node.leafSetIsNull())
//		{
//			System.out.println("Leaf set is null");
//		}
//		else
//			System.out.println("Leaf set is not null");
		
		node.leafSet[0] = "2022";
		//node.leafSet[1] = "2132";
		if(node.inLeafSet("2122"))
		{
			System.out.println("In leaf set...");
		}
		else
			System.out.println("Not in leaf set...");
		Vector<String> testStrList = new Vector<String>(3);
		testStrList.add("new 1");
		testStrList.add("new 2");
		testStrList.addElement("new 3");
		for(int i = 0; i < testStrList.size(); i++)
		{
			System.out.println("testStrList [" + i + "] = " + testStrList.elementAt(i));
		}
		
		String testStrMatrix[][] = new String [3][3];
		for(int i = 0; i < 3; i++)
		{
			for(int j = 0; j < 3; j++)
				System.out.print(testStrMatrix[i][j]+"  ");
			System.out.println();
		}
		
		node.leafSet[1] = "2132";
		node.leafSet[2] = "1111";
		node.neighborhoodSet[0] = "1111";
		node.neighborhoodSet[1] = "2132";
		node.neighborhoodSet[2] = "1033";
		node.routingTable[1][3] = "3333";
		node.routingTable[3][0] = "2222";
		node.routingTable[0][2] = "2000";
		node.getNodesUnionList();
		for(int i = 0; i < node.nodesUnionList.size(); i++)
		{
			System.out.println(node.nodesUnionList.elementAt(i));
		}
	}
}
