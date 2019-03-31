package org.processmining.incorporatenegativeinformation.plugins;
import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XAttributeLiteral;

public class StringSplit {

	public static void main(String[] args) {
		String location = "/home/dkf/ProcessMining/programs/masterthesis/dataset/synthetic/Process-Normal-02-Log-01.xes";
		// replace(location);
		boolean flag = true;
		XFactory factory = XFactoryRegistry.instance().currentDefault();
		

		// XAttributeDiscrete attr = factory.createAttributeDiscrete("pos", 10, null);
		XAttributeLiteral attr = factory.createAttributeLiteral("pos", "10.34", null);
		
		// XAttributeBoolean attr = factory.createAttributeBoolean("pos", flag, null);
		
		System.out.println(attr.toString());
		/*
		 * String location = "/home/dkf/ProcessMining/programs/masterthesis/official_report/testcases/testdata/tc_seq_02_01.xes";
		
		String[] tmp = location.split(".*[\\\\/]");
		// String tmp = location.replaceFirst("xes", "pnml");
		String[] secondTmp = tmp[1].split("\\.");
		System.out.println(location);
		*/
	}
	
	// second replace, we need to replace the sth of it the prefix that's all..
	// we check until the Log-04.xes, then replace it with PN.pnml 
	public static void replace(String path) {
		String tmp = path.replaceFirst("(Log.*)","PN.pnml");
		System.out.println(tmp);
	}
}
