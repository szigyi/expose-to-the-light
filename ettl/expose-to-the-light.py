#!/usr/bin/python3
import datetime
import time
import numpy as np
import gphoto2 as gp

from ettl.CurvedSettings import SettingsCurve
from ettl.Curveture import settings


def main():
    dt_format = "%Y-%m-%dT%H:%M:%S"
    darkness_start_changing_at = datetime.datetime.strptime("2020-07-03T18:00:00", dt_format)
    sc = SettingsCurve(darkness_start_changing_at, np.flip(settings))

    while True:
        t = datetime.datetime.now()
        ss = sc.calc_setting_for_time(t, 'shutter_speed')
        iso = sc.calc_setting_for_time(t, 'iso')

        print("Settings[iso: " + str(iso) + ", shutter_speed: " + str(ss) + "]")

        text = camera.get_summary()
        print('Summary')
        print('=======')
        print(str(text))

        time.sleep(1)


if __name__ == "__main__":
    try:
        camera = gp.Camera()
        camera.init()
        main()
    except KeyboardInterrupt:
        camera.exit()
        print("Terminating...as you told me...")
    finally:
        camera.exit()
        print("I'm done...terminating...")

