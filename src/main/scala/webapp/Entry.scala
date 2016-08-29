package webapp

import scala.language.implicitConversions

import org.scalajs.dom.raw.HTMLImageElement

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, JSExport}
import org.scalajs.dom
import org.scalajs.dom.html
import org.denigma.threejs._
import scala.scalajs.js.Dynamic.literal
import scala.scalajs.js.native

import Pumpings._
import ThreeJSTypings._
import ThreeJSTypings.NTexture._
//import ThreeJSTypings.NTextureFilter._

object Pumpings{
  //https://en.wikipedia.org/wiki/Radian
  //http://stackoverflow.com/questions/15994194/how-to-convert-x-y-coordinates-to-an-angle
  implicit class CirclePump(x: Double) {
    def radianToDeg = x * 180 / js.Math.PI
    def detToRadian = x * js.Math.PI / 180
    def degMulWithPI = x/360 * 2 * js.Math.PI
  }
}

object Resouces {
  val image_0 = "/classes/image_0.png"
  val image_1 = "/classes/image_1.png"
  val image_2 = "/classes/image_2.png"
  val logo_image = "/classes/logo.png"
}

/*
todo:
 */
@JSExport
object Entry {
  var render: WebGLRenderer = null
  var scene: Scene = null
  var camera: Camera = null

  //webapp.Entry().main();
  @JSExport
  def main(_canvas: html.Canvas, width: Double, height: Double, dpr: Double): Unit = {
    println("this is first output line")
    render = Helpers.createRender(_canvas, width, height, dpr)
    scene = new Scene()
    dom.window.asInstanceOf[js.Dynamic].scene = scene // export to window.scene , for three.js inspector work
    dom.window.asInstanceOf[js.Dynamic].redraw = redraw _
    camera = new PerspectiveCamera(45, width/height, 1, 200)
    camera.position.y = 150
    camera.lookAt(new Vector3(0, 0, 0))
    scene.add(camera)


    val light = new DirectionalLight(0xffffff, 0.7)
    light.position.set(0, 0, 10)
    scene.add(light)

    /*

    var ambientLight = new THREE.AmbientLight(0x555555);
    scene.add(ambientLight);
     */

//    val cube = new Mesh(new BoxGeometry(1, 2, 3), new MeshBasicMaterial(literal("color"->0xff0000).asInstanceOf[MeshBasicMaterialParameters]))
//    scene.add(cube)
    val firstCircle = Helpers.createDashedCircle(20, 100)
    scene.add(firstCircle)

    scene.add(Helpers.createDashedCircle(40, 100))
    scene.add(Helpers.createDashedCircle(70, 100))
    val lastCircle = Helpers.createDashedCircle(120, 100)
    val lastBall = createBall(20, 30)
    lastBall.name = "lastBall"
    scene.add(lastCircle)
    scene.add(lastBall)

    render.render(scene, camera)

    val loader = new TextureLoader()
    loadPanels(loader)

    // load logo
    val onLogoload = (texture: Texture) => {
      texture.anisotropy =  16
      val ratio = 328.0/612
      val width = 60

      val geometry = new PlaneGeometry(width, width*ratio)
      val material = new MeshPhongMaterial(
        literal(
          "color"->0xffffff,
          "map"->texture,
          "transparent"->true,//set to transparent, to avoid black on margins of this image
          "opacity"->1.0,
          "emissive"->0x222222
        ).asInstanceOf[MeshPhongMaterialParameters])
      val plane = new Mesh( geometry, material )
      plane.position.y = 10
      scene.add( plane )
      render.render(scene, camera)

    }
    loader.load(Resouces.logo_image, onLogoload)


//    val pointLight = new PointLight(0xff0000, 1, 100)
//    pointLight.position.set(0, 0, 0)
//    scene.add(pointLight)
//
//    val pointLightHelper = new PointLightHelper(pointLight, 1)
//    scene.add(pointLightHelper)

//    animate(lastCircle)
    circleBall(lastBall, 20)


    animateCamera()
  }

  // http://stackoverflow.com/questions/30245990/how-to-merge-two-geometries-or-meshes-using-three-js-r71
  def createBall(radius: Double, deg: Double):Mesh = {
    val ball = new SphereGeometry(1, 10, 10)
    val material = new MeshPhongMaterial(
      literal(
        "color"->0xffffff,
        "emissive"->0x666666,
        "transparent"->true,//set to transparent, to avoid black on margins of this image
        "opacity"->0.7
      ).asInstanceOf[MeshPhongMaterialParameters])

    val result = new Mesh(ball, material)
    val degnumber = deg.degMulWithPI
    result.position.x = js.Math.cos(degnumber) * radius
    result.position.y = js.Math.sin(degnumber) * radius
    result.position.z = 0

    return result
  }

  def animate(target: Object3D): Unit ={
    dom.window.requestAnimationFrame{_:Double=>
      animate(target)
    }

    target.rotation.z = js.Date.now() * 0.00005
    redraw()
  }

  def animateCamera():Unit = {
    var deg = 0.0
    val toDeg = 90
    val radius = 150
    val speed = 0.3
    var handler = 0

    def startj():Unit = {
      if ( Math.floor(deg) >= toDeg){
        dom.window.cancelAnimationFrame(handler)
        camera.position.z = 150
        camera.position.y = 0
        camera.lookAt(new Vector3(0, 0, 0))
        redraw()
      }else{
        handler = dom.window.requestAnimationFrame{_:Double=>startj()}
      }
      deg += speed
      camera.position.y = js.Math.cos(deg.degMulWithPI) * radius
      camera.position.z = js.Math.sin(deg.degMulWithPI) * radius
      camera.lookAt(new Vector3(0, 0, 0))
      redraw()
      ()
    }

    startj()
  }

  def redraw():Unit = {
    render.render(scene, camera)
  }

  def circleBall(mesh: Mesh, radiusToCenter: Double): Unit ={
    dom.window.requestAnimationFrame {_:Double =>
      circleBall(mesh, radiusToCenter)
    }
    var deg = js.Math.atan2(mesh.position.y, mesh.position.x).radianToDeg
    deg = (deg-0.1 + 360)%360
    val theta = deg.degMulWithPI
    mesh.position.x = js.Math.cos(theta) * radiusToCenter
    mesh.position.y = js.Math.sin(theta) * radiusToCenter

    render.render(scene, camera)
  }

  def loadPanel(loader: TextureLoader, src: String)( cb: (Mesh)=>Unit ): Unit ={
    val onload = (texture: Texture) => {
      //      texture.magFilter = NThree.NearestMipMapLinearFilter.asInstanceOf[TextureFilter]
      //      texture.minFilter = NThree.NearestMipMapLinearFilter.asInstanceOf[TextureFilter]
      //      println("max is: ", render.getMaxAnisotropy())
      texture.anisotropy =  16
      val ratio = 630.0/1398
      val width = 100

      val geometry = new PlaneGeometry(width, width*ratio)
      val material = new MeshPhongMaterial(
        literal(
          "color"->0xffffff,
          "map"->texture,
          "transparent"->true,//set to transparent, to avoid black on margins of this image
          "opacity"->1.0,
          "emissive"->0x222222
        ).asInstanceOf[MeshPhongMaterialParameters])
      val plane = new Mesh( geometry, material )
      plane.position.y = -40
      cb(plane)
      scene.add( plane )
      render.render(scene, camera)

    }
    loader.load(Resouces.image_1, onload)
  }

  def loadPanels(loader: TextureLoader):Unit = {
    loadPanel(loader, Resouces.image_1){ mesh=>
      mesh.position.z = 3
    }

    loadPanel(loader, Resouces.image_0){ mesh: Mesh=>
      mesh.position.x = -30
      mesh.position.y = -55
      mesh.position.z = 2
    }

    loadPanel(loader, Resouces.image_0){ mesh: Mesh=>
      mesh.position.x = 30
      mesh.position.y = -55
      mesh.position.z = 2
    }
  }
}




object ThreeJSTypings {
  @js.native
  @JSName("THREE.LineSegments")
  class LineSegments extends Object3D {
    def this(geometry: Geometry = js.native, material: LineMaterial = js.native, `type`: Double = js.native) = this()
  }


  type NTextureFilter = Double
  object NTextureFilter{
    implicit def morph(target: NTextureFilter):TextureFilter = target.asInstanceOf[TextureFilter]
  }

  @js.native
  @JSName("THREE")
  object NThree extends js.Object{
    val NearestMipMapLinearFilter:NTextureFilter = js.native

  }

  @js.native
  trait NWebGLRenderer extends WebGLRenderer{
    def setPixelRatio(value: Double): Unit = js.native
  }

  object NWebGLRenderer{
    implicit def morph(target: WebGLRenderer):NWebGLRenderer = target.asInstanceOf[NWebGLRenderer]

    implicit def morphBack(target: NWebGLRenderer): WebGLRenderer = target.asInstanceOf[WebGLRenderer]
  }

  @js.native
  trait NTexture extends Texture{
  }

  object NTexture {
    implicit def morph(target: Texture):NTexture = target.asInstanceOf[NTexture]

    implicit def morphBack(target: NTexture): Texture = target.asInstanceOf[Texture]
  }
}

object Helpers {
  import ThreeJSTypings.NWebGLRenderer._

  def createStraightDashedCircle(radius: Double, segmentCount: Int = 32): LineSegments = {

    val geometry = new Geometry()
    val material = new LineDashedMaterial(
      literal(
        "color"->0xffffff, "dashSize"->3, "gapSize"->0.5, "linewidth"->2).asInstanceOf[LineDashedMaterialParameters])


    for (n <- 0 to segmentCount){
      val theta = (n * 1.0 /segmentCount) * js.Math.PI * 2

      val vector = new Vector3(js.Math.cos(theta)*radius, js.Math.sin(theta)*radius, 0)
//      println(s"${js.Math.cos(theta)*radius}, ${js.Math.cos(theta)*radius}, ${theta}, ${n}")
      geometry.vertices.push(vector)
    }

    return new LineSegments(geometry, material)
  }

  def createDashedCircle(radius: Double, segmentCount: Int = 100, gapSize: Double = 6, dashSize: Double = 6) = {
    val curvePath = new CurvePath()

//    var lastTheta = 0.0
//    var theta = 0.0

    val curve = new EllipseCurve(0, 0, radius, radius, 0, 359*js.Math.PI*2, false)
//    for (n <- 0 until segmentCount){
//      lastTheta = theta
//      theta = (n * 1.0/segmentCount) * js.Math.PI * 2
//      val ax = js.Math.cos(theta) * radius
//      val ay = js.Math.sin(theta) * radius
//
//
////      val path = new Path(curve.getPoint(50))
////      val geometry = path.createPointsGeometry(50)
//
//      curvePath.add(curve)
//
//    }


//    val material = new LineBasicMaterial(literal("color"-> 0xffffff).asInstanceOf[LineBasicMaterialParameters])
    // dashSize: 3, gapSize: 1

    curvePath.add(curve)
    curvePath.closePath()
    val material = new LineDashedMaterial(
      literal(
        "gapSize"->6, "dashSize"->6, "color"->0x398aff, "transparent"->true, "opacity"->0.7
      ).asInstanceOf[LineDashedMaterialParameters])
    val geometry = curvePath.createPointsGeometry(segmentCount)
    new LineSegments(geometry, material)

  }


  def createCirclePanel(radius: Double): Mesh ={
      val geometry = new CircleGeometry(radius, 32)
  //    val material = new LineDashedMaterial(literal().asInstanceOf[LineDashedMaterialParameters])
      val material = new MeshBasicMaterial(
        literal("wireframe"->true, "wireframeLinecap" -> 1).asInstanceOf[MeshBasicMaterialParameters])
      val circle = new Mesh( geometry, material )

      return circle
  }



  def createRender(canvas: html.Canvas, width: Double, height: Double, ratio: Double): WebGLRenderer ={
    val render = new WebGLRenderer(
      literal("canvas"->canvas, "antialias"->true).asInstanceOf[WebGLRendererParameters])
    render.setClearColor(new Color(0x006dfe))
    render.setPixelRatio(ratio)
    /*
    Resizes the output canvas to (width, height) with device pixel ratio taken into account,
    and also sets the viewport to fit that size, starting in (0, 0).
    Setting updateStyle to true adds explicit pixel units to the output canvas style.
     */
    render.setSize(width, height)
    return render
  }
}
