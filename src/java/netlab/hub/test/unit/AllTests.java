package netlab.hub.test.unit;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ClientSessionTest.class,
	ServiceListenerTest.class,
	DispatcherTest.class,
	DispatcherActionTest.class,
	WildcardPatternMatchTest.class,
	ServiceMessageTest.class,
})

public class AllTests {}
