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
	
	public static void reverse(float[] a){
	    int l = a.length;
	    for (int j = 0; j < l / 2; j++){
	        float temp = a[j];
	        a[j] = a[l - j - 1];
	        a[l - j - 1] = temp;
	    }
	}
}
