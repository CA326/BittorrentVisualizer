package btv.tests.bencoding;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class BEncodingTestRunner {
   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(BEncodingTestSuite.class);
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
      System.out.println(result.wasSuccessful());
   }
}  	