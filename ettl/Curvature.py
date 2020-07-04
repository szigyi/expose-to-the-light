import numpy as np
from datetime import timedelta
from ettl.CurvedSettings import SettingsWithTime


def sunset_curvature():
    return np.flip(settings)


def sunrise_curvature():
    return settings


settings = np.array([
    SettingsWithTime(timedelta(minutes=20), 15, 1600),
    SettingsWithTime(timedelta(minutes=20), 8, 1600),
    SettingsWithTime(timedelta(minutes=15), 4, 800),
    SettingsWithTime(timedelta(minutes=10), 2, 800),
    SettingsWithTime(timedelta(minutes=5),  1, 800),
    SettingsWithTime(timedelta(minutes=5),  1/2, 400),
    SettingsWithTime(timedelta(minutes=5),  1/4, 400),
    SettingsWithTime(timedelta(minutes=5),  1/8, 400),
    SettingsWithTime(timedelta(minutes=5),  1/15, 400),
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
