package examples.followmouse

import examples.followmouse._
import react.events.ImperativeEvent
import react.SignalSynt
import react.Var
import react.Signal
import macro.SignalMacro.{ SignalM => Signal }
import swing.{ Panel, MainFrame, SimpleSwingApplication }
import java.awt.{ Color, Graphics2D, Dimension }
import java.awt.Point
import scala.swing.Swing
import scala.swing.event._
import java.awt.Font


object FollowMouseStarter {
  def main(args: Array[String]) {
    val app = new FollowMouse
    app.main(args)
    while (true) {
      Thread sleep 50
      app.tick()
    }
  }
}

class FollowMouse extends SimpleSwingApplication {
  
  val Max_X = 700
  val Max_Y = 600
  val Size = 20
  val Range = 100
  
  

  val tick = new ImperativeEvent[Unit]  
  val time = tick.iterate(0.0){ acc: Double => (acc + 0.1) % (math.Pi * 2)}  
  
  val mouse = new Mouse
  val mouseX = Signal { mouse.position().getX.toInt - Size / 2 }
  val mouseY = Signal { mouse.position().getY.toInt - Size / 2 }
  
  val xOffset = Signal { math.sin(time()) * Range }
  val yOffset = Signal { math.cos(time()) * Range }
  
  val x = Signal { mouseX() + xOffset().toInt }
  val y = Signal { mouseY() + yOffset().toInt }
  
  // redraw code
  val stateChanged = mouse.position.changed || tick  
  stateChanged += { _ => frame.repaint() }

  // drawing code
  def top = frame
  val frame: MainFrame = new MainFrame {
    title = "Rotating around the mouse"
    resizable = false
    contents = new Panel() {
      listenTo(mouse.moves, mouse.clicks)

      /** forward mouse events to EScala wrapper class. Should be replaced once reactive GUI lib is complete */
      reactions += {
        case e: MouseMoved => { FollowMouse.this.mouse.mouseMovedE(e.point) }
        case e: MousePressed => FollowMouse.this.mouse.mousePressedE(e.point)
        case e: MouseDragged => { FollowMouse.this.mouse.mouseDraggedE(e.point) }
        case e: MouseReleased => FollowMouse.this.mouse.mouseReleasedE(e.point)
      }

      preferredSize = new Dimension(Max_X, Max_Y)
      val scoreFont = new Font("Tahoma", java.awt.Font.PLAIN, 32)
      override def paintComponent(g: Graphics2D) {
        g.setColor(java.awt.Color.DARK_GRAY)
        g.fillOval(x.getValue, y.getValue, Size, Size)
      }
    }
  }
}
