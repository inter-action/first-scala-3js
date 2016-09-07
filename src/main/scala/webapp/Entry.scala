package webapp

import scala.language.implicitConversions

import org.scalajs.dom.raw.{MouseEvent, HTMLImageElement}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSName, JSExport}
import org.scalajs.dom
import org.scalajs.dom.window.document
import org.scalajs.dom.window
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

// object Resouces {
//   val image_0 = "/images/index/image_0.png"
//   val image_1 = "/images/index/image_1.png"
//   val image_2 = "/images/index/image_2.png"
//   val logo_image = "/images/index/logo.png"
//   val slogan_image = "/images/index/slogan.png"
// }


object Resouces {
  val image_0 = "/classes/image_0.png"
  val image_1 = "/classes/image_1.png"
  val image_2 = "/classes/image_2.png"
  val logo_image = "/classes/logo.png"
  val slogan_image = "/classes/slogan.png"
}

/*
todo:
 */
@JSExport
object Entry {
  var render: WebGLRenderer = null
  var scene: Scene = null
  var camera: Camera = null
  var mouseX: Double = 0.0
  var mouseY: Double = 0.0
  var isMouseMoving = false

  var canvasWidth: Double = 0.0
  var canvasHeight: Double = 0.0
  var _topOffset = 0.0

  var balls: Seq[Mesh] = Seq.empty[Mesh]
  val ballRadiuses = Seq(20, 40, 70, 120)

  var logo: Mesh = null


  //webapp.Entry().main();
  @JSExport
  def main(_canvas: html.Canvas, width: Double, height: Double, dpr: Double, topOffset:Double = 72): Unit = {
    canvasWidth = width
    canvasHeight = height
    _topOffset = topOffset

    render = Helpers.createRender(_canvas, width, height, dpr)
    scene = new Scene()
    dom.window.asInstanceOf[js.Dynamic].scene = scene // export to window.scene , for three.js inspector work
    dom.window.asInstanceOf[js.Dynamic].redraw = redraw _
    camera = new PerspectiveCamera(45, width/height, 1, 200)
    camera.position.y = 150
    camera.lookAt(new Vector3(0, 0, 0))
    scene.add(camera)


    val light = new DirectionalLight(0xffffff, 0.73)
    light.position.set(0, 0, 10)
    scene.add(light)

//    val cube = new Mesh(new BoxGeometry(1, 2, 3), new MeshBasicMaterial(literal("color"->0xff0000).asInstanceOf[MeshBasicMaterialParameters]))
//    scene.add(cube)

    ballRadiuses.zipWithIndex.map(e=>{
      val i = e._2
      val circle = Helpers.createDashedCircle(ballRadiuses(i), 100)
      scene.add(circle)
      circle
    })

    balls = ballRadiuses.zipWithIndex.map(x=>{
      val i = x._2
      val ball = createBall(ballRadiuses(i), 30*i, "ball"+i)
      scene.add(ball)
      ball
    })
    render.render(scene, camera)

    val loader = new TextureLoader()
    loadPanels(loader)

    val params: (Texture)=>MeshPhongMaterialParameters = { texture=>
      literal(
        "color"->0xffffff,
        "map"->texture,
        "transparent"->true,//set to transparent, to avoid black on margins of this image
        "opacity"->1.0,
        "emissive"->0x6785D8,
        "side"->NThree.DoubleSide
      ).asInstanceOf[MeshPhongMaterialParameters]
    }

    // load slogan
    loadPanel(loader, Resouces.slogan_image, 130.0/612, 60, params){ mesh=>
      mesh.position.y = 20
      mesh.position.z = 1
    }

    loadPanel(loader, Resouces.logo_image, 118.0/102, 10, params){ mesh=>
      mesh.position.z = 1
      mesh.name = "logo"
      logo = mesh
    }


//    val pointLight = new PointLight(0xff0000, 1, 100)
//    pointLight.position.set(0, 0, 0)
//    scene.add(pointLight)
//
//    val pointLightHelper = new PointLightHelper(pointLight, 1)
//    scene.add(pointLightHelper)

//    animate(lastCircle)

    animateCamera(()=>{
      animate()
    })

//    registerMouseEvent()
  }

  // http://stackoverflow.com/questions/30245990/how-to-merge-two-geometries-or-meshes-using-three-js-r71
  def createBall(radius: Double, deg: Double, name: String = ""):Mesh = {
    val ball = new SphereGeometry(1, 10, 10)
    val material = new MeshPhongMaterial(
      literal(
        "color"->0x09EECF,
        "emissive"->0xCFE2D2,
        "transparent"->true,//set to transparent, to avoid black on margins of this image
        "opacity"->0.64,
        "specular"-> 0xF9FBFF
      ).asInstanceOf[MeshPhongMaterialParameters])

    val result = new Mesh(ball, material)
    val degnumber = deg.degMulWithPI
    result.position.x = js.Math.cos(degnumber) * radius
    result.position.y = js.Math.sin(degnumber) * radius
    result.position.z = 0
    result.name = name

    return result
  }

  def animate(): Unit ={
    dom.window.requestAnimationFrame{_:Double=>
      animate
    }

    ballRadiuses.zipWithIndex.map( x =>{
      val (r, i) = x
      circleBall(balls(i), r, (i+1) % 2 == 1)
    })

    // onMouseMoving()
    rotateLogo()
    redraw()
  }

  def animateCamera(onFinish: ()=>Unit):Unit = {
    var deg = 45.0
    val toDeg = 90
    val radius = 150
    val speed = 0.3
    var handler = 0

    def startj():Unit = {

      ballRadiuses.zipWithIndex.map( x =>{
        val (r, i) = x
        circleBall(balls(i), r, (i+1) % 2 == 1)
      })

      if ( Math.floor(deg) >= toDeg){
        dom.window.cancelAnimationFrame(handler)
        camera.position.z = 150
        camera.position.y = 0
        camera.lookAt(new Vector3(0, 0, 0))
        redraw()

        // onMouseMoving()
        onFinish()
      }else{
        handler = try {
          window.requestAnimationFrame { _:Double => startj() }
        } catch {
          case _: Throwable => 0
        }
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

  //效果不理想
  def onMouseMoving():Unit = {

    if (isMouseMoving){
      logo.rotation.y =  ( mouseX * 0.0005 ) % 3 // 3 ~= Math.PI
//      camera.position.x +=  ( mouseX - camera.position.x ) % 100 * 0.005
      //        camera.position.y += ( - mouseY - camera.position.y ) * .05;
//      camera.lookAt(new Vector3(0, 0, 0))
//      println(s"cameras, ${camera.position.x}, ${camera.position.y}")
    }else{
      logo.rotation.y = 0
//      camera.position.x = 0
//      camera.position.y = 0
//      camera.lookAt(new Vector3(0, 0, 0))
    }
  }

  def redraw():Unit = {
    render.render(scene, camera)
  }

  def circleBall(mesh: Mesh, radiusToCenter: Double, clockWise: Boolean = true): Unit ={
    var deg = js.Math.atan2(mesh.position.y, mesh.position.x).radianToDeg
    if (clockWise){
      deg = (deg-0.1 + 360)%360
    }else{
      deg = (deg+0.1)%360
    }
    val theta = deg.degMulWithPI
    mesh.position.x = js.Math.cos(theta) * radiusToCenter
    mesh.position.y = js.Math.sin(theta) * radiusToCenter
  }

  def loadPanel(loader: TextureLoader, src: String)( cb: (Mesh)=>Unit ): Unit ={
    val params: (Texture)=>MeshPhongMaterialParameters = { texture=>
      literal(
        "color"->0xffffff,
        "map"->texture,
        "transparent"->true,//set to transparent, to avoid black on margins of this image
        "opacity"->1.0,
        "emissive"->0x222222
      ).asInstanceOf[MeshPhongMaterialParameters]
    }
    loadPanel(loader, src, 630.0/1398, 100, params){ mesh=>
      mesh.position.y = -40
      cb(mesh)
    }
  }

  def loadPanel(loader: TextureLoader, src: String, ratio: Double, width: Double, options: (Texture)=> MeshPhongMaterialParameters)( cb: Mesh=>Unit ): Unit = {
    val onload = (texture: Texture) => {
      //      texture.magFilter = NThree.NearestMipMapLinearFilter.asInstanceOf[TextureFilter]
      //      texture.minFilter = NThree.NearestMipMapLinearFilter.asInstanceOf[TextureFilter]
      //      println("max is: ", render.getMaxAnisotropy())

//      texture.anisotropy =  16 //increase sample accuracy, resolve to more resolution
      val geometry = new PlaneGeometry(width, width*ratio)
      val material = new MeshPhongMaterial( options(texture) )
      val plane = new Mesh( geometry, material )
      cb(plane)
      scene.add( plane )
      render.render(scene, camera)

    }
    loader.load(src, onload)
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

    loadPanel(loader, Resouces.image_2){ mesh: Mesh=>
      mesh.position.x = 30
      mesh.position.y = -55
      mesh.position.z = 2
    }
  }


  def rotateLogo() ={
    logo.rotation.y += 0.002
  }


  def registerMouseEvent(): Unit ={
    var handler = 0
    document.addEventListener("mousemove", { evt: MouseEvent =>
      val isInRange = (evt.clientX > 0 && evt.clientX < window.innerWidth) && ( evt.clientY > _topOffset && evt.clientY < (canvasHeight + _topOffset))

      if (isInRange){
        mouseX = evt.clientX - window.innerWidth / 2
        mouseY = evt.clientY - canvasHeight/2
        isMouseMoving = true
      }

      window.clearTimeout(handler)
      handler = window.setTimeout(() => isMouseMoving = false , 300)
    })
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
    val DoubleSide:Double = js.native
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
