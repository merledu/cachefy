// package caches

// import org.scalatest.FreeSpec
// import chisel3._ 
// import chiseltest._
// import chiseltest.ChiselScalatestTester
// import chiseltest.internal.VerilatorBackendAnnotation
// import chiseltest.experimental.TestOptionBuilder._


// class DMCacheTest extends FreeSpec with ChiselScalatestTester {
//     "Index Test" in {
//         test(new DMCache).withAnnotations(Seq(VerilatorBackendAnnotation)){ c =>
//             c.io.adr.poke(12.U)
//             c.io.wr_en.poke(true.B)
//             c.io.data_in.poke(4.U)
//             c.clock.step(10)
//         }
//     }
// }