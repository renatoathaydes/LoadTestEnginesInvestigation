package common;

import com.google.code.tempusfugit.concurrency.ConcurrentRule;
import com.google.code.tempusfugit.concurrency.RepeatingRule;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Closeables;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.Rule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author renato
 */
public abstract class PerformanceTest
{
	public static final int RUNS_PER_THREAD = 10;
	public static final int THREADS = 2;
	public static boolean logEverything = true;

	static
	{
		//FIXME set this in some other way, just quickly trying out the PhantomJS runner
		System.setProperty( "phantomjs.binary.path", "/Java/phantomjs-1.9.2-windows/phantomjs.exe" );
	}

	public static final LoadingCache<Thread, WebDriver> drivers = CacheBuilder.newBuilder()
			.maximumSize( THREADS )
			.build(
					new CacheLoader<Thread, WebDriver>()
					{
						public WebDriver load( Thread key )
						{
							if( logEverything )
							{
								System.out.println( "Creating WebDriver for Thread " + key.getName() );
							}
							return new PhantomJSDriver();
							//return new HtmlUnitDriver();
						}
					} );

	public static final LoadingCache<Thread, CloseableHttpClient> httpClients = CacheBuilder.newBuilder()
			.maximumSize( THREADS )
			.build(
					new CacheLoader<Thread, CloseableHttpClient>()
					{
						public CloseableHttpClient load( Thread key )
						{
							if( logEverything )
							{
								System.out.println( "Creating WebDriver for Thread " + key.getName() );
							}
							return HttpClientBuilder.create().build();
						}
					} );

	@Rule
	public RepeatingRule repeatingRule = new RepeatingRule();

	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule();

	static final AtomicInteger counter = new AtomicInteger( 0 );

	static long times = 0;

	public synchronized void collectResults( long start )
	{
		long time = ( System.currentTimeMillis() - start );
		times += time;

		if( logEverything )
			System.out.println( "Ran test " + counter.getAndIncrement() + " on thread " +
					Thread.currentThread().getName() + " in " + time + " ms" );
		else
			counter.getAndIncrement();
	}

	@AfterClass
	public static void finalCleanup()
	{
		assertThat( counter.get(), is( RUNS_PER_THREAD * THREADS ) );

		System.out.println( "Number of runs per thread: " + RUNS_PER_THREAD );
		System.out.println( "Maximum Number of threads: " + THREADS );

		Collection<WebDriver> driverInstances = drivers.asMap().values();
		System.out.println( "Number of WebDriver instances created : " + driverInstances.size() );

		for( WebDriver driver : driverInstances )
		{
			if( logEverything )
			{
				System.out.println( "Removing and quitting a WebDriver" );
			}
			driver.quit();
		}

		drivers.cleanUp();

		Collection<CloseableHttpClient> httpClientInstances = httpClients.asMap().values();
		System.out.println( "Number of HttpClient instances created : " + httpClientInstances.size() );

		for( CloseableHttpClient client : httpClientInstances )
		{
			if( logEverything )
			{
				System.out.println( "Closing HttpClient" );
			}
			Closeables.closeQuietly( client );
		}

		System.out.println( "Times: " + times );
		System.out.println( "Average: " + ( times / counter.get() ) );
	}


}
