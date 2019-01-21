package engine.utilities;

public class ArraysExt{
	
	/**
	 * If all elements in the @param array are equal to @param value then returns true. Returns false otherwise.
	 * 
	 * @param array
	 * @param value
	 * @return
	 */
	public static boolean areAllElementsEqualTo(boolean[] array, boolean value ) {
		for(boolean element: array)
			if(element!=value)
				return false;
		
		return true;
	}
	
	/**
	 * Returns the inverse of @param a.
	 * 
	 * @param a
	 */
	public static void reverse(float[] a){
	    int l = a.length;
	    for (int j = 0; j < l / 2; j++){
	        float temp = a[j];
	        a[j] = a[l - j - 1];
	        a[l - j - 1] = temp;
	    }
	}
}
