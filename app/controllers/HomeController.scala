package controllers

import javax.inject.Inject

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.i18n._
import play.api.mvc._
import views._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Manage a database of computers
  */
class HomeController @Inject()(titleService: TitleRepository,
                               ratingService: RatingRepository,
                               crewService: CrewRepository,
                               principalsService: PrincipalsRepository,
                               cc: MessagesControllerComponents,
                               )(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  private val logger = play.api.Logger(this.getClass)
  println("Hello, world!")
  logger.trace("mpika mesa stin list tis main")

  /**
    * This result directly redirect to the application home.
    */
  val Home = Redirect(routes.HomeController.list(0, 2, ""))

  // -- Actions

  /**
    * Handle default path requests, redirect to computers list
    */
  def index = Action {
    Home
  }

  /**
    * Display the paginated list of titles.
    *
    * @param page    Current page number (starts from 0)
    * @param orderBy Column to be sorted
    * @param filterPrimary  Filter applied on primary title entries
    * @param filterOriginal  Filter applied on original title entries
    */
  def list(page: Int, orderBy: Int, filterPrimary: String, filterOriginal: String) = Action.async { implicit request =>
    titleService.list(page = page, orderBy = orderBy, filterPrimary = ("%" + filterPrimary + "%"), filterOriginal = ("%" + filterOriginal + "%")).map { page =>
      println("page",page)
      logger.trace("mpika mesa stin list tis main")
      Ok(html.list(page, orderBy, filterPrimary, filterOriginal))
    }
  }


  val titleForm = Form(
    mapping(
      "tconst" -> ignored(None: Option[String]),
      "titleType" -> nonEmptyText,
      "primaryTitle" -> nonEmptyText,
      "originalTitle" -> nonEmptyText,
      "isAdult" -> nonEmptyText,
      "startYear" -> nonEmptyText,
      "endYear" -> optional(text),
      "runtimeMinutes" -> optional(text),
      "genres" -> optional(text)
    )(Title.apply)(Title.unapply)
  )

  val ratingForm = Form(
    mapping(
      "tconst" -> ignored(None: Option[String]),
      "averageRating" -> nonEmptyText,
      "numVotes" -> nonEmptyText
    )(Rating.apply)(Rating.unapply)
  )

  val crewForm = Form(
    mapping(
      "tconst" -> ignored(None: Option[String]),
      "directors" -> nonEmptyText,
      "writers" -> optional(text)
    )(Crew.apply)(Crew.unapply)
  )

  val principalsForm = Form(
    mapping(
      "tconst" -> ignored(None: Option[String]),
      "ordering" -> nonEmptyText,
      "nconst" -> nonEmptyText,
      "category" -> nonEmptyText,
      "job" -> optional(text),
      "characters" -> optional(text)
    )(Principals.apply)(Principals.unapply)
  )

  def view(tconst: String) = Action.async { implicit request =>
    println("mpika stin view")
    titleService.findById(tconst).flatMap {
      case Some(title) =>
        println("mpika stin view title")
        ratingService.findRatingById(tconst).flatMap {
          case Some(rating) =>
            crewService.findCrewById(tconst).flatMap {
              case Some(crew) =>
                println("mpika stin view 2")
                titleService.options(tconst).map { options =>
                  println("mpika stin view 3")
                  Ok(html.view(tconst, titleForm.fill(title), ratingForm.fill(rating), crewForm.fill(crew)))
                }
              case other =>
                println("dn vrika crew")
                Future.successful(NotFound)
            }
          case other =>
            println("dn vrika rating")
            Future.successful(NotFound)
        }

      case other =>
        println("dn vrika title")
        Future.successful(NotFound)
    }
  }
}
