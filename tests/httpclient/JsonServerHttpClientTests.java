package httpclient;

import com.google.code.tempusfugit.concurrency.annotations.Concurrent;
import com.google.code.tempusfugit.concurrency.annotations.Repeating;
import common.PerformanceTest;
import org.apache.http.client.HttpClient;
import org.junit.Test;

import static httpclient.SimpleHttpClientTest.request;

/**
 * @author renato
 */
public class JsonServerHttpClientTests extends PerformanceTest
{
	static final String TARGET = "http://loaduitest.smartp.qasoftwareplanner.com:4000/";

	@Test
	@Repeating( repetition = PerformanceTest.RUNS_PER_THREAD )
	@Concurrent( count = PerformanceTest.THREADS )
	public void testHttpClient() throws Exception
	{
		HttpClient client = PerformanceTest.httpClients.get( Thread.currentThread() );
		long start = System.currentTimeMillis();

		request( client, TARGET );
		request( client, TARGET + "posts" );
		request( client, TARGET + "posts/1" );
		request( client, TARGET + "posts/1/comments" );

		collectResults( start );
	}

}
