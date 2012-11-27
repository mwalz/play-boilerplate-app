package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class IntegrationSpec extends Specification {
  
  "Application" should {
    
    "login and logout from within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")
        browser.pageSource must contain("Login")

        browser.goTo("http://localhost:3333/login")
        browser.$("#email").text("test@test.com")
        browser.$("#password").text("1234")
        browser.$("#loginbutton").click()
        browser.pageSource must contain("Invalid email or password")
        
        browser.$("#email").text("user3@user.com")
        browser.$("#password").text("")
        browser.$("#loginbutton").click()
        browser.pageSource must not contain("Login")
        browser.pageSource must contain("Abraham")
        browser.pageSource must contain("Logout")
        
        browser.goTo("http://localhost:3333/logout")
        browser.pageSource must contain("Login")
      }
    }
    
  }
  
}