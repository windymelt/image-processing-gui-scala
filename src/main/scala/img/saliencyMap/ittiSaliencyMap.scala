package img.saliencyMap

import img._
import _root_.img.filtering.GaborFilter

class ittiSaliencyMap() extends SaliencyMap() {
  def calcRImage(src: Image): Image = {
    val rPixels: List[Pixel] = for (pixel <- src.pixels) yield {
      val tmpValue = pixel.red - (pixel.green + pixel.blue) / 2
      val newValue = Math.max(tmpValue, 0.0)
      val newColor =
        0xff000000 + ((newValue * 255).toInt << 16) + ((newValue * 255).toInt << 8) + (newValue * 255).toInt

      new Pixel(pixel.x, pixel.y, newColor)
    }

    new Image(rPixels, src.width, src.height)
  }

  def calcGImage(src: Image): Image = {
    val gPixels: List[Pixel] = for (pixel <- src.pixels) yield {
      val tmpValue = pixel.green - (pixel.red + pixel.blue) / 2
      val newValue = Math.max(tmpValue, 0.0)
      val newColor =
        0xff000000 + ((newValue * 255).toInt << 16) + ((newValue * 255).toInt << 8) + (newValue * 255).toInt

      new Pixel(pixel.x, pixel.y, newColor)
    }

    new Image(gPixels, src.width, src.height)
  }

  def calcBImage(src: Image): Image = {
    val bPixels: List[Pixel] = for (pixel <- src.pixels) yield {
      val tmpValue = pixel.blue - (pixel.red + pixel.green) / 2
      val newValue = Math.max(tmpValue, 0.0)
      val newColor =
        0xff000000 + ((newValue * 255).toInt << 16) + ((newValue * 255).toInt << 8) + (newValue * 255).toInt

      new Pixel(pixel.x, pixel.y, newColor)
    }

    new Image(bPixels, src.width, src.height)
  }

  def calcYImage(src: Image): Image = {
    val yPixels: List[Pixel] = for (pixel <- src.pixels) yield {
      val tmpValue = (pixel.red + pixel.green) / 2 - Math.abs(
        pixel.red - pixel.green
      ) / 2 - pixel.blue
      val newValue = Math.max(tmpValue, 0.0)
      val newColor =
        0xff000000 + ((newValue * 255).toInt << 16) + ((newValue * 255).toInt << 8) + (newValue * 255).toInt

      new Pixel(pixel.x, pixel.y, newColor)
    }

    new Image(yPixels, src.width, src.height)
  }

  def calcColorSaliencyMap(src: Image): (Image, Image) = {
    val rImage = calcRImage(src)
    val gImage = calcGImage(src)
    val bImage = calcBImage(src)
    val yImage = calcYImage(src)

    val rPyramid = makePyramid(rImage)
    val gPyramid = makePyramid(gImage)
    val bPyramid = makePyramid(bImage)
    val yPyramid = makePyramid(yImage)

    val rgMap =
      ((rPyramid(0) - gPyramid(0)) - (rPyramid(1) - gPyramid(1)))
        .+((rPyramid(0) - gPyramid(0)) - (rPyramid(2) - gPyramid(2)))
        .+((rPyramid(1) - gPyramid(1)) - (rPyramid(2) - gPyramid(2)))
        .+((rPyramid(2) - gPyramid(2)) - (rPyramid(3) - gPyramid(3)))

    val byMap =
      ((bPyramid(0) - yPyramid(0)) - (bPyramid(1) - yPyramid(1)))
        .+((bPyramid(0) - yPyramid(0)) - (bPyramid(2) - yPyramid(2)))
        .+((bPyramid(1) - yPyramid(1)) - (bPyramid(2) - yPyramid(2)))
        .+((bPyramid(2) - yPyramid(2)) - (bPyramid(3) - yPyramid(3)))

    (rgMap, byMap)
  }

  def calcOrientationSaliencyMap(src: Image): (Image, Image, Image, Image) = {
    val gaborFilter0 = new GaborFilter(111, 10.0, 1.2, 10, 0, 0)
    val gaborFilter45 = new GaborFilter(111, 10.0, 1.2, 10, 0, 45)
    val gaborFilter90 = new GaborFilter(111, 10.0, 1.2, 10, 0, 90)
    val gaborFilter135 = new GaborFilter(111, 10.0, 1.2, 10, 0, 135)

    val gaborFilterdImage0: Image = gaborFilter0.filtering(src)
    val gaborFilterdImage45: Image = gaborFilter45.filtering(src)
    val gaborFilterdImage90: Image = gaborFilter90.filtering(src)
    val gaborFilterdImage135: Image = gaborFilter135.filtering(src)

    val pyramid0 = makePyramid(gaborFilterdImage0)
    val pyramid45 = makePyramid(gaborFilterdImage45)
    val pyramid90 = makePyramid(gaborFilterdImage90)
    val pyramid135 = makePyramid(gaborFilterdImage135)

    val map0 =
      ((pyramid0(0) - pyramid0(0)) - (pyramid0(1) - pyramid0(1)))
        .+((pyramid0(0) - pyramid0(0)) - (pyramid0(2) - pyramid0(2)))
        .+((pyramid0(1) - pyramid0(1)) - (pyramid0(2) - pyramid0(2)))
        .+((pyramid0(2) - pyramid0(2)) - (pyramid0(3) - pyramid0(3)))

    val map45 =
      ((pyramid45(0) - pyramid45(0)) - (pyramid45(1) - pyramid45(1)))
        .+((pyramid45(0) - pyramid45(0)) - (pyramid45(2) - pyramid45(2)))
        .+((pyramid45(1) - pyramid45(1)) - (pyramid45(2) - pyramid45(2)))
        .+((pyramid45(2) - pyramid45(2)) - (pyramid45(3) - pyramid45(3)))

    val map90 =
      ((pyramid90(0) - pyramid90(0)) - (pyramid90(1) - pyramid90(1)))
        .+((pyramid90(0) - pyramid90(0)) - (pyramid90(2) - pyramid90(2)))
        .+((pyramid90(1) - pyramid90(1)) - (pyramid90(2) - pyramid90(2)))
        .+((pyramid90(2) - pyramid90(2)) - (pyramid90(3) - pyramid90(3)))

    val map135 =
      ((pyramid135(0) - pyramid135(0)) - (pyramid135(1) - pyramid135(1)))
        .+((pyramid135(0) - pyramid135(0)) - (pyramid135(2) - pyramid135(2)))
        .+((pyramid135(1) - pyramid135(1)) - (pyramid135(2) - pyramid135(2)))
        .+((pyramid135(2) - pyramid135(2)) - (pyramid135(3) - pyramid135(3)))

    (map0, map45, map90, map135)
  }
}
