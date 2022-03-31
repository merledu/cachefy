package ccache.caches

import org.scalatest.FreeSpec
import chisel3._ 
import chiseltest._
import chiseltest.ChiselScalatestTester
import chiseltest.internal.VerilatorBackendAnnotation
import chiseltest.experimental.TestOptionBuilder._


class DMCacheTest extends FreeSpec with ChiselScalatestTester {
    "Direct Mapped Cache Test" in {
        test(new DMCache(2,16,32)).withAnnotations(Seq(VerilatorBackendAnnotation)){ c =>
            c.io.adr.poke(4.U)
            c.io.wr_en.poke(true.B)
            c.io.data_in.poke(20.U)
            c.clock.step(2)

            c.io.adr.poke(4.U)
            c.io.wr_en.poke(false.B)
            c.io.data_in.poke(0.U)
            c.clock.step(2)

            c.io.adr.poke(4.U)
            c.io.wr_en.poke(false.B)
            c.io.data_in.poke(0.U)
            c.clock.step(2)
        }
    }
}