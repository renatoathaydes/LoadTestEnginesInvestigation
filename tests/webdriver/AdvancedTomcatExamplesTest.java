package webdriver;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;

/**
 * @author renato
 */
public class AdvancedTomcatExamplesTest extends PerformanceTest
{
	static final String TARGET = "http://weblogic.sthlm.smartbear.local:8080/examples/";

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void testTomcatExamples() throws Exception
	{
		WebDriver driver = drivers.get( Thread.currentThread() );
		driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

		driver.get( TARGET );
		driver.findElement( By.linkText( "Servlets examples" ) ).click();
		driver.findElement( By.linkText( "Execute" ) ).click();
		assertEquals( "Hello World!", driver.findElement( By.xpath( "//h1" ) ).getText() );
		driver.navigate().back();
		driver.findElement( By.xpath( "(//a[contains(text(),'Execute')])[2]" ) ).click();
		assertEquals( "/examples/servlets/servlet/RequestInfoExample", driver.findElement( By.xpath( "//table/tbody/tr[2]/td[2]" ) ).getText() );
		driver.navigate().back();
		driver.findElement( By.xpath( "(//a[contains(text(),'Execute')])[5]" ) ).click();
		driver.findElement( By.name( "cookiename" ) ).clear();
		driver.findElement( By.name( "cookiename" ) ).sendKeys( "anotherCookie" );
		driver.findElement( By.name( "cookievalue" ) ).clear();
		driver.findElement( By.name( "cookievalue" ) ).sendKeys( "cookieValue" );
		driver.findElement( By.cssSelector( "input[type=\"submit\"]" ) ).click();

		// ERROR: Caught exception [ERROR: Unsupported command [getCookie |  | ]]

	}
}
