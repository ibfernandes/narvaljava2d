package engine.utilities;

import engine.logic.Chunk;

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
	
	public static void shift(Object mat[][], int kRow, int kCol) {
		
		//Shifting k row values.
		if(kRow>0) {
			for(int r=mat.length-1; r>=0; r--) {
				for(int c=0; c<mat[0].length; c++) {
					if(r<kRow) {
						mat[r][c] = null;
					}else {
						mat[r][c] = mat[r-kRow][c];
					}
				}
			}
		}else if (kRow<0) {
			kRow = kRow * -1;
			for(int r=0; r<mat.length; r++) {
				for(int c=0; c<mat[0].length; c++) {
					if(r > mat.length-kRow-1) {
						mat[r][c] = null;
					}else {
						mat[r][c] = mat[r+kRow][c];
					}
				}
			}
		}
		
		//Shifting k column values.
		if(kCol>0) {
			for(int c=mat[0].length-1; c>=0; c--) {
				for(int r=0; r<mat.length; r++) {
					if(c<kCol) {
						mat[r][c] = null;
					}else {
						mat[r][c] = mat[r][c-kCol];
					}
				}
			}
		}else if (kCol<0) {
			kCol = kCol * -1;
			for(int c=0; c<mat[0].length; c++) {
				for(int r=0; r<mat.length; r++) {
					if(c > mat[0].length-kCol-1) {
						mat[r][c] = null;
					}else {
						mat[r][c] = mat[r][c+kCol];
					}
				}
			}
		}
	}
	
	public static void print(Object mat[][]) {
		for(int i=0; i<mat.length; i++) {
			for(int k=0;k<mat[0].length;k++) {
				System.out.print(mat[i][k]+"\t");
			}
			System.out.println();
		}
	}
}
