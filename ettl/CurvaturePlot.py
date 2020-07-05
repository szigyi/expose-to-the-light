import datetime

import termplotlib as tpl
import pandas as pd
import numpy as np
from ettl.Curvature import sunset_curvature

from ettl.CurvedSettings import SettingsCurve


def print_plot(ev):
    fig = tpl.figure()
    now = datetime.datetime.now().timestamp()
    x = np.array(ev.apply(lambda r: (r['time'].timestamp() - now) / 60, axis=1).values)
    y = np.array(ev['ev'].values)

    fig.plot(x, y, width=150, height=20)
    fig.show()


def main():
    dt_format = "%Y-%m-%dT%H:%M:%S"
    darkness_start_changing_at = datetime.datetime.strptime("2020-07-04T18:00:00", dt_format)
    sc = SettingsCurve(darkness_start_changing_at, sunset_curvature())
    ev = sc.ev_curve()
    print_plot(ev)


if __name__ == "__main__":
    main()
