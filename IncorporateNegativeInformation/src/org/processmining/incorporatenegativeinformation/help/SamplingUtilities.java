package org.processmining.incorporatenegativeinformation.help;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SamplingUtilities {
	// one function to generate a sublist of index w.r.t. probability
	public static List<Integer> sample(int bound, double prob){
		
		int num = (int)(prob*bound);
		return sample(bound, num);
	}
	
	public static List<Integer> sample(int bound, int num){
		ArrayList<Integer> idx_list = new ArrayList<>();
		Random random = new Random();
		int index;
		while(num>0) {
			index = random.nextInt(bound);
			if(!idx_list.contains(index)) { // random.nextDouble()< prob && 
				idx_list.add(index);
				num--;
				// System.out.println(index);
			}
		}
		return idx_list;
	}
	
	public static void main(String[] agrs) {
		List<Integer> data = SamplingUtilities.sample(10, 0.62);
		System.out.println(data);
	}
}
