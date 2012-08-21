//
//  BindleAppDelegate.m
//  
//
//  Created by Steven Kemper on 5/15/12.
//

#import "BindleAppDelegate.h"
#import "SwarmViewController.h"
#import "AukViewController.h"
#import "NGlobals.h"
#import "NAppIDAuk.h"
#import "NCommandAuk.h"
#import "NGrain.h"
#import "NSand.h"

@implementation BindleAppDelegate

@synthesize window = _window;
@synthesize appSand;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
        
    
    // Override point for customization after application launch.
    [[UIApplication sharedApplication] setStatusBarHidden:YES withAnimation:UIStatusBarAnimationFade];
    appSand = [[NSand alloc] init]; 
    
    // SAND:  set a pointer inside appSand so we get notified when network data is available
    [appSand setDelegate:self];
    AukViewController *avc = [[AukViewController alloc] init];
    [[self window] setRootViewController:avc];
    [self.window makeKeyAndVisible];
    [[UIApplication sharedApplication] setIdleTimerDisabled:YES]; //Prevents device from sleeping
    
    //Code to turn volume all the way up--needs MediaPlayer framework
//    MPMusicPlayerController *musicPlayer = [MPMusicPlayerController applicationMusicPlayer];
//    musicPlayer.volume = 1;

    //   self.window.backgroundColor = [UIColor whiteColor];
    
    return YES;
}

// input data function ===================================================================

- (void)dataReadyHandle:(NGrain *)inGrain;
{

    CLog(@"App Delegate dataReadyHandle");
}


- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
