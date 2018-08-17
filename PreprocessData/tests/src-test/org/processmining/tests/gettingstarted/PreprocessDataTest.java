package org.processmining.tests.gettingstarted;
import org.junit.Test;
import org.processmining.contexts.cli.CLI;

import junit.framework.TestCase;

public class PreprocessDataTest extends TestCase {

  @Test
  public void testPreprocessDataTest1() throws Throwable {
    String args[] = new String[] {"-l"};
    CLI.main(args);
  }

  @Test
  public void testPreprocessDataTest2() throws Throwable {
    String testFileRoot = System.getProperty("test.testFileRoot", ".");
    String args[] = new String[] {"-f", testFileRoot+"/GettingStarted_Example.txt"};
    
    CLI.main(args);
  }
  
  public static void main(String[] args) {
    junit.textui.TestRunner.run(PreprocessDataTest.class);
  }
  
}
