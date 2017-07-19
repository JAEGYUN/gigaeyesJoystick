#import <Cordova/CDVPlugin.h>
#import <Cordova/CDV.h>
#import <Foundation/Foundation.h>
#import "JoystickViewController.h"

@interface GigaeyesJoystick : CDVPlugin

- (void) watch : (CDVInvokedUrlCommand*) command;
- (void) finishOkAndDismiss;

@property (strong,nonatomic) CDVInvokedUrlCommand* lastCommand;
@property (strong,nonatomic) JoystickViewController* overlay;
@property (readwrite, assign) BOOL hasPendingOperation;

@end
