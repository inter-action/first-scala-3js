package webapp

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.html
import org.denigma.threejs._
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.native


@JSExport
object Entry {
  //webapp.Entry().main();
  @JSExport
  def main(_canvas: html.Canvas): Unit = {
    println("this is first output line")

    val render = new WebGLRenderer(literal("canvas"->_canvas).asInstanceOf[WebGLRendererParameters])
    render.setClearColor(new Color(0x000000))
    val scene = new Scene()
    val camera = new PerspectiveCamera(45.0, 4.0 / 3, 1.0, 1000)
    camera.position.set(0f, 0f, 5f)
    scene.add(camera)

    val cube = new Mesh(new BoxGeometry(1f, 2f, 3f), new MeshBasicMaterial(literal("color"->0xff0000).asInstanceOf[MeshBasicMaterialParameters]))
    scene.add(cube)
    render.render(scene, camera)

  }
}
