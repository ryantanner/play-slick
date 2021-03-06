package play.api.db.slick

import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.db._
import play.api.http._
import play.api.mvc.Results._
import com.jolbox.bonecp.BoneCPDataSource
import scala.slick.driver.H2Driver
import play.api.mvc._
import scala.concurrent.Await
import java.util.concurrent.TimeUnit
import play.api.libs.iteratee.Enumerator
import play.api.libs.iteratee.Iteratee
import play.api.Application

class TestableDBActionSpec extends Specification {
  
  Class.forName("org.h2.Driver")
  val datasource = new BoneCPDataSource
  datasource.setJdbcUrl("jdbc:h2:mem:play")
  datasource.setUsername("sa")
  datasource.setPassword("")
  
  val driver = H2Driver
  val database = new Database("test", datasource, driver)
  val testDBAction = new DBAction(database)
  import driver.simple._
  
  val Ids = new Table[Int]("IDS") {
    def id = column[Int]("id")
    def * = id
  }
  
  "DBAction" should {
    "be instantiable without a Play Application" in {
      val ids = List(1, 2, 3)
      database.withSession { implicit session: Session =>
        Ids.ddl.create
        Ids.*.insertAll(ids: _*)
      }

      val listAction = testDBAction { implicit rs =>
        Ok(Query(Ids).list.mkString(" "))
      }

      val result = listAction(FakeRequest())

      status(result) must equalTo(OK)
      contentAsString(result) must contain(ids.mkString(" "))
    }
  }
}