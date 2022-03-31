package ccache.caches

import chisel3._ 
import chisel3.util._ 
import jigsaw.rams.fpga.BlockRamWithMasking
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class DMCacheWrapper[A <: AbstrRequest, B <: AbstrResponse]
                    (cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int)(gen: A, gen1: B) extends Module {
    val io = IO(new Bundle{
      val reqIn:          DecoupledIO[A]      =      Flipped(Decoupled(gen))
      val rspOut:       DecoupledIO[B]      =      Decoupled(gen1)
      val reqOut:       DecoupledIO[A]      =     Decoupled(gen)
      val rspIn:          DecoupledIO[B]      =     Flipped(Decoupled(gen1))
    })

  //TODO: MAKE ROWS AND COLS DYNAMIC
    // val cache = Module(new DMCache(10,32,32/*, mainMem*/))

    val cacheRows: Int = math.pow(2,cacheAddrWidth).toInt
    val indexBits: UInt = io.reqIn.bits.addrRequest(cacheAddrWidth,0)
    val tagBits: UInt = io.reqIn.bits.addrRequest(dataAddrWidth-1,cacheAddrWidth+1)

    val cache_valid: SyncReadMem[Bool] = SyncReadMem(cacheRows, Bool())    // VALID
    val cache_tags: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

    val startCaching: Bool = RegInit(false.B)

    for(i <- 0 until cacheRows){
        cache_valid.write(i.U(cacheAddrWidth.W),false.B)
    }


    val validReg: Bool = RegInit(false.B)
    val dataReg: UInt = RegInit(0.U)
    val addrReg: UInt = RegInit(0.U)
    val miss = WireInit(false.B)

    val idle :: caching :: wait_for_dmem :: cache_refill :: Nil = Enum(4)
    val state: UInt = RegInit(idle)


    when(startCaching) {
        when(io.reqIn.fire() && !io.reqIn.bits.isWrite) {
            // READ


            when(cache_valid.read(indexBits) && cache_tags.read(indexBits) === tagBits) {
                // CACHE HIT
                dataReg := cache_data(indexBits)
                validReg := true.B
            }.otherwise {
                // CACHE MISS
                cache_valid(indexBits) := true.B
                cache_tags(indexBits) := tagBits
                // cache_data(indexBits) := // data Mem ka data
                addrReg := indexBits
                miss := true.B
                // state := wait_for_dmem
            }

        }.elsewhen(io.reqIn.fire() && io.reqIn.bits.isWrite) {
            // WRITE -- MISS

            // TODO: WRITE INTO CACHE (NO WRITE MISSES)
            cache_valid(indexBits) := true.B
            cache_tags(indexBits) := tagBits
            cache_data(indexBits) := io.reqIn.bits.dataRequest
            validReg := true.B
            dataReg := io.reqIn.bits.dataRequest
        }
    }

        io.reqIn.ready := false.B
        io.rspIn.ready := false.B

        io.rspOut.valid := validReg
        io.rspOut.bits.error := false.B
        // io.reqIn.ready := true.B // assuming we are always ready to accept requests from device

        io.rspOut.bits.dataResponse := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, dataReg)


        io.reqOut.bits <> io.reqIn.bits
        io.reqOut.valid := miss

        when(state === idle){
            io.reqIn.ready := true.B
            state := Mux(io.reqIn.valid, caching, idle)
            startCaching := Mux(io.reqIn.valid, true.B, false.B)
        }.elsewhen(state === caching){

            state := Mux(miss,wait_for_dmem, idle)
        }.elsewhen(state === wait_for_dmem){
            io.rspIn.ready := true.B
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