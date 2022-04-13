package ccache.caches

import chisel3._ 
import chisel3.util._ 
// import math
import jigsaw.rams.fpga.BlockRamWithMasking

class DMCache(cacheAddrWidth:Int, dataAddrWidth:Int, dataWidth:Int) extends Module {
    val io = IO(new Bundle{

        val adr        = Input(UInt(dataAddrWidth.W))
        val wr_en      = Input(Bool())
        val data_in    = Input(UInt(dataWidth.W))
        val data_out   = Output(UInt(dataWidth.W))
        val miss       = Output(Bool())
        
    })
    
    val cacheRows: Int = math.pow(2,cacheAddrWidth).toInt
    
    val cache_valid : SyncReadMem[Bool] = SyncReadMem(cacheRows, Bool())    // VALID
    val cache_tags  : SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt((dataAddrWidth - cacheAddrWidth).W))   // TAGS
    val cache_data  : SyncReadMem[UInt] = SyncReadMem(cacheRows,UInt(dataWidth.W))  // DATA

    val indexBits: UInt = io.adr(cacheAddrWidth-1,0)
    val tagBits  : UInt = io.adr(dataAddrWidth-1,cacheAddrWidth)


    val data = WireInit(0.U(dataWidth.W))
    val miss = WireInit(true.B)

    val takeRequest :: throwResponse :: Nil = Enum(2)
    val state = RegInit(takeRequest)

    val dataReg    = RegInit(0.U)
    val tagsReg    = RegInit(0.U)
    val validReg   = RegInit(false.B)
    val tagBitsFwd = RegInit(0.U)
    val datainFwd  = RegInit(0.U)
    val wrEnFwd    = RegInit(false.B)


    when(wrEnFwd === true.B){         // write req

        miss := true.B
        cache_valid(indexBits) := true.B
        cache_tags(indexBits) := tagBitsFwd
        cache_data(indexBits) := datainFwd

    }.otherwise{

    // CACHE HIT
        when(validReg && tagsReg === tagBitsFwd){
                
                data := dataReg
                miss := false.B

        // CACHE MISS
        }.otherwise{

                miss := true.B
                
        }
    }

    dataReg      := cache_data.read(indexBits, true.B)
    tagsReg      := cache_tags.read(indexBits, true.B)
    validReg     := cache_valid.read(indexBits, true.B)
    tagBitsFwd   := tagBits
    datainFwd    := io.data_in
    wrEnFwd      := io.wr_en

    io.data_out  := data
    io.miss      := miss

}