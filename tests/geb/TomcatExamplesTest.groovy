package geb

import geb.junit4.GebTest
import org.junit.Test

/**
 *
 * @author renato
 */
class TomcatExamplesTest extends GebTest {



    @Test
    void "test we can use Geb"() {
        go "http://localhost:8080/examples/"

        assert title == "Apache Tomcat Examples"
        assert $("h3").tag() == "Apache Tomcat Examples"

        close()
    }

}
