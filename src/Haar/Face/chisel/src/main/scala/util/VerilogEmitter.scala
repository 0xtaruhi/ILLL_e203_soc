package vj.util

import chisel3._
import chisel3.util._
import chisel3.stage._

object VerilogEmitter extends App {
  println("Generaing the Verilog code")
  (new chisel3.stage.ChiselStage).execute(Array(
    "--target-dir", 
    "generated"
  ),
  Seq(ChiselGeneratorAnnotation(() => 
    new SqrtInt(26))))
}
