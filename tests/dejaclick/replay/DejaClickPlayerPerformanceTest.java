package dejaclick.replay;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import groovy.util.slurpersupport.GPathResult;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author renato
 */
public class DejaClickPlayerPerformanceTest extends PerformanceTest
{
	private static DejaClickPlayer player = new DejaClickPlayer();
	private static GPathResult script;

	@BeforeClass
	public static void before() throws Exception
	{
		script = player.parse( DejaClickPlayerPerformanceTest.class.getResource(
				//"dejaclick-simple-tomcat.xml"
				"smartbear.xml"
		) );
	}

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void testDejaClickPlayer() throws Exception
	{
		long start = System.currentTimeMillis();

		player.play( script, httpClients.get( Thread.currentThread() ) );

		collectResults( start );
	}

}
