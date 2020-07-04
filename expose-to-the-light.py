#!/usr/bin/python3
import datetime
import time
import numpy as np
import gphoto2 as gp

from datetime import timedelta
from CurvedSettings import SettingsCurve, SettingsWithTime

settings = np.array([
    SettingsWithTime(timedelta(minutes=20), 15, 1600),
    SettingsWithTime(timedelta(minutes=20), 8, 1600),
    SettingsWithTime(timedelta(minutes=15), 4, 800),
    SettingsWithTime(timedelta(minutes=10), 2, 800),
    SettingsWithTime(timedelta(minutes=5), 1, 800),
    SettingsWithTime(timedelta(minutes=5), 1/2, 400),
    SettingsWithTime(timedelta(minutes=5), 1/4, 400),
    SettingsWithTime(timedelta(minutes=5), 1/8, 400),
    SettingsWithTime(timedelta(minutes=5), 1/15, 400),
    SettingsWithTime(timedelta(minutes=10), 1/30, 400),
    SettingsWithTime(timedelta(minutes=10), 1/60, 400),
    SettingsWithTime(timedelta(minutes=15), 1/125, 400),
    SettingsWithTime(timedelta(minutes=20), 1/250, 400),
    SettingsWithTime(timedelta(minutes=20), 1/500, 400),
    SettingsWithTime(timedelta(minutes=30), 1/1000, 200),
    SettingsWithTime(timedelta(minutes=30), 1/2000, 200),
    SettingsWithTime(timedelta(minutes=40), 1/4000, 100),
    SettingsWithTime(timedelta(minutes=40), 1/8000, 100)
])


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

