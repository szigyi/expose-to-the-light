package hu.szigyi.ettl.v1.service

import java.time.Instant

import hu.szigyi.ettl.v1.influx.InfluxDomain.ToCaptureDomain
import hu.szigyi.ettl.v1.influx.InfluxDomain.ToCaptureDomain._

object TickService {

  def ticking(id: String, intervalSeconds: Int, count: Int, startAt: Instant): Seq[ToCaptureDomain] = {
    var copyStart = startAt.minusSeconds(intervalSeconds)
    (0 until count).map(index => {
      copyStart = copyStart.plusSeconds(intervalSeconds)
      ToCaptureDomain(copyStart, id, index)
    })
  }
}
