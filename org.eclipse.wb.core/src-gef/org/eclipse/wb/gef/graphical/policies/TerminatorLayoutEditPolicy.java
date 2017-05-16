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
package org.eclipse.wb.gef.graphical.policies;

import org.eclipse.wb.gef.core.Command;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;

/**
 * Special {@link LayoutEditPolicy} that accepts any object, but returns <code>null</code> as
 * {@link Command}.
 * <p>
 * Separate {@link LayoutEditPolicy} for different types of objects is good, because we can filter
 * objects in one place, instead of each <code>getXXXCommand()</code>. But if {@link Request} was
 * not handled by {@link EditPart} at all, this {@link EditPart} will become "transparent", so this
 * will for example look like we drop new object on parent {@link EditPart}. This is not good in
 * most cases.
 *
 * @author scheglov_ke
 * @coverage gef.graphical
 */
public final class TerminatorLayoutEditPolicy extends LayoutEditPolicy {
}
