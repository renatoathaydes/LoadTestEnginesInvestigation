package webdriver;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author renato
 */
public class CodeCollaboratorTest extends PerformanceTest
{

	static final String TARGET = "http://127.0.0.1:8080/";

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void login_CreateACodeReview_DeleteIt_logout() throws Exception
	{
		WebDriver driver = drivers.get( Thread.currentThread() );
		driver.manage().timeouts().implicitlyWait( 5, TimeUnit.SECONDS );

		long start = System.currentTimeMillis();

		// ERROR: Caught exception [ERROR: Unsupported command [allowNativeXpath | false | ]]
		driver.get( TARGET + "ui" );
		driver.findElement( By.id( "loginFormUserName" ) ).clear();
		driver.findElement( By.id( "loginFormUserName" ) ).sendKeys( "loadtester" );
		driver.findElement( By.id( "loginFormPassword" ) ).clear();
		driver.findElement( By.id( "loginFormPassword" ) ).sendKeys( "loadtest" );
		driver.findElement( By.id( "loginFormSubmit" ) ).click();

		isElementPresent( driver, By.linkText( "CREATE NEW REVIEW" ), true );

		driver.findElement( By.linkText( "CREATE NEW REVIEW" ) ).click();
		Thread.sleep( 1000 );
		isElementPresent( driver, By.xpath( "(//input[@type='text'])[3]" ), true );
		driver.findElement( By.xpath( "(//input[@type='text'])[3]" ) ).clear();
		driver.findElement( By.xpath( "(//input[@type='text'])[3]" ) ).sendKeys( "A new review" );
		isElementPresent( driver, By.cssSelector( "textarea.gwt-TextArea.ccollab-CustomField-control" ), true );

		driver.findElement( By.cssSelector( "textarea.gwt-TextArea.ccollab-CustomField-control" ) ).clear();
		driver.findElement( By.cssSelector( "textarea.gwt-TextArea.ccollab-CustomField-control" ) ).sendKeys( "This is a load test" );
		driver.findElement( By.id( "gwt-uid-124" ) ).click();

		isElementPresent( driver, By.cssSelector( ".gwt-TextArea.ccollab-CustomField-control" ), true );

		driver.findElement( By.cssSelector( ".gwt-TextArea.ccollab-CustomField-control" ) ).click();
		isElementPresent( driver, By.linkText( "ADD" ), true );
		driver.findElement( By.linkText( "ADD" ) ).click();
		driver.findElement( By.xpath( "(//a[contains(text(),'Done Editing')])[2]" ) ).click();
		driver.findElement( By.linkText( "BEGIN REVIEW" ) ).click();
		driver.findElement( By.xpath( "(//a[contains(text(),'Home')])[2]" ) ).click();

		driver.findElement( By.partialLinkText( "Review #" ) );
		isElementPresent( driver, By.partialLinkText( "Review #" ), true );

		driver.findElement( By.partialLinkText( "Review #" ) ).click();

		isElementPresent( driver, By.xpath( "//td[4]/a/span" ), true );

		driver.findElement( By.xpath( "//td[4]/a/span" ) ).click();

		isElementPresent( driver, By.linkText( "OK" ), true );

		driver.findElement( By.linkText( "OK" ) ).click();

		isElementPresent( driver, By.id( "gwt-uid-24" ), true );

		assertFalse( isElementPresent( driver, By.partialLinkText( "Review #" ), false ) );
		driver.findElement( By.linkText( "Logout" ) ).click();

		collectResults( start );
	}

	private boolean isElementPresent( WebDriver driver, By by, boolean failIfNotFound )
	{
		if( logEverything )
			System.out.println( "Checking if element is present: " + by + ", time: " + new Date() );
		try
		{

			driver.findElement( by );
			return true;
		}
		catch( NoSuchElementException e )
		{
			System.out.println( "Not found: " + by + ", time: " + new Date() );
			if( failIfNotFound )
				fail( "Not found " + by );
			return false;
		}
	}

}
