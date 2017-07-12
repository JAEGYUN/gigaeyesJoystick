

PTZ 조이스텍 제어를 위한 Cordova UI 플러그인.

## install
ionic cordova plugin add https://github.com/JAEGYUN/gigaeyesJoystick.git

## android build (Android Studio)

add code in build.gradle(Module:android)

```
buildscript {
    repositories{
        ...
        maven {
            url 'https://maven.google.com'
        }
    }
    ...
}
...
dependencies {
    ...
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
}     
```

## Using

``` javascript
cordova.plugins.gigaeyesjoystick.watch("rtsp://10.0.0.100:554/video", callbackSucces, callbackError);
```

## return status

* T:U  MOVE_UP
* T:D  MOVE_DOWN
* P:L  MOVE_LEFT
* P:R  MOVE_RIGHT
* Z:I  ZOOM_IN
* Z:O  ZOOM_OUT

