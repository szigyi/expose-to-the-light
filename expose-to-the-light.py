#!/usr/bin/python3

import datetime
import math
from datetime import timedelta

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
    # print("t1 " + str(t1))
    # print("t2 " + str(t2))
    # print("now " + str(now))

    # Figure out how 'wide' each range is
    left_span = t2 - t1
    # print("left_span " + str(left_span))
    right_span = curve_zone_end - curve_zone_start

    # Convert the left range into a 0-1 range (float)
    value_scaled = float(now - t1) / float(left_span)

    # Convert the 0-1 range into a value in the right range.
    return curve_zone_start + (value_scaled * right_span)


def brightness_to_settings(brightness):
    max_shutter_speed = 15
    max_iso = 1600
    aperture = 2.8
    # if brightness <


now = datetime.datetime.strptime("2020-06-15T20:00:00", dtFormat)


for i in range(0, 18):
    t = (now + timedelta(hours=i))
    mapped = timedelta_to_range(astroTwiStartAt.timestamp(), noonAt.timestamp(), t.timestamp())
    res = gen_logistic_curve(mapped)
    # print(t.strftime("%Y-%m-%dT%H:%M:%S") + "," + str(res))
    print(str(res))


# for i in range(-6, 6, 1):
#     # print(str(i) + "," + str(sigmoid(i)) + "," + str(genLogisticCurve(i)))
#     print(str(gen_logistic_curve(i)))
