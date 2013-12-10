package webdriver;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * @author renato
 */
public class CodeCollaboratorTest extends PerformanceTest
{

	static final String TARGET = "http://127.0.0.1:8080/";
	List<Thread> threads = new ArrayList<Thread>( THREADS );

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void login_CreateACodeReview_DeleteIt_logout() throws Exception
	{
		WebDriver driver = drivers.get( Thread.currentThread() );
		driver.manage().timeouts().implicitlyWait( 1, TimeUnit.SECONDS );

		final String randomReviewName = UUID.randomUUID().toString();

		final String userName = userName();
		final String password = password();
		if( logEverything )
		{
			System.out.println( "Using username: " + userName + ", password:" + password );
		}

		long start = System.currentTimeMillis();

		// ERROR: Caught exception [ERROR: Unsupported command [allowNativeXpath | false | ]]
		driver.get( TARGET + "ui" );

		driver.findElement( By.id( "loginFormUserName" ) ).clear();
		driver.findElement( By.id( "loginFormUserName" ) ).sendKeys( userName );
		driver.findElement( By.id( "loginFormPassword" ) ).clear();
		driver.findElement( By.id( "loginFormPassword" ) ).sendKeys( password );
		driver.findElement( By.id( "loginFormSubmit" ) ).click();

		for( int i = 0; i < 10; i++ )
		{
			try
			{
				isElementPresent( driver, By.linkText( "CREATE NEW REVIEW" ), true );
			}
			catch( AssertionError error )
			{
				System.err.println( "Couldn't get the CREATE NEW REVIEW button, will try " + ( 10 - i ) + " times" );
			}
		}

		driver.findElement( By.linkText( "CREATE NEW REVIEW" ) ).click();
		Thread.sleep( 500 );
		isElementPresent( driver, By.xpath( "(//input[@type='text'])[3]" ), true );
		driver.findElement( By.xpath( "(//input[@type='text'])[3]" ) ).clear();
		driver.findElement( By.xpath( "(//input[@type='text'])[3]" ) ).sendKeys( randomReviewName );
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

		driver.findElement( By.partialLinkText( randomReviewName ) );
		isElementPresent( driver, By.partialLinkText( randomReviewName ), true );

		driver.findElement( By.partialLinkText( randomReviewName ) ).click();

		isElementPresent( driver, By.xpath( "//td[4]/a/span" ), true );

		driver.findElement( By.xpath( "//td[4]/a/span" ) ).click();

		isElementPresent( driver, By.linkText( "OK" ), true );

		driver.findElement( By.linkText( "OK" ) ).click();

		isElementPresent( driver, By.id( "gwt-uid-24" ), true );

		assertFalse( isElementPresent( driver, By.partialLinkText( randomReviewName ), false ) );
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

	private String userName()
	{
		return "loadtester" + threadIndex();
	}

	private String password()
	{
		return "loadtest" + threadIndex();
	}

	private synchronized int threadIndex()
	{
		Thread t = Thread.currentThread();
		int index = threads.indexOf( t );
		if( index >= 0 ) return index;

		threads.add( t );
		return threads.size() - 1;
	}

}
