package engine.utilities;

import java.util.Arrays;

public class ArraysExt{
	private ArraysExt() {
		
	}
	
	public static boolean areAllElementsEqual(boolean[] array, boolean value ) {
		for(boolean element: array)
			if(element!=value)
				return false;
		
		return true;
	}
	
	/*public static ArrayList<GameObject> sort(){
		Arrays.sort
	}*/
}
