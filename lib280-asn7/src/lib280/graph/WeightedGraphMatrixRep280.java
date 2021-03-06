package lib280.graph;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import lib280.base.CursorPosition280;
import lib280.exception.ItemNotFound280Exception;
import lib280.graph.GraphMatrixRep280;
import lib280.graph.Vertex280;


public class WeightedGraphMatrixRep280<V extends Vertex280> extends GraphMatrixRep280<V, WeightedEdge280<V>> {

	public WeightedGraphMatrixRep280(int cap, boolean d) {
		super(cap, d, "lib280.graph.Vertex280", "lib280.graph.WeightedEdge280");
		// TODO Auto-generated constructor stub
	}

	public WeightedGraphMatrixRep280(int cap, boolean d,
			String vertexTypeName) {
		super(cap, d, vertexTypeName, "lib280.graph.WeightedEdge280");
	}

	
	/**
	 * Replaces the current graph with a graph read from a data file.
	 * 
	 * File format is a sequence of integers. The first integer is the total
	 * number of nodes which will be numbered between 1 and n.
	 * 
	 * Remaining integers are treated as ordered pairs of (source, destination)
	 * indicies defining graph edges.
	 * 
	 * @param fileName
	 *            Name of the file from which to read the graph.
	 * @throws RuntimeException
	 *             if the file format is incorrect, or an edge appears more than
	 *             once in the input.
	 */
	@SuppressWarnings("unchecked")
	public void initGraphFromFile(String fileName) {

		// Erase the current graph.
		this.clear();

		// Try to open the data file.
		Scanner inFile = null;
		try {
			inFile = new Scanner(new File(fileName));
		} catch (IOException e) {
			throw new RuntimeException("Error opening the file with name "
					+ fileName);
		}

		if (!inFile.hasNextInt())
			throw new RuntimeException(
					"Did not find number of nodes, illegal file format or unexpected end of file.");

		// Get the number of nodes in the graph.
		int numV = inFile.nextInt();

		// Add more capacity if needed.
		if (numV > this.capacity()) {
			this.vertexArray = (V[]) new Vertex280[numV];
			this.createEdgeDataStructure();
		}

		// Make sure all needed vertices exist. (this also updates
		// this.numVertices because it adds each vertex with this.addVertex())
		this.ensureVertices(numV);

		// Read the adjacency list until there are no more tokens available.
		while (inFile.hasNext()) {

			// Get source vertex. If there is a next token and it's not an
			// integer, issue error.
			if (!inFile.hasNextInt())
				throw new RuntimeException(
						"Illegal file format or unexpected end of file.");
			int srcIdx = inFile.nextInt();

			// Get destination vertex. If there is a next token and it's not an
			// integer, issue error.
			if (!inFile.hasNextInt())
				throw new RuntimeException(
						"Illegal file format or unexpected end of file.");
			int dstIdx = inFile.nextInt();

			if(!inFile.hasNextDouble() ) {
				throw new RuntimeException("Illegal file format or unexpected end of file.");
			}
			double weight = inFile.nextDouble();
			
			
			if (this.isAdjacent(srcIdx, dstIdx))
				throw new RuntimeException("Edge (" + srcIdx + ", " + dstIdx
						+ ") appears multiple times in the data file.");

			this.addEdge(srcIdx, dstIdx);
			this.setEdgeWeight(srcIdx, dstIdx, weight);
		}
	}

	/**
	 * String representation of the graph for output.
	 * 
	 * @timing = O(max(numVertices, numEdges))
	 */
	public String toString() {
		CursorPosition280 position = this.currentPosition();
		StringBuffer result = new StringBuffer();
		result.append(this.numVertices);
		if (directed)
			result.append("   directed");
		else
			result.append("   undirected");

		
		if(this.isEmpty()) {
			result.append("\n Empty graph.");
			return new String(result);
		}

		this.goFirst();
		while (!this.after()) {
			result.append("\n" + item() + " : ");
			this.eGoFirst(this.item());
			while (!this.eAfter()) {
				result.append(" " + this.eItem().toStringGraphIO(item()) +  ":" + this.eItem().getWeight()+", ");
				this.eGoForth();
			}
			// result.append(" 0");
			this.goForth();
		}
		this.goPosition(position);
		return new String(result);
	}
	
	
	
	/**
	 * Obtain the weight of an edge.
	 * @param srcIdx Index of the source vertex of the edge.
	 * @param dstIdx Index of the destination vertex of the edge.
	 * @return The weight of the edge (srcIdx, dstIdx).
	 */
	public double getEdgeWeight(int srcIdx, int dstIdx) {
		return this.getEdgeWeight(this.vertex(srcIdx), this.vertex(dstIdx));
	}
	
	/**
	 * Obtain the weight of the edge from v1 to v2
	 * @param v1 Source vertex of the edge.
	 * @param v2 Destination vertex of the edge.
	 * @return THe weight of the edge (v1, v2)
	 */
	public double getEdgeWeight(V v1, V v2) {
		// Find the edge (v1, v2)
		CursorPosition280 p = this.currentPosition();
		this.eSearch(v1, v2);
		
		// Make sure the search was successful.
		if( !this.eItemExists() ) {
			throw new ItemNotFound280Exception("Attempt to obtain weight of nonexistent edge: (" + v1.index() + ", " + v2.index() + ")");
		}
		
		// Obtain it's weight
		double weight = this.eItem.getWeight();
		
		// Restore the cursors
		this.goPosition(p);
		
		// Return the weight
		return weight;
	}
	
	/**
	 * Set the weight of an edge.
	 * @param srcIdx Index of the source vertex of the edge.
	 * @param dstIdx Index of the destination vertex of the edge.
	 * @param weight New weight for the edge (srcIdx, dstIdx).
	 */
	public void setEdgeWeight(int srcIdx, int dstIdx, double weight) {
		this.setEdgeWeight(this.vertex(srcIdx), this.vertex(dstIdx), weight);
	}
	
	/**
	 * Set the weight of an edge.
	 * @param v1 Source vertex of the edge.
	 * @param v2 Destination vertex of the edge.
	 * @param weight New weight for the edge (srcIdx, dstIdx).
	 */
	public void setEdgeWeight(V v1, V v2, double weight) {
		// Find the edge (v1, v2)
		CursorPosition280 p = this.currentPosition();
		this.eSearch(v1, v2);
		
		// Update it's weight
		this.eItem().setWeight(weight);
		
		// If the graph is undirected, update the weight of the symmetric edge.
		if( !this.directed ) {
			this.eSearch(v2, v1);
			this.eItem().setWeight(weight);
		}
			
		// Restore the cursor positions.
		this.goPosition(p);
	}
	
	public static void main(String args[]) {
		WeightedGraphMatrixRep280<Vertex280> G = new WeightedGraphMatrixRep280<Vertex280>(10, false);

		G.initGraphFromFile("src/lib280/graph/weightedtestgraph.gra");
		
		System.out.println(G);	
		
		G.clear();
		
		System.out.println(G);
	
		
	}
}
