package ccache.caches

import chisel3._ 
import chisel3.util._ 
// import math
import jigsaw.rams.fpga.BlockRamWithMasking

class DMCache(cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int) extends Module {
    val io = IO(new Bundle{
        val adr = Input(UInt(dataAddrWidth.W))
        val wr_en = Input(Bool())
        val data_in = Input(UInt(dataWidth.W))
        val data_out = Output(UInt(dataWidth.W))
        val miss = Output(Bool())
    })

   val cacheRows = (math.pow(2,cacheAddrWidth)).toInt
    
    // val mem = Module(mainMem) //SyncReadMem(cache_width, UInt(32.W))
    val cache_valid = SyncReadMem(cacheRows, Bool())    // VALID
    val cache_tags = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

    for(i <- 0 to cacheRows.toInt-1){
        cache_valid.write(i.U(cacheAddrWidth.W),false.B)
    }

    val data = WireInit(0.U(dataWidth.W))
    val miss = WireInit(true.B)


    when(io.wr_en === true.B){         // write req
        miss := true.B
        cache_valid(io.adr(cacheAddrWidth, 0)) := true.B
        cache_tags(io.adr(cacheAddrWidth, 0)) := io.adr(dataAddrWidth-1,cacheAddrWidth+1)
        cache_data(io.adr(cacheAddrWidth, 0)) := io.data_in
    }.otherwise{

        // CACHE HIT
        when(cache_valid.read(io.adr(cacheAddrWidth,0)) && cache_tags.read(io.adr(cacheAddrWidth,0)) === io.adr(dataAddrWidth-1,cacheAddrWidth+1)){
                
                data := cache_data(io.adr(cacheAddrWidth,0))
                miss := false.B

        // CACHE MISS
        }.otherwise{

                // TO BE IMPLEMENTED
                miss := true.B
                
        }
    }


    io.data_out := data
    io.miss := miss

}