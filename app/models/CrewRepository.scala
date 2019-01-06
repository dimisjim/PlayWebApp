package models

import javax.inject.Inject

import anorm._
import anorm.SqlParser.{ get, str }

import play.api.db.DBApi

import scala.concurrent.Future

case class Crew(tconst: Option[String],
                  directors: String,
                  writers: Option[String])

@javax.inject.Singleton
class CrewRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * Parse a Company from a ResultSet
    */
  private[models] val simple = {
    get[Option[String]]("title_crew.tconst") ~
      get[String]("title_crew.directors") ~
      get[Option[String]]("title_crew.writers")map {
      case tconst ~ directors ~ writers =>
        Crew(tconst, directors, writers)
    }
  }

  def findCrewById(tconst: String): Future[Option[Crew]] = Future {
    println("mpika stin findRating")
    db.withConnection { implicit connection =>
      SQL"select * from title_crew where tconst = $tconst".as(simple.singleOpt)
    }
  }(ec)
}