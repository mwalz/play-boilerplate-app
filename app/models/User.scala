package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play._
import java.util.Date
import java.sql.SQLException

/**
 * An individual user for the application
 */
case class User(
  val id: Pk[Long] = NotAssigned, 
  val email: String, 
  val passwordHash: String, 
  val firstName: String, 
  val lastName: String, 
  val isActive: Boolean, 
  val lastModified: Option[Date],
  val roles: Seq[Right] = List() ) {
  
  def isUser: Boolean = {
    roles.contains(Right.USER.getOrElse(throw new Exception("User right not defined.")))
  }
  
  def isAdmin: Boolean = {
    roles.contains(Right.ADMIN.getOrElse(throw new Exception("Admin right not defined.")))
  }
  
  def hasRole(right: Right): Boolean = {
    roles.contains(right)
  }
}

object User {

  val simple = {
    get[Pk[Long]]("users.user_id") ~
    get[String]("users.email") ~
    get[String]("users.password_hash") ~
    get[String]("users.first_name") ~
    get[String]("users.last_name") ~
    get[Boolean]("users.is_active") ~
    get[Option[Date]]("users.last_modified") map {
      case id~email~passwordHash~firstName~lastName~isActive~lastModified => User(id, email, passwordHash, firstName, lastName, isActive, lastModified)
    }
  }
  
  val withRights = simple ~ Right.simpleEmpty map {
    case u~r => (u, r)	
  }

  def authenticate(email: String, passwordHash: String): Option[User] = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          SELECT * FROM users
          WHERE email = {email} and password_hash = {passwordHash}
        """
      ).on(
        'email -> email,
        'passwordHash -> passwordHash
      ).as(User.simple.singleOpt)
    }
  }
  
  def create(user: User): Option[User] = {
    findByEmail(user.email) match {
      case Some(u) => {
        throw new Exception("User already exists.")
      }
      case None => {
		val userId = DB.withConnection { implicit connection =>
		  SQL(
		    """
		      INSERT INTO users
		      (email, password_hash, first_name, last_name, is_active)
		      VALUES ({email}, {passwordHash}, {firstName}, {lastName}, {isActive})
		    """
		  ).on(
		    'email -> user.email,
		    'passwordHash -> user.passwordHash,
		    'firstName -> user.firstName,
		    'lastName -> user.lastName,
		    'isActive -> user.isActive
		  ).executeInsert()
		}
		val createdUser = user.copy(id = Id(userId.getOrElse(throw new SQLException("Problem creating user."))))
		
		val rolesSQL = createdUser.roles.map(r => "(" + createdUser.id + ", " + r.id + ")").mkString(", ")
		
		Option(rolesSQL) match {
		  case Some(roles) => {
		    println("INSERT INTO user_right_map (user_id, right_id) VALUES " + roles)
		    DB.withConnection { implicit connection =>
			  SQL("INSERT INTO user_right_map (user_id, right_id) VALUES " + roles
			  ).execute()
		    }		    
		  }
		  case _ => 
		}
		
    	Some(createdUser)
      }
    }      
  }

  def update(user: User) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          UPDATE users SET
          email={email}, password_hash={passwordHash}, first_name={firstName}, last_name={lastName}, is_active={isActive}
          WHERE user_id = {userId}
        """
      ).on(
        'userId -> user.id,
        'email -> user.email,
        'passwordHash -> user.passwordHash,
        'firstName -> user.firstName,
        'lastName -> user.lastName,
        'isActive -> user.isActive
      ).executeUpdate()
    }
  }
  
  def delete(user: User) = {
    DB.withConnection { implicit connection =>
      SQL("DELETE FROM user_right_map WHERE user_id = {userId}")
        .on('userId -> user.id)
        .executeUpdate()
    }

    DB.withConnection { implicit connection =>
      SQL("DELETE FROM users WHERE user_id = {userId}")
        .on('userId -> user.id)
        .executeUpdate()
    } 
  }
  
  def findById(userId: Long): Option[User] = {
    val rows: List[(User, Option[Right])] = {
      DB.withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM users
            LEFT JOIN user_right_map 
              ON users.user_id = user_right_map.user_id
            LEFT JOIN rights 
              ON user_right_map.right_id = rights.right_id
            WHERE users.user_id = {userId}
          """
        ).on(
          'userId -> userId
        ).as(User.withRights *)
      }
    }
    
    rows.headOption.map(_._1) match {
      case Some(u) => Some(u.copy(roles = rows.flatMap(r => r._2)))
      case _ => None
    }
  }

  def findByEmail(email: String): Option[User] = {
    val rows: List[(User, Option[Right])] = {
      DB.withConnection { implicit connection =>
        SQL(
          """
            SELECT * FROM users
            LEFT JOIN user_right_map 
              ON users.user_id = user_right_map.user_id
            LEFT JOIN rights 
              ON user_right_map.right_id = rights.right_id
            WHERE users.email = {email}
          """
        ).on(
          'email -> email
        ).as(User.withRights *)
      }
    }
    
    rows.headOption.map(_._1) match {
      case Some(u) => Some(u.copy(roles = rows.flatMap(r => r._2)))
      case _ => None
    }
  }
  
    
}