package caches

import chisel3._ 
import chisel3.util._ 

class DMCache(DW:Int, AW:Int) extends Module {
    val io = IO(new Bundle{
        val adr = Input(UInt(AW.W))
        val wr_en = Input(Bool())
        val data_in = Input(UInt(DW.W))
        val data_out = Output(UInt(DW.W))
        val miss = Output(Bool())
    })

    val cache_width = log2Ceil(AW)
    val cache_half_width = cache_width/2

    val mem = SyncReadMem(cache_width, UInt(32.W))
    val cache_valid = SyncReadMem(cache_width, Bool())    // VALID
    val cache_tags = SyncReadMem(cache_width,UInt(2.W))   // TAGS
    val cache_data = SyncReadMem(cache_width,UInt(32.W))  // DATA

    for(i <- 0 to cache_width.toInt-1){
        cache_valid.write(i.U(log2Ceil(cache_width).W),false.B)
    }

    val data = WireInit(0.U(32.W))

    io.miss := true.B

    when(io.wr_en === true.B){         // write req
        mem.write(io.adr, io.data_in)
        io.miss := true.B
    }.otherwise{

        // CACHE HIT
        when(cache_valid.read(io.adr(cache_half_width,0)) && cache_tags.read(io.adr(cache_half_width,0)) === io.adr(cache_width-1,cache_width-2)){
                
                io.data_out := cache_data(io.adr(cache_half_width,0))
                io.miss := false.B

        // CACHE MISS
        }.otherwise{

                
                data := mem.read(io.adr)
                cache_valid.write(io.adr(cache_half_width,0), true.B)
                cache_tags.write(io.adr(cache_half_width,0), io.adr(cache_width-1,cache_width-2))
                cache_data.write(io.adr(cache_half_width,0), data)
                io.miss := true.B
                
        }
    }


    io.data_out := data

}