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
#include <jni.h>

#import <Cocoa/Cocoa.h>
#import <Carbon/Carbon.h>
#import <objc/objc-runtime.h>


#ifndef __WBP_H_
#define __WBP_H_

#define JHANDLE jobject
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_macosx_OSSupportMacOSXCocoa_##func

// dynamic functions
OSStatus (*ChangeMenuItemAttributes_)(MenuRef, MenuItemIndex, MenuItemAttributes, MenuItemAttributes);
EventTargetRef (*GetMenuEventTarget_)(MenuRef);
MenuRef (*NSGetCarbonMenu_)(NSMenu*);
OSStatus (*CancelMenuTracking_)(MenuRef, Boolean, UInt32);
#ifdef InstallMenuEventHandler
#undef InstallMenuEventHandler
#endif
#define InstallMenuEventHandler( target, handler, numTypes, list, userData, outHandlerRef ) \
InstallEventHandler( GetMenuEventTarget_( target ), (handler), (numTypes), (list), (userData), (outHandlerRef) )

#endif


