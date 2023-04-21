import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation
import org.scalatest.FreeSpec

import caravan.bus.common.{DummyCore}
import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkAdapter}
import directmapped.DMCache
import common.CacheConfig

class TileTest extends FreeSpec with ChiselScalatestTester {
  "Tile" in {
    implicit val config = CacheConfig(8, 32, 8, 32)
    implicit val busConfig = TilelinkConfig()
    test(new Tile(new DummyCore(4), new TLRequest(), new TLResponse(), new TilelinkAdapter())).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      // c.io.req.poke(0.U.asTypeOf(new AbstrRequest))
      // c.io.rsp.expect(0.U.asTypeOf(new AbstrResponse))
      c.clock.step(10)
      c.io.rsp.poke(0.U.asTypeOf(Decoupled(new TLResponse)))
    }
  }
}