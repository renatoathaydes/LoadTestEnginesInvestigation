package dejaclick.replay

import com.google.common.base.Supplier
import com.google.common.collect.Multimaps
import groovy.util.logging.Log
import groovy.util.slurpersupport.GPathResult
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.logging.FileHandler
import java.util.logging.SimpleFormatter

import static java.lang.Math.max
import static java.util.concurrent.TimeUnit.SECONDS

/**
 *
 * @author renato
 */
@Log
class DejaClickPlayer {

    final bySeq = { it.@seq.toInteger() }
    final timer = new Timer( false )
    final runner = Executors.newCachedThreadPool()

    final doNotHandleHeaders = [ 'Cookie' ].asImmutable()

    DejaClickPlayer() {
        def logHandler = new FileHandler( "out/dejaClick.log" )
        logHandler.formatter = new SimpleFormatter()
        log.setUseParentHandlers( false ) // stop logging to stdout
        log.addHandler( logHandler )
    }

    GPathResult parse( URL pathToScript ) {
        new XmlSlurper().parse( pathToScript.newInputStream() )
    }

    void play( URL pathToScript ) {
        play parse( pathToScript )
    }

    void play( GPathResult script, CloseableHttpClient httpClient = HttpClients.createDefault() ) {
        def replayAction = script.actions.find { it.@type == 'replay' }

        if ( !replayAction ) throw new RuntimeException( "Script does not have a replay action: $pathToScript" )

        def actionList = replayAction.action.list().sort( bySeq )
        log.warning "ActionList ${actionList.collect { ( it.@seq.toString() ) + '->' + it.@description }}"

        def eventList = actionList.collect { it.event.list().sort( bySeq ) }.flatten()
        log.warning "EventList ${eventList.collect { ( it.@seq.toString() ) + '->' + it.@description }}"

        def requestList = eventList.collect { it.request.list() }.flatten()
        log.warning "Number of requests found: ${requestList.size()}"

        def steps = requestList.collect { it.step.list() }.flatten()
        runSteps( steps, httpClient )
    }

    void runSteps( List steps, CloseableHttpClient httpClient ) {
        def requestsByTimestamp = Multimaps.newListMultimap( [ : ], [ get: { [ ] } ] as Supplier )
        def toTimeStamp = { step -> step.@timeBeforeRequest.text() as double }

        final earliestTime = steps.collect( toTimeStamp ).min()

        for ( step in steps ) {
            requestsByTimestamp.get( toTimeStamp( step ) - earliestTime ) << step
        }

        log.warning "Requests by Timestamp: ${requestsByTimestamp.keys()}"

        assert steps.size() == requestsByTimestamp.values().collect { it.size() }.sum()
        final taskWaiter = new CountDownLatch( steps.size() )

        long actualCalls = 0
        requestsByTimestamp.keySet().toList().sort().each { time ->
            def simultaneousSteps = requestsByTimestamp.get( time )
            for ( step in simultaneousSteps ) {
                timer.schedule( {
                    runner.execute { runStep( step, httpClient, taskWaiter ) }
                } as TimerTask, time.toLong() )
                actualCalls++
            }
        }
        assert steps.size() == actualCalls
        def success = taskWaiter.await( 30, SECONDS )

        if ( success ) {
            log.warning "Successfully ran all steps"
        } else {
            log.warning "Failed miserably: TIMEOUT"
        }
    }

    private void runStep( step, CloseableHttpClient httpClient, CountDownLatch latch ) {
        log.warning "Running STEP"

        try {
            def method = step.@method?.text() ?: 'GET'

            if ( method == 'GET' ) {
                def uri = decode( step.@url.text() ).toURI()
                log.warning "Running request to URI $uri"
                def get = new HttpGet( uri )
                step.reqhdr.each { headerEntry ->
                    def headerName = headerEntry.@name.text()
                    log.warning( "Header: " + headerEntry.@name + '   ' + decode( headerEntry.text() ) )
                    if ( headerName in doNotHandleHeaders ) {
                        log.warning "Not handling this header!"
                    } else {
                        get.setHeader( headerEntry.@name.text(), decode( headerEntry.text() ) )
                    }
                }
                def response = httpClient.execute( get )

                try {
                    log.warning "StatusLine: $response.statusLine"
                    if ( response.statusLine.statusCode == 200 ) {
                        def responseEntity = response.entity
                        log.warning "Response length: ${responseEntity.contentLength}"
                        if ( responseEntity.contentType?.value?.contains( 'text' ) ) {
                            def responseText = EntityUtils.toString( responseEntity )
                            log.warning( "Response (last chars): ...${responseText[ ( max( 0, responseText.size() - 100 ) )..-1 ]}" )
                        } else {
                            EntityUtils.consume( responseEntity )
                        }
                    } else {
                        log.warning "********* RESPONSE NOT OK: ${response.statusLine.statusCode} *************"
                    }
                } finally {
                    response.close()
                }

            } else {
                log.warning "METHOD $method NOT SUPPORTED YET!!!"
            }
        } catch ( e ) {
            log.warning "Problem running step $e"
            e.printStackTrace()
        } finally {
            latch.countDown()
        }

    }

    private String decode( str ) {
        URLDecoder.decode( str as String, 'UTF-8' )
    }

}
