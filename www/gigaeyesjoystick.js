var exec = require('cordova/exec');

exports.watch = function(moviePath, success, error) {
    exec(success, error, "GigaeyesJoystick", "watch", [moviePath]);
};

exports.watch = function(moviePath, camName, camId, recordState, isFavorite, success, error) {
    exec(success, error, "GigaeyesJoystick", "watch", [moviePath,camName, camId, recordState, isFavorite]);
};
