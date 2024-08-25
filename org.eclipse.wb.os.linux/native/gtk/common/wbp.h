/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
#include <stdio.h>
#include <stdlib.h>

#include <gtk/gtk.h>
#include <gdk/gdkx.h>

#if (defined(__LP64__) && (!defined(WBP_ARCH64))) || defined(_WIN64)
	#if !defined(WBP_ARCH64)
		#define WBP_ARCH64
	#endif
#endif /*__LP64__*/

#define JHANDLE jlong
#ifdef WBP_ARCH64
	#define CHANDLE jlong
#else
	#define CHANDLE jint
#endif

#define GTK_WIDGET_X(arg0) (arg0)->allocation.x
#define GTK_WIDGET_Y(arg0) (arg0)->allocation.y
#define GTK_WIDGET_WIDTH(arg0) (arg0)->allocation.width
#define GTK_WIDGET_HEIGHT(arg0) (arg0)->allocation.height

#define CALLBACK_SIG "(Ljava/lang/Number;Ljava/lang/Number;)V"
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_linux_OSSupportLinux_##func

