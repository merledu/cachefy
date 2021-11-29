// See README.md for license details.

package gcd

import chisel3._
import chisel3.tester._
import org.scalatest.FreeSpec
import chisel3.experimental.BundleLiterals._
import chiseltest.internal.VerilatorBackendAnnotation
import chiseltest.experimental.TestOptionBuilder._

class CacheMemTest extends FreeSpec with ChiselScalatestTester {
  "Cache Mem Test" in {
    test(new CacheMem).withAnnotations(Seq(VerilatorBackendAnnotation)){ c=>
            c.io.adr.poke(12.U)
            c.io.wr_en.poke(true.B)
            c.io.data_in.poke(4.U)

            c.clock.step(10)
    }
  }
}
