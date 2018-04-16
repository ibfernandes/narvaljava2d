package engine.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import engine.utilities.Vec2i;



public class AStar {
	
	private Stack<Integer> touchedLocations = new Stack<Integer>();
	private ArrayList<ArrayList<PathFinderNodeFast>> nodes = new ArrayList<ArrayList<PathFinderNodeFast>>();
	
	static final int Manhattan           = 1;
    static final int MaxDXDY             = 2;
    static final int DiagonalShortCut    = 3;
    static final int Euclidean           = 4;
    static final int EuclideanNoSQR      = 5;
	static final int Custom1 = 6;
	
	
	 private PriorityQueue<Vec2i>            mOpen                    =  new PriorityQueue<Vec2i>();
     private List<Vec2i>         	         mClose                  = new ArrayList<Vec2i>();
     private boolean                            mStop                   = false;
     private boolean                            mStopped                = true;
     private int                mFormula                = Manhattan;
     private boolean                            mDiagonals              = true;
     private int                             mHEstimate              = 2;
     private boolean                            mPunishChangeDirection  = false;
     private boolean                            mTieBreaker             = false;
     private boolean                            mHeavyDiagonals         = false;
     private int                             mSearchLimit            = 2000;
     private double                          mCompletedTime          = 0;
     private boolean                            mDebugProgress          = false;
     private boolean                            mDebugFoundPath         = false;
     private int                            mOpenNodeValue          = 1;
     private int                            mCloseNodeValue         = 2;
     
     private int                             mH                      = 0;
     private Vec2i                        mLocation             = new Vec2i(0, 0);
     private int                             mNewLocation            = 0;
     private int                          mLocationX              = 0;
     private int                          mLocationY              = 0;
     private int                          mNewLocationX           = 0;
     private int                          mNewLocationY           = 0;
     private int                             mCloseNodeCounter       = 0;
     private int                          mGridX                  = 0;
     private int                          mGridY                  = 0;
     private int                          mGridXMinus1            = 0;
     private int                          mGridXLog2              = 0;
     private boolean                            mFound                  = false;
     private int[][]                        mDirection              = {{0,-1} , {1,0}, {0,1}, {-1,0}, {1,-1}, {1,1}, {-1,1}, {-1,-1}};
     private int                             mEndLocation            = 0;
     private int mNewG = 0;
     private boolean CHILDREN_LOOP_END = false;
     
	
	 public List<Vec2i> findPath(Vec2i start, Vec2i end, int characterWidth, int characterHeight, boolean mGrid[][]){
		 
		 
		 //start variables
           mGridX          = mGrid.length + 1;
           mGridY          = mGrid[0].length;
           mGridXMinus1    = mGridX - 1;
           mGridXLog2      = (int) Math.log(mGridX);

		 
		 
		 
		 
		 for(int i=0;i<mGrid.length*mGrid[0].length; i++) {
			 nodes.add(i, new ArrayList<>());
		 }
		 
		 
         while (touchedLocations.size() > 0)
             nodes.get(touchedLocations.pop()).clear();

        /* boolean inSolidTile = false;
         
         
         for (int i = 0; i < 2; ++i){
             inSolidTile = false;
             for (int w = 0; w < characterWidth; ++w){
                 if (!mGrid[end.x + w][end.y] || !mGrid[end.x + w][ end.y + characterHeight - 1]){
                     inSolidTile = true; 
                     break;
                 }

             }
             if (inSolidTile == false) {
                 for (int h = 1; h < characterHeight - 1; ++h){
                     if (!mGrid[end.x][ end.y + h] || !mGrid[end.x + characterWidth - 1][ end.y + h]) {
                         inSolidTile = true;
                         break;
                     }
                 }
             }

             if (inSolidTile)
                 end.x -= characterWidth - 1;
             else
                 break;
         }
         
         
         if (inSolidTile)
             return null;*/
        

         mFound              = false;
         mStop               = false;
         mStopped            = false;
         mCloseNodeCounter   = 0;
         mOpenNodeValue      += 2;
         mCloseNodeValue     += 2;
         mOpen.clear();

         
         mLocation.x                     = (start.y << mGridXLog2) + start.x;
         mLocation.y                     = 0;
         mEndLocation                    = (end.y << mGridXLog2) + end.x;

         PathFinderNodeFast firstNode = new PathFinderNodeFast();
         firstNode.G = 0;
         firstNode.F = mHEstimate;
         firstNode.PX = start.x;
         firstNode.PY = start.y;
         firstNode.PZ = 0;
         firstNode.Status = mOpenNodeValue;

         boolean startsOnGround = false;

         for (int x = start.x; x < start.x + characterWidth; ++x){
             if (mGrid[x][ start.y - 1]){
                 startsOnGround = true;
                 break;
             }
         }


         nodes.get(mLocation.x).add(firstNode);
         
         
         
         touchedLocations.push(mLocation.x);

        // mOpen.push(mLocation);
         mOpen.add(mLocation);
         
         
         while(mOpen.size() > 0 && !mStop){
             mLocation    = mOpen.poll();

             //Is it in closed list? means this node was already processed
             if (nodes.get(mLocation.x).get(mLocation.y).Status == mCloseNodeValue)
                 continue;

             mLocationX   =  (mLocation.x & mGridXMinus1);
             mLocationY   =  (mLocation.x >> mGridXLog2);

             if (mLocation.x == mEndLocation){
         
                 
                 nodes.get(mLocation.x).set(mLocation.y, nodes.get(mLocation.x).get(mLocation.y).UpdateStatus(mCloseNodeValue));
                 mFound = true;
                 break;
             }

             if (mCloseNodeCounter > mSearchLimit){
                 mStopped = true;
                 return null;
             }

             //Lets calculate each successors
             for (int i=0; i<(mDiagonals ? 8 : 4); i++){
            	 CHILDREN_LOOP_END = false;
                 mNewLocationX =  (mLocationX + mDirection[i][0]);
                 mNewLocationY =  (mLocationY + mDirection[i][1]);
                 mNewLocation  = (mNewLocationY << mGridXLog2) + mNewLocationX;

                 boolean onGround = false;
                 boolean atCeiling = false;

                 for (int w = 0; w < characterWidth; ++w){
                     if (!mGrid[mNewLocationX + w][mNewLocationY] || !mGrid[mNewLocationX + w][ mNewLocationY + characterHeight - 1]) {
                         CHILDREN_LOOP_END = true;
                    	 break;
                     }
                     if (mGrid[mNewLocationX + w][ mNewLocationY - 1])
                         onGround = true;
                     else if (!mGrid[mNewLocationX + w][ mNewLocationY + characterHeight])
                         atCeiling = true;
                 }
                 for (int h = 1; h < characterHeight - 1; ++h){
                     if (!mGrid[mNewLocationX][ mNewLocationY + h] || !mGrid[mNewLocationX + characterWidth - 1][ mNewLocationY + h] ) {
                    	CHILDREN_LOOP_END = true;
                	 	break;
                     }
                 }
					
				
                 if(!CHILDREN_LOOP_END) {
	                 mNewG = nodes.get(mLocation.x).get(mLocation.y).G + ((mGrid[mNewLocationX][ mNewLocationY])? 1:0) ;
	
	                 if (nodes.get(mNewLocation).size() > 0) {
	                     int lowestJump = Integer.MAX_VALUE;
	                     int lowestG = Integer.MAX_VALUE;
	                     boolean couldMoveSideways = false;
	                     for (int j = 0; j < nodes.get(mNewLocation).size(); ++j){
	
	                         if (nodes.get(mNewLocation).get(j).G < lowestG)
	                             lowestG = nodes.get(mNewLocation).get(j).G;
	
	                     }
	
	                 }
						
	                 switch(mFormula){
	                     default:
	                     case Manhattan:
	                         mH = mHEstimate * (Math.abs(mNewLocationX - end.x) + Math.abs(mNewLocationY - end.y));
	                         break;
	                     case MaxDXDY:
	                         mH = mHEstimate * (Math.max(Math.abs(mNewLocationX - end.x), Math.abs(mNewLocationY - end.y)));
	                         break;
	                     case DiagonalShortCut:
	                         int h_diagonal  = Math.min(Math.abs(mNewLocationX - end.x), Math.abs(mNewLocationY - end.y));
	                         int h_straight  = (Math.abs(mNewLocationX - end.x) + Math.abs(mNewLocationY - end.y));
	                         mH = (mHEstimate * 2) * h_diagonal + mHEstimate * (h_straight - 2 * h_diagonal);
	                         break;
	                     case Euclidean:
	                         mH = (int) (mHEstimate * Math.sqrt(Math.pow((mNewLocationY - end.x) , 2) + Math.pow((mNewLocationY - end.y), 2)));
	                         break;
	                     case EuclideanNoSQR:
	                         mH = (int) (mHEstimate * (Math.pow((mNewLocationX - end.x) , 2) + Math.pow((mNewLocationY - end.y), 2)));
	                         break;
	                     case Custom1:
	                         Vec2i dxy       = new Vec2i(Math.abs(end.x - mNewLocationX), Math.abs(end.y - mNewLocationY));
	                         int Orthogonal  = Math.abs(dxy.x - dxy.y);
	                         int Diagonal    = Math.abs(((dxy.x + dxy.y) - Orthogonal) / 2);
	                         mH = mHEstimate * (Diagonal + Orthogonal + dxy.x + dxy.y);
	                         break;
	                 }
	
	                 PathFinderNodeFast newNode = new PathFinderNodeFast();
	
	                 newNode.PX = mLocationX;
	                 newNode.PY = mLocationY;
	                 newNode.PZ = mLocation.y;
	                 newNode.G = mNewG;
	                 newNode.F = mNewG + mH;
	                 newNode.Status = mOpenNodeValue;
	
	                 if (nodes.get(mNewLocation).size() == 0)
	                     touchedLocations.push(mNewLocation);
	
	                 nodes.get(mNewLocation).add(newNode);
	                 mOpen.add(new Vec2i(mNewLocation, nodes.get(mNewLocation).size() - 1));
						
                 }
             }

             nodes.get(mLocation.x).set(mLocation.y, nodes.get(mLocation.x).get(mLocation.y).UpdateStatus(mCloseNodeValue));
             mCloseNodeCounter++;
         }

         if (mFound){
             mClose.clear();
             int posX = end.x;
             int posY = end.y;
				
             PathFinderNodeFast fPrevNodeTmp = new PathFinderNodeFast();
             PathFinderNodeFast fNodeTmp = nodes.get(mEndLocation).get(0);
				
             Vec2i fNode = end;
             Vec2i fPrevNode = end;

             int loc = (fNodeTmp.PY << mGridXLog2) + fNodeTmp.PX;
				
             while(fNode.x != fNodeTmp.PX || fNode.y != fNodeTmp.PY){
            	 PathFinderNodeFast fNextNodeTmp = nodes.get(loc).get(fNodeTmp.PZ);
                 
                 if ((mClose.size() == 0)
                     || (fNode.y > mClose.get(mClose.size() - 1).y && fNode.y > fNodeTmp.PY)
                     || (fNode.y < mClose.get(mClose.size() - 1).y && fNode.y < fNodeTmp.PY)
                     || ((mGrid[fNode.x - 1][ fNode.y] || mGrid[fNode.x + 1][ fNode.y]) 
                         && fNode.y != mClose.get(mClose.size() - 1).y && fNode.x != mClose.get(mClose.size() - 1).x))
                     mClose.add(fNode);

                 fPrevNode = fNode;
					posX = fNodeTmp.PX;
                 posY = fNodeTmp.PY;
					fPrevNodeTmp = fNodeTmp;
                 fNodeTmp = fNextNodeTmp;
					loc = (fNodeTmp.PY << mGridXLog2) + fNodeTmp.PX;
                 fNode = new Vec2i(posX, posY);
             } 

             mClose.add(fNode);

             mStopped = true;

             return mClose;
         }
         mStopped = true;
         return null;
         
	 }
}

