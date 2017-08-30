#import "GigaeyesJoystick.h"
#import "JoystickViewController.h"
#import <SGPlayer/SGPlayer.h>

@interface JoystickViewController (){
    BOOL isHidden;
    BOOL isMove;
    BOOL isPinch;
    CGFloat pinchScale;
    UIActivityIndicatorView *spinner;
    NSOperationQueue *opQueue;
}

//@property (nonatomic, retain) NSTimer *nextFrameTimer;
@property (nonatomic, strong) SGPlayer * player;

@property (weak, nonatomic) IBOutlet UILabel *stateLabel;
@property (weak, nonatomic) IBOutlet UILabel *titleLabel;
@property (weak, nonatomic) IBOutlet UISlider *progressSilder;
@property (weak, nonatomic) IBOutlet UILabel *currentTimeLabel;
@property (weak, nonatomic) IBOutlet UILabel *totalTimeLabel;
@property (weak, nonatomic) IBOutlet UINavigationBar *navigationBar;
@property (weak, nonatomic) IBOutlet UINavigationItem *navigationBarTitle;
@property (weak, nonatomic) IBOutlet UIImageView *joystickImageView;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *indicator;

@property (nonatomic, assign) BOOL progressSilderTouching;
@end

@implementation JoystickViewController

// Load with xib :)
- (id) initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    
    isHidden = NO;
    isMove = NO;
    isPinch = NO;
    return self;
}


- (void)viewDidLoad {
    [super viewDidLoad];
    
    // 플레이어 호출 부분
    self.view.backgroundColor = [UIColor blackColor];
    
    [self.indicator startAnimating];
    // 플레이어 등록
    self.player = [SGPlayer player];
    
    // callback handler 등록
    [self.player registerPlayerNotificationTarget:self
                                stateAction:@selector(stateAction:)
                                progressAction:@selector(progressAction:)
                                playableAction:@selector(playableAction:)
                                errorAction:@selector(errorAction:)];
     [self.navigationBarTitle.title setValue:self.camName forKey:self.camName];
    // 탭하여 화면 재생
    [self.player setViewTapAction:^(SGPlayer * _Nonnull player, SGPLFView * _Nonnull view) {
        NSLog(@"player display view did click!");
        
        if(isMove){
            NSString* moveType  = @"S";
            [self.origem.commandDelegate runInBackground:^{
            //        NSDictionary *jsonInfo =
                CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
                pluginResult.keepCallback = [NSNumber numberWithBool:YES];
                [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
            }];
            isMove = NO;
        }else{
            isHidden = !isHidden;
            [self hiddenBar];
        }
    }];
    [self.view insertSubview:self.player.view atIndex:0];

    // 캠명...   
    self.navigationBarTitle.title = self.camName;
    
    NSLog(@"요청 URL %@", self.videoAddress);
    // URL을 UTF-8로 변환하여 저장(NSString --> NSURL)
    NSURL* urlString =  [NSURL URLWithString:[self.videoAddress stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];

    NSLog(@"요청 URL Check %@", [urlString absoluteString]);
    
    // 플레이어 디코더 선택...AVPlayer와 FFmepgDecoder 또는 모두 사용가능하나 FFmpeg을 사용하도록 설정
    // self.player.decoder =  [SGPlayerDecoder decoderByFFmpeg];
    self.player.decoder =  [SGPlayerDecoder decoderByDefault];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"tcp" forKey:@"rtsp_transport"];
    [self.player.decoder setFFmpegCodecContextOptionStringValue :@"prefer_tcp" forKey:@"rtsp_flags"];
    
   // 하드웨어 가속
    self.player.decoder.hardwareAccelerateEnableForFFmpeg = YES;
    // 자동재생
    // self.player.backgroundMode = SGPlayerBackgroundModeContinue;
    self.player.backgroundMode = SGPlayerBackgroundModeAutoPlayAndPause;
    // 일반 영상 재생
    [self.player replaceVideoWithURL:urlString];
    [self addTapGesture];
    
    opQueue = [[NSOperationQueue alloc] init];
    opQueue.maxConcurrentOperationCount = 1; // set to 1 to force everything to run on one thread;
    self.joystickImageView.userInteractionEnabled = YES;
//    self.joystickImageView.
//    UISwipeGestureRecognizer * swipe = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipe:)];
//    swipe.numberOfTouchesRequired = 1;
//    [swipe setDirection: UISwipeGestureRecognizerDirectionUp|UISwipeGestureRecognizerDirectionDown|UISwipeGestureRecognizerDirectionRight|UISwipeGestureRecognizerDirectionLeft];
//    [self.view addGestureRecognizer:swipe];
    [self addGesture];
}

-(void)addGesture {
    UISwipeGestureRecognizer *rightSwipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipe:)];
    rightSwipeGestureRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
    [self.view addGestureRecognizer:rightSwipeGestureRecognizer];
    
    UISwipeGestureRecognizer *leftSwipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipe:)];
    leftSwipeGestureRecognizer.direction = UISwipeGestureRecognizerDirectionLeft;
    [self.view addGestureRecognizer:leftSwipeGestureRecognizer];
    
    UISwipeGestureRecognizer *upSwipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipe:)];
    upSwipeGestureRecognizer.direction = UISwipeGestureRecognizerDirectionUp;
    [self.view addGestureRecognizer:upSwipeGestureRecognizer];
    
    UISwipeGestureRecognizer *downSwipeGestureRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(handleSwipe:)];
    downSwipeGestureRecognizer.direction = UISwipeGestureRecognizerDirectionDown;
    [self.view addGestureRecognizer:downSwipeGestureRecognizer];
    
    UIPinchGestureRecognizer *zoomInGestureRecognizer = [[UIPinchGestureRecognizer alloc] initWithTarget:self action:@selector(handlePinch:)];
     [self.view addGestureRecognizer:zoomInGestureRecognizer];
}

- (void) hiddenBar{
    [self.navigationBar setHidden:isHidden];

}

-(void) addJoystickImageView{
    self.joystickImageView.userInteractionEnabled = YES;
}

- (void)viewDidLayoutSubviews
{
    [super viewDidLayoutSubviews];
    self.player.view.frame = self.view.bounds;
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

-(void) viewDidAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [opQueue addOperationWithBlock:^{

    }];
}

- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)toInterfaceOrientation
{
    // enable both landscape modes
    return (toInterfaceOrientation == UIInterfaceOrientationLandscapeRight || toInterfaceOrientation == UIInterfaceOrientationLandscapeLeft);
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskLandscape;
}


-(void) addTapGesture {
    UITapGestureRecognizer *singleTap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tapImageView:)];
    [singleTap setNumberOfTapsRequired:1];
    
    UILongPressGestureRecognizer *longTap = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(longTapImageView:)];
    [longTap setNumberOfTapsRequired:0];
    [longTap setMinimumPressDuration:1.0];
    [singleTap requireGestureRecognizerToFail:longTap];
    UIImageView *get = self.joystickImageView; //(UIImageView*)[self.joystickImageView viewWithTag:100];
    
    [get setUserInteractionEnabled:YES];
    [get addGestureRecognizer:singleTap];
    [get addGestureRecognizer:longTap];
}

-(void) imageTap {
    
    isHidden = !isHidden;
    int direction;
    
    if(isHidden) {
        direction = -1;
    } else {
        direction = 1;
    }
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5f];
    
    
    [UIView commitAnimations];
}

- (IBAction)buttonDismissPressed:(id)sender {
}

- (IBAction)back:(id)sender
{
    NSLog(@"뒤로가기 요청 : %@", sender);
    [self.presentingViewController dismissViewControllerAnimated:YES completion:nil ];
}

- (IBAction)play:(id)sender
{
    [self.player play];
}

- (IBAction)pause:(id)sender
{
    [self.player pause];
}

- (IBAction)progressTouchDown:(id)sender
{
    self.progressSilderTouching = YES;
}

- (IBAction)progressTouchUp:(id)sender
{
    self.progressSilderTouching = NO;
    [self.player seekToTime:self.player.duration * self.progressSilder.value];
}

- (void)stateAction:(NSNotification *)notification
{
    SGState * state = [SGState stateFromUserInfo:notification.userInfo];
    
    NSString * text;
    switch (state.current) {
        case SGPlayerStateNone:
            text = @"None";
            break;
        case SGPlayerStateBuffering:
            text = @"Buffering...";
            break;
        case SGPlayerStateReadyToPlay:
            text = @"Prepare";
            self.totalTimeLabel.text = [self timeStringFromSeconds:self.player.duration];
            [self.player play];
            break;
        case SGPlayerStatePlaying:
            text = @"Playing";
            [self.indicator setHidden:YES];
            break;
        case SGPlayerStateSuspend:
            text = @"Suspend";
            break;
        case SGPlayerStateFinished:
            text = @"Finished";
            break;
        case SGPlayerStateFailed:
            text = @"Error";
            break;
    }
    self.stateLabel.text = text;
}

// 프로그레스 바 액션
- (void)progressAction:(NSNotification *)notification
{
    SGProgress * progress = [SGProgress progressFromUserInfo:notification.userInfo];
    if (!self.progressSilderTouching) {
        self.progressSilder.value = progress.percent;
    }
    self.currentTimeLabel.text = [self timeStringFromSeconds:progress.current];
}


- (void)playableAction:(NSNotification *)notification
{
    SGPlayable * playable = [SGPlayable playableFromUserInfo:notification.userInfo];
    NSLog(@"playable time : %f", playable.current);
}

// 에러 액션
- (void)errorAction:(NSNotification *)notification
{
    SGError * error = [SGError errorFromUserInfo:notification.userInfo];
    NSLog(@"player did error : %@", error.error);
}

//재생시간 표시
- (NSString *)timeStringFromSeconds:(CGFloat)seconds
{
    return [NSString stringWithFormat:@"%ld:%.2ld", (long)seconds / 60, (long)seconds % 60];
}

- (void)dealloc
{
    [self.player removePlayerNotificationTarget:self];
}

// Joystick 이벤트 처리
- (void)tapImageView:(UITapGestureRecognizer *)recognizer
{
// 상단 타이틀 바 show/hidden
    isHidden = !isHidden;
    int direction;
    
    if(isHidden) {
        direction = -1;
    } else {
        direction = 1;
    }
    
    [UIView beginAnimations:nil context:nil];
    [UIView setAnimationDuration:0.5f];
    
    
    [UIView commitAnimations];
    
// joystic click event
    CGPoint location = [recognizer locationInView:self.joystickImageView];
    if ([self.joystickImageView pointInside:location withEvent:nil]) {
        
        
        NSString* moveType  = @"S";
        [self.origem.commandDelegate runInBackground:^{
            //        NSDictionary *jsonInfo =
            CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
            pluginResult.keepCallback = [NSNumber numberWithBool:YES];
            [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
        }];
    }
}

- (void)longTapImageView:(UILongPressGestureRecognizer *)recognizer
{
    
    CGPoint location = [recognizer locationInView:self.joystickImageView];
    
    if(recognizer.state == UIGestureRecognizerStateBegan){
    if ([self.joystickImageView pointInside:location withEvent:nil]) {
        NSLog(@"longTouch UIGestureRecognizerStateBegan");
        int imgW = self.joystickImageView.bounds.size.width;
        
        int ww = imgW;
        int hw = ww/2;
        
        int pl = (location.x - hw)*(location.x - hw) + (location.y - hw)*(location.y - hw);
        
        int r1 = hw * hw;
        int r2 = (hw*3/4) * (hw*3/4);
        int r3 = (hw*5/8) * (hw*5/8);
        
        NSString* moveType ;
        
        
        
        if(pl <= r1 && pl > r2){
            // 네방향
            if(location.y - location.x > 0){              // left, down
                if(location.y + location.x - ww > 0) {   // down
                    NSLog(@" click event Down!!! %f , %f",location.x, location.y);
                    moveType = @"T:D";
                    
                }else{                      // left
                    NSLog(@" click event LEFT!!! %f , %f",location.x, location.y);
                    moveType = @"P:L";
                }
            }else{              // right, up
                if(location.x + location.y - ww > 0) {   // right
                    NSLog(@" click event RIGHT!!! %f , %f",location.x, location.y);
                    moveType = @"P:R";
                }else{                      // up
                    NSLog(@" click event UP!!! %f , %f",location.x, location.y);
                    moveType = @"T:U";
                }
            }
            
        }else if(pl < r3){
            // zoomIn, zoomOut
            if(location.y - hw > 0){
                NSLog(@" click event Zoom Out!!! %f , %f",location.x, location.y);
                moveType = @"Z:O";
            }else{
                NSLog(@" click event Zoom IN!!! %f , %f",location.x, location.y);
                moveType = @"Z:I";
            }
        }
        
        
            [self.origem.commandDelegate runInBackground:^{
                //        NSDictionary *jsonInfo =
                CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
                pluginResult.keepCallback = [NSNumber numberWithBool:YES];
                [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
            }];
    }
    }else if(recognizer.state == UIGestureRecognizerStateEnded){
        NSLog(@"longTouch UIGestureRecognizerStateEnded");
        NSString* moveType  = @"S";
        [self.origem.commandDelegate runInBackground:^{
            //        NSDictionary *jsonInfo =
            CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
            pluginResult.keepCallback = [NSNumber numberWithBool:YES];
            [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
        }];

    }
}

- (void)handleSwipe:(UISwipeGestureRecognizer*)recognizer {
    
    NSString* moveType ;
    if ( recognizer.direction == UISwipeGestureRecognizerDirectionLeft ){
        NSLog(@" *** SWIPE LEFT ***");
        moveType = @"P:L";
    }
    if ( recognizer.direction == UISwipeGestureRecognizerDirectionRight ){
        NSLog(@" *** SWIPE RIGHT ***");
        moveType = @"P:R";
    }
    if ( recognizer.direction== UISwipeGestureRecognizerDirectionUp ){
        NSLog(@" *** SWIPE UP ***");
         moveType = @"T:U";

    }
    if ( recognizer.direction == UISwipeGestureRecognizerDirectionDown ){
        NSLog(@" *** SWIPE DOWN ***");
        moveType = @"T:D";
    }
    
    [self.origem.commandDelegate runInBackground:^{
        //        NSDictionary *jsonInfo =
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
        pluginResult.keepCallback = [NSNumber numberWithBool:YES];
        [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
    }];
    
    isMove = YES;
}

- (void)handlePinch:(UIPinchGestureRecognizer*)recognizer {
    
        if ( recognizer.state == UIGestureRecognizerStateBegan ){
        NSLog(@" *** Pinch START *** %f",recognizer.scale);
        pinchScale = recognizer.scale;
        isPinch = YES;
    }
    if ( recognizer.state == UIGestureRecognizerStateEnded ){
        NSLog(@" *** Pinch End *** %f",recognizer.scale);
        NSString* moveType ;

        if((pinchScale - recognizer.scale )>0){
            moveType = @"Z:O";
        }else {
            moveType = @"Z:I";
        }
        isPinch = NO;
        [self.origem.commandDelegate runInBackground:^{
            //        NSDictionary *jsonInfo =
            CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: moveType];
            pluginResult.keepCallback = [NSNumber numberWithBool:YES];
            [self.origem.commandDelegate sendPluginResult:pluginResult callbackId:self.origem.lastCommand.callbackId];
        }];
        
        isMove = YES;
    }
    
  
}
@end

