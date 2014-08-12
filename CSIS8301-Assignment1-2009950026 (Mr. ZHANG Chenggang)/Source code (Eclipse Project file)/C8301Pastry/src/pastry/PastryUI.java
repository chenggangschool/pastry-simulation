package pastry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

import pastry.Pastry.PastryNode;

public class PastryUI 
{
	private int workingMode;
	private static final int USER_MODE = 0;
	private static final int EXPERIMENT_MODE = 1;
	private static final int numOfTests = 100;
	
	private static final int b = 2;
	private static final int LENGTH = 4;
	
	private Pastry pastryForUserModeWork;
	
	private int defaultLForExperimentMode = 4;
	private int defaultMForExperimentMode = 4;
	
	
	public PastryUI()
	{
		workingMode = 0;
		pastryForUserModeWork = new Pastry(2, 4, 4, 4);
	}
	
	public int getWorkingMode()
	{
		return this.workingMode;
	}
	
	public void setWorkingMode(int workingMode)
	{
		this.workingMode = workingMode;
	}
	
	public void displayWelcome()
	{
		System.out.println("|--------------------------------------------------------------------------|");
		System.out.println("|                        Welcome to Pastry System                          |");
		System.out.println("|       Assignment 1 for CSIS8301 Advanced Topics in Computer Systems      |");
		System.out.println("|     Developed by ZHANG Chenggang (U no.: 2009950026), CS Department, HKU |");
		System.out.println("|--------------------------------------------------------------------------|");
		System.out.println("");
	}
	
	public String getLine()
	{
		try
		{
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			//System.out.println("Here!");
			return br.readLine();
		}
		catch(Exception e)
		{
			System.out.println( "Error happens: "+ e.getMessage());
			return "ERROR";
		}
//		finally
//		{
//			
//		}
	}
	
	public void chooseWorkingMode()
	{
		while(true)
		{
			System.out.println("Please choose the working mode: 0 for USER mode, 1 for EXPERIMENT mode.");
			String line = this.getLine();
			if(line.equals("ERROR"))
			{
				System.out.println("Some errors have occured, and system is exiting...");
				System.exit(0);
			}
			else
			{
				char firstChar = line.charAt(0);
				if(line.length() > 1 || ( firstChar != '0' && firstChar != '1' ) )
				{
					System.out.println("INVALID INPUT!");
				}
				else
				{
					this.setWorkingMode(firstChar - '0');
					return;
				}
			}
		}
	}
	
	public void beginWork()
	{
		if(this.workingMode == USER_MODE)
		{
			beginUserModeWork();
		}
		else if(this.workingMode == EXPERIMENT_MODE)
		{
			beginExperimentModeWork();
		}
		else
		{
			System.out.println("Some wrong working mode, system will exit......");
			System.exit(0);
		}
	}
	
	public int getL()
	{
		String line = "";
		while(true)
		{
			System.out.println("Please input the size of the leaf set, |L| (needs to be an even number like 2,4,6...) :");
			line = this.getLine();
			for(int i = 0; i < line.length(); i++)
			{
				char eachChar = line.charAt(i);
				if(!Character.isDigit(eachChar))
				{
					System.out.println("INVALID INPUT!");
					break;
				}
				if(i == line.length() - 1)
				{
					//the last digit
					if( (eachChar - '0') % 2 == 1 )
					{
						//the number is an odd number
						System.out.println("|L| should be an even number, you just input an odd number!");
						break;
					}
					else
					{
						//this is an even number
						if(Integer.parseInt(line) == 0)
						{
							System.out.println("|L| should be positive!");
							break;
						}
						return Integer.parseInt(line);
					}
				}
			}
		}
	}
	
	public int getM()
	{
		String line = "";
		while(true)
		{
			System.out.println("Please input the size of the neighborhood set, |M|:");
			line = this.getLine();
			for(int i = 0; i < line.length(); i++)
			{
				char eachChar = line.charAt(i);
				if(!Character.isDigit(eachChar))
				{
					System.out.println("INVALID INPUT!");
					break;
				}
				if(i == line.length() - 1)
				{
					//the last digit
					{
						if(Integer.parseInt(line) == 0)
						{
							System.out.println("|M| should be positive!");
							break;
						}
						return Integer.parseInt(line);
					}
				}
			}
		}
	}
	public boolean insertNodes()
	{
		boolean hasInputedSomeNodes = false;
		while(true)
		{
			System.out.println("Please enter the NodeIDs you want to insert, each nodeId on one line and ending with -1!");
			System.out.println("For Example:");
			System.out.println("1102");
			System.out.println("2131");
			System.out.println("3100");
			System.out.println("-1");
			
			String currentNodeId = "";
			currentNodeId = this.getLine();
			while(!currentNodeId.equals("-1"))
			{
				if(!this.pastryForUserModeWork.isValidNodeId(currentNodeId))
				{
					System.out.println("The node ID is INVALID! Please re-enter:");
				}
				else
				{
					boolean insertSuccessful = this.pastryForUserModeWork.insertNode(currentNodeId);
					if(insertSuccessful)
					{
						hasInputedSomeNodes = true;
						System.out.println("Node " + "\"" + currentNodeId + "\" has been successfully inserted!" );
						System.out.println("Totally   " + this.pastryForUserModeWork.getLiveNodesNum() + "   nodes are in the Pastry network,");
					}
					else
					{
						System.out.println("Inserting failure!");
						System.out.println("Node " + "\"" + currentNodeId + "\" has already been in the network!" );
					}
					System.out.println("Please enter the next node ID or \"-1\" to end:");
				}
				currentNodeId = this.getLine();
			}
			return hasInputedSomeNodes;
		}
	}
	
	public void listAllLiveNodes()
	{	
		System.out.println("********    ALL the nodes    ********");
		for(int i = 0; i < this.pastryForUserModeWork.getNodeList().size(); i++)
		{
			if(this.pastryForUserModeWork.getNodeList().elementAt(i).getLiveOrNot())
				System.out.println("  " + (this.pastryForUserModeWork.getNodeList().elementAt(i)).getIdStr());
		}
		System.out.println("********    ALL the nodes    ********");
	}
	
	public void query()
	{
		while(true)
		{
			System.out.println();
			System.out.println();
			System.out.println("|------------------------------------------------------------------------|");
			System.out.println("|Please choose the operations as follows:                                |");
			System.out.println("|   l: List ALL the nodes                                 e.g.  l        |");
			System.out.println("|   c: Check the Routing table and L,M set of a node      e.g.  c3333    |");
			System.out.println("|   r: Route a message starting from some node            e.g.  r2312    |");
			//System.out.println("******  i: Insert some new nodes      e.g.   i2313i1231i3123");
			System.out.println("|   q: Quit                                               e.g.  q        |");
			System.out.println("|------------------------------------------------------------------------|");
			String command = this.getLine();
			if(command.equals("q"))
			{
				System.out.println("Pastry exited, ByeBye!");
				System.exit(0);
			}
			else if(command.equals("l"))
			{
				System.out.println();
				this.listAllLiveNodes();
			}
			else if(command.startsWith("c"))
			{
				if(command.length() != 5)
				{
					System.out.println("INVALID INPUT!");
				}
				else
				{
					String nodeIdStr = command.substring(1, 5);
					
					if(!this.pastryForUserModeWork.isValidNodeId(nodeIdStr))
					{
						//not a valid node ID
						System.out.println("\"" + nodeIdStr + "\" --> INVALID node ID!");
					}
					else
					{
						//valid
						if(this.pastryForUserModeWork.nodeIsLive(nodeIdStr))
						{
							//live node
							PastryNode queriedNode = this.pastryForUserModeWork.getNodeList().elementAt(this.pastryForUserModeWork.getIndex(nodeIdStr));
							
							System.out.println("|*****************   Node  \""+ nodeIdStr +"\"'s state infromation  *************");
							queriedNode.printRoutingTable();
							queriedNode.printLeafSet();
							queriedNode.printNeighborSet();
							System.out.println("|*****************   Node  \""+ nodeIdStr +"\"'s state infromation  *************");
							
							//queriedNode.printRoutingPathWhenInserted();
							//queriedNode.printNodesUnionList();
						}
						else
						{
							//valid but not live node
							System.out.println("Node \""+ nodeIdStr +"\" is not in the Pastry network!");
							this.listAllLiveNodes();
						}
					}
				}
			}
			else if(command.startsWith("r"))
			{
				if(command.length() != 5)
				{
					System.out.println("INVALID INPUT!");
				}
				else
				{
					String startingNodeIdStr = command.substring(1, 5);
					if(!this.pastryForUserModeWork.isValidNodeId(startingNodeIdStr))
					{
						//not a valid node ID
						System.out.println("INVALID node ID!");
					}
					else
					{
						//valid
						if(this.pastryForUserModeWork.nodeIsLive(startingNodeIdStr))
						{
							//live node
							PastryNode startingNode = this.pastryForUserModeWork.getNodeList().elementAt(this.pastryForUserModeWork.getIndex(startingNodeIdStr));
							
							System.out.println("Input key:");
							String key = this.getLine();
							
							Vector<PastryNode> routingPathAfterInserted = new Vector<PastryNode>();
							startingNode.route(key, routingPathAfterInserted);
					
							if(routingPathAfterInserted.size() == 0)
							{
								System.out.println("Routing path is null!");
							}
							else
							{
								System.out.println("Routing path is as follows:");
								for(int i = 0; i < routingPathAfterInserted.size() - 1; i++)
								{
									System.out.print(routingPathAfterInserted.elementAt(i).getIdStr() + " --> ");
								}
								System.out.println(routingPathAfterInserted.elementAt(routingPathAfterInserted.size() - 1).getIdStr());
							}
						}
						else
						{
							//valid but not live node
							System.out.println("Node \""+ startingNodeIdStr +"\" is not in the Pastry network!");
							this.listAllLiveNodes();
						}
					}
				}
			}
		}
	}
	
	public void chooseProtocolId()
	{
		System.out.println();
		while(true)
		{
			System.out.println("Currently there are 2 protocols implemented, please choose one to work with:");
			System.out.println("   0: Improved Pastry Protocol (Highly Recommended!)");
			System.out.println("   1: Original Pastry Protocol");
			String line = this.getLine();
			if(line.equals("0") || line.equals("1"))
			{
				this.pastryForUserModeWork.setProtocolID(Integer.parseInt(line));
				break;
			}
			else
			{
				System.out.println("INVALID INPUT!");
			}
		}
	}
	
	public void beginUserModeWork()
	{
		//Pastry pastryForUserModeWork;
		System.out.println("For USER mode, the default setting is: base = 4, an ID has 4 quaternary digits");
		
		int L = this.getL();
		int M = this.getM();
		this.pastryForUserModeWork = new Pastry(b, LENGTH, L, M);
		
		boolean hasInsertSomeNodes = this.insertNodes();
		
		while(!hasInsertSomeNodes)
		{
			hasInsertSomeNodes = this.insertNodes();
		}
		
		System.out.println("Finished inserting nodes!");
		
		this.query();
		
		//System.out.println("L = " + L + "; M = " + M);
	}
	
	public int chooseExperimentID()
	{
		System.out.println();
		System.out.println();
		System.out.println("|------------------------------------------------------------------------|");
		System.out.println("|Please choose the experiment ID as follows:                             |");
		System.out.println("|   0: Check the averaged routing hops for different # of nodes          |");
		System.out.println("|   1: See the \"# of hops\" distribution for some given # of nodes        |");
		System.out.println("|   2: See the Relative Distance of routing for different # of nodes     |");
		System.out.println("|   3: Check the averaged routing hops for different |L|                 |");
		System.out.println("|   4: See the Relative Distance of routing for different |M|            |");
		System.out.println("|   5: See the Correct Percentage of routing for different # of nodes    |");
		System.out.println("|   6: Quit                                                              |");
		System.out.println("|------------------------------------------------------------------------|");
	    String line = this.getLine();
	    int choiceId = Integer.parseInt(line);
		return choiceId;
	}
	public void doExperiment(int experimentId)
	{
		if(experimentId == 0)
		{
			experiment0();
		}
	    else if(experimentId == 1)
		{
			experiment1();
		}
		else if(experimentId == 2)
		{
			experiment2();
		}
		else if(experimentId == 3)
		{
			experiment3();
		}
		else if(experimentId == 4)
		{
			experiment4();
		}
		else if(experimentId == 5)
		{
			experiment5();
		}
		else if(experimentId == 6)
		{
			System.exit(0);
		}
	}
	/*
	 *  System.out.println("|------------------------------------------------------------------------|");
		System.out.println("|Please choose the experiment ID as follows:                             |");
		System.out.println("|   0: Check the averaged routing hops for different # of nodes          |");
		System.out.println("|   1: See the \"# of hops\" distribution for some given # of nodes      |");
		System.out.println("|   2: See the Relative Distance of routing for different # of nodes     |");
		System.out.println("|   3: Check the averaged routing hops for different |L|                 |");
		System.out.println("|   4: See the Relative Distance of routing for different |M|            |");
		System.out.println("|   5: See the Correct Percentage of routing for different # of nodes    |");
		System.out.println("|   6: Quit                                                              |");
		System.out.println("|------------------------------------------------------------------------|");
	 * */
	public void experiment0()
	{
		System.out.println("Checking the averaged routing hops for different # of nodes......");
		System.out.println("****************************************************");
		
		//int numOfTests = 60;//1000;
		
		for(int numOfNodes = 2; numOfNodes <= 256 ; numOfNodes = numOfNodes * 2)
		{
			Pastry pastry = new Pastry(2, 4, this.defaultLForExperimentMode, this.defaultMForExperimentMode);
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
			Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
			int sumHops = 0;
			
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
				sumHops += ( resultRoutingPath.size() - 1);
			}
			System.out.println( "numOfNodes = "+ numOfNodes + "; Averaged num of routing hops: = " + (double)sumHops / (double)numOfTests);
			//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
		}
		System.out.println("****************************************************");
	}
	public void experiment1()
	{
		System.out.println("Checking the \"# of hops\" distribution for some given # of nodes......");
		System.out.println();
		System.out.println("Please input the # of nodes:");
		String line = this.getLine();
		int numOfNodes = Integer.parseInt(line);
		
		Pastry pastry = new Pastry(2, 4, this.defaultLForExperimentMode, this.defaultMForExperimentMode);

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
		
		//2. For the constructed Pastry network, run numOfTests routing test and record the # of hops
		int[] numOfHops = new int[numOfTests];  //used to store the # of hops of each test
		Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
		
		int [] times = new int[20];
		
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
			numOfHops[i] = resultRoutingPath.size() - 1;
			times[numOfHops[i]]++;
		}
		
		for(int m = 0; m < 7; m++)
		{
			System.out.println( "Hops num: " + m + "£º  " + times[m] +" times");
		}
		//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
	}
	
	public void experiment2()
	{
		System.out.println("Checking the Relative Distance of routing for different # of nodes......");
		System.out.println("****************************************************");
		
		for(int numOfNodes = 2; numOfNodes <= 256; numOfNodes = numOfNodes * 2)
		{
			Pastry pastry = new Pastry(2, 4, this.defaultLForExperimentMode, this.defaultMForExperimentMode);
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
			Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
			
			double relativeDistanceSum = 0;
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
				
				double currentRelativeDistance = 0;
				int size = resultRoutingPath.size();
				PastryNode firstNodeInRoutingPath = resultRoutingPath.elementAt(0);
				PastryNode lastNodeInRoutingPath = resultRoutingPath.elementAt(size - 1);
				int optimalDistance = this.pastryForUserModeWork.getPhysicalDistance(firstNodeInRoutingPath.getIdStr(), lastNodeInRoutingPath.getIdStr());
				int actuallDistance = 0;
				for(int p = 0; p < size - 1; p++)
				{
					actuallDistance += this.pastryForUserModeWork.getPhysicalDistance(resultRoutingPath.elementAt(p).getIdStr(), resultRoutingPath.elementAt(p + 1).getIdStr());
				}
				if(optimalDistance == 0)
				{
					currentRelativeDistance = 1;
				}
				else
				{
					currentRelativeDistance = actuallDistance / optimalDistance;
				}
				relativeDistanceSum += currentRelativeDistance;
			}
			System.out.println( "numOfNodes = "+ numOfNodes + "; Averaged Relative Distance: = " + relativeDistanceSum / (double)numOfTests);
			//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
		}
		System.out.println("****************************************************");
	}
	
	public void experiment3()
	{
		//System.out.println("|   3: Check the averaged routing hops for different |L|                 |");
		
		System.out.println("Checking the averaged routing hops for different |L| ......");
		System.out.println("****************************************************");
		
		int numOfNodes = 256;
		
		for(int l = 2; l <= 16 ; l = l * 2)
		{
			Pastry pastry = new Pastry(2, 4, l, this.defaultMForExperimentMode);
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
			Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
			int sumHops = 0;
			
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
				sumHops += ( resultRoutingPath.size() - 1);
			}
			System.out.println( "Number of nodes = " + numOfNodes + ", |L| = "+ l + ";  Averaged num of routing hops: = " + (double)sumHops / (double)numOfTests);
			//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
		}
		System.out.println("****************************************************");
	}
	
	public void experiment4()
	{
		//System.out.println("|   4: See the Relative Distance of routing for different |M|            |");
		System.out.println("Checking the Relative Distance of routing for different |M|......");
		System.out.println("****************************************************");
		
		int numOfNodes = 128;
		
		for(int m = 0; m <= 16; m++)
		{
			Pastry pastry = new Pastry(2, 4, this.defaultLForExperimentMode, m);
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
			Vector<PastryNode> liveNodesList = pastry.getLiveNodesList();
			
			double relativeDistanceSum = 0;
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
				
				double currentRelativeDistance = 0;
				int size = resultRoutingPath.size();
				PastryNode firstNodeInRoutingPath = resultRoutingPath.elementAt(0);
				PastryNode lastNodeInRoutingPath = resultRoutingPath.elementAt(size - 1);
				int optimalDistance = this.pastryForUserModeWork.getPhysicalDistance(firstNodeInRoutingPath.getIdStr(), lastNodeInRoutingPath.getIdStr());
				int actuallDistance = 0;
				for(int p = 0; p < size - 1; p++)
				{
					actuallDistance += this.pastryForUserModeWork.getPhysicalDistance(resultRoutingPath.elementAt(p).getIdStr(), resultRoutingPath.elementAt(p + 1).getIdStr());
				}
				if(optimalDistance == 0)
				{
					currentRelativeDistance = 1;
				}
				else
				{
					currentRelativeDistance = actuallDistance / optimalDistance;
				}
				relativeDistanceSum += currentRelativeDistance;
			}
			System.out.println( "numOfNodes = "+ numOfNodes + ", |M| = " + m + ";   Averaged Relative Distance: = " + relativeDistanceSum / (double)numOfTests);
			//we can get the averaged # of hops and the hops' distribution graph using numOfHops array
		}
		System.out.println("****************************************************");
		
	}
	public void experiment5()
	{
		//System.out.println("|   5: See the Correct Percentage of routing for different # of nodes    |");
	}
	
	public void beginExperimentModeWork()
	{
		System.out.println("The EXPERIMENT mode is designed to test Pastry Protocol's performance on various aspects.");
		System.out.println("The default setting is: base = 4, an ID has 4 quaternary digits.");
		System.out.println();
		int L = this.getL();
		int M = this.getM();
		
		this.defaultLForExperimentMode = L;
		this.defaultMForExperimentMode = M;
		
		while(true)
		{
			int experimentId = chooseExperimentID();
			this.doExperiment(experimentId);
		}
	}
	
	public static void main(String args[])
	{
		PastryUI ui = new PastryUI();
		ui.displayWelcome();
		ui.chooseWorkingMode();
		ui.chooseProtocolId();
		ui.beginWork();
	}
}
