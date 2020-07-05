#!/usr/bin/python3
from __future__ import print_function

import sys
import datetime
import time
import gphoto2 as gp

from ettl.CameraControl import configured_camera, empty_event_queue, shutter_speed_to_string, set_camera_config
from ettl.CurvedSettings import SettingsCurve
from ettl.Curvature import sunset_curvature


def main():
    dt_format = "%Y-%m-%dT%H:%M:%S"
    darkness_start_changing_at = datetime.datetime.strptime("2020-07-05T09:00:00", dt_format)
    interval_seconds = 30
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

                print(datetime.datetime.strftime(t, dt_format) + " " + str(count) + ". [iso: " + str(iso) + ", shutter_speed: " + shutter_speed + " (" + str(ss) + ")]")

                set_camera_config(camera, iso, shutter_speed, aperture)

                # camera.capture(gp.GP_CAPTURE_IMAGE)
                count += 1

                time.sleep(interval_seconds)
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

