package btv.tests.download.message;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
   BitfieldTestCase.class,
   MessageTestCase.class,
   HaveTestCase.class,
   RequestTestCase.class,
   PieceTestCase.class,
   CancelTestCase.class
})

public class MessageTestSuite {   
} 