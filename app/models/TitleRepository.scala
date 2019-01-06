package models

import java.util.Date

import javax.inject.Inject
import anorm.SqlParser.{get, scalar, str}
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future
import scala.util.{ Failure, Success }

case class Title(tconst: Option[String],
                 titleType: String,
                 primaryTitle: String,
                 originalTitle: String,
                 isAdult: String,
                 startYear: String,
                 endYear: Option[String],
                 runtimeMinutes: Option[String],
                 genres: Option[String])

object Title {
  implicit def toParameters: ToParameterList[Title] =
    Macro.toParameters[Title]
}

/**
  * Helper for pagination.
  */
case class Page[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

@javax.inject.Singleton
class TitleRepository @Inject()(dbapi: DBApi, ratingRepository: RatingRepository, principalsRepository: PrincipalsRepository)(implicit ec: DatabaseExecutionContext) {

  private val logger = play.api.Logger(this.getClass)
  private val db = dbapi.database("default")

  private val simple = {
    get[Option[String]]("title_basics.tconst") ~
      get[String]("title_basics.titleType") ~
      get[String]("title_basics.primaryTitle") ~
      get[String]("title_basics.originalTitle") ~
      get[String]("title_basics.isAdult") ~
      get[String]("title_basics.startYear") ~
      get[Option[String]]("title_basics.endYear") ~
      get[Option[String]]("title_basics.runtimeMinutes") ~
      get[Option[String]]("title_basics.genres")map {
      case tconst ~ titleType ~ primaryTitle ~ originalTitle ~ isAdult ~ startYear ~ endYear ~ runtimeMinutes ~ genres =>
        Title(tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, endYear, runtimeMinutes, genres)
    }
  }

  private val withRating = simple ~ (ratingRepository.simple.?) map {
    case title_basics ~ title_ratings => title_basics -> title_ratings
  }

  def findById(tconst: String): Future[Option[Title]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from title_basics where tconst = $tconst".as(simple.singleOpt)
    }
  }(ec)


  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterPrimary: String = "%", filterOriginal: String = "%"): Future[Page[(Title, Option[Rating])]] = Future {
    logger.trace("mpika kai kanw query twra")
    val offset = pageSize * page

    db.withConnection { implicit connection =>
      logger.trace("mpika kai kanw query twra")
      val titles = SQL"""
        select * from title_basics
        left join title_ratings on title_basics.tconst = title_ratings.tconst
        where title_basics.primaryTitle like ${filterPrimary}
        order by ${orderBy} nulls last
        limit ${pageSize} offset ${offset}
      """.as(withRating.*)

      println("title",titles)

      val totalRows = SQL"""
        select count(*) from title_basics
        left join title_ratings on title_basics.tconst = title_ratings.tconst
        where title_basics.primaryTitle like ${filterPrimary}
      """.as(scalar[Long].single)
      println("totalRows",totalRows)
      Page(titles, page, offset, totalRows)
    }
  }(ec)


  def options(tconst: String): Future[Seq[(String,String)]] = Future(db.withConnection { implicit connection =>
    println(tconst)
    println("mpika stin options")
    SQL"select * from title_basics where title_basics.tconst like ${tconst}".
      fold(Seq.empty[(String, String)], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        println("mpika stin options fold")
        row.as(simple) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }

          case Success(Title(Some(tconst), name, _, _, _, _, _, _, _)) =>
            println("mpika stin options success")
            (tconst.toString -> name) +: acc

          case Success(Title(None, _, _, _, _, _, _, _, _)) =>
            println("mpika stin options success 2")
            acc
        }
      }
  }).flatMap {
    case Left(err :: _) =>
      println("mpika stin options flatmap 1")
      Future.failed(err)
    case Left(_) =>
      println("mpika stin options flatmap 2")
      Future(Seq.empty)
    case Right(acc) =>
      println("mpika stin options flatmap 3")
      Future.successful(acc.reverse)
  }

}