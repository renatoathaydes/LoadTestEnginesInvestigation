package httpclient;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.net.URI;

import static junit.framework.Assert.assertEquals;

/**
 * @author renato
 */
public class SimpleHttpClientTest extends PerformanceTest
{

	@Test
	@Repeating( repetition = RUNS_PER_THREAD )
	@Concurrent( count = THREADS )
	public void testHttpClient() throws Exception
	{
		HttpClient client = httpClients.get( Thread.currentThread() );
		long start = System.currentTimeMillis();

		request( client, "http://localhost:8080/examples/" );
		request( client, "http://localhost:8080/examples/servlets" );
		request( client, "http://localhost:8080/examples/servlets/servlet/HelloWorldExample" );
		request( client, "http://localhost:8080/examples/servlets/servlet/RequestInfoExample" );
		request( client, "http://localhost:8080/examples/servlets/servlet/CookieExample" );

		collectResults( start );
	}

	public static void request( HttpClient client, String url ) throws Exception
	{
		HttpGet get = new HttpGet( new URI( url ) );
		HttpResponse response = client.execute( get );
		assertEquals( response.getStatusLine().getStatusCode(), 200 );
		String responseText = EntityUtils.toString( response.getEntity() );
		if( logEverything )
		{
			System.out.println( "RESPONSE -> Size: " + responseText.length() );
		}
		response.getEntity().consumeContent();
	}

}
