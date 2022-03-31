package ccache.caches
import caravan.bus.common.{AbstrRequest, AbstrResponse}
import caravan.bus.native.{NativeRequest, NativeResponse}
import caravan.bus.tilelink.{TLRequest, TLResponse, TilelinkConfig}
import caravan.bus.wishbone.{WBRequest, WBResponse}
import chisel3._
import chisel3.util._
import jigsaw.rams.fpga.{BlockRam, BlockRamWithMasking}
class DMCacheWrapperTop extends Module{
    implicit val config: TilelinkConfig = TilelinkConfig()
    val io = IO(new Bundle{
        val reqIn:          DecoupledIO[TLRequest]      =      Flipped(Decoupled(new TLRequest))
        val rspOut:       DecoupledIO[TLResponse]      =      Decoupled(new TLResponse())
//        val reqOut:       DecoupledIO[TLRequest]      =     Flipped(Decoupled(new TLRequest()))
//        val rspIn:          DecoupledIO[TLResponse]      =     Decoupled(new TLResponse())
    })

    val dmem: BlockRamWithMasking[_ >: WBRequest with NativeRequest with TLRequest <: AbstrRequest, _ >: WBResponse with NativeResponse with TLResponse <: AbstrResponse] = Module(BlockRam.createMaskableRAM(bus = config, rows=1024))
    val cache: DMCacheWrapper[TLRequest, TLResponse] = Module(new DMCacheWrapper(4, 10, 32)(new TLRequest(), new TLResponse()))

    cache.io.reqIn          <>              io.reqIn
    io.rspOut                  <>              cache.io.rspOut

    dmem.io.req            <>              cache.io.reqOut
    cache.io.rspIn          <>              dmem.io.rsp


}
