var exec = require('cordova/exec');

exports.watch = function(moviePath, success, error) {
    exec(success, error, "GigaeyesJoystick", "watch", [moviePath]);
};
