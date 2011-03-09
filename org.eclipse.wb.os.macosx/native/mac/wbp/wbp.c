/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
#include <Carbon/Carbon.h>
#include <stdio.h>

#include "wbp.h"

//#define DEBUG

static jint captureImage(HIViewRef controlRef, WindowRef windowRef) {
	#ifdef DEBUG
		printf("Entering native image capture call\n");
	#endif

	// The image handle of the resulting image
	CGImageRef imageHandle = (CGImageRef) 0;

	if (!IsValidWindowPtr(windowRef))
	{
		windowRef = NULL;
	}
			
	// Check to see if the controlHandle is a valid view ref
	if (HIViewIsValid(controlRef))
	{
			
		// Bounds rectangle of resulting image
		HIRect boundsRect;
		
		// Pointer to persist the current front process
		ProcessSerialNumber originalFrontProcess;
		
		// Pointer to persist the current running process (the SWT remote VM)
		ProcessSerialNumber swtVMProcess;
	
		// Grab the current front process (The current foreground application in OS X)
		GetFrontProcess(&originalFrontProcess);
		
		// Grab the process id of the SWT remote VM
		GetCurrentProcess(&swtVMProcess);

		// Set the SWT VM process as the front process		
		SetFrontProcess(&swtVMProcess);
		
		// Show the window and select it
		if (windowRef != NULL)
		{
			#ifdef DEBUG
				printf("Window reference %d is valid.\n", (int)shellHandle);
			#endif

			// Select the window			
			SelectWindow(windowRef);
		}
		
		// Create an image capture of the SWT VM
		HIViewCreateOffscreenImage(controlRef, 0, &boundsRect, &imageHandle);
		
		#ifdef DEBUG
			printf("Image width: %f height: %f\n", boundsRect.size.width, boundsRect.size.height);
		#endif
	
		// Restore the old front process
		SetFrontProcess(&originalFrontProcess);
	}

	#ifdef DEBUG
		printf("Exiting native image capture call\n");
		fflush( stdout );
    #endif
	
	// Return the handle of the created image to Java
  	return (jint)imageHandle;
}


MenuRef menu;
jint menuImage;
jint *menuSize;
jint *itemsSizes;
EventLoopTimerRef menuHideTimer;

OSStatus menuHandlerProc(EventHandlerCallRef caller, EventRef event, void* refcon) {
	// let the other handlers to finish their work
 	OSStatus result = CallNextEventHandler(caller, event);
	Rect itemBounds;
	MenuItemIndex itemIndex;
	// get item index and bounds
	GetEventParameter(event, kEventParamMenuItemBounds, typeQDRectangle, NULL, 	sizeof(itemBounds), NULL, &itemBounds);
	GetEventParameter(event, kEventParamMenuItemIndex, typeMenuItemIndex, NULL, sizeof(itemIndex), NULL, &itemIndex);
	*(itemsSizes + (itemIndex - 1) * 4 + 0) = itemBounds.left;                                          
	*(itemsSizes + (itemIndex - 1) * 4 + 1) = itemBounds.top;                                          
    *(itemsSizes + (itemIndex - 1) * 4 + 2) = itemBounds.right - itemBounds.left;                              
    *(itemsSizes + (itemIndex - 1) * 4 + 3) = itemBounds.bottom - itemBounds.top;
	// return the result of handlers' work
	return result;
}
pascal void menuHideTimerProc (EventLoopTimerRef theTimer, void* userData) {
	// cancel menu tracking
	CancelMenuTracking(menu, true, 0);
	if (menuHideTimer != NULL) {
		RemoveEventLoopTimer(menuHideTimer);
		menuHideTimer = NULL;
	}
}

OSStatus windowHandlerProc(EventHandlerCallRef caller, EventRef event, void* userData) {
	// menu view passed as user data
	HIViewRef menuView = (HIViewRef)userData;
	// get the menu view window
	WindowRef owner = HIViewGetWindow(menuView);
	WindowRef eventWindow;
	// get the window for which this event handler is called 
	GetEventParameter(event, kEventParamDirectObject, typeWindowRef, NULL, sizeof(eventWindow), NULL, &eventWindow);
    // is this window the menu view window?
	if (owner != NULL && eventWindow == owner) {
        // find the content view
        HIViewRef content;
        HIViewFindByID(HIViewGetRoot(owner), kHIViewWindowContentID, &content);
		// get menu bounds
		HIRect menuBoundsRect;
		HIViewGetFrame(content, &menuBoundsRect);
		menuSize[0] = menuBoundsRect.origin.x;
		menuSize[1] = menuBoundsRect.origin.y;
		menuSize[2] = menuBoundsRect.size.width;
		menuSize[3] = menuBoundsRect.size.height;
		// capture the image of menu
		menuImage = captureImage(content, owner);
		// try to hide the menu window to prevent flickering (works not so good :( )
		HideWindow(owner);
		// install event timer to cancel menu popup tracking (the popup menu is modal)
		EventLoopTimerUPP timerUPP = NewEventLoopTimerUPP(menuHideTimerProc);
		InstallEventLoopTimer (GetCurrentEventLoop(), kEventDurationNoWait, kEventDurationNoWait, timerUPP, 0, &menuHideTimer);
		// all ok
		return noErr;
	}
 	return eventNotHandledErr;
}

JNIEXPORT jint JNICALL OS_NATIVE(_1fetchPopupMenuVisualData)
  (JNIEnv * env, jobject this, jint menuHandle, jintArray jmenuSize, jintArray jmenuItemsSizes) {	
	menu = (MenuRef)menuHandle;
	menuImage = 0;
	menuHideTimer = NULL;
	// prepare data
	jsize sizesSize = (*env)->GetArrayLength(env, jmenuSize); 
	menuSize = malloc(sizesSize * sizeof(jint));
	jsize itemsSizesSize = (*env)->GetArrayLength(env, jmenuItemsSizes); 
	itemsSizes = malloc(itemsSizesSize * sizeof(jint));
	// get menu view
	HIViewRef menuView;
	HIMenuGetContentView (menu, kThemeMenuTypePullDown, &menuView);
	// install draw item event handler for to store item bounds
	EventTypeSpec menuEvent;
	menuEvent.eventClass = kEventClassMenu;
	menuEvent.eventKind = kEventMenuDrawItem;
	EventHandlerUPP menuHandler = NewEventHandlerUPP(menuHandlerProc);
	EventHandlerRef menuEventRef;
	InstallMenuEventHandler(menu, menuHandler, 1, &menuEvent, 0, &menuEventRef);
	// install show window event handler for to get menu image and bounds when menu window shown
	EventTypeSpec windowEvent;
	windowEvent.eventClass = kEventClassWindow;
	windowEvent.eventKind = kEventWindowShown;
	EventHandlerUPP windowHandler = NewEventHandlerUPP(windowHandlerProc);
	EventHandlerRef windowEventRef;
	InstallEventHandler(GetApplicationEventTarget(), windowHandler, 1, &windowEvent, menuView, &windowEventRef);
	// show the menu
	PopUpMenuSelect(menu, 0, 0, 0);
	// do some clean up
	RemoveEventHandler(menuEventRef);
	RemoveEventHandler(windowEventRef);
	// copy dimensions into java array                                                                           
    (*env)->SetIntArrayRegion(env, jmenuSize, 0, sizesSize, menuSize);   
    (*env)->SetIntArrayRegion(env, jmenuItemsSizes, 0, itemsSizesSize, itemsSizes);   
	// free memory
	free(menuSize);
	free(itemsSizes);
	//
	return menuImage;
}

JNIEXPORT jint JNICALL OS_NATIVE(_1makeShot)
  (JNIEnv * env, jobject this, jint controlHandle, jint shellHandle)
{	
	HIViewRef controlRef = (HIViewRef)controlHandle;
	WindowRef windowRef = (WindowRef)shellHandle;
	return captureImage(controlRef, windowRef);
}

JNIEXPORT jint JNICALL OS_NATIVE(_1HIViewGetRoot)
  (JNIEnv * env, jobject this, jint shellHandle)
{	
	WindowRef windowRef = (WindowRef)shellHandle;
	return (jint)HIViewGetRoot(windowRef);
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getMenuBarHeight)
  (JNIEnv * env, jobject this)
{	
	return (jint)GetMBarHeight();
}
JNIEXPORT void JNICALL OS_NATIVE(_1setAlpha)
(JNIEnv * env, jobject this, jint shellHandle, jint alpha)
{	
	alpha &= 0xFF;
	SetWindowAlpha((WindowRef)shellHandle, alpha / 255);
}
JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)
(JNIEnv * env, jobject this, jint shellHandle)
{	
	float alpha;
	if (GetWindowAlpha ((WindowRef)shellHandle, &alpha) == noErr) {
		return (jint)(alpha * 255);
	}
	return (jint)0xFF;
}
