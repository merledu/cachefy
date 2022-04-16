package ccache.caches

import chisel3._ 
import chisel3.util._ 
import jigsaw.rams.fpga.BlockRamWithMasking
import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class DMCacheWrapper2[A <: AbstrRequest, B <: AbstrResponse]
                    (cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int)(gen: A, gen1: B) extends Module {
    val io = IO(new Bundle{

      val reqIn :         DecoupledIO[A]      =      Flipped(Decoupled(gen ))
      val rspOut:         DecoupledIO[B]      =              Decoupled(gen1)
      val reqOut:         DecoupledIO[A]      =              Decoupled(gen )
      val rspIn :         DecoupledIO[B]      =      Flipped(Decoupled(gen1))

    })

  //TODO: MAKE ROWS AND COLS DYNAMIC

    val cacheRows: Int = math.pow(2,cacheAddrWidth).toInt
    
    val cache_valid: SyncReadMem[Bool] = SyncReadMem(cacheRows, Bool())    // VALID
    val cache_tags: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

    val startCaching: Bool = RegInit(false.B)

    val idle :: caching : wait_for_dmem :: cache_refill :: Nil = Enum(4)
    val state     = RegInit(idle)

    val reqSaver  = RegInit(0.U.asTypeOf(Flipped(Decoupled(gen ))))

    val indexBits = RegInit(0.U)
    val tagBits   = RegInit(0.U)

    when(state === idle){
        state        := Mux(io.reqIn.valid, caching, idle)
        startCaching := io.reqIn.valid
        reqSaver := io.reqIn
    }.elsewhen(state === caching){

    }