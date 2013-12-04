package webdriver;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

/**
 * @author renato
 */
public class JsonServerWebDriverTest extends PerformanceTest
{

	static final String TARGET = "http://loaduitest.smartp.qasoftwareplanner.com:4000/";

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void testJsonServer() throws Exception
	{
		WebDriver driver = drivers.get( Thread.currentThread() );
		driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

		long start = System.currentTimeMillis();

		driver.get( TARGET );
		driver.navigate().to( TARGET + "posts" );
		driver.navigate().to( TARGET + "posts/1" );
		driver.navigate().to( TARGET + "posts/1/comments" );

		collectResults( start );
	}

}
