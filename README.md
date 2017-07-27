

PTZ 조이스텍 제어를 위한 Cordova UI 플러그인.

## install
ionic cordova plugin add https://github.com/JAEGYUN/gigaeyesJoystick.git

## libVLC 지원 (only Android)

## Using

``` javascript
cordova.plugins.gigaeyesjoystick.watch("rtsp://10.0.0.100:554/video", callbackSucces, callbackError);
```

``` javascript
(only iphone.)
cordova.plugins.gigaeyesjoystick.watch("rtsp://10.0.0.100:554/video", "title명", callbackSucces, callbackError);
```


## return status

* T:U  MOVE_UP
* T:D  MOVE_DOWN
* P:L  MOVE_LEFT
* P:R  MOVE_RIGHT
* Z:I  ZOOM_IN
* Z:O  ZOOM_OUT

## 플러그인 종료시 이벤트
* ok


