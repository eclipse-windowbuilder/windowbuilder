/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
//
//  wbp.m
//  wbp-cocoa
//
//  Created by Alexander Mitin on 3/31/09.
//

#import "wbp.h"
#import "utils.h"
#import "MenuHookView.h"
/**
 * Captures a view contents using [NSView displayRectIgnoringOpacity: inContext:] 
 */
static void renderImage(NSView* view, NSGraphicsContext* context) {
	NSRect bounds = [view bounds];
	// store context
	[NSGraphicsContext saveGraphicsState];
	[NSGraphicsContext setCurrentContext:context];
	// apply transform
	NSAffineTransform* transform = [NSAffineTransform transform];
	[transform translateXBy:0 yBy:bounds.size.height];
	[transform scaleXBy:1 yBy:-1];
	[transform concat];
	// render
	[view displayRectIgnoringOpacity:bounds inContext:context];
	// restore context
	[NSGraphicsContext restoreGraphicsState];
}
/**
 * Captures a view contents using [NSBitmapImageRep initWithFocusedViewRect] 
 */
static void renderImage2(NSView* view, NSGraphicsContext* context) {
	NSRect bounds = [view bounds];
	NSSize imageSize = bounds.size;
	
	[view lockFocus];
	NSBitmapImageRep* bitmapRep = [[NSBitmapImageRep alloc] initWithFocusedViewRect:
								   NSMakeRect(0, 0, imageSize.width, imageSize.height)];
	[view unlockFocus];
	//
	NSImage* image = [[NSImage alloc] initWithSize:imageSize];
	[image addRepresentation:bitmapRep];
	[bitmapRep release];
	//
	[NSGraphicsContext saveGraphicsState];
	[NSGraphicsContext setCurrentContext:context];
	// apply transform
	NSAffineTransform* transform = [NSAffineTransform transform];
	[transform translateXBy:0 yBy:bounds.size.height];
	[transform scaleXBy:1 yBy:-1];
	[transform concat];
	NSRect rect = NSMakeRect(0, 0, imageSize.width, imageSize.height);
	[image drawAtPoint:rect.origin fromRect:rect operation:NSCompositeSourceOver fraction:1.0];
	[image release];
	[NSGraphicsContext restoreGraphicsState];
}

JNIEXPORT void JNICALL OS_NATIVE(_1orderOut)
(JNIEnv* env, jobject this, JHANDLE jwindow)
{	
	NSWindow* window = (NSWindow*)unwrap_pointer(env, jwindow);
	[window orderOut:window];
}

JNIEXPORT void JNICALL OS_NATIVE(_1makeWindowShot)
(JNIEnv* env, jobject this, JHANDLE jview, JHANDLE jcontext)
{	
	NSView* view = (NSView*)unwrap_pointer(env, jview);
	NSGraphicsContext* context = (NSGraphicsContext*)unwrap_pointer(env, jcontext);
	NSView* superView = [view superview];
	while (superView != nil) {
		view = superView;
		superView = [view superview];
	}
	renderImage(view, context);
}

JNIEXPORT void JNICALL OS_NATIVE(_1makeShot)
(JNIEnv* env, jobject this, JHANDLE jview, JHANDLE jparentView, JHANDLE jcontext)
{	
	NSView* view = (NSView*)unwrap_pointer(env, jview);
	NSView* parentView = (NSView*)unwrap_pointer(env, jparentView);
	NSGraphicsContext* context = (NSGraphicsContext*)unwrap_pointer(env, jcontext);
	//
	while (1) {
		NSView* superView = [view superview];
		BOOL equal = [superView isEqual:parentView];
		if (equal) {
			break;
		}
		view = superView;
	}
	renderImage(view, context);
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getMenuBarHeight)
(JNIEnv* env, jobject this)
{	
	NSMenu *mainMenu = [NSApp mainMenu];
	return (jint)[mainMenu menuBarHeight];
}

JNIEXPORT void JNICALL OS_NATIVE(_1setAlpha)
(JNIEnv* env, jobject this, JHANDLE shellHandle, jint alpha)
{	
	NSWindow* window = (NSWindow*)unwrap_pointer(env, shellHandle);
	alpha &= 0xFF;
	[window setAlphaValue: (alpha / 255)];
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)
(JNIEnv* env, jobject this, JHANDLE shellHandle)
{	
	NSWindow* window = (NSWindow*)unwrap_pointer(env, shellHandle);
	CGFloat alpha = [window alphaValue];
	return (jint)(alpha * 255);
}

////////////////////////////////////////////////////////////////////////////
//
// Menu 
//
////////////////////////////////////////////////////////////////////////////
// menu globals
MenuRef carbonMenu;
jint *menuSize;
jint *itemsSizes;
NSView *fakeView;
NSGraphicsContext* menuDrawContext;

// hook for -[NSMenuItem _viewHandlesEvents];
static Boolean hooked(id self, SEL _cmd) {
	return NO;
}
// hook for -[NSMenuItem view]
static id hooked2(id self, SEL _cmd) {
	return fakeView;
}
// menu handlers
static OSStatus menuHandlerProc(EventHandlerCallRef caller, EventRef event, void* refcon) {
	UInt32   eventKind = GetEventKind(event);
	OSStatus result = eventNotHandledErr;
	
	switch (eventKind) {
		case kEventMenuEnableItems:
			{
				// change Carbom menu items to be owner draw. 
				NSMenu* menu = (NSMenu*)refcon;
				int itemsCount = [menu numberOfItems];
				for (int i = 1; i < itemsCount + 1; ++i) {
					ChangeMenuItemAttributes_(carbonMenu, i, kMenuItemAttrCustomDraw, 0);
				}
			}
			break;
		case kEventMenuClosed:
			{
				NSMenu* menu = (NSMenu*)refcon;
				int itemsCount = [menu numberOfItems];
				// try to get the whole menu image and bound
				for (int i = 0; i < itemsCount; ++i) {
					NSMenuItem *item = [menu itemAtIndex:i];
					NSView *view = [item view];
					if ([view isKindOfClass:[MenuHookView class]]) {
						MenuHookView *hookView = (MenuHookView*)view;
						// get the screenshot from menu view
						NSWindow* menuWindow = [hookView menuWindow]; // NSCarbonMenuWindow
						if (menuWindow != nil) {
							NSView* menuView = [menuWindow contentView]; // NSCarbonWindowContentView
							if (menuView != nil) {
								renderImage2(menuView, menuDrawContext);
							}
						}
						break;
					}
				}
				// restore owner draw attr 
				for (int i = 1; i < itemsCount + 1; ++i) {
					ChangeMenuItemAttributes_(carbonMenu, i, 0, kMenuItemAttrCustomDraw);
				}
			}
			break;
		case 104:
		case 105:
			{
				// AppKit 'eats' these event kinds.
				// i.e., it handles these events which sent by default HIToolBox menu handler and returns noErr,
				// resulting the default menu handler never invoked for these event kinds.
				// So, kEventMenuMeasureItemHeight/Width returns default menu width/height.
				id clazz = objc_getClass("NSMenuItem");
				// hook -[NSMenuItem _viewHandlesEvents]
				SEL mSel = @selector(_viewHandlesEvents);
				Method m = class_getInstanceMethod(clazz, mSel);
				IMP old = method_setImplementation(m, (IMP)hooked);
				// hook -[NSMenuItem view];
				SEL mSel2 = @selector(view);
				Method m2 = class_getInstanceMethod(clazz, mSel2);
				IMP old2 = method_setImplementation(m2, (IMP)hooked2);
				// let AppKit and default handler do the job
				CallNextEventHandler(caller, event);
				// restore back
				method_setImplementation(m, old);
				method_setImplementation(m2, old2);
				// yes, handled
				return noErr;
			}
			break;		
		case kEventMenuDrawItem:
			{
				// let the other handlers to finish their work
				result = CallNextEventHandler(caller, event);
				// get item index and bounds
				MenuItemIndex itemIndex;
				GetEventParameter(event, kEventParamMenuItemIndex, typeMenuItemIndex, NULL, sizeof(itemIndex), NULL, &itemIndex);
				Rect itemBounds;
				GetEventParameter(event, kEventParamMenuItemBounds, typeQDRectangle, NULL, 	sizeof(itemBounds), NULL, &itemBounds);
				Rect viewRect;
				GetEventParameter(event, kEventParamCurrentBounds, typeQDRectangle, NULL, 	sizeof(viewRect), NULL, &viewRect);
				// store bounds
				*(itemsSizes + (itemIndex - 1) * 4 + 0) = 0;                                          
				*(itemsSizes + (itemIndex - 1) * 4 + 1) = itemBounds.top - viewRect.top + 6; // small correction
				*(itemsSizes + (itemIndex - 1) * 4 + 2) = itemBounds.right - itemBounds.left;                              
				*(itemsSizes + (itemIndex - 1) * 4 + 3) = itemBounds.bottom - itemBounds.top;
			}
			break;
		case 106:
			{
				// this event sent by default HIToolBox handler for kEventMenuItemDrawContents
				// AppKit 'eats' this event and doesn't call the default menu handler, so items are not drawn
				NSMenu* menu = (NSMenu*)refcon;
				// get item index
				MenuItemIndex itemIndex;
				GetEventParameter(event, kEventParamMenuItemIndex, typeMenuItemIndex, NULL, sizeof(itemIndex), NULL, &itemIndex);
				// get Cocoa menu item and set it's state to mixed
				NSMenuItem *item = [menu itemAtIndex: itemIndex - 1];
				NSInteger oldState = [item state];
				[item setState: NSMixedState];
				// set mixed state image to any other
				NSImage* oldmixedImage = [item mixedStateImage];
				[item setMixedStateImage:[NSImage imageNamed:NSImageNameIconViewTemplate]];
				// call other handlers in chain; AppKit now won't 'eat' this event
				CallNextEventHandler(caller, event);
				// restore back
				[item setMixedStateImage:oldmixedImage];
				[item setState:oldState];
				// yes, handled
				return noErr;
			}
		default:
			break;
	}

	return result;
}
static OSStatus menuHandlerProc2(EventHandlerCallRef caller, EventRef event, void* refcon) {
	UInt32   eventKind = GetEventKind(event);
	OSStatus result = eventNotHandledErr;
	
	switch (eventKind) {
		case kEventMenuClosed:
		{
			NSMenu* menu = (NSMenu*)refcon;
			int itemsCount = [menu numberOfItems];
			// try to get the whole menu image and bound
			for (int i = 0; i < itemsCount; ++i) {
				NSMenuItem *item = [menu itemAtIndex:i];
				NSView *view = [item view];
				if ([view isKindOfClass:[MenuHookView class]]) {
					MenuHookView *hookView = (MenuHookView*)view;
					// get the screenshot from menu view
					NSWindow* menuWindow = [hookView menuWindow]; // NSCarbonMenuWindow
					if (menuWindow != nil) {
						NSView* menuView = [menuWindow contentView]; // NSCarbonWindowContentView
						if (menuView != nil) {
							// fill bounds
							NSRect frame = [menuView frame];
							menuSize[0] = frame.origin.x;
							menuSize[1] = frame.origin.y;
							menuSize[2] = frame.size.width;
							menuSize[3] = frame.size.height;
						}
					}
					break;
				}
			}
		}
			break;
	}
	
	return result;
}
static MenuRef bringCarbonMenuToLife(NSMenu *menu) {
	// AppKit initializes Carbon menu while menu added to some item as submenu
	// trick is to create temp item, set this menu as it's submenu and add menu item into the main menu
	NSMenu *mainMenu = [NSApp mainMenu];
	NSMenuItem *fakeMenuItem = [mainMenu addItemWithTitle: @"sub" action: NULL keyEquivalent: @""];
	// store old parent item and remove this menu from this item
	NSMenuItem *oldParentItem = nil;
	NSMenu* oldParentMenu = [menu supermenu];
	if (oldParentMenu) {
		NSInteger index = [oldParentMenu indexOfItemWithSubmenu:menu];
		oldParentItem = [oldParentMenu itemAtIndex:index];
		if (oldParentItem) {
			[oldParentItem setSubmenu: nil];
		}
	}
	// add and remove
	[fakeMenuItem setSubmenu: menu];
	[fakeMenuItem setSubmenu: nil];
	[mainMenu removeItem: fakeMenuItem];
	// restore old
	if (oldParentItem) {
		[oldParentItem setSubmenu: menu];
	}
	MenuRef menu_ = NSGetCarbonMenu_(menu);
	return menu_;
}
static BOOL initializeMenuRelated() {
	// dynamically load and bind nesessary functions since they are unavailable in headers using 64-bit arch.
	static BOOL initialized = NO;
	static BOOL initDone = NO;
	if (!initDone) { 
		CancelMenuTracking_ = NULL;
		GetMenuEventTarget_ = NULL;
		ChangeMenuItemAttributes_ = NULL;
		NSGetCarbonMenu_ = NULL;
		CFBundleRef carbonBundle = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.Carbon"));
		if (carbonBundle) {
			CancelMenuTracking_ = CFBundleGetFunctionPointerForName(carbonBundle, CFSTR("CancelMenuTracking"));
			GetMenuEventTarget_ = CFBundleGetFunctionPointerForName(carbonBundle, CFSTR("GetMenuEventTarget"));
			ChangeMenuItemAttributes_ = CFBundleGetFunctionPointerForName(carbonBundle, CFSTR("ChangeMenuItemAttributes"));
		}
		CFBundleRef cocoaBundle = CFBundleGetBundleWithIdentifier(CFSTR("com.apple.Cocoa"));
		if (cocoaBundle) {
			NSGetCarbonMenu_ = CFBundleGetFunctionPointerForName(cocoaBundle, CFSTR("_NSGetCarbonMenu"));
		}
		initialized = CancelMenuTracking_ && GetMenuEventTarget_ && ChangeMenuItemAttributes_ && NSGetCarbonMenu_;
		initDone = YES;
	}	
	return initialized;
}

static void prepareAndTrackPopupMenu(NSMenu* menu, EventHandlerUPP handler, const EventTypeSpec* events, int eventsCount) {
	// install event handlers
	EventHandlerRef menuEventRef;
	InstallMenuEventHandler(carbonMenu, handler, eventsCount, events, menu, &menuEventRef);
	// setup menu item hook view to get drawn view
	fakeView = [[NSView alloc] init];
	MenuHookView* hookView = [[MenuHookView alloc] initWithFrame:NSMakeRect(0, 0, 1, 1) andMenu:menu]; 
	NSMenuItem* hookItem = [[NSMenuItem allocWithZone:nil] initWithTitle:@"" action:NULL keyEquivalent:@""];
	[hookItem setView:hookView];
	[hookView release];
	[menu addItem:hookItem];
	// track menu as popup menu
	NSEvent *nsEvent = 
	[NSEvent otherEventWithType: NSApplicationDefined 
					   location:NSMakePoint(10000.0, 10000.0) 
				  modifierFlags:0
					  timestamp:0.0
				   windowNumber:[[NSApp mainWindow] windowNumber]
						context: [NSGraphicsContext currentContext]
						subtype:0
						  data1:0 
						  data2:0];
	[NSMenu popUpContextMenu:menu withEvent:nsEvent forView:[[NSApp mainWindow] contentView]];
	// remove fake item as no longer needed
	[menu removeItem:hookItem];
	// do some cleanup
	[hookItem release];
	[fakeView release];
	RemoveEventHandler(menuEventRef);
}

JNIEXPORT void JNICALL OS_NATIVE(_1fetchPopupMenuBounds)
(JNIEnv* env, jobject this, JHANDLE menuHandle, jintArray jmenuSize) {
	NSMenu *menu = (NSMenu*)unwrap_pointer(env, menuHandle);
	if (!initializeMenuRelated()) {
		// don't do anything if not completely initialized
		return;
	}	
	// get Carbon menu
	carbonMenu = bringCarbonMenuToLife(menu);
	if (carbonMenu == NULL) {
		return;
	}
	// prepare data
	jsize sizesSize = (*env)->GetArrayLength(env, jmenuSize); 
	menuSize = malloc(sizesSize * sizeof(jint));
	// install event handlers for storing menu bounds
	EventHandlerUPP menuHandler = NewEventHandlerUPP(menuHandlerProc2);
	static const EventTypeSpec kMenuEvents[] =
	{
		{kEventClassMenu, kEventMenuClosed},
	};
	// track the menu
	prepareAndTrackPopupMenu(menu, menuHandler, kMenuEvents, GetEventTypeCount(kMenuEvents));
	// now we should have menu sizes
	// so, set the values back to java
	(*env)->SetIntArrayRegion(env, jmenuSize, 0, sizesSize, menuSize);   
	// free memory
	free(menuSize);
	// all done
}

JNIEXPORT void JNICALL OS_NATIVE(_1fetchPopupMenuVisualData)
(JNIEnv* env, jobject this, JHANDLE menuHandle, JHANDLE jcontext, jintArray jmenuItemsSizes) {	
	NSMenu *menu = (NSMenu*)unwrap_pointer(env, menuHandle);
	menuDrawContext = (NSGraphicsContext*)unwrap_pointer(env, jcontext);
	if (!initializeMenuRelated()) {
		// don't do anything if not completely initialized
		return;
	}	
	// get Carbon menu
	carbonMenu = bringCarbonMenuToLife(menu);
	if (carbonMenu == NULL) {
		return;
	}
	// prepare data
	jsize itemsSizesSize = (*env)->GetArrayLength(env, jmenuItemsSizes); 
	itemsSizes = malloc(itemsSizesSize * sizeof(jint));
	// install event handlers for storing item bounds
	EventHandlerUPP menuHandler = NewEventHandlerUPP(menuHandlerProc);
	// 104, 105, 106 events are send by HIToolKit's
	// the problem is that there is AppKit's event handler in between our handler and HIToolBox default menu handler
	// and the AppKit 'eats' some menu event. The workaround is to force AppKit to call default menu handler.
	static const EventTypeSpec kMenuEvents[] =
	{
		{kEventClassMenu, kEventMenuEnableItems},
		{kEventClassMenu, kEventMenuDrawItem},
		{kEventClassMenu, kEventMenuClosed},
		{kEventClassMenu, 105 }, // internally send by HIToolKit's kEventMenuMeasureItemHeight default handler
		{kEventClassMenu, 104 }, // internally send by HIToolKit's kEventMenuMeasureItemWidth default handler
		{kEventClassMenu, 106 }, // internally send by HIToolKit's kEventMenuDrawItemContents default handler
	};
	// track the menu
	prepareAndTrackPopupMenu(menu, menuHandler, kMenuEvents, GetEventTypeCount(kMenuEvents));
	// now we should have the image and sizes
	// so, set the values back to java
	(*env)->SetIntArrayRegion(env, jmenuItemsSizes, 0, itemsSizesSize, itemsSizes);   
	// free memory
	free(itemsSizes);
	// all done
}

