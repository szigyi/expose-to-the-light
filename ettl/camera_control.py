
from contextlib import contextmanager
import gphoto2 as gp


@contextmanager
def configured_camera():
    # initialise camera
    camera = gp.Camera()
    camera.init()
    try:
        # adjust camera configuratiuon
        cfg = camera.get_config()
        capturetarget_cfg = cfg.get_child_by_name('capturetarget')
        capturetarget = capturetarget_cfg.get_value()
        capturetarget_cfg.set_value('Memory card')
        # camera dependent - 'imageformat' is 'imagequality' on some
        imageformat_cfg = cfg.get_child_by_name('imageformat')
        imageformat = imageformat_cfg.get_value()
        imageformat_cfg.set_value('Small Fine JPEG')

        imageformatsd_cfg = cfg.get_child_by_name('imageformatsd')
        imageformatsd = imageformatsd_cfg.get_value()
        imageformatsd_cfg.set_value('Small Fine JPEG')

        camera.set_config(cfg)
        # use camera
        yield camera
    finally:
        # reset configuration
        capturetarget_cfg.set_value(capturetarget)
        imageformat_cfg.set_value(imageformat)
        camera.set_config(cfg)
        # free camera
        print("Freeing camera connection!")
        camera.exit()


def set_camera_config(camera, iso, shutter_speed, aperture):
    cfg = camera.get_config()
    iso_cfg = cfg.get_child_by_name('iso')
    iso_cfg.set_value(str(iso))

    shutterspeed_cfg = cfg.get_child_by_name('shutterspeed')
    shutterspeed_cfg.set_value(shutter_speed)

    aperture_cfg = cfg.get_child_by_name('aperture')
    aperture_cfg.set_value(str(aperture))
    camera.set_config(cfg)


def shutter_speed_to_string(ss):
    m = {
        15: '15',
        13: '13',
        10: '10',
        8: '8',
        6: '6',
        5: '5',
        3.2: '3.2',
        2.5: '2.5',
        2: '2',
        1.6: '1.6',
        1.3: '1.3',
        1: '1',
        0.8: '0.8',
        0.6: '0.6',
        0.5: '0.5',
        0.4: '0.4',
        0.3: '0.3',
        1 / 4: '1/4',
        1 / 5: '1/5',
        1 / 6: '1/6',
        1 / 8: '1/8',
        1 / 10: '1/10',
        1 / 13: '1/13',
        1 / 15: '1/15',
        1 / 20: '1/20',
        1 / 25: '1/25',
        1 / 30: '1/30',
        1 / 40: '1/40',
        1 / 50: '1/50',
        1 / 60: '1/60',
        1 / 80: '1/80',
        1 / 100: '1/100',
        1 / 125: '1/125',
        1 / 160: '1/160',
        1 / 200: '1/200',
        1 / 250: '1/250',
        1 / 320: '1/320',
        1 / 400: '1/400',
        1 / 500: '1/500',
        1 / 640: '1/640',
        1 / 800: '1/800',
        1 / 1000: '1/1000',
        1 / 1250: '1/1250',
        1 / 1600: '1/1600',
        1 / 2000: '1/2000',
        1 / 2500: '1/2500',
        1 / 3200: '1/3200',
        1 / 4000: '1/4000',
        1 / 5000: '1/5000',
        1 / 6400: '1/6400',
        1 / 8000: '1/8000'
    }
    return m.get(ss, 'Invalid Shutter speed!')


def empty_event_queue(camera):
    timeout_milli_sec = 10
    while True:
        type_, data = camera.wait_for_event(timeout_milli_sec)
        if type_ == gp.GP_EVENT_TIMEOUT:
            return
        if type_ == gp.GP_EVENT_FILE_ADDED:
            # get a second image if camera is set to raw + jpeg
            print('Unexpected new file', data.folder + data.name)

