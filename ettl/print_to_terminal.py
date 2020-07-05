import datetime

import termplotlib as tpl
import numpy as np

from ettl.camera_control import shutter_speed_to_string


def print_settings(df):
    dt_format = "%Y-%m-%dT%H:%M:%S"
    df.apply(lambda r: print(datetime.datetime.strftime(r['time'], dt_format) + " [iso: " + str(r['iso']) + ", shutter_speed: " + shutter_speed_to_string(r['shutter_speed']) + " (" + str(r['shutter_speed']) + ")]"), axis=1)
    print("#################################")


def print_plot(ev):
    fig = tpl.figure()
    now = datetime.datetime.now().timestamp()
    x = np.array(ev.apply(lambda r: (r['time'].timestamp() - now) / 60, axis=1).values)
    y = np.array(ev['ev'].values)

    fig.plot(x, y, width=150, height=20)
    fig.show()
