# expose-to-the-light

## Dependencies
> order is important. First install system dependencies and then python dependencies!
Install the actual gphoto2 and other as system dependency

You need first `python3`

On Mac Os
   * `brew install pkg-config`
   * `brew install gphoto2`

On Raspberry Pi (unix)
   * `apt install pkg-config`
   * `apt install gphoto2`

Install the python wrapper lib as python dependency
   * `sudo pip install -v gphoto2`

## Install
In the root folder of this project.

`pip install .`

## Connect camera to your computer
   * Make sure the camera is in PTP mode.
> For my Canon 70D I always have to 'Disable' the wifi connection otherwise it does not detect.

   * Connect the MacOs and the camera by USB cable (no other cable will work)
   * Run the `gphoto2 --auto-detect` to list what the gphoto2 lib sees
> $ gphoto2 --auto-detect
> Model                          Port
> ----------------------------------------------------------
> Canon EOS 70D                  usb:020,005
   * Final test that everything is okey `gphoto2 --summary`

## Run the app
   * ``
   
## Useful docs
   * http://www.gphoto.org/doc/remote/
   * https://github.com/jim-easterbrook/python-gphoto2
   * https://github.com/jim-easterbrook/python-gphoto2/blob/master/examples/time_lapse.py
   
## Debugging or for Development
In order to run python-gphoto2 examples you need to install further dependencies

https://github.com/jim-easterbrook/python-gphoto2/blob/39f392c8793b990cbd2e8f517e0436e012833b92/examples/cam-conf-view-gui.py
   * `sudo -H pip install tzlocal`
   * `pip install Pillow`
   * `pip install PyQt5`