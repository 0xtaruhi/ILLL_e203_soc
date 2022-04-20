package vj.util

import chisel3._
import chisel3.util._
import scala.math

class SqrtIntIO(val w: Int) extends Bundle {
  require (w % 2 == 0)
  val en        = Input(Bool())
  val radical   = Input(UInt(w.W))
  val ready     = Output(Bool())
  val root      = Output(UInt((w/2).W))
  val remainder = Output(UInt((w/2+1).W))
}

class SqrtInt(val w: Int) extends Module {
  require (w % 2 == 0)
  val io = IO(new SqrtIntIO(w))
  val comp_src  = Reg(UInt((w/2+2).W))
  val comp_des  = Reg(UInt((w/2+2).W))
  val cur_bit   = Reg(UInt(log2Ceil(w).W))
  val radical_r = Reg(UInt(w.W))
  val res       = Reg(UInt((w/2).W))
  val remainder = Reg(UInt((w/2+1).W))

  // control signals 
  val start = io.en & ~RegNext(io.en)
  val ready = RegNext(cur_bit === 0.U)
  io.ready := ready
  val processing = RegInit(false.B)
  val comp_ge = comp_src >= comp_des

  when (start) {
    processing := true.B
    comp_src := io.radical >> ((w-2).U) & 3.U
  } .elsewhen (ready) {
    processing := false.B
  } .otherwise {
    processing := processing
  }

  radical_r := Mux(start, io.radical, radical_r)
  // cur_bit is 1-bit higher than current fetching bits' index
  cur_bit := Mux(start, w.U, Mux(processing, cur_bit - 2.U, w.U))

  when (processing) {
    when (comp_ge) {
      comp_src := (comp_src - comp_des) << 2.U | radical_r >> (cur_bit-2.U) & 3.U
    }.otherwise {
      comp_src := comp_src << 2.U | radical_r >> (cur_bit-2.U) & 3.U
    }
  }.elsewhen (ready) { 
    comp_src := comp_src - Mux(comp_ge, comp_des, 0.U)
  }.elsewhen (start) { comp_src := 0.U }

  val nxt_res = res << 1.U | comp_ge
  comp_des := Mux(start, 1.U, nxt_res << 2.U | 1.U)
  res := Mux(start, 0.U, Mux(processing, nxt_res, 0.U))
  remainder := Mux(processing, comp_src - comp_des, 0.U)
  io.root := res
  io.remainder := remainder
}
