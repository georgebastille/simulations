import java.util.*;

class Network {

    int N, L; // Number of Nodes & Lattice Size
    int largestCluster = 0;
    double p, K; // Link probability
    Node[] nodes;
    Vector<Integer> current = new Vector<Integer>();
    Vector<Integer> largest = new Vector<Integer>();
	
    public Network (int N, double p, double K, int type) {
	this.N = N;
	this.L = (int)Math.ceil(Math.sqrt(N));	
	this.p = p;
	this.K = K;
	this.nodes = new Node[N];
	growNetwork(type);
    }

    void growNetwork(int type) {
		
	int c = 0;
	
	// Add the nodes in typewriter fashion
	for (int i = 0; i < L; i++) {
	    for (int j = 0; j < L; j++) {
		if (c < N){
		    nodes[c] = new Node(i,j,c);
		}
		c++;
	    }
	}
	// Random Graph
	if (type == 1){
	// Add the links (Upper Matrix Method)
	   for (int i = 0; i < N; i++) {
	       for (int j = (i + 1); j < N; j++) {
	           if (Math.random() < p) {
		       nodes[i].addLink(j);
		       nodes[j].addLink(i);
		   }
	       }
	   }
	// Evolution
	} else if (type == 2) {
	   for (int i = 0; i < N; i++) {
	      for (int j = (i-1); j >= 0; j--) {
	         if (Math.random() < (K/(i+1))) {
		    nodes[i].addLink(j);
		    nodes[j].addLink(i);
	    	 }
	      }
           }
	// Evolution + Popularity Attractive
    	} else if (type == 3) {
	   // This isn't true, but necessay to avoid div by zero
	   int maxLinks = 1;
	   double linkProb, scalingFactor;
	   for (int i = 0; i < N; i++) {
	      for (int j = (i-1); j >= 0; j--) {
		 scalingFactor = (double)nodes[j].noLinks() / maxLinks + 0.01;
		 linkProb = K/(i+1) * scalingFactor;
		 if (Math.random() < linkProb) {
		    nodes[i].addLink(j);
		    nodes[j].addLink(i);
		    if (nodes[i].noLinks() > maxLinks)
			    maxLinks = nodes[i].noLinks();
		    if (nodes[j].noLinks() > maxLinks)
			    maxLinks = nodes[i].noLinks();
		 }
	      }
	   }
	}
    }

    public void resize(int size) {
	this.N = size;
	this.L = (int)Math.ceil(Math.sqrt(N));	
	this.nodes = new Node[N];
    }
 
    public void swapLink(int fromID, int toID, int link) {

	    // Get the end point of the selected link
	    int endID = nodes[fromID].links.get(link);
	    // Remove the link from the selected from node
	    nodes[fromID].removeLink(link);
	    // Find and remove the link from the end node
	    nodes[endID].removeLink(nodes[endID].findLink(fromID));
	    // Add the link to the 'end' and 'to' nodes
	    nodes[toID].addLink(endID);
	    nodes[endID].addLink(toID);
    }

    void reset(int type) {
	this.nodes = new Node[N];
    	current.clear();
    	largest.clear();
	growNetwork(type);
    }

    
	
    void minimise() {

	// Kawasaki Method

	// Swap two nodes
	// Re-Calculate length
	// If shorter allow swap
	// if not, swap back
	    
	double oldLength, newLength;
	int firstID, secondID, x, y;

	for (int i = 0; i < (100*N); i++){

	    // Pick two random Nodes (maybe implement typewriter?)
	    firstID = (int)(Math.random() * N);

	    do{	secondID = (int)(Math.random() * N);
	    } while (firstID == secondID);

	    oldLength = nodes[firstID].totalLinkLength(nodes);
	    oldLength += nodes[secondID].totalLinkLength(nodes);

	    // Swap Positions
	    x = nodes[firstID].x;
	    y = nodes[firstID].y;
	    nodes[firstID].x = nodes[secondID].x;
	    nodes[firstID].y = nodes[secondID].y;
	    nodes[secondID].x = x;
	    nodes[secondID].y = y;

	    newLength = nodes[firstID].totalLinkLength(nodes);
	    newLength += nodes[secondID].totalLinkLength(nodes);

	    // If no improvement, swap back
	    if (newLength > oldLength){
		x = nodes[firstID].x;
		y = nodes[firstID].y;
		nodes[firstID].x = nodes[secondID].x;
		nodes[firstID].y = nodes[secondID].y;
		nodes[secondID].x = x;
		nodes[secondID].y = y;
	    }
	}
    }
    
    void findCluster() {

	current.clear();
	largest.clear();

        for (int i = 0; i < N; i++) {
	    nodes[i].inBigCluster = false;
	    nodes[i].checked = false;
        }		    

	for (int i = 0; i < N; i++){

	    if(nodes[i].checked == false){
		followLinks(i);
	    }

	    if (current.size() > largest.size()){
		largest.clear();

		for (int k = 0; k < current.size(); k++){
		    largest.addElement(current.get(k));
		}
	    }
	    current.clear();
	}

	largestCluster = largest.size();

	for (int l = 0; l < largestCluster; l++)
	    nodes[largest.get(l)].inBigCluster = true;

    }

    void followLinks(int a) {
	nodes[a].checked = true;
	current.add(a);
	int noLinks = nodes[a].links.size();
	for (int j = 0; j < noLinks; j++) {
		// Horrendous Bit of code, checks if the node at the end
		// of the current link has been checked yet, could make
		// it look alot neater but what the hey, at least I've 
		// put a comment about it so everyone knows what it does
	    if (nodes[nodes[a].links.get(j)].checked == false){
		followLinks(nodes[a].links.get(j));
	    }
	}
    }
}

