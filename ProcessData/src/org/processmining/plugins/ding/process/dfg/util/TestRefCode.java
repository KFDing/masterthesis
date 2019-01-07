package org.processmining.plugins.ding.process.dfg.util;

import java.util.ArrayList;
import java.util.List;

public class TestRefCode {
	// get the combinations of unknown list, which is given by 
	// an unknown length list with unknown elements
	// list 1:[1,2,3]
	// list 2:[2,5]
	// list 3:[3,7,8]
    // list 4:[4,9,0,5]
	
	// many methods to test on it 
	public static void main(String[] args) {
		List<Integer> num1 = new ArrayList<>();
		for(int i=0;i<1;i++) {
			num1.add(i);
		}
		
		List<Integer> num2 = new ArrayList<>();
		int num2_size = 2;
		for(int i=0;i<num2_size;i++) {
			num2.add(i + num2_size);
		}
		
		List<Integer> num3 = new ArrayList<>();
		int num3_size = 3;
		for(int i=0;i<num3_size;i++) {
			num3.add(num3_size + i);
		}
		
		List<Integer> num4 = new ArrayList<>();
		int num4_size = 4;
		for(int i=0;i<num4_size;i++) {
			num4.add(num4_size + i);
		}
		
		List<List<Integer>> data = new ArrayList<>();
		data.add(num1);
		data.add(num2);
		data.add(num3);
		//data.add(num4);
		
		// generate all the combination of data and print them out
		generateCombination(data);
	}

	private static void generateCombination(List<List<Integer>> data) {
		// TODO by change the index method
		int data_count = data.size();
		// indexes to get the data
		int[] indexes = new int[data_count];
		
		// store the combination
		while(true) {
			List<Integer> comb = new ArrayList<Integer>();
			
			for(int i=0; i< data_count;i++) {
				comb.add(data.get(i).get(indexes[i]));
			}
			System.out.println(comb);
			
			// change the value of indexes w.r.t to visited number, we change from the last one
			int incrementIdx = data_count - 1;
			// at first only change the last index to check if it is ok, then to one condition 
			// to get the values in it 
			while(incrementIdx >=0 && 
					(++indexes[incrementIdx]) >= data.get(incrementIdx).size()) {
				indexes[incrementIdx] =0;
				incrementIdx --;
			}
			// to the last one, then we stop it 
			if(incrementIdx <0)
				break;
			
		}
		
		
		
	}
	
	
	
}
