from datetime import timedelta
import math
import numpy as np
import pandas as pd


class SettingsWithTime:
    def __init__(self, delta, shutter_speed, iso):
        self.delta = delta
        self.shutter_speed = shutter_speed
        self.iso = iso


class SettingsCurve:
    def __init__(self, begins_at, settings_with_time):
        self.begins_at = begins_at
        self.settings_with_time = settings_with_time
        self.df = self.buildSettingsFrame()

    def ev(self, iso, shutter_speed, aperture):
        ev100 = math.log2(math.pow(aperture, 2) / shutter_speed)
        if iso > 100:
            return ev100 + math.log2(iso / 100)
        else:
            return ev100

    def build_settings_arrays(self):
        alpha = self.begins_at
        delta = np.array([], dtype='timedelta64')
        time = np.array([], dtype='datetime64')
        ss = np.array([], dtype=np.dtype('f8'))
        iso = np.array([], dtype=np.dtype('u4'))
        for s in self.settings_with_time:
            alpha += s.delta
            d = alpha
            # print("Append: " + d.strftime("%Y-%m-%dT%H:%M:%S") + ", " + str(s.shutter_speed) + ", " + str(s.iso))
            time = np.append(time, d)
            delta = np.append(delta, s.delta)
            ss = np.append(ss, s.shutter_speed)
            iso = np.append(iso, s.iso)
        return {'delta': delta, 'time': time, 'shutter_speed': ss, 'iso': iso}

    def build_settings_frame(self):
        sett = self.build_settings_arrays()
        d = pd.DataFrame({
            'delta': pd.Series(sett['delta'], dtype='timedelta64[ns]'),
            'time': pd.Series(sett['time'], dtype='datetime64[ns]'),
            'shutter_speed': pd.Series(sett['shutter_speed'], dtype='float64'),
            'iso': pd.Series(sett['iso'], dtype='int32'),
            'aperture': pd.Series(np.full(sett['time'].size, 2.8), dtype='float64')
        })
        d['ev'] = d.apply(lambda r: self.ev(r['iso'], r['shutter_speed'], r['aperture']), axis=1)
        return d

    def calc_setting_for_time(self, t, column):
        if t < self.df['time'].iloc[0]:
            # print(str(t) + " < " + str(self.df['time'].iloc[0]))
            return self.df[column].iloc[0]
        if t > self.df['time'].iloc[-1]:
            # print(str(t) + " > " + str(self.df['time'].iloc[-1]))
            return self.df[column].iloc[-1]
        diff = self.df['time'] - t
        only_pos = diff[diff <= timedelta(minutes=0)]
        sortss = only_pos.abs().argsort()
        res = self.df.iloc[sortss[:1]][column]
        # print(res)
        return res.iloc[0]

