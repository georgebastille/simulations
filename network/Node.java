import java.util.*;

class Node {
	
	public int x,y,ID;
        public boolean checked = false, inBigCluster = false;
	public Vector<Integer> links = new Vector<Integer>();
	
	public Node(int x, int y, int ID) { 
		
		this.x = x;
		this.y = y;
		this.ID = ID;
	}

	public void addLink(int endID) {
		
		links.add(endID);
	}

	public void removeLink(int linkNo) {
		
		links.removeElementAt(linkNo);
	}

	public int findLink(int endID) {
		for (int i = 0; i < links.size(); i++) {
			if (links.get(i) == endID){
				return i;
			}
		}
		// If the link does not exist
		return -1;
	}

	public int noLinks() {
		
		return links.size();
	}

	private double distanceTo(Node b) {
		
		int dX = this.x - b.x;
		int dY = this.y - b.y;
		
		return Math.sqrt(dX*dX + dY*dY);
	}

	public double totalLinkLength(Node[] nodes) {
		
		double linkLength = 0.0;
		int noLinks = links.size();
	
		for (int i = 0; i < noLinks; i++) {
			Integer e = links.get(i);
			linkLength += distanceTo(nodes[e]);
		}

		return linkLength;
	}
}
