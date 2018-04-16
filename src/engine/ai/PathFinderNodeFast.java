package engine.ai;

public class PathFinderNodeFast{
    public int     F; // f = gone + heuristic
    public int     G;
    public int  PX; // Parent
    public int PY;
    public int    Status;
    public int    PZ;

    public PathFinderNodeFast UpdateStatus(int newStatus){
        PathFinderNodeFast newNode = this;
        newNode.Status = newStatus;
        return newNode;
    }
}