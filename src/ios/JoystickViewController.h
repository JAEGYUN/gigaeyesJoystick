#import <UIKit/UIKit.h>

@class GigaeyesJoystick;

@interface JoystickViewController : UIViewController {
    BOOL getFrame;
    float lastFrameTime;
}

-(void) imageTap;

@property (retain, nonatomic) GigaeyesJoystick* origem;
@property (retain, nonatomic) NSString* videoAddress;
@property (retain, nonatomic) NSString* playType;
@property (retain, nonatomic) NSString* camName;
@end
