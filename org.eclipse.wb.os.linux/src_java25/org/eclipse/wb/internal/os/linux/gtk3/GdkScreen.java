/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux.gtk3;

/**
 * {@code GdkScreen} objects are the GDK representation of the screen on which
 * windows can be displayed and on which the pointer moves. X originally
 * identified screens with physical screens, but nowadays it is more common to
 * have a single GdkScreen which combines several physical monitors (see
 * gdk_screen_get_n_monitors()).
 *
 * GdkScreen is used throughout GDK and GTK+ to specify which screen the top
 * level windows are to be displayed on. it is also used to query the screen
 * specification and default settings such as the default visual
 * (gdk_screen_get_system_visual()), the dimensions of the physical monitors
 * (gdk_screen_get_monitor_geometry()), etc.
 */
public record GdkScreen(long handle) {

}
