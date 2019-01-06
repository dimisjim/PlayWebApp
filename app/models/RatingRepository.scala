package models

import javax.inject.Inject

import anorm._
import anorm.SqlParser.{ get, str }

import play.api.db.DBApi

import scala.concurrent.Future

case class Rating(tconst: Option[String],
                  averageRating: String,
                  numVotes: String)

@javax.inject.Singleton
class RatingRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * Parse a Company from a ResultSet
    */
  private[models] val simple = {
    get[Option[String]]("title_ratings.tconst") ~
      get[String]("title_ratings.averageRating") ~
      get[String]("title_ratings.numVotes")map {
      case tconst ~ averageRating ~ numVotes =>
        Rating(tconst, averageRating, numVotes)
    }
  }

  def findRatingById(tconst: String): Future[Option[Rating]] = Future {
    println("mpika stin findRating")
    db.withConnection { implicit connection =>
      SQL"select * from title_ratings where tconst = $tconst".as(simple.singleOpt)
    }
  }(ec)
}