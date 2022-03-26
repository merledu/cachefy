package chisel_caches.caches

import chisel3._ 
import chisel3.util._ 
import jigsaw.rams.fpga.BlockRamWithMasking
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class DMCacheWrapper[A <: AbstrRequest, B <: AbstrResponse]
                    (gen: A, gen1: B) extends Module {
    val io = IO(new Bundle{
        val reqIn = Flipped(Decoupled(gen))
        val rspOut = Decoupled(gen1)
        val reqOut = Flipped(Decoupled(gen))
        val rspIn = Decoupled(gen1)
    })

  //TODO: MAKE ROWS AND COLS DYNAMIC
    val cache = Module(new DMCache(10,32,32/*, mainMem*/))
    val validReg = RegInit(false.B)
    val dataReg = RegInit(0.U)
    
    when(io.reqIn.fire() && !io.reqIn.bits.isWrite) {
      // READ
      cache.io.adr := io.reqIn.bits.addrRequest
      cache.io.wr_en := false.B
      cache.io.data_in := 0.U
      dataReg := cache.io.data_out 
      validReg := true.B
    } .elsewhen(io.reqIn.fire() && io.reqIn.bits.isWrite) {
      // WRITE
      cache.io.adr := io.reqIn.bits.addrRequest
      cache.io.wr_en := true.B
      cache.io.data_in := io.reqIn.bits.dataRequest
      validReg := true.B
      // io.rspOut.bits.dataResponse := DontCare
    } .otherwise {
      cache.io.adr := 0.U
      cache.io.wr_en := false.B
      cache.io.data_in := 0.U
      validReg := false.B
      // io.rspOut.bits.dataResponse := DontCare
    }

    io.rspOut.valid := validReg
    io.rspOut.bits.error := false.B
    io.reqIn.ready := true.B // assuming we are always ready to accept requests from device

    io.rspOut.bits.dataResponse := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, dataReg)


    io.reqOut.bits <> io.reqIn.bits
    io.reqOut.valid := ~cache.io.miss

}