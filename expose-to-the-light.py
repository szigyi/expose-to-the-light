#!/usr/bin/python3

import datetime
import math
from datetime import timedelta
from scipy.interpolate import interp1d

# current date 2020.06.15

dtFormat = "%Y-%m-%dT%H:%M:%S"
sunSetAt = datetime.datetime.strptime("2020-06-15T21:19:00", dtFormat)
sunRiseAt = datetime.datetime.strptime("2020-06-15T21:19:00", dtFormat)
astroTwiStartAt = datetime.datetime.strptime("2020-06-15T23:30:00", dtFormat)
astroTwiEndAt = datetime.datetime.strptime("2020-06-16T02:30:00", dtFormat)

noonAt = datetime.datetime.strptime("2020-06-16T12:00:00", dtFormat)


# https://en.wikipedia.org/wiki/Sigmoid_function
def sigmoid(x):
    return 1 / (1 + math.pow(math.e, (x * -1)))


# https://en.wikipedia.org/wiki/Generalised_logistic_function
def gen_logistic_curve(t):
    a = 0
    b = 0.7
    k = 1.0
    q = 0.5
    v = 0.5
    m = 0
    c = 1
    return a + ((k - a) / (math.pow(c + q * (math.pow(math.e, (b * t) * -1)), (1 / v))))


def timedelta_to_range(t1, t2, now):
    curve_zone_start = -6
    curve_zone_end = 6
    polate = interp1d([t1, t2], [curve_zone_start, curve_zone_end])
    return polate(now)


def brightness_to_settings(brightness):
    min_shutter_speed = 15
    max_shutter_speed = 1/8000
    min_iso = 1600
    max_iso = 100
    aperture = 2.8
    shutter_polate = interp1d([0, 1], [min_shutter_speed, max_shutter_speed])
    iso_polate = interp1d([0, 1], [min_iso, max_iso])
    return shutter_polate(brightness), iso_polate(brightness)


now = datetime.datetime.strptime("2020-06-15T23:30:00", dtFormat)


for i in range(0, 13):
    t = (now + timedelta(hours=i))
    mapped = timedelta_to_range(astroTwiStartAt.timestamp(), noonAt.timestamp(), t.timestamp())
    brightness = gen_logistic_curve(mapped)
    settings = brightness_to_settings(brightness)
    print(t.strftime("%Y-%m-%dT%H:%M:%S") + "," + str(brightness) + "," + str(settings[0]) + "," + str(settings[1]))
    # print(str(res))


# for i in range(-6, 6, 1):
#     # print(str(i) + "," + str(sigmoid(i)) + "," + str(genLogisticCurve(i)))
#     print(str(gen_logistic_curve(i)))
