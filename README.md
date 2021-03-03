# expose-to-the-light

## Install application on Raspberry Pi
artifact = https://www.dropbox.com/sh/tjhu0op7hzqvonk/AAACatOs5-DtAsaOV4DFC9hoa?dl=0

script = https://www.dropbox.com/sh/rndmhfrp1xni4h5/AAD2LiDVxeInX6LtV7R8hGzCa?dl=0

### Download install script
`wget https://www.dropbox.com/s/fd88r1lybiscjgb/install.sh?dl=1`

### Install the version you want
`./install.sh 0.1.2`

*install script only works on unix, not on macOS as the hashmap datastructure does not work*

### Run the app
`./run.sh 0.1.2 --imagesBasePath /home/pi/dev/expose-to-the-light/captured-images/ --setSettings --numberOfCaptures 5 --intervalSeconds 5`


## Distribute the app

### Assembly jar
https://github.com/sbt/sbt-assembly

`sbt assebmly`

### Publish artifact to github
https://github.com/djspiewak/sbt-github-packages

`sbt publisher/publish`


### Publish artifact to Dropbox
Can generate new token from https://www.dropbox.com/developers/apps
* `./deploy.sh $dropbox_bearer_token $buildNumber`
* `./deploy.sh abcd 4`




## TODO
- [ ] merge sunrise curvature into the sunset
- [X] use sunset and sunrise times to adjust curvature
- [ ] add http UI to start the app and see logs, curvature plots
- [ ] consider time of year to adjust length of curvature ??

## Dependencies
> order is important. First install system dependencies and then python dependencies!
Install the actual gphoto2 and other as system dependency

You need first `python3`

On Mac Os
   * `brew install pkg-config`
   * `brew install gphoto2`
   * `brew install gnuplot`

On Raspberry Pi (unix)
   * `sudo apt install pkg-config`
   * `sudo apt install gphoto2`
   * `sudo apt install gnuplot`

Install the python wrapper lib as python dependency
   * `sudo pip install -v gphoto2`

## Install
In the root folder of this project.

`pip3 install .`

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
`build_and_run.sh 30 2020-07-05T12:12:00`

### Run in test mode
This mode just tries all the settings on your camera. So you can verify that every settings will work and your camera can accept it.

`./build_and_test.sh`

## Errors at the start
> gphoto2.GPhoto2Error: [-105] Unknown model

Chance is your camera is not connected to the computer
   * Is it turned on?
   * Is the USB cable connected?
   * Is it in PTP mode?

> gphoto2.GPhoto2Error: [-53] Could not claim the USB device

Kill the running gphoto2 processes!

`ps aux | grep gphoto`
```
pi         745  0.0  0.7  43780  7252 ?        Ssl  14:14   0:00 /usr/lib/gvfs/gvfs-gphoto2-volume-monitor
pi        1439  0.1  1.0 116320 10172 ?        Sl   14:26   0:00 /usr/lib/gvfs/gvfsd-gphoto2 --spawner :1.4 /org/gtk/gvfs/exec_spaw/1
pi        1461  0.0  0.0   7348   572 pts/0    S+   14:28   0:00 grep --color=auto gphoto
```
   * `kill -9 745`
   * `kill -9 1439`

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

### Remote monitoring
The app is running on my raspberry pi which is connected to a powerbank and to the camera.

I trigger the `ettl` app from a machine remote. In order to SSH into the raspberry pi in the middle of nowhere you need a phone.

Turn on tethering on the phone and connect the raspberry pi and your computer via wifi. (raspberry pi needs to be setup to connect when you have screen etc.)

Then just ssh into the raspberry pi from the computer
`ssh -o ConnectTimeout=5 pi@172.20.10.12`