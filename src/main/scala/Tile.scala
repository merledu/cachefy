import chisel3._
import chisel3.util._

import common._
import directmapped.DMCache
import caravan.bus.common.{AbstrRequest, AbstrResponse, AbstractAdapter, AbstractCore, BusConfig}

class Tile[A <:AbstrRequest, B <:AbstrResponse, C <:AbstractAdapter[A,B], D <: AbstractCore[A,B]](val core_ : D,val request: A,val response:B,val adapter:C )(implicit val config: CacheConfig, implicit val busConfig: BusConfig) extends Module {

  val io = IO(new Bundle {
    val req   = Decoupled(request)            // request goes out to the SoC
    val rsp   = Flipped(Decoupled(response))   // response comes back from the SoC
  })

  val icache = Module(new DMCache)
  val dcache = Module(new DMCache)

  val icache_adapter = Module(adapter)
  val dcache_adapter = Module(adapter)

  val core = Module(core_)

  // core <-> icache_adapter <-> icache
  icache_adapter.io.reqIn <> core.io.imemReq
  core.io.imemRsp <> icache_adapter.io.rspOut
  icache.io.req <> icache_adapter.io.reqOut
  icache_adapter.io.rspIn <> icache.io.rsp.asTypeOf(response)

  // core <-> dcache_adapter <-> dcache
  dcache_adapter.io.reqIn <> core.io.dmemReq
  core.io.dmemRsp <> dcache_adapter.io.rspOut
  dcache.io.req <> dcache_adapter.io.reqOut
  dcache_adapter.io.rspIn <> dcache.io.rsp.asTypeOf(response)
 

  
}