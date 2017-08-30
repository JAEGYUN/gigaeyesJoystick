

PTZ 조이스텍 제어를 위한 Cordova UI 플러그인.

## install
ionic cordova plugin add https://github.com/JAEGYUN/gigaeyesJoystick.git

## libVLC 지원 (only Android)

## Using

``` javascript
cordova.plugins.gigaeyesjoystick.watch("rtsp://10.0.0.100:554/video","cam명","cam_id","record_status","isFavroites", callbackSucces, callbackError);
```


``` javascript
(only iphone.)
cordova.plugins.gigaeyesjoystick.watch("rtsp://10.0.0.100:554/video", "title명","cam_id","record_status","isFavroites" callbackSucces, callbackError);
```

참조하는 이미지를  gigaeyesPlayer의 것을 재사용하고 있으므로, 먼저 gigaeyesPlayer를 설치할 것.
moviePath, camName, camId, recordState, isFavorite, success, error


## record status
* Y 정상저장
* N 장애로 인한 저장실패
* ? 코드 미정 스케줄에 인한 저장 안함.

## 

## return status

* T:U  MOVE_UP
* T:D  MOVE_DOWN
* P:L  MOVE_LEFT
* P:R  MOVE_RIGHT
* Z:I  ZOOM_IN
* Z:O  ZOOM_OUT

## 플러그인 종료시 이벤트
* ok


