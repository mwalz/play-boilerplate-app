package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

class ApplicationSpec extends Specification {
  
  import models._
  
  "Application" should {
    
    "redirect to the login page with accessing Admin Only page" in {
      
      val result = controllers.Application.adminOnly(FakeRequest())
      
      redirectLocation(result) must beSome.which(_ == "/login")
    }
    
    "redirect to the login page with accessing Users Only page" in {
      
      val result = controllers.Application.usersOnly(FakeRequest())
      
      redirectLocation(result) must beSome.which(_ == "/login")      
    }
    
  }
  
}