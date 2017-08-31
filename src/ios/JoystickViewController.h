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
@property (retain, nonatomic) NSString* camId;
@property (retain, nonatomic) NSString* recordState;
@property (retain, nonatomic) NSString* isFavorite;
@end
