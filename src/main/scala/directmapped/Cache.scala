package directmapped

import chisel3._
import chisel3.util._

import common._

class DMCache(implicit val config: CacheConfig) extends Module {
  val io = IO(new CacheBundle())

  val s_idle :: s_read :: s_write :: Nil = Enum(3)
  val state = RegInit(s_idle)

  val data = Reg(Vec(config.dataRows, UInt(config.dataWidth.W)))
  val valid = RegInit(VecInit(Seq.fill(config.dataRows)(false.B)))
  val tag = Reg(Vec(config.dataRows, UInt(config.addrWidth.W)))

  val req = Reg(new CacheReq())
  val rsp = Reg(new CacheRsp())

  val hit = valid.asUInt & (tag.asUInt === req.addrRequest(config.addrWidth - 1, 0))
  val hitIdx = OHToUInt(hit)

  io.req.ready := state === s_idle
  io.rsp.valid := state === s_write
  io.rsp.bits := rsp
  io.rsp.bits.error := false.B

  switch(state) {
    is(s_idle) {
      when(io.req.valid) {
        req := io.req.bits
        rsp.dataResponse := 0.U
        // rsp.addr := req.addrRequest
        rsp.hit := false.B
        when(hit.orR) {
          state := s_read
        }.otherwise {
          state := s_write
        }
      }
    }
    is(s_read) {
      rsp.dataResponse := data(hitIdx)
      rsp.hit := true.B
      state := s_idle
    }
    is(s_write) {
      data(hitIdx) := req.dataRequest
      tag(hitIdx) := req.addrRequest(config.addrWidth - 1, 0)
      valid(hitIdx) := true.B
      state := s_idle
    }
  }
}