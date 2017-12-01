/*
 * Created on Jun 21, 2003
 */
package innards.math.graph;

import java.util.*;

import innards.iLaunchable;

/**
 * An implementation of the Djikstra algorithm for solving the single-source shortest paths problem.
 * on a directed, weighted graph.  See CLR section 25.2 for massive detail on the algorithm, which 
 * has the dual virtues of being relatively straightforward and provably correct.
 * 
 * Details on the graph representation used in this version of the algorithm may be found in the
 * documentation for the <code>computePaths()</code> method.
 * 
 * @author derek
 */
public class DjikstraPathSolver implements iLaunchable
{
	private ArrayList solvedNodes = new ArrayList();
	private ArrayList unsolvedNodes = new ArrayList();
	
	private ArrayList distances = new ArrayList();
	private ArrayList predecessors = new ArrayList();
	
	private boolean pathsSolved;
	
	private int currentSourceNode;

	public DjikstraPathSolver()
	{
		super();
		pathsSolved = false;
	}
	
	/**
	 * Given a graph, represented as an adjacency list, and a source node, compute the shortest
	 * paths to each node in the graph from the source.
	 * <p>
	 * The particulars of the adjacency list representation used here are as follows.  The entry at index
	 * <code>i</code> of the argument <code>ArrayList graph</code> contains edge data for node <code>i</code>.  
	 * This edge data is itself represented as an <code>ArrayList</code>.  
	 * <p>
	 * That is, <code>graph.get(i)</code> should return an <code>ArrayList</code>  (hereafter
	 * referred to as the edge <code>ArrayList</code>) containing edge information  for node <code>i</code>.
	 * <p>
	 * The edge <code>ArrayList</code> should contain <code>Float</code> objects.  The Float object stored
	 * at index <code>j</code> of the edge list should give the length of the directed edge connecting node <code>i</code>  
	 * to node <code>j</code>.  If there is no edge connecting <code>i</code> to <code>j</code>, then the Float
	 * object should contain <code>Float.MAX_VALUE</code> to indicate this fact.  Note that edge lengths are
	 * allowed to be both zero and negative; nodes should always have 0 distance to
	 * themselves. 
	 * 
	 * @param graph An adjacency list representation of the graph.
	 * @param sourceNode
	 */
	public void computePaths(ArrayList graph, int sourceNode)
	{
		initializeSource(graph, sourceNode);
		
		int currentNodeIndex;
		ArrayList currentNodeNeigbors = new ArrayList();
		while (!unsolvedNodes.isEmpty())
		{
			currentNodeIndex = getNearestUnsolvedNode(sourceNode);
			currentNodeNeigbors = (ArrayList) graph.get(currentNodeIndex);
			
			for (int thisNeighborIndex = 0; thisNeighborIndex < currentNodeNeigbors.size(); thisNeighborIndex++)
			{
				float thisNeighborDistance = ((Float) currentNodeNeigbors.get(thisNeighborIndex)).floatValue();
				if (thisNeighborDistance != Float.MAX_VALUE)
				{
					relax(currentNodeIndex, thisNeighborIndex, thisNeighborDistance);
				}
			}
		}
		
		pathsSolved = true;
	}
	
	/**
	 * Retrieve the shortest path from the currently specified source node to the
	 * argument node.  Note that this method should be called only <i>after</i>
	 * <code>computePaths()</code> has been used to solve for the shortest paths.
	 * @param destinationNodeIndex the destination node's index
	 * @return An <code>ArrayList</code> of <code>Integers</code> specifiyng 
	 * 	the sequence of nodes (referenced by integer indices) that should be followed to get from the source
	 * 	node to the specified destination.  The destination node's index is always the
	 * 	int value of the last element in the <code>ArrayList</code>.
	 */
	public ArrayList getPathTo(int destinationNodeIndex)
	{
		assert pathsSolved == true : "You must call computePaths() before invoking getPathsTo()";
		
		ArrayList revPath = new ArrayList();
		ArrayList path = new ArrayList();
		Integer nextStep = null;
		
		int currentNode = destinationNodeIndex;
		revPath.add(new Integer(destinationNodeIndex));
		
		do
		{
			nextStep = (Integer) predecessors.get(currentNode);
			if (nextStep != null)
			{
				revPath.add(new Integer(nextStep.intValue()));
				currentNode = nextStep.intValue();
			}
		}
		while (nextStep != null);
		
		int numSteps = revPath.size();
		for (int i = 0; i < numSteps; i++)
		{
			path.add(revPath.get(numSteps - 1 - i));
		}
		
		return path;
	}
	
	/**
	 * Get the index for the graph node from which shortest paths have currently been 
	 * solved.  Throws an assert if paths have not yet been calculated.
	 * @return
	 */
	public int getCurrentSourceNode()
	{
		assert pathsSolved == true : "No graph or source node has yet been specified!";
		return currentSourceNode;
	}
	
	/**
	 * Reset state and prepare to compute shortest paths for the new
	 * argument graph / source node.
	 */
	private void initializeSource(ArrayList graph, int sourceNode)
	{
		currentSourceNode = sourceNode;
		
		distances.clear();
		predecessors.clear();
		solvedNodes.clear();
				
		for (int i = 0; i < graph.size(); i++)
		{
			distances.add(new Float(Float.MAX_VALUE));
			predecessors.add(null);
			
			if (graph.get(i) != null)
			{
				unsolvedNodes.add(new Integer(i));
			}
		}
		
		distances.set(sourceNode, new Float(0f));
		pathsSolved = false;
	}
	
	/**
	 * Extract the index of the node in the <code>unsolvedNode</code> list with the
	 * minimum currently computed distance to the sourceNode.
	 * @return the index of the identified node
	 */
	private int getNearestUnsolvedNode(int sourceNode)
	{
		Integer nearestNode = null;
		int nearestNodeIndex = 0;
		float smallestDistance = Float.MAX_VALUE;
		
		for (Iterator iter = unsolvedNodes.iterator(); iter.hasNext();)
		{
			Integer thisNode = (Integer) iter.next();
			int thisNodeIndex = thisNode.intValue();
			float thisDistance = ((Float) distances.get(thisNodeIndex)).floatValue();
			
			if (thisDistance < smallestDistance)
			{
				smallestDistance = thisDistance;
				nearestNodeIndex = thisNodeIndex;
				nearestNode = thisNode;
			}
		}
		
		assert nearestNode != null : "nearestNode should neve be null!";
		
		unsolvedNodes.remove(nearestNode);
		solvedNodes.add(nearestNode);
		return nearestNodeIndex;
	}
	
	/**
	 * Perform relaxation between the nodes indexed by <code>currentNodeIndex</code>
	 * and <code>neighborIndex</code>.
	 */
	private void relax(int currentNodeIndex, int neighborIndex, float neighborDistance)
	{
		float currentNeighborPathLength = ((Float) distances.get(neighborIndex)).floatValue();
		float currentNodePathLength = ((Float) distances.get(currentNodeIndex)).floatValue();
		
		float newNeighborPathLength = currentNodePathLength + neighborDistance;
		
		if (newNeighborPathLength < currentNeighborPathLength)
		{
			distances.set(neighborIndex, new Float(newNeighborPathLength));
			predecessors.set(neighborIndex, new Integer(currentNodeIndex));
		}
	}
	
	/**
	 * Test driver.  Solve shortest paths for the example graph specified in Figure 25.5 of CLR (page 528).
	 */
	public static void main(String[] args)
	{
		DjikstraPathSolver djikstra = new DjikstraPathSolver();
		
		ArrayList graph = new ArrayList();
		
		ArrayList node0 = new ArrayList();
		ArrayList node1 = new ArrayList();
		ArrayList node2 = new ArrayList();
		ArrayList node3 = new ArrayList();
		ArrayList node4 = new ArrayList();
		
		djikstra.fillWithMaxValue(node0, 5);
		djikstra.fillWithMaxValue(node1, 5);
		djikstra.fillWithMaxValue(node2, 5);
		djikstra.fillWithMaxValue(node3, 5);
		djikstra.fillWithMaxValue(node4, 5);
		
		//Node 0
		node0.set(0, new Float(0f));
		node0.set(1, new Float(10f));
		node0.set(2, new Float(5f));
		
		//Node 1
		node1.set(1, new Float(0f));
		node1.set(2, new Float(2f));
		node1.set(3, new Float(1f));
		
		//Node 2
		node2.set(2, new Float(0f));
		node2.set(1, new Float(3f));
		node2.set(3, new Float(9f));
		node2.set(4, new Float(2f));
		
		//Node 3
		node3.set(3, new Float(0f));
		node3.set(4, new Float(4f));
		
		//Node 4
		node3.set(4, new Float(0f));
		node3.set(3, new Float(6f));
		node3.set(0, new Float(7f));
		
		graph.add(node0);
		graph.add(node1);
		graph.add(node2);
		graph.add(node3);
		graph.add(node4);
		
		djikstra.computePaths(graph, 0);
		ArrayList path = djikstra.getPathTo(3);
		
		System.out.println(path);
	}
	
	/**
	 * Identical to the <code>main()</code> method, but provided so that the class can
	 * be launched and then debugged inside eclipse.
	 */
	public void launch()
	{
		
		ArrayList graph = new ArrayList();
		
		ArrayList node0 = new ArrayList();
		ArrayList node1 = new ArrayList();
		ArrayList node2 = new ArrayList();
		ArrayList node3 = new ArrayList();
		ArrayList node4 = new ArrayList();
		
		fillWithMaxValue(node0, 5);
		fillWithMaxValue(node1, 5);
		fillWithMaxValue(node2, 5);
		fillWithMaxValue(node3, 5);
		fillWithMaxValue(node4, 5);
		
		//Node 0
		node0.set(0, new Float(0f));
		node0.set(1, new Float(10f));
		node0.set(2, new Float(5f));
		
		//Node 1
		node1.set(1, new Float(0f));
		node1.set(2, new Float(2f));
		node1.set(3, new Float(1f));
		
		//Node 2
		node2.set(2, new Float(0f));
		node2.set(1, new Float(3f));
		node2.set(3, new Float(9f));
		node2.set(4, new Float(2f));
		
		//Node 3
		node3.set(3, new Float(0f));
		node3.set(4, new Float(4f));
		
		//Node 4
		node4.set(4, new Float(0f));
		node4.set(3, new Float(6f));
		node4.set(0, new Float(7f));
		
		graph.add(node0);
		graph.add(node1);
		graph.add(node2);
		graph.add(node3);
		graph.add(node4);
		
		computePaths(graph, 0);
		ArrayList path = getPathTo(3);
		
		System.out.println(path);
	}
	
	/**
	 * Convenience function for the test driver
	 */
	private void fillWithMaxValue(ArrayList list, int size)
	{
		for (int i = 0; i < size; i++)
		{
			list.add(i, new Float(Float.MAX_VALUE));
		}
	}

}
