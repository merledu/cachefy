// package caches

// import chisel3._ 
// import chisel3.util._ 

// class DMCache extends Module {
//     val io = IO(new Bundle{
//         val adr = Input(UInt(2.W))
//         val wr_en = Input(Bool())
//         val data_in = Input(UInt(32.W))
//         val data_out = Output(UInt(32.W))
//     })

//     // val mem = SyncReadMem(1024, UInt(32.W))
//     // val cache_ind = SyncReadMem(16, Valid(UInt(4.W)))

//     // for(i <- 0 to 16){
//     //     cache_ind(i).valid := false.B
//     // }

//     io.data_out := 0.U

// }