package caches

import chisel3._ 
import chisel3.util._ 

class DMCache extends Module {
    val io = IO(new Bundle{
        val adr = Input(UInt(4.W))
        val wr_en = Input(Bool())
        val data_in = Input(UInt(32.W))
        val data_out = Output(UInt(32.W))
        val miss = Output(Bool())
    })

    val mem = SyncReadMem(16, UInt(32.W))
    val cache_valid = SyncReadMem(4, Bool())    // VALID
    val cache_tags = SyncReadMem(4,UInt(2.W))   // TAGS
    val cache_data = SyncReadMem(4,UInt(32.W))  // DATA

    for(i <- 0 to 3){
        cache_valid.write(i.U(2.W),false.B)
    }

    val data = WireInit(0.U(32.W))

    io.miss := true.B

    when(io.wr_en === true.B){         // write req
        mem.write(io.adr, io.data_in)
        io.miss := true.B
    }.otherwise{

        // CACHE HIT
        when(cache_valid.read(io.adr(1,0)) && cache_tags.read(io.adr(1,0)) === io.adr(3,2)){
                
                io.data_out := cache_data(io.adr(1,0))
                io.miss := false.B

        // CACHE MISS
        }.otherwise{

                
                data := mem.read(io.adr)
                cache_valid.write(io.adr(1,0), true.B)
                cache_tags.write(io.adr(1,0), io.adr(3,2))
                cache_data.write(io.adr(1,0), data)
                io.miss := true.B
                
        }
    }


    io.data_out := data

}