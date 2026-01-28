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
package org.eclipse.wb.internal.os.linux.cairo;

import java.lang.foreign.MemorySegment;

/**
 * A {@code cairo_t} contains the current state of the rendering device,
 * including coordinates of yet to be drawn shapes.
 *
 * Cairo contexts, as {@code cairo_t} objects are named, are central to cairo
 * and all drawing with cairo is always done to a cairo_t object.
 */
public record CairoContext(MemorySegment segment) {

}
