// package cachefy.caches

// import chisel3._ 
// import chisel3.util._ 
// import jigsaw.rams.fpga.BlockRamWithMasking
// import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}
// import caravan.bus.tilelink.{TilelinkMaster, channelEBundle, TilelinkSlave, OpCodes, TilelinkConfig}

// class DMCache[A <: AbstrRequest, B <: AbstrResponse]
//                     (cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int)(gen: A, gen1: B)(implicit val config:TilelinkConfig) extends Module with OpCodes {
//     val io = IO(new Bundle{

//       val tlMasterReceiver = Flipped(Decoupled(new TilelinkMaster()))
//       val tlSlaveTransmitter = Decoupled(new TilelinkSlave())

//       val tlMasterTransmitter = Decoupled(new TilelinkMaster)
//       val tlSlaveReceiver    = Flipped(Decoupled(new TilelinkSlave))

//       // val tlAckTransmitter     = Decoupled(new channelEBundle)

//     })

//   //TODO: MAKE ROWS AND COLS DYNAMIC

//     val cacheRows: Int = math.pow(2,cacheAddrWidth).toInt
    
//     val cache_valid: Mem[Bool] = Mem(cacheRows, Bool())    // VALID
//     val cache_tags: Mem[UInt] = Mem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
//     val cache_data: Mem[UInt] = Mem(cacheRows,UInt(dataWidth.W))  // DATA

//     val startCaching: Bool = RegInit(false.B)

//     val idle :: caching :: wait_for_dmem :: Nil = Enum(3)
//     val state     = RegInit(idle)

//     val reqSaver  = RegInit(0.U.asTypeOf(new TilelinkMaster))
//     val rspGiver  = RegInit(0.U)

//     val currentCacheValid = RegInit(false.B)
//     val currentCacheTags  = RegInit(0.U)

//     val indexBits = RegInit(0.U)
//     val tagBits   = RegInit(0.U)

//     val miss = WireInit(false.B)

//     val pipedValid = Wire(Valid(UInt(1.W)))
//     val hello = WireInit(indexBits)
//     dontTouch(hello)

//     // io.tlAckTransmitter.bits.e_sink := 0.U
//     // io.tlAckTransmitter.valid := false.B 

//     // dontTouch(cache_valid)

//     io.tlMasterTransmitter.bits <> 0.U.asTypeOf(new TilelinkMaster)
//     io.tlMasterTransmitter.valid := false.B
//     // pipedValid <> 0.U.asTypeOf(Valid(UInt(1.W)))
//     io.tlSlaveTransmitter.bits <> 0.U.asTypeOf(new TilelinkSlave)
//     io.tlSlaveTransmitter.valid := false.B
//     pipedValid.bits := 0.U
//     pipedValid.valid := false.B

//     io.tlMasterReceiver.ready := true.B
//     io.tlSlaveReceiver.ready := false.B

//     dontTouch(rspGiver)

//     // rspGiver <> 0.U.asTypeOf(gen1)

//     when(state === idle){

//       switch(io.tlMasterReceiver.valid){
//         is(true.B){
//           state := caching
//           reqSaver := io.tlMasterReceiver.bits
//           currentCacheTags := cache_tags.read(io.tlMasterReceiver.bits.a_address(cacheAddrWidth-1,0))
//           currentCacheValid := cache_valid.read(io.tlMasterReceiver.bits.a_address(cacheAddrWidth-1,0))
//           indexBits := io.tlMasterReceiver.bits.a_address(cacheAddrWidth-1,0)
//           tagBits := io.tlMasterReceiver.bits.a_address(dataAddrWidth-1,cacheAddrWidth)
//         }
//         is(false.B){
//           state := idle
//         }
//       }
//         // state        := Mux(io.reqIn.valid, caching, idle)
//         // startCaching := io.reqIn.valid
//         // reqSaver := io.reqIn.bits
//         // currentCacheTags := cache_tags.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0), true.B)
//         // currentCacheValid := cache_valid.read(io.reqIn.bits.addrRequest(cacheAddrWidth-1,0), true.B)
//         // indexBits := io.reqIn.bits.addrRequest(cacheAddrWidth-1,0)
//         // tagBits := io.reqIn.bits.addrRequest(dataAddrWidth-1,cacheAddrWidth)

//     }.elsewhen(state === caching){

//       switch(reqSaver.a_opcode === PutFullData.U || reqSaver.a_opcode === PutPartialData.U){
//         is(true.B){

//           // send write through req to DMEM
//           io.tlMasterTransmitter.bits.a_opcode  := PutFullData.U 
//           io.tlMasterTransmitter.bits.a_address := reqSaver.a_address
//           io.tlMasterTransmitter.bits.a_data    := reqSaver.a_data
//           io.tlMasterTransmitter.bits.a_mask    := reqSaver.a_mask

//           io.tlMasterTransmitter.valid := true.B

//           // Filling up cache
//           cache_data(indexBits) := reqSaver.a_data
//           cache_tags(indexBits) := tagBits
//           cache_valid(indexBits) := true.B

//           io.tlSlaveTransmitter.valid := true.B
//           state := idle

//         }
//         is(false.B){

//           miss := ~(currentCacheTags === tagBits && currentCacheValid)

//           switch(miss){
//             is(true.B){

//               io.tlMasterTransmitter.bits.a_opcode   := Acquire.U
//               io.tlMasterTransmitter.bits.a_address  := reqSaver.a_address
//               io.tlMasterTransmitter.bits.a_data     := reqSaver.a_data
//               io.tlMasterTransmitter.bits.a_mask     := reqSaver.a_mask

//               io.tlMasterTransmitter.valid := true.B

//               cache_tags(indexBits) := tagBits
//               cache_valid(indexBits) := true.B

//               state := wait_for_dmem

//             }
//             is(false.B){
//                 // cache_data(indexBits) := Mux(io.rspIn.valid, io.rspIn.bits.dataResponse, 0.U)
      

//               // state := Mux(io.rspIn.valid, idle, wait_for_dmem)
//                       io.tlSlaveTransmitter.bits.d_data := cache_data.read(indexBits)
                      
//                       rspGiver := cache_data.read(indexBits)
//                       // pipedValid.valid := true.B
//                       // pipedValid.bits  := 1.U
//                       // val pipeVal = Pipe(pipedValid)
//                       // io.rspOut.valid := pipeVal.bits.asBool
//                       io.tlSlaveTransmitter.valid := true.B
//                       state := idle
//             }
//           }

//         }
//       }
    

//     }.elsewhen(state === wait_for_dmem){

//       io.tlMasterReceiver.ready := false.B
//       io.tlSlaveReceiver.ready := true.B

//       switch(io.tlSlaveReceiver.valid){

//         is(true.B){
//             cache_data(indexBits) := io.tlSlaveReceiver.bits.d_data
//             io.tlSlaveTransmitter.bits.d_data := io.tlSlaveReceiver.bits.d_data
//             io.tlMasterTransmitter.valid := true.B

//             // io.tlAckTransmitter.bits.e_sink := 0.U
//             // io.tlAckTransmitter.valid := true.B

//             state := idle
//         }
//         is(false.B){
//             state := wait_for_dmem
//         }
        

//       }

//     }

//     // currentCacheValid := currentCacheValid
//     dontTouch(currentCacheValid)

// }


