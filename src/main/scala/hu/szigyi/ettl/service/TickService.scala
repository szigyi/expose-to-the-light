package hu.szigyi.ettl.service

import java.time.Instant

import hu.szigyi.ettl.client.influx.InfluxDomain.ToCaptureDomain
import hu.szigyi.ettl.client.influx.InfluxDomain.ToCaptureDomain._

object TickService {

  def ticking(id: String, intervalSeconds: Int, count: Int, startAt: Instant): Seq[ToCaptureDomain] = {
    var copyStart = startAt.minusSeconds(intervalSeconds)
    (0 until count).map(index => {
      copyStart = copyStart.plusSeconds(intervalSeconds)
      ToCaptureDomain(copyStart, id, index)
    })
  }
}
