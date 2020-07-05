# expose-to-the-light

## Dependencies
> order is important. First install system dependencies and then python dependencies!
Install the actual gphoto2 and other as system dependency

You need first `python3`

On Mac Os
   * `brew install pkg-config`
   * `brew install gphoto2`
   * `brew install gnuplot`

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

## Errors at the start
> gphoto2.GPhoto2Error: [-105] Unknown model

Chance is your camera is not connected to the computer
   * Is it turned on?
   * Is the USB cable connected?
   * Is it in PTP mode?


kill running gphoto2 processes!

## Useful docs
   * http://www.gphoto.org/doc/remote/
   * https://github.com/jim-easterbrook/python-gphoto2
   * https://github.com/jim-easterbrook/python-gphoto2/blob/master/examples/time_lapse.py
   * http://gphoto-software.10949.n7.nabble.com/Beginner-Using-libgphoto2-how-to-find-set-config-values-td16449.html
   
## Debugging or for Development
In order to run python-gphoto2 examples you need to install further dependencies

https://github.com/jim-easterbrook/python-gphoto2/blob/39f392c8793b990cbd2e8f517e0436e012833b92/examples/cam-conf-view-gui.py
   * `sudo -H pip install tzlocal`
   * `pip install Pillow`
   * `pip install PyQt5`