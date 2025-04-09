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
//  MenuHookView.h
//  wbp-cocoa
//
//  Created by Alexander Mitin on 5/18/09.
//

#import <Cocoa/Cocoa.h>

NSWindow* m_window;
NSMenu* m_menu;

@interface MenuHookView : NSView {

}

- (id)initWithFrame:(NSRect)frame andMenu:(NSMenu*)menu;
-(NSWindow*)menuWindow;
-(void)cancelTracking;

@end
