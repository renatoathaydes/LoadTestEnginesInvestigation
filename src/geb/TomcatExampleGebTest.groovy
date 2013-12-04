import geb.Browser

import java.util.concurrent.TimeUnit

Browser.drive {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)

    go "http://localhost:8080/examples/"

    assert title == "Apache Tomcat Examples"

    assert $("h3").text() == "Apache Tomcat Examples"

    $("a", text: "Servlets examples").click()
    $("a", text: "Execute", 0).click()

    assert $("h1").text() == "Hello World!"

    driver.navigate().back()

    $("a", text: "Execute", 1).click()

    assert $("table tbody tr", 1).find("td", 1).text() == "/examples/servlets/servlet/RequestInfoExample"

//    driver.navigate().back()
//
//    $("a", text: "Execute", 4).click()
//    $(name: "cookiename") << "anotherCookie"
//    $(name: "cookievalue") << "cookieValue"
//    $("input", type: "submit").click()

    println "Success!"

    close()
    //assert $("h3").text() == "Apache Tomcat Examples"
}
