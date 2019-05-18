package org.processmining.plugins.inductiveminer2.helperclasses;

public class Arrays {

	public static <X> void add(X[] array, X element) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == null) {
				array[i] = element;
				return;
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	public static boolean containsSorted(int[] array, int value) {
		return java.util.Arrays.binarySearch(array, value) >= 0;
	}
}
