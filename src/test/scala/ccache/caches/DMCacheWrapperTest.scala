package ccache.caches
import  chisel3._
import  chiseltest._
import  org.scalatest._
import  chiseltest.experimental.TestOptionBuilder._
import  chiseltest.internal.VerilatorBackendAnnotation

class DMCacheWrapperTest extends FreeSpec with ChiselScalatestTester {

  "D M C a c h e" in {
    test(new DMCacheWrapperTop).withAnnotations(Seq(VerilatorBackendAnnotation)){ c =>

      c.io.reqIn.bits.addrRequest.poke(4.U)
      c.io.reqIn.bits.dataRequest.poke(10.U)
      c.io.reqIn.bits.isWrite.poke(true.B)
      c.io.reqIn.bits.activeByteLane.poke("b1111".U)
      c.io.reqIn.valid.poke(true.B)
      c.io.rspOut.ready.poke(true.B)
      c.clock.step(5)

      c.io.reqIn.bits.addrRequest.poke(4.U)
      c.io.reqIn.bits.dataRequest.poke(0.U)
      c.io.reqIn.bits.isWrite.poke(false.B)
      c.io.reqIn.bits.activeByteLane.poke("b1111".U)
      c.io.reqIn.valid.poke(true.B)
      c.io.rspOut.ready.poke(true.B)
      c.clock.step(5)
    }
  }

}
