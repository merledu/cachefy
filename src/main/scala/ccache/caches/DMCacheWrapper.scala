// package cachefy.caches

// import chisel3._ 
// import chisel3.util._ 
// import jigsaw.rams.fpga.BlockRamWithMasking
// import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

// class DMCacheWrapper[A <: AbstrRequest, B <: AbstrResponse]
//                     (cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int)(gen: A, gen1: B) extends Module {
//     val io = IO(new Bundle{

//       val reqIn :         DecoupledIO[A]      =      Flipped(Decoupled(gen ))
//       val rspOut:         DecoupledIO[B]      =              Decoupled(gen1)
//       val reqOut:         DecoupledIO[A]      =              Decoupled(gen )
//       val rspIn :         DecoupledIO[B]      =      Flipped(Decoupled(gen1))

//     })

//   //TODO: MAKE ROWS AND COLS DYNAMIC

//     val cacheRows: Int = math.pow(2,cacheAddrWidth).toInt
    
//     val cache_valid: SyncReadMem[Bool] = SyncReadMem(cacheRows, Bool())    // VALID
//     val cache_tags: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
//     val cache_data: SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

//     val startCaching: Bool = RegInit(false.B)



//     val validReg: Bool = RegInit(false.B)
//     val dataReg: UInt = RegInit(0.U)
//     val addrReg: UInt = RegInit(0.U)
//     val miss = WireInit(false.B)
//     val addrSaver = RegInit(io.reqIn.bits.addrRequest)
//     val dataSaver = RegInit(io.reqIn.bits.dataRequest)
//     val funcSaver = RegInit(io.reqIn.bits.isWrite)

//     val idle :: cache_read :: cache_write :: wait_for_dmem :: cache_refill :: Nil = Enum(5)
//     val state: UInt = RegInit(idle)

//     val indexBits: UInt = addrSaver(cacheAddrWidth-1,0)
//     val tagBits: UInt = addrSaver(dataAddrWidth-1,cacheAddrWidth)

//     val  cond = WireInit(false.B)
//     val valuu = Wire(UInt((dataAddrWidth - cacheAddrWidth).W))

//     val currentTag = RegInit(cache_tags.read(indexBits, true.B))
//     val currentValid = RegInit(cache_valid.read(indexBits, true.B))
    
//     currentTag := cache_tags.read(indexBits, true.B)
//     currentValid := cache_valid.read(indexBits, true.B)

//     dontTouch(currentTag)
//     dontTouch(currentValid)

//     def fire() = io.reqIn.valid
//     dontTouch(io.reqIn.ready)
//     cond := cache_valid.read(indexBits, true.B) && cache_tags.read(indexBits, true.B) === tagBits
//     valuu := cache_tags.read(indexBits, true.B)
//     dontTouch(cond)
//     dontTouch(valuu)
//     when(startCaching) {
//         when(fire() && !io.reqIn.bits.isWrite) {
//             // READ


            
//             when(cache_valid.read(indexBits) && cache_tags.read(indexBits) === tagBits) {
//                 // CACHE HIT
//                 dataReg := cache_data(indexBits)
//                 validReg := true.B
//                 miss := false.B
//             }.otherwise {
//                 // CACHE MISS
//                 cache_valid(indexBits) := true.B
//                 cache_tags(indexBits) := tagBits
//                 // cache_data(indexBits) := // data Mem ka data
//                 addrReg := indexBits
//                 miss := true.B
//                 // state := wait_for_dmem
//             }

//         }.elsewhen(fire() && io.reqIn.bits.isWrite) {
//             // WRITE -- MISS

//             // TODO: WRITE INTO CACHE (NO WRITE MISSES)
//             cache_valid(indexBits) := true.B
//             cache_tags(indexBits) := tagBits
//             cache_data(indexBits) := dataSaver
//             validReg := true.B
//             dataReg := dataSaver

//         }
//     }

//         io.reqIn.ready := false.B
//         io.rspIn.ready := false.B

//         io.rspOut.valid := validReg
//         io.rspOut.bits.error := false.B
//         // io.reqIn.ready := true.B // assuming we are always ready to accept requests from device

//         io.rspOut.bits.dataResponse := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, dataReg)
//         // io.reqOut.bits <> io.reqIn.bits
//         io.reqOut.bits.addrRequest := addrSaver
//         io.reqOut.bits.dataRequest := dataSaver
//         io.reqOut.bits.isWrite := funcSaver
//         io.reqOut.bits.activeByteLane := "b1111".U
//         io.reqOut.valid := miss

//         when(state === idle){

//             io.reqIn.ready := true.B
//             state := Mux(io.reqIn.valid, Mux(io.reqIn.bits.isWrite, cache_write, cache_read), idle)
//             startCaching := Mux(io.reqIn.valid, true.B, false.B)
//             validReg := false.B

//         }.elsewhen(state === cache_read || state === cache_write){

//             state := Mux(miss,wait_for_dmem, idle)

//         }.elsewhen(state === wait_for_dmem){

//             io.rspIn.ready := true.B
//             state := Mux(io.rspIn.valid, cache_refill, wait_for_dmem)

//         }.elsewhen(state === cache_refill){

//             cache_data(addrReg) := io.rspIn.bits.dataResponse
//             dataReg := io.rspIn.bits.dataResponse
//             state := idle

//         }

//     // delaying the response by 1 clk cycle
//     val vvalid = Wire(Valid(UInt(1.W)))
//     vvalid.valid := true.B
//     vvalid.bits := validReg.asUInt
//     val pipedVal = Pipe(vvalid)
//     io.rspOut.valid :=Mux(state === cache_read, pipedVal.bits.asBool, validReg) 
//     io.rspOut.bits.error := false.B
    
//     io.rspOut.bits.dataResponse := dataReg

//     addrSaver := io.reqIn.bits.addrRequest
//     dataSaver := io.reqIn.bits.dataRequest
//     funcSaver := io.reqIn.bits.isWrite

//     when(startCaching && fire && io.reqIn.bits.isWrite){
//         io.reqOut.bits.addrRequest := addrSaver
//         io.reqOut.bits.dataRequest := dataSaver
//         io.reqOut.bits.isWrite := true.B
//         io.reqOut.bits.activeByteLane := "b1111".U 
//         io.reqOut.valid := true.B
//     }

// }