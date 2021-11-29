package gcd

import chisel3._

class CacheMem extends Module {
  val io = IO(new Bundle {
        val adr = Input(UInt(10.W))
        val wr_en = Input(Bool())
        val data_in = Input(UInt(32.W))
        val data_out = Output(UInt(32.W))
  })

    val mem = SyncReadMem(1024, UInt(32.W))
    val cache_ind = SyncReadMem(16, Bool())
    val cache_tags = SyncReadMem(16,UInt(6.W))
    val cache_data = SyncReadMem(16,UInt(32.W))

    for(i <- 0 to 16){
        cache_ind(i.U) := false.B
        // cache_ind(i.U).bits := 0.U
    }

    val data = WireInit(0.U(32.W))

    when(io.wr_en === true.B){
        mem(io.adr) := io.data_in
    }.otherwise{

        when(cache_ind(io.adr(3,0)) === true.B & cache_tags(io.adr(3,0)) === io.adr(9,4)){
                data := cache_data(io.adr(3,0))
        }.otherwise{
                data := mem(io.adr)
                cache_ind(io.adr(3,0)) := true.B
                cache_tags(io.adr(3,0)) := io.adr(9,4)
                cache_data(io.adr(3,0)) := data
        }
    }

    



    io.data_out := data
}
