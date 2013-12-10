package dejaclick.replay

import org.junit.Test

/**
 *
 * @author renato
 */
class DejaClickPlayerTest {

    @Test
    void testDejaClickParser() {
        //def scriptPath = 'dejaclick-sample-script.xml'
        def scriptPath = 'dejaclick-simple-tomcat.xml'
        //def scriptPath = 'smartbear.xml'
        println scriptPath
        def path = this.class.getResource(scriptPath)
        assert path
        new DejaClickPlayer().play(path)
    }

}
