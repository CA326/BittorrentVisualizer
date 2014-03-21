package btv.tests.download;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class DLManagerTestRunner {
   public static void main(String[] args) {
      Result result = JUnitCore.runClasses(DLManagerTestCase.class);
      for (Failure failure : result.getFailures()) {
         System.out.println(failure.toString());
      }
      System.out.println(result.wasSuccessful());
   }
}