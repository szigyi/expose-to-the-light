package org.gphoto2;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.gphoto2.jna.GPhoto2Native;

public class CameraFileMod extends CameraFile {

    final Pointer cf;

    public PathMod p;

    /**
     * Creates a new file link. The file is not yet linked to any particular camera file - the link is performed later on, by invoking gphoto functions.
     */
    CameraFileMod() {
        final PointerByReference p = new PointerByReference();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_new(p), "gp_file_new");
        cf = p.getValue();
    }

    CameraFileMod(PathMod path) {
        this();
        this.p = path;
    }

    public void clean() {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_clean(cf), "gp_file_clean");
    }

    /**
     * Closes this file link and frees allocated resources.
     */
    public void close() {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_free(cf), "gp_file_free");
    }

    /**
     * Saves the file from the camera to the local file system.
     *
     * @param filename OS-dependent path on the local file system.
     */
    public void save(String filename) {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_save(cf, filename), "gp_file_save");
    }

    void ref() {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_ref(cf), "gp_file_ref");
    }

    void unref() {
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_file_unref(cf), "gp_file_unref");
    }

    public PathMod getPath() {
        return this.p;
    }

}
