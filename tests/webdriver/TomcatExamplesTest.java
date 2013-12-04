package webdriver;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

public class TomcatExamplesTest extends PerformanceTest
{
	static final String TARGET = "http://weblogic.sthlm.smartbear.local:8080/examples/";

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void testTomcatExamples() throws Exception
	{
		WebDriver driver = drivers.get( Thread.currentThread() );
		driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

		long start = System.currentTimeMillis();

		driver.get( TARGET );
		driver.findElement( By.linkText( "Servlets examples" ) ).click();
		driver.findElement( By.linkText( "Execute" ) ).click();
		driver.navigate().back();
		driver.findElement( By.xpath( "(//a[contains(text(),'Execute')])[2]" ) ).click();
		driver.navigate().back();
		driver.findElement( By.xpath( "(//a[contains(text(),'Execute')])[5]" ) ).click();

		collectResults( start );
	}

}
