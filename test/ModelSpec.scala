package test

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import anorm.NotAssigned

class ModelSpec extends Specification {
  
  import models._
  
  "User model" should {
    
    "be retrieved by email" in {
      running(FakeApplication()) {
        
        val Some(user) = User.findByEmail("user2@user.com")
      
        user.firstName must equalTo("Jane")
        user.lastName must equalTo("Doe")
        user.isActive must equalTo(true)
      }
    }    

    "be retrieved by id" in {
      running(FakeApplication()) {
        
        val Some(emailUser) = User.findByEmail("user3@user.com")

        val Some(user) = User.findById(emailUser.id.get)
      
        user.firstName must equalTo("Abraham")
        user.lastName must equalTo("Lincoln")
        user.isActive must equalTo(true)
      }
    }
    
    "be created" in {
      running(FakeApplication()) {

        val Some(newUser) = User.create(User(NotAssigned, "new@user.com", "password", "Test", "User", true, None, List(Right.ADMIN.get, Right.USER.get)))
      
        val Some(retrievedUser) = User.findByEmail("new@user.com")
        
        retrievedUser.id must equalTo(newUser.id)   
        retrievedUser.email must equalTo(newUser.email)   
        retrievedUser.passwordHash must equalTo(newUser.passwordHash)   
        retrievedUser.firstName must equalTo(newUser.firstName)   
        retrievedUser.lastName must equalTo(newUser.lastName)   
        retrievedUser.isActive must equalTo(newUser.isActive)   
        retrievedUser.roles must contain(Right.ADMIN.get)   
        retrievedUser.roles must contain(Right.USER.get)   
      }
    }
    
    "be updated if needed" in {
      running(FakeApplication()) {
        
        val Some(user) = User.findByEmail("user4@user.com")

        User.update(user.copy(isActive = true))
        
        val Some(updatedUser) = User.findByEmail("user4@user.com")
        
        updatedUser.firstName must equalTo("George")
        updatedUser.lastName must equalTo("Washington")
        updatedUser.isActive must equalTo(true)     
      }
    }
    
    "be deleted" in {
      running(FakeApplication()) {
      
        val Some(user) = User.findByEmail("new@user.com")
        User.delete(user)
        
        val deletedUser = User.findByEmail("new@user.com")
        
        deletedUser must equalTo(None)   
      }
    }
    
  }
  
}