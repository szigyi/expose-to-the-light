package org.gphoto2;


import com.sun.jna.Pointer;
import hu.szigyi.ettl.hal.GFile;
import org.gphoto2.jna.GPhoto2Native;
import org.gphoto2.jna.GPhoto2Native.CameraFilePath;

/**
 * Represents a path of a camera file.
 */
public class Path {

    public final String filename;
    public final String path;

    /**
     * Creates new path.
     *
     * @param filename the file name, without the path, gphoto-dependent. See {@link CameraFilePath} for details.
     * @param path     the path, gphoto-dependent.
     */
    public Path(String filename, String path) {
        this.filename = filename;
        this.path = path;
    }

    public Path(CameraFilePath path) {
        this.filename = CameraUtils.toString(path.name);
        this.path = CameraUtils.toString(path.folder);
    }

    public String getFilename() {
        return this.filename;
    }

    @Override
    public String toString() {
        return "Path{" + path + " " + filename + '}';
    }

    /**
     * Returns a referenced camera file.
     *
     * @param cam the camera handle.
     * @return camera file.
     */
    CameraFile newFile(Pointer cam) {
        boolean returnedOk = false;
        final CameraFile cf = new CameraFile(this);
        try {
            // use instead of filename when https://github.com/gphoto/gphoto2/issues/48 is solved
            String jpgName = GFile.rawFileNameToJpg(this.filename); // Use it later when gphoto2 can read images when photo was taken with RAW+JPG mode
            CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_file_get(cam, path, filename, GPhoto2Native.GP_FILE_TYPE_NORMAL, cf.cf, CameraList.CONTEXT), "gp_camera_file_get");
            returnedOk = true;
            return cf;
        } finally {
            if (!returnedOk) {
                CameraUtils.closeQuietly(cf);
            }
        }
    }
}
