#!/usr/bin/python3
from __future__ import print_function

import argparse
import sys
import datetime
import time
import gphoto2 as gp

from ettl.camera_control import configured_camera, empty_event_queue, shutter_speed_to_string, set_camera_config
from ettl.print_to_terminal import print_plot, print_settings
from ettl.curved_settings import SettingsCurve
from ettl.curvature import sunset_curvature, test_sunset_curvature


def main():
    darkness_start_changing_at = sunset_at - brightness_change_before_sunset_time_delta
    sc = SettingsCurve(darkness_start_changing_at, sunset_curve)

    print_plot(sc.ev_curve())
    print_settings(sc.df)

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

                if capture_image:
                    camera.capture(gp.GP_CAPTURE_IMAGE)

                count += 1

                time.sleep(interval_seconds)
            except KeyboardInterrupt:
                break
    return 0


if __name__ == "__main__":
    try:
        dt_format = "%Y-%m-%dT%H:%M:%S"
        parser = argparse.ArgumentParser(description='expose-to-the-light arguments')
        parser.add_argument('--test-run', help='test run (default: false)', default=False, action='store_true')
        parser.add_argument('interval', help='interval between two photos taken', type=int)
        parser.add_argument('sunset_at', help='datetime when the sun sets', type=lambda s: datetime.datetime.strptime(s, dt_format))
        args = parser.parse_args()

        brightness_change_before_sunset_time_delta = datetime.timedelta(hours=3)

        if args.test_run:
            capture_image = False
            interval_seconds = 0.5
            sunset_curve = test_sunset_curvature()
            sunset_at = datetime.datetime.now() + brightness_change_before_sunset_time_delta
        else:
            capture_image = True
            interval_seconds = args.interval
            sunset_curve = sunset_curvature()
            sunset_at = args.sunset_at

        sys.exit(main())
    except KeyboardInterrupt:
        print("Terminating...as you told me...")
    finally:
        print("I'm done...terminating...")

