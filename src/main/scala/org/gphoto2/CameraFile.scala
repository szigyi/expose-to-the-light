package org.gphoto2

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
import org.gphoto2.jna.GPhoto2Native

import java.io.Closeable

class CameraFile() extends Closeable {
  private[gphoto2] var cf: Pointer = null

  /**
   * Creates a new file link. The file is not yet linked to any particular camera file - the link is performed later on, by invoking gphoto functions.
   */
  def this() = {
    this
    val p = new PointerByReference
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_new(p), "gp_file_new")
    cf = p.getValue
  }


  def clean(): Unit =
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_clean(cf), "gp_file_clean")

  /**
   * Closes this file link and frees allocated resources.
   */
  override def close(): Unit =
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_free(cf), "gp_file_free")

  /**
   * Saves the file from the camera to the local file system.
   *
   * @param filename OS-dependent path on the local file system.
   */
  def save(filename: String): Unit =
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_save(cf, filename), "gp_file_save")

  private[gphoto2] def ref(): Unit =
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_ref(cf), "gp_file_ref")

  private[gphoto2] def unref(): Unit =
    CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_unref(cf), "gp_file_unref")

  /**
   * Represents a path of a camera file.
   */
  private[gphoto2] class Path {
    final var filename: String = null
    final var path: String = null

    /**
     * Creates new path.
     *
     * @param filename the file name, without the path, gphoto-dependent. See {@link GPhoto2Native# CameraFilePath} for details.
     * @param path     the path, gphoto-dependent.
     */
    def this(filename: String, path: String) {
      this()
      this.filename = filename
      this.path = path
    }

    def this(path: GPhoto2Native.CameraFilePath) {
      this()
      filename = CameraUtils.toString(path.name)
      this.path = CameraUtils.toString(path.folder)
    }

    override def toString: String = "Path{" + path + " " + filename + '}'

    /**
     * Returns a referenced camera file.
     *
     * @param cam the camera handle.
     * @return camera file.
     */
    private[gphoto2] def newFile(cam: Pointer) = {
      var returnedOk = false
      val cf = new CameraFile
      try {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_file_get(cam, path, filename, GPhoto2Native.GP_FILE_TYPE_NORMAL, cf.cf, CameraList.CONTEXT), "gp_camera_file_get")
        returnedOk = true
        cf
      } finally if (!returnedOk) CameraUtils.closeQuietly(cf)
    }
  }
}
