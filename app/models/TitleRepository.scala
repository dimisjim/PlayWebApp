package models

import java.util.Date

import javax.inject.Inject
import anorm.SqlParser.{get, scalar, str}
import anorm._
import play.api.db.DBApi

import scala.concurrent.Future

case class Title(tconst: Option[String],
                 titleType: Option[String],
                 primaryTitle: Option[String],
                 originalTitle: Option[String],
                 isAdult: Option[String],
                 startYear: Option[String],
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
      get[Option[String]]("title_basics.titleType") ~
      get[Option[String]]("title_basics.primaryTitle") ~
      get[Option[String]]("title_basics.originalTitle") ~
      get[Option[String]]("title_basics.titleType") ~
      get[Option[String]]("title_basics.startYear") ~
      get[Option[String]]("title_basics.endYear") ~
      get[Option[String]]("title_basics.runtimeMinutes") ~
      get[Option[String]]("title_basics.genres")map {
      case tconst ~ titleType ~ primaryTitle ~ originalTitle ~ isAdult ~ startYear ~ endYear ~ runtimeMinutes ~ genres =>
        Title(tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, endYear, runtimeMinutes, genres)
    }
  }

//  private[models] val simple2 = {
//    get[Option[Long]]("company.id") ~ str("company.name") map {
//      case id ~ name => Title(tconst, titleType, primaryTitle, originalTitle, isAdult, startYear, endYear, runtimeMinutes, genres)
//    }
//  }

  // -- Queries

  /**
    * Retrieve a computer from the id.
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
  def list(page: Int = 0, pageSize: Int = 10, orderBy: Int = 1, filter: String = "%"): Future[Page2[(Title)]] = Future {

    val offset = pageSize * page

    db.withConnection { implicit connection =>

      val titles = SQL"""
        select * from title_basics
        where title_basics.primaryTitle like ${filter}
        order by ${orderBy} nulls last
        limit ${pageSize} offset ${offset}
      """.as(simple1.*)

      val totalRows = SQL"""
        select count(*) from title_basics
        where title_basics.primaryTitle like ${filter}
      """.as(scalar[Long].single)

      Page2(titles, page, offset, totalRows)
    }
  }(ec)

}