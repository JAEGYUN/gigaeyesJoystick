var exec = require('cordova/exec');

exports.watch = function(moviePath, success, error) {
    exec(success, error, "gigaeyesjoystick", "watch", [moviePath]);
};

exports.move = function(){
    var callbackSuccess = function(result){
        alert(result);
    }

    var callbackFail = function(err){
        alert(err);
    }

    exec(callbackSuccess, callbackFail,"gigaeyesjoystick","move",[]);
}