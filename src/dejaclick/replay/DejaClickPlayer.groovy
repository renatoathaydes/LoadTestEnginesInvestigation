package dejaclick.replay

import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

/**
 *
 * @author renato
 */
class DejaClickPlayer {

    def bySeq = { it.@seq.toInteger() }

    void play( URL pathToScript ) {
        def script = new XmlSlurper().parse( pathToScript.newInputStream() )
        def replayAction = script.actions.find { it.@type == 'replay' }

        if ( !replayAction ) throw new RuntimeException( "Script does not have a replay action: $pathToScript" )

        def actionList = replayAction.action.list().sort( bySeq )
        println "ActionList ${actionList.collect { ( it.@seq.toString() ) + '->' + it.@description }}"

        def eventList = actionList.collect { it.event.list().sort( bySeq ) }.flatten()
        println "EventList ${eventList.collect { ( it.@seq.toString() ) + '->' + it.@description }}"

        def requestList = eventList.collect { it.request.list() }.flatten()
        println "Number of requests found: ${requestList.size()}"

        runRequests( requestList )
    }

    void runRequests( List requests ) {
        for ( request in requests ) {
            def steps = request.step.list()
            runSteps( steps )
        }
    }

    void runSteps( List steps ) {
        for ( step in steps ) {
            def url = step.@url.text()
            println "Running step ${decode( url )}"
            def decodedUri = decode( url ).toURI()
            runRequest( decodedUri, step )
        }
    }

    private void runRequest( URI uri, step ) {
        def http = HttpClients.createDefault()
        def method = step.@method?.text() ?: 'GET'

        if ( method == 'GET' ) {
            def get = new HttpGet( uri )
            step.reqhdr.each { headerEntry ->
                println( "Header: " + headerEntry.@name + '   ' + decode( headerEntry.text() ) )
                get.setHeader( headerEntry.@name.text(), decode( headerEntry.text() ) )
            }
            def response = http.execute( get )

            try {
                println "StatusLine: $response.statusLine"
                if ( response.statusLine.statusCode == 200 ) {
                    def responseEntity = response.entity
                    println "Response length: ${responseEntity.contentLength}"
                }
            } finally {
                response.close()
            }

        } else {
            println "METHOD $method NOT SUPPORTED YET!!!"
        }
    }

    private String decode( str ) {
        URLDecoder.decode( str as String, 'UTF-8' )
    }

}
