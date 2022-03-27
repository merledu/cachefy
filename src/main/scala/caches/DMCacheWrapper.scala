package chisel_caches.caches

import chisel3._ 
import chisel3.util._ 
import jigsaw.rams.fpga.BlockRamWithMasking
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class DMCacheWrapper[A <: AbstrRequest, B <: AbstrResponse]
                    (cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int)(gen: A, gen1: B) extends Module {
    val io = IO(new Bundle{
        val reqIn = Flipped(Decoupled(gen))
        val rspOut = Decoupled(gen1)
        val reqOut = Flipped(Decoupled(gen))
        val rspIn = Decoupled(gen1)
    })

  //TODO: MAKE ROWS AND COLS DYNAMIC
    // val cache = Module(new DMCache(10,32,32/*, mainMem*/))

    val cacheRows = (math.pow(2,cacheAddrWidth)).toInt
    val cacheAddress = io.reqIn.bits.addrRequest(cacheAddrWidth,0)
    val tagsAddress = io.reqIn.bits.addrRequest(dataAddrWidth-1,cacheAddrWidth+1)

    val cache_valid = SyncReadMem(cacheRows, Bool())    // VALID
    val cache_tags = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

    for(i <- 0 to cacheRows.toInt-1){
        cache_valid.write(i.U(cacheAddrWidth.W),false.B)
    }


    val validReg = RegInit(false.B)
    val dataReg = RegInit(0.U)
    val addrReg = RegInit(0.U)
    val miss = WireInit(false.B)

    val idle : caching : wait_for_dmem : cache_refill : Nil = Enum(4)
    val state = RegInit(idle)   



    when(io.reqIn.fire() && !io.reqIn.bits.isWrite) {
      // READ

      
      when(cache_valid.read(cacheAddress) && cache_tags.read(cacheAddress) === tagsAddress{
        // CACHE HIT
        dataReg := cache_data(cacheAddress)
        validReg := true.B
      }.otherwise{
        // CACHE MISS
        cache_valid(cacheAddress) := true.B
        cache_tags(cacheAddress) := tagsAddress
        // cache_data(cacheAddress) := // data Mem ka data
        addrReg := cacheAddress
        miss := true.B
        // state := wait_for_dmem
      }

    } .elsewhen(io.reqIn.fire() && io.reqIn.bits.isWrite) {
      // WRITE -- MISS

      // TODO: WRITE INTO CACHE (NO WRITE MISSES)
        cache_valid(cacheAddress) := true.B
        cache_tags(cacheAddress) := tagsAddress
        cache_data(cacheAddress) := io.reqIn.bits.dataRequest
    } 

    io.reqIn.ready := false.B

    io.rspOut.valid := validReg
    io.rspOut.bits.error := false.B
    // io.reqIn.ready := true.B // assuming we are always ready to accept requests from device

    io.rspOut.bits.dataResponse := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, dataReg)


    io.reqOut.bits <> io.reqIn.bits
    io.reqOut.valid := miss

    when(state === idle){
      io.reqIn.ready := true.B
      state := Mux(miss,wait_for_dmem, idle)
    }.elsewhen(state === wait_for_dmem){
      state := Mux(io.rspIn.valid, cache_refill, wait_for_dmem)
    }.elsewhen(state === cache_refill){
      cache_data(addrReg) := io.rspIn.bits.dataResponse
      dataReg := io.rspIn.bits.dataResponse
      state := idle
    }

    io.rspOut.valid := validReg
    io.rspOut.bits.error := false.B
    // io.reqIn.ready := true.B // assuming we are always ready to accept requests from device

    io.rspOut.bits.dataResponse := dataReg




    

}