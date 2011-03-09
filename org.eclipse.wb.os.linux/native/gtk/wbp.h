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
#include <stdio.h>
#include <stdlib.h>

#include <gtk/gtk.h>
#include <gdk/gdkx.h>

#include "utils.h"

#define GTK_WIDGET_X(arg0) (arg0)->allocation.x
#define GTK_WIDGET_Y(arg0) (arg0)->allocation.y
#define GTK_WIDGET_WIDTH(arg0) (arg0)->allocation.width
#define GTK_WIDGET_HEIGHT(arg0) (arg0)->allocation.height

#define JHANDLE jobject
#define CALLBACK_SIG "(Ljava/lang/Number;Ljava/lang/Number;)V"
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_linux_OSSupportLinux_##func

