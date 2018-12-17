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
                 genres: String)

object Title {
  implicit def toParameters: ToParameterList[Title] =
    Macro.toParameters[Title]
}

/**
  * Helper for pagination.
  */
case class Page2[A](items: Seq[A], page: Int, offset: Long, total: Long) {
  lazy val prev = Option(page - 1).filter(_ >= 0)
  lazy val next = Option(page + 1).filter(_ => (offset + items.size) < total)
}

@javax.inject.Singleton
class TitleRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  // -- Parsers

  /**
    * Parse a Computer from a ResultSet
    */
  private val simple1 = {
    get[Option[String]]("title_basics.tconst") ~
      get[String]("title_basics.titleType") ~
      get[String]("title_basics.primaryTitle") ~
      get[String]("title_basics.originalTitle") ~
      get[String]("title_basics.titleType") ~
      get[String]("title_basics.startYear") ~
      get[Option[String]]("title_basics.endYear") ~
      get[Option[String]]("title_basics.runtimeMinutes") ~
      get[String]("title_basics.genres")map {
      case tconst ~ titleType ~ primaryTitle ~ originalTitle ~ isAdult ~ startYear ~ endYear ~ runtimeMinutes ~ genres =>
        Title(tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, endYear, runtimeMinutes, genres)
    }
  }

  // -- Queries

  /**
    * Retrieve a title from the identifier.
    */
  def findById(tconst: String): Future[Option[Title]] = Future {
    db.withConnection { implicit connection =>
      SQL"select * from title_basics where tconst = $tconst".as(simple1.singleOpt)
    }
  }(ec)

  /**
    * Return a page of (Computer,Company).
    *
    * @param page Page to display
    * @param pageSize Number of computers per page
    * @param orderBy Computer property used for sorting
    * @param filter Filter applied on the name column
    */
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filterPrimary: String = "%", filterOriginal: String = "%"): Future[Page2[(Title)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val titles = SQL"""
        select * from title_basics
        where title_basics.primaryTitle like ${filterPrimary}
        order by ${orderBy} nulls last
        limit ${pageSize} offset ${offset}
      """.as(simple1.*)

      val totalRows = SQL"""
        select count(*) from title_basics
        where title_basics.primaryTitle like ${filterPrimary}
      """.as(scalar[Long].single)

      Page2(titles, page, offset, totalRows)
    }
  }(ec)


  /**
    * Construct the Seq[(String,String)] needed to fill a select options set.
    *
    * Uses `SqlQueryResult.fold` from Anorm streaming,
    * to accumulate the rows as an options list.
    */
  def options: Future[Seq[(String,String)]] = Future(db.withConnection { implicit connection =>
    SQL"select * from title_basics order by primaryTitle".
      fold(Seq.empty[(String, String)], ColumnAliaser.empty) { (acc, row) => // Anorm streaming
        row.as(simple1) match {
          case Failure(parseErr) => {
            println(s"Fails to parse $row: $parseErr")
            acc
          }

          case Success(Title(Some(tconst), name, _, _, _, _, _, _, _)) =>
            (tconst.toString -> name) +: acc

          case Success(Title(None, _, _, _, _, _, _, _, _)) => acc
        }
      }
  }).flatMap {
    case Left(err :: _) => Future.failed(err)
    case Left(_) => Future(Seq.empty)
    case Right(acc) => Future.successful(acc.reverse)
  }

}