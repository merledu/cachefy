package chisel_caches.caches

import chisel3._ 
import chisel3.util._ 
import jigsaw.rams.fpga.BlockRamWithMasking
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class DMCacheWrapper[A <: AbstrRequest, B <: AbstrResponse]
                    (mainMem:BlockRamWithMasking) extends Module {
    val io = IO(new Bundle{
        val req = Flipped(Decoupled(gen))
        val rsp = Decoupled(gen1)
    })

    val cache = Module(new DMCache(32,1024, mainMem))

    val validReg = RegInit(false.B)

    io.rsp.valid := validReg
    io.rsp.bits.error := false.B
    io.req.ready := true.B // assuming we are always ready to accept requests from device

    when(io.req.fire() && !io.req.bits.isWrite) {
    // READ
    cache.io.adr := io.req.bits.addrRequest
    cache.io.wr_en := false.B
    cache.io.data_in := 0.U
    io.rsp.bits.dataResponse := cache.io.data_out 
    validReg := true.B
  } .elsewhen(io.req.fire() && io.req.bits.isWrite) {
    // WRITE
    cache.io.adr := io.req.bits.addrRequest
    cache.io.wr_en := true.B
    cache.io.data_in := io.req.bits.dataRequest
    validReg := true.B
    io.rsp.bits.dataResponse := DontCare
  } .otherwise {
    cache.io.adr := 0.U
    cache.io.wr_en := false.B
    cache.io.data_in := 0.U
    validReg := false.B
    io.rsp.bits.dataResponse := DontCare
  }

}