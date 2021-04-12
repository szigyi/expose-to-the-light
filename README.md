# expose-to-the-light

[![Donate](https://img.shields.io/badge/Donate-PayPal-green.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=FRLU958RSV3KY)

![Scala CI](https://github.com/szigyi/expose-to-the-light/actions/workflows/scala.yml/badge.svg)
![Publish to Maven Central](https://github.com/szigyi/expose-to-the-light/actions/workflows/publish.yml/badge.svg)

## Install application

### Download installer

`curl -L -o install.sh https://www.dropbox.com/s/z5f5ch8997c1xmn/install.sh?dl=1`

### Install

* `chmod u+x install.sh`
* `./install.sh`

The `install.sh` script downloads the latest jar and script files and then setup them to your PATH so you can use it later as any other unix command.

### Runtime dependencies if you are not using the installer

Installer should install these dependencies for you.

#### On Mac Os

* `brew cask install java11` [https://medium.com/macoclock/using-homebrew-to-install-java-jdk11-on-macos-44b30f497b38]
* `brew install gphoto2`
* `brew install libgphoto2`

#### On Raspberry Pi (unix)

* `sudo apt-get install openjdk-11-jdk`
* `sudo apt install pkg-config`
* `sudo apt install gphoto2`
* `sudo apt install gnuplot`

### Run the app

`ettl INFO /home/pi/dev/expose-to-the-light/logs/ --imagesBasePath /home/pi/dev/expose-to-the-light/captured-images/ --setSettings --numberOfCaptures 5 --intervalSeconds 5 --rawFileExtension CR2`

First argument is the level of the logging ie: `INFO`, `DEBUG`, `WARN`, `ERROR`, `TRACE`
Second argument is the location of the logs file.

You can use dummy camera to run a test and check your settings. Just use `--dummyCamera` as programme argument.

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

## Connect the camera to the computer

* Make sure the camera is in PTP mode.

> For my Canon 70D I always have to 'Disable' the wifi connection otherwise the gphoto2 does not detect it.

* Connect the MacOs/raspberry pi and the camera by USB cable (no other type of cable will work)
* Run the `gphoto2 --auto-detect` to list what the gphoto2 lib sees

> $ gphoto2 --auto-detect
> Model                          Port
> ----------------------------------------------------------
> Canon EOS 70D                  usb:020,005

* Final test that everything is okey `gphoto2 --summary`

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
* http://gphoto-software.10949.n7.nabble.com/Beginner-Using-libgphoto2-how-to-find-set-config-values-td16449.html

### Remote monitoring

The app is running on my raspberry pi which is connected to a powerbank and to the camera.

I trigger the `ettl` app from a machine remote. In order to SSH into the raspberry pi in the middle of nowhere you need a phone.

Turn on tethering on the phone and connect the raspberry pi and your computer via wifi. (raspberry pi needs to be setup to connect when you have screen etc.)

Then just ssh into the raspberry pi from the computer
`ssh -o ConnectTimeout=5 pi@172.20.10.12`

## Install on raspberry pi

* Installing raspbian on SD card
    * [Install Raspberry Pi OS using Raspberry Pi Imager](https://www.raspberrypi.org/software/)
    * Fully fledged version
* Install hotspot on the pi - so you can use it anywhere without wifi
    * [Autohotspot](https://www.raspberryconnect.com/projects/65-raspberrypi-hotspot-accesspoints/183-raspberry-pi-automatic-hotspot-and-static-hotspot-installer)
    * I have to modify the `nameserver` dns lookup list after this
        * open file `sudo nano /etc/resolv.conf` and add
            * `nameserver 8.8.8.8`
            * `nameserver 8.8.4.4`
* Install ettl app
    * Use the link from the beginning of this README file to download the installer
