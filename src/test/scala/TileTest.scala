import chisel3._
import chisel3.util._
import chiseltest._
import chiseltest.experimental.TestOptionBuilder._
import chiseltest.internal.VerilatorBackendAnnotation

import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig, TilelinkAdapter}
import directmapped.DMCache

class TileTest extends FreeSpec with ChiselScalatestTester {
  "Tile" in {
    implicit val config = CacheConfig(32, 32, 4)
    implicit val busConfig = TilelinkConfig()
    test(new Tile[TLRequest, TLResponse, TilelinkAdapter](new Core(), new DMCache())).withAnnotations(Seq(VerilatorBackendAnnotation)) { c =>
      c.io.req.poke(0.U.asTypeOf(new AbstrRequest))
      c.io.rsp.expect(0.U.asTypeOf(new AbstrResponse))
    }
  }
}