package hu.szigyi.ettl.v1.service

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.StrictLogging
import org.gphoto2.CameraFile

class CapturedImageService extends StrictLogging {
  private val nothingIsCapturedYetImage = Ref.of[IO, String]("iVBORw0KGgoAAAANSUhEUgAAAe8AAAAwCAYAAAA4lvKDAAAfGUlEQVR4Xu2dddgVxRfHBzswMbAwwBYFRbGxWwwsLGxR7Eaxxe5uBRW7u7uxu7u7W3/PZ3/P6t7znjM7e+O99753zj/wvHd3Z+ZMfE9Pp3/++ecfFylyIHIgciByIHIgcqBpONApgnfTzFXsaORA5EDkQORA5EDCgQjegQvhxx9/dN9//33J0126dHHjjjtu4BeKPfb555+7P//889+XxhhjDNe1a9diH2nBp3/55Rf3zTfflIx80kkndRNMMEELciMOOXKg/hz46aef3HfffVfSkcknn9yNN9549e9cE/cggnfg5G266abuoosuKnn62GOPdbvvvnvgF8If+/nnn92EE07Y5oWPP/7YTTPNNOEfasEnDzroIHfwwQeXjHyHHXZwp5xySgtyIw45cqD+HNhqq63ceeedV9KRww8/3A0dOrT+nWviHkTwDpy8jTbayI0aNark6aOOOsrttddegV8IfwxJtXPnzm1e+PDDD910000X/qEWfHL//fd3hx12WMnIt9tuO3f66ae3IDfikCMH6s+BzTff3F144YUlHTn00EPdsGHD6t+5Ju5BBO/AyYvgHcioOj8WwbvOExCbjxwQHIjgXZslEcE7kK8RvAMZVefHInjXeQJi85EDEbzbZQ20FHgTAPbXX3+VMDY04Kw9wdvyeX/00Udu2mmnbZeF0ayNHHjgge6QQw4p6X70eTfrbJbf799++63kZQI+xx577PI/GN8smwNR8w5jHVnbv//+e8nDY445phtrrLHUD7QUeE899dSOKO4svfDCC26eeebJ5W57gjedwb+dnUgmsFu3brn9bPUHiBf47LPPStgw5ZRTuokmmqjVWdMy43/ttdfcHHPMUTJe5l9mi7QMQ+o80AjeYRNw/vnnuy233LLk4fXWW89dfvnlEbwnnnhi98MPP5QwYvTo0W6BBRbI5W57g3duh+IDkQORAyoHnn/+eTfffPO1+S3Wo6rPgongHcZ3gmqHDBlS8vDqq6/ubrjhhgjeEbzDFlF8KnKgmTkQwbuxZi+Cd9h8RPD28CmCd9giik9FDjQzByJ4N9bsRfAOm48I3hG8w1ZKfCpyoINyIIJ3Y01sBO+w+ag5eBMJ/dZbbyUBVQSAzDzzzG622WZzlKCsFhGo9frrryfBZV999VUSbTfVVFMl5UG7d+9edjO11rzxqcGbDz74IAmaIlCKwBkixDt16lR2v6v1Iv16//33HZXaKALDvM0444xmNGPRdr/88stk/J988on7448/HAGC008/vZtlllmKfqruzzOXzOG7776bzCf8Yhzwq55lHX/99Vf3xhtvuC+++CLZG/SFeaRv1Yympp333nvPvf32247SwDPMMEOy19mHtVjLnCesTdYOvKeSILxm/RSljgbe8OXNN99MzsNJJpkkidFhHvKIOXzllVfcp59+mpQnnWmmmdzss8/uJptssrxXg35nnjinOU/o2/jjj5/sedrI4kFHBW/GT3AkWUDsR877Oeecs+yMoIrAe6GFFko2bEp9+/b911n+zjvvuJNPPjkpcyeDvniexbThhhsmaTrlRvY+9thjSSWs6667Tm2DdtjMAwcOdDvttFPuxmYRffvtt/+OR0aapz9kN8I444zjGKsMz/cFrCHQ0O8zzjgjOewkwY8ll1zSHXfcccnCziPSXHr06NEmbYCNSE1gSddcc42jiliWrrzyyqTNv//+O4lWZO7gr0YLLrigo1rc0ksvndc19fdbb701KT/KvxrNNddcbvvtt3fbbrttwtebb77ZbbHFFv8+ilD23HPPldW2fIl+yAprBIEccMABQd9/8sknHelm1ljSNbj11lu73XbbTa2EF9RQwYfozwUXXOCYV4vg85prrpmUndQq9OU1yQF//PHHO6JeAVOLVlxxRUcZ2oUXXjjvk8nv8ly5/vrrk3dZmyNGjEj2DXzXiOdY25tssokpNHCAstZTCtnnPLvCCiu0KXlMKikgJ1N2nn322eDSxCgYCDxZYnxatggVGuFBlhCAmb+nn346qUIm1+Ixxxzj9thjD5P3r776qqN08xVXXGGe1ez5DTbYwG288cZBc5h9iPPuhBNOSNaJdt7x7BprrJGcKZx3tQDvVVZZxT311FMlfUdgePnllx3pVSHEOtxmm23aPMq4Vl11VfMT3J3A+NmP2j7hvEfAWmaZZZIKnFY68siRI92ee+5ZeN2y99gTJaliUjNFmmchAQ4DBgwI4UcCqCxGOh5KMIODUJbQy3v/yCOPTAZPDqdG5WoIgCcgniULvJdbbrlEmEACDSHABcb7FljR8qjnnnuuA0yydOmllzoO2UGDBrkbb7wxpGsJwMLTUOGLTbzj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")
  val image: Ref[IO, String] = nothingIsCapturedYetImage.unsafeRunSync()

  def saveCapturedImage(cf: CameraFile): IO[Unit] = {
//    cf.save(imagePath.toFile.getAbsolutePath)
//    logger.info(s"Saved image to: ${imagePath.toFile.getAbsolutePath}")
//    val img: Array[Byte] = Files.readAllBytes(imagePath)
//    val encoded: String = BaseEncoding.base64().encode(img)
//    image.set(encoded)
    logger.trace(s"Saving: $cf")
    IO.unit
  }
}
