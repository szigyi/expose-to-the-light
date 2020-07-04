#!/usr/bin/python3
from __future__ import print_function

import os
import subprocess
import sys
import datetime
import time
import gphoto2 as gp

from contextlib import contextmanager
from ettl.CurvedSettings import SettingsCurve
from ettl.Curvature import sunset_curvature


@contextmanager
def configured_camera():
    # initialise camera
    camera = gp.Camera()
    camera.init()
    try:
        # adjust camera configuratiuon
        cfg = camera.get_config()
        capturetarget_cfg = cfg.get_child_by_name('capturetarget')
        capturetarget = capturetarget_cfg.get_value()
        capturetarget_cfg.set_value('Internal RAM')
        # camera dependent - 'imageformat' is 'imagequality' on some
        imageformat_cfg = cfg.get_child_by_name('imageformat')
        imageformat = imageformat_cfg.get_value()
        imageformat_cfg.set_value('Small Fine JPEG')
        camera.set_config(cfg)
        # use camera
        yield camera
    finally:
        # reset configuration
        capturetarget_cfg.set_value(capturetarget)
        imageformat_cfg.set_value(imageformat)
        camera.set_config(cfg)
        # free camera
        print("Freeing camera connection!")
        camera.exit()


def empty_event_queue(camera):
    timeout_milli_sec = 10
    while True:
        type_, data = camera.wait_for_event(timeout_milli_sec)
        if type_ == gp.GP_EVENT_TIMEOUT:
            return
        if type_ == gp.GP_EVENT_FILE_ADDED:
            # get a second image if camera is set to raw + jpeg
            print('Unexpected new file', data.folder + data.name)


def shutter_speed_to_string(ss):
    m = {
        15: '15',
        8: '8',
        4: '4',
        2: '2',
        1: '1',
        1 / 2: '1/2',
        1 / 4: '1/4',
        1 / 8: '1/8',
        1 / 15: '1/15',
        1 / 30: '1/30',
        1 / 60: '1/60',
        1 / 125: '1/125',
        1 / 250: '1/250',
        1 / 500: '1/500',
        1 / 1000: '1/1000',
        1 / 2000: '1/2000',
        1 / 4000: '1/4000',
        1 / 8000: '1/8000'
    }
    return m.get(ss, 'Invalid Shutter speed!')


def set_camera_config(camera, iso, shutter_speed, aperture):
    cfg = camera.get_config()
    # imgsettings_cfg = cfg.get_child_by_name('imgsettings')
    # imgsettings = imgsettings_cfg.get_value()
    iso_cfg = cfg.get_child_by_name('iso')
    iso_cfg.set_value(str(iso))

    shutterspeed_cfg = cfg.get_child_by_name('shutterspeed')
    shutterspeed_cfg.set_value(shutter_speed)

    aperture_cfg = cfg.get_child_by_name('aperture')
    aperture_cfg.set_value(str(aperture))
    # imageformat_cfg = cfg.get_child_by_name('imageformat')
    # imageformat = imageformat_cfg.get_value()
    # imageformat_cfg.set_value('Small Fine JPEG')
    camera.set_config(cfg)


def main():
    dt_format = "%Y-%m-%dT%H:%M:%S"
    darkness_start_changing_at = datetime.datetime.strptime("2020-07-04T11:00:00", dt_format)
    sc = SettingsCurve(darkness_start_changing_at, sunset_curvature())

    count = 0
    with configured_camera() as camera:
        while True:
            try:
                empty_event_queue(camera)

                t = datetime.datetime.now()
                aperture = 2.8
                ss = sc.calc_setting_for_time(t, 'shutter_speed')
                shutter_speed = shutter_speed_to_string(ss)
                iso = sc.calc_setting_for_time(t, 'iso')

                print(datetime.datetime.strftime(t, dt_format) + " [iso: " + str(iso) + ", shutter_speed: " + shutter_speed + " (" + str(ss) + ")]")

                set_camera_config(camera, iso, shutter_speed, aperture)

                # path = camera.capture(gp.GP_CAPTURE_IMAGE)
                count += 1

                time.sleep(1)
            except KeyboardInterrupt:
                break
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        print("Terminating...as you told me...")
    finally:
        print("I'm done...terminating...")

