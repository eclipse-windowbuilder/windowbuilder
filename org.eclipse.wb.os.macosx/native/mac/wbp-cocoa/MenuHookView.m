//
//  MenuHookView.m
//  wbp-cocoa
//
//  Created by Alexander Mitin on 5/18/09.
//  Copyright 2009 __MyCompanyName__. All rights reserved.
//

#import "MenuHookView.h"
#import "wbp.h"

@implementation MenuHookView

- (id)initWithFrame:(NSRect)frame andMenu:(NSMenu*)menu
{
    self = [super initWithFrame:frame];
    if (self) {
        m_menu = menu;
		m_window = nil;
    }
    return self;
}

-(void)viewDidMoveToWindow 
{
	NSWindow *window = [self window];
	if (window) {
		m_window = window;
		[self cancelTracking];
	}
	[super viewDidMoveToWindow];
}

-(void)cancelTracking
{
	[m_menu cancelTracking];
	{
		if (NSGetCarbonMenu_ != NULL && CancelMenuTracking_ != NULL) {
			MenuRef carbonMenu = NSGetCarbonMenu_(m_menu);
			CancelMenuTracking_(carbonMenu, true, 0);
		}
	}
}

-(NSWindow*)menuWindow {
	return m_window;
}

@end
