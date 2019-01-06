package models

import javax.inject.Inject

import anorm._
import anorm.SqlParser.{ get, str }

import play.api.db.DBApi

import scala.concurrent.Future

case class Principals(tconst: Option[String],
                     ordering: String,
                     nconst: String,
                     category: String,
                     job: Option[String],
                     characters: Option[String])

@javax.inject.Singleton
class PrincipalsRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * Parse a Company from a ResultSet
    */
  private[models] val simple = {
    get[Option[String]]("title_principals.tconst") ~
      get[String]("title_principals.ordering") ~
      get[String]("title_principals.nconst") ~
      get[String]("title_principals.category") ~
      get[Option[String]]("title_principals.job") ~
      get[Option[String]]("title_principals.characters")map {
      case tconst ~ ordering ~ nconst ~ category ~ job ~ characters =>
        Principals(tconst, ordering, nconst, category, job, characters)
    }
  }

  //principalsService.findPrincipalsById(tconst).foreach(println)
  def findPrincipalsById(tconst: String): Future[List[Principals]] = Future {
    println("mpika stin findPrincipals")
    db.withConnection { implicit connection =>
      SQL"select * from title_principals where tconst = $tconst".as(simple.*)
    }
  }(ec)
}