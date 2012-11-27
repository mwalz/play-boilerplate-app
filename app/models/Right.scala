package models

import anorm._
import anorm.SqlParser._
import play.api.db._
import play.api.Play._
import java.util.Date
import java.sql.SQLException

/**
 * Rights for the application
 */
case class Right(
    val id: Pk[Long] = NotAssigned, 
    val name: String, 
    val lastModified: Option[Date])

object Right {

  val simple = {
    get[Pk[Long]]("rights.right_id") ~
    get[String]("rights.name") ~
    get[Option[Date]]("rights.last_modified") map {
      case id~name~lastModified => Right(id, name, lastModified)
    }
  }
  
  val simpleEmpty = {
    get[Option[Pk[Long]]]("rights.right_id") ~
    get[Option[String]]("rights.name") ~
    get[Option[Date]]("rights.last_modified") map {
      case Some(id)~Some(name)~lastModified => Option(Right(id, name, lastModified))
      case _ => None
   }
  }
  
  val ADMIN = findByName("Admin")
  val USER = findByName("User")

  def create(right: Right): Option[Right] = {
    findByName(right.name) match {
      case Some(r) => {
        throw new Exception("Right already exists.")
      }
      case None => {
		val roleId = DB.withConnection { implicit connection =>
		  SQL(
		    """
		      INSERT INTO rights
		      (name)
		      VALUES ({name})
		    """
		  ).on(
		    'name -> right.name
		  ).executeInsert()
		}
		right.copy(id = Id(roleId.getOrElse(throw new SQLException("Problem inserting right."))))
		Some(right)
      }
    }      
  }

  def update(right: Right) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          UPDATE rights SET
          name={name}
          WHERE right_id = {rightId}
        """
      ).on(
        'rightId -> right.id
      ).executeUpdate()
    }
  }
  
  def delete(right: Right) = {
    DB.withConnection { implicit connection =>
      SQL(
        "DELETE FROM rights WHERE right_id = {rightId}"
      ).on(
        'rightId -> right.id
      ).executeUpdate()
    }   
  }

  def findAll: List[Right] = {
    DB.withConnection { implicit connection =>
      SQL("SELECT * FROM rights").as(Right.simple *)
    }
  }
  
  def findById(rightId: Long): Option[Right] = {
    DB.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM rights WHERE right_id = {rightId}"
      ).on(
        'rightId -> rightId
      ).as(Right.simple.singleOpt)
    }
  }  
    
  def findByName(name: String): Option[Right] = {
    DB.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM rights WHERE name = {name}"
      ).on(
        'name -> name
      ).as(Right.simple.singleOpt)
    }
  }
    
}