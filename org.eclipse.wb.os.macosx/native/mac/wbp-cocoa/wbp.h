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


