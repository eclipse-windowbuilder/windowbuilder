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
//
//  MenuHookView.h
//  wbp-cocoa
//
//  Created by Alexander Mitin on 5/18/09.
//

#import <Cocoa/Cocoa.h>


@interface MenuHookView : NSView {

}
NSWindow* m_window;
NSMenu* m_menu;

- (id)initWithFrame:(NSRect)frame andMenu:(NSMenu*)menu;
-(NSWindow*)menuWindow;
-(void)cancelTracking;

@end
