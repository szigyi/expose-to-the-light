import numpy as np
from datetime import timedelta
from ettl.curved_settings import SettingsWithTime


def sunset_curvature():
    return np.flip(settings)


def test_sunset_curvature():
    def mins_to_seconds(s):
        return SettingsWithTime(timedelta(seconds=0.5), s.shutter_speed, s.iso)

    return np.array(list(map(mins_to_seconds, sunset_curvature())))


def sunrise_curvature():
    return settings


settings = np.array([
    SettingsWithTime(timedelta(minutes=20.0), 15, 1600),
    #     SettingsWithTime(timedelta(minutes=15.0), 13, 1600),
    #     SettingsWithTime(timedelta(minutes=15.0), 10, 1600),
    SettingsWithTime(timedelta(minutes=10.0), 8, 800),
    SettingsWithTime(timedelta(minutes=8.0), 6.3, 800),
    SettingsWithTime(timedelta(minutes=5.0), 5, 800),
    SettingsWithTime(timedelta(minutes=4.0), 3.2, 800),
    #     SettingsWithTime(timedelta(minutes=4.0), 2.5, 800),
    #     SettingsWithTime(timedelta(minutes=4.0), 2, 800),
    #     SettingsWithTime(timedelta(minutes=4.0),  1.6, 800),
    SettingsWithTime(timedelta(minutes=4.0), 1.3, 400),
    SettingsWithTime(timedelta(minutes=3.0), 1, 400),
    SettingsWithTime(timedelta(minutes=3.0), 0.8, 400),
    SettingsWithTime(timedelta(minutes=3.0), 0.6, 400),
    SettingsWithTime(timedelta(minutes=3.0), 0.5, 400),
    #     SettingsWithTime(timedelta(minutes=3.0),  0.4, 400),
    #     SettingsWithTime(timedelta(minutes=3.0),  0.3, 400),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/4, 400),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/5, 400),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 6, 200),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 8, 200),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 10, 200),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/13, 200),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/15, 200),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/20, 200),
    #     SettingsWithTime(timedelta(minutes=3.0),  1/25, 200),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 30, 100),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 40, 100),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 50, 100),
    SettingsWithTime(timedelta(minutes=3.0), 1 / 60, 100),
    SettingsWithTime(timedelta(minutes=4.0), 1 / 80, 100),
    SettingsWithTime(timedelta(minutes=4.0), 1 / 100, 100),
    SettingsWithTime(timedelta(minutes=4.0), 1 / 125, 100),
    SettingsWithTime(timedelta(minutes=4.0), 1 / 160, 100),
    SettingsWithTime(timedelta(minutes=4.0), 1 / 200, 100),
    SettingsWithTime(timedelta(minutes=5.0), 1 / 250, 100),
    SettingsWithTime(timedelta(minutes=5.0), 1 / 320, 100),
    SettingsWithTime(timedelta(minutes=5.0), 1 / 400, 100),
    SettingsWithTime(timedelta(minutes=5.0), 1 / 500, 100),
    SettingsWithTime(timedelta(minutes=5.0), 1 / 640, 100),
    SettingsWithTime(timedelta(minutes=10.0), 1 / 800, 100),
    SettingsWithTime(timedelta(minutes=10.0), 1 / 1000, 100),
    SettingsWithTime(timedelta(minutes=10.0), 1 / 1250, 100),
    SettingsWithTime(timedelta(minutes=10.0), 1 / 1600, 100),
    SettingsWithTime(timedelta(minutes=10.0), 1 / 2000, 100),
    SettingsWithTime(timedelta(minutes=15.0), 1 / 2500, 100),
    SettingsWithTime(timedelta(minutes=15.0), 1 / 3200, 100),
    SettingsWithTime(timedelta(minutes=15.0), 1 / 4000, 100),
    SettingsWithTime(timedelta(minutes=15.0), 1 / 5000, 100),
    SettingsWithTime(timedelta(minutes=20.0), 1 / 6400, 100),
    SettingsWithTime(timedelta(minutes=20.0), 1 / 8000, 100)
])
