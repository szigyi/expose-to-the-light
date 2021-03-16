/**
 * Java bindings for the libgphoto2 library.
 * Copyright (C) 2011 Innovatrics s.r.o.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.gphoto2;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.gphoto2.jna.GPhoto2Native;
import org.gphoto2.jna.GPhoto2Native.CameraFilePath;

import java.io.IOException;

/**
 * Represents a camera. Thread-unsafe.
 * @author Martin Vysny
 */
public class CameraMod extends Camera {

    final Pointer camera;

    /**
     * Creates a reference to the first connected camera.
     */
    public CameraMod() {
        final PointerByReference ref = new PointerByReference();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_new(ref), "gp_camera_new");
        camera = ref.getValue();
    }
    private boolean isInitialized = false;

    /**
     * Initializes the camera.
     */
    public void initialize() {
        checkNotClosed();
        if (!isInitialized) {
            CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_init(camera, CameraList.CONTEXT), "gp_camera_init");
            isInitialized = true;
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * De-initializes the camera.
     */
    public void deinitialize() throws IOException {
        checkNotClosed();
        if (isInitialized) {
            isInitialized = false;
            CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_exit(camera, CameraList.CONTEXT), "gp_camera_exit");
        }
    }

    private boolean closed = false;

    /**
     * De-initializes the camera and frees all resources. Further invocations to this method do nothing. Any camera method
     * will fail from now on with {@link java.lang.IllegalStateException}.
     */
    public void close() throws IOException {
        if (!closed) {
            deinitialize();
            closed = true;
            CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_free(camera), "gp_camera_free");
        }
    }

    private void checkNotClosed() {
        if (closed) {
            throw new IllegalStateException("Invalid state: closed");
        }
    }

    /**
     * Captures a quick preview image on the camera.
     * @return camera file, never null. Must be closed afterwards.
     */
    public CameraFileMod capturePreview() {
        checkNotClosed();
        boolean returnedOk = false;
        final CameraFileMod cfile = new CameraFileMod();
        try {
            CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_capture_preview(camera, cfile.cf, CameraList.CONTEXT), "gp_camera_capture_preview");
            returnedOk = true;
            return cfile;
        } finally {
            if (!returnedOk) {
                CameraUtils.closeQuietly(cfile);
            }
        }
    }

    /**
     * Returns new configuration for the camera.
     * @return the configuration, never null. Must be closed afterwards.
     */
    public CameraWidgets newConfiguration() {
        checkNotClosed();
        return new CameraWidgets(this);
    }

    /**
     * Captures a full-quality image image on the camera.
     * @return camera file, never null. Must be closed afterwards.
     */
    public CameraFileMod captureImage() {
        checkNotClosed();
        final CameraFilePath path = new CameraFilePath.ByReference();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_capture(camera, GPhoto2Native.GP_CAPTURE_IMAGE, path, CameraList.CONTEXT), "gp_camera_capture");
        final PathMod p = new PathMod(path);
        return p.newFile(camera);
    }

    void ref() {
        checkNotClosed();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_ref(camera), "gp_camera_ref");
    }

    void unref() {
        checkNotClosed();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_unref(camera), "gp_camera_ref");
    }

    /**
     * Returns library versions as a displayable string.
     * @return verbose version, never null, for example: "2.4.10.1 gcc (C compiler used) ltdl (for portable loading of camlibs) EXIF (for special handling of EXIF files) "
     */
    public static String getLibraryVersion() {
        final String[] versions = GPhoto2Native.INSTANCE.gp_library_version(GPhoto2Native.GP_VERSION_VERBOSE);
        final StringBuilder sb = new StringBuilder();
        for (final String v : versions) {
            sb.append(v).append(' ');
        }
        return sb.toString();
    }

    public void setPortInfo(Pointer portInfo) {
        checkNotClosed();
        CameraUtils.check(GPhoto2Native.INSTANCE.gp_camera_set_port_info(camera, portInfo), "gp_camera_set_port_info");
    }
}
