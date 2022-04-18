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
    
    val cache_valid: Mem[Bool] = Mem(cacheRows, Bool())    // VALID
    val cache_tags: Mem[UInt] = Mem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data: Mem[UInt] = Mem(cacheRows,UInt(dataWidth.W))  // DATA

    val startCaching: Bool = RegInit(false.B)

    val idle :: caching :: wait_for_dmem :: Nil = Enum(3)
    val state     = RegInit(idle)

    val reqSaver  = RegInit(0.U.asTypeOf(gen))
    val rspGiver  = RegInit(0.U)

    val currentCacheValid = RegInit(false.B)
    val currentCacheTags  = RegInit(0.U)

    val indexBits = RegInit(0.U)
    val tagBits   = RegInit(0.U)

    val miss = WireInit(false.B)

    val pipedValid = Wire(Valid(UInt(1.W)))
    val hello = WireInit(indexBits)
    dontTouch(hello)

    // dontTouch(cache_valid)

    io.reqOut.bits <> 0.U.asTypeOf(gen)
    io.reqOut.valid := false.B
    // pipedValid <> 0.U.asTypeOf(Valid(UInt(1.W)))
    io.rspOut.bits <> 0.U.asTypeOf(gen1)
    io.rspOut.valid := false.B
    pipedValid.bits := 0.U
    pipedValid.valid := false.B

    io.reqIn.ready := true.B
    io.rspIn.ready := false.B

    dontTouch(rspGiver)

    // rspGiver <> 0.U.asTypeOf(gen1)

    when(state === idle){

      switch(io.reqIn.valid){
        is(true.B){
          state := caching
          reqSaver := io.reqIn.bits
          currentCacheTags := cache_tags.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0))
          currentCacheValid := cache_valid.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0))
          indexBits := io.reqIn.bits.addrRequest(cacheAddrWidth-1,0)
          tagBits := io.reqIn.bits.addrRequest(dataAddrWidth-1,cacheAddrWidth)
        }
        is(false.B){
          state := idle
        }
      }
        // state        := Mux(io.reqIn.valid, caching, idle)
        // startCaching := io.reqIn.valid
        // reqSaver := io.reqIn.bits
        // currentCacheTags := cache_tags.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0), true.B)
        // currentCacheValid := cache_valid.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0), true.B)
        // indexBits := io.reqIn.bits.addrRequest(cacheAddrWidth-1,0)
        // tagBits := io.reqIn.bits.addrRequest(dataAddrWidth-1,cacheAddrWidth)

    }.elsewhen(state === caching){

      switch(reqSaver.isWrite){
        is(true.B){

          // send write through req to DMEM
          io.reqOut.bits <> reqSaver
          io.reqOut.valid := true.B

          // Filling up cache
          cache_data(indexBits) := reqSaver.dataRequest
          cache_tags(indexBits) := tagBits
          cache_valid(indexBits) := true.B

          io.rspOut.valid := true.B
          state := idle

        }
        is(false.B){

          miss := ~(currentCacheTags === tagBits && currentCacheValid)

          switch(miss){
            is(true.B){

              io.reqOut.bits <> reqSaver
              io.reqOut.valid := true.B

              cache_tags(indexBits) := tagBits
              cache_valid(indexBits) := true.B

              state := wait_for_dmem

            }
            is(false.B){
                // cache_data(indexBits) := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, 0.U)
      

              // state := Mux(io.rspIn.valid, idle, wait_for_dmem)
                      io.rspOut.bits.dataResponse := cache_data.read(indexBits)
                      
                      rspGiver := cache_data.read(indexBits)
                      // pipedValid.valid := true.B
                      // pipedValid.bits  := 1.U
                      // val pipeVal = Pipe(pipedValid)
                      // io.rspOut.valid := pipeVal.bits.asBool
                      io.rspOut.valid := true.B
                      state := idle
            }
          }

        }
      }
    

    }.elsewhen(state === wait_for_dmem){

      io.reqIn.ready := false.B
      io.rspIn.ready := true.B

      switch(io.rspIn.valid){

        is(true.B){
            cache_data(indexBits) := io.rspIn.bits.dataResponse
            io.rspOut.bits.dataResponse := io.rspIn.bits.dataResponse
            io.rspOut.valid := true.B
            state := idle
        }
        is(false.B){
            state := wait_for_dmem
        }
        

      }

    }

    // currentCacheValid := currentCacheValid
    dontTouch(currentCacheValid)

}


