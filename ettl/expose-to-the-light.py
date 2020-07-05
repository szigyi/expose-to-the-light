#!/usr/bin/python3
from __future__ import print_function

import argparse
import sys
import datetime
import time

from ettl.camera_control import configured_camera, empty_event_queue, shutter_speed_to_string, set_camera_config
from ettl.print_to_terminal import print_plot, print_settings
from ettl.curved_settings import SettingsCurve
from ettl.curvature import sunset_curvature, test_sunset_curvature


def main():
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

                # camera.capture(gp.GP_CAPTURE_IMAGE)
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
        args = parser.parse_args()

        if args.test_run:
            interval_seconds = 0.5
            sunset_curve = test_sunset_curvature()
            darkness_start_changing_at = datetime.datetime.now()
        else:
            interval_seconds = 30
            sunset_curve = sunset_curvature()
            darkness_start_changing_at = datetime.datetime.strptime("2020-07-05T12:12:00", dt_format)

        sys.exit(main())
    except KeyboardInterrupt:
        print("Terminating...as you told me...")
    finally:
        print("I'm done...terminating...")

