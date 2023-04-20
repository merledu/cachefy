package common

import chisel3._
import chisel3.util._

import caravan.bus.common.{AbstrRequest, AbstrResponse, BusConfig}

class CacheReq (implicit val config: CacheConfig) extends AbstrRequest{
  override val addrRequest: UInt = UInt(config.addrWidth.W)
  override val dataRequest: UInt = UInt(config.dataWidth.W)
  override val isWrite: Bool = Bool()
  override val activeByteLane: UInt = UInt((config.dataWidth/8).W)
}

class CacheRsp (implicit val config: CacheConfig) extends AbstrResponse{
  override val dataResponse: UInt = UInt(config.dataWidth.W)
  override val error: Bool = Bool()
  val hit: Bool = Bool()
}

class CacheBundle (implicit val config: CacheConfig) extends Bundle {
  val req = Flipped(Decoupled(new CacheReq()))
  val rsp = Decoupled(new CacheRsp())
}