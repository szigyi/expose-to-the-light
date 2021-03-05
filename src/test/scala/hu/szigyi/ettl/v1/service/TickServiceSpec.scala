package hu.szigyi.ettl.v1.service

import java.time.Instant
import java.util.UUID

import hu.szigyi.ettl.v1.influx.InfluxDomain.ToCaptureDomain
import hu.szigyi.ettl.v1.influx.InfluxDomain.ToCaptureDomain._
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class TickServiceSpec extends AnyFunSuite with Matchers {

  test("generate ticks") {
    val id = UUID.randomUUID().toString
    val intervalSeconds  = 10
    val count = 3
    val start = Instant.parse("2020-07-26T12:00:00Z")

    val res = TickService.ticking(id, intervalSeconds, count, start)

    res shouldBe Seq(
      ToCaptureDomain(start, id, 0),
      ToCaptureDomain(start.plusSeconds(intervalSeconds), id, 1),
      ToCaptureDomain(start.plusSeconds(2 * intervalSeconds), id, 2),
    )
  }
}
