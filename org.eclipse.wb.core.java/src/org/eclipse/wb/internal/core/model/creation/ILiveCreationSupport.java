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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.util.live.AbstractLiveManager;

/**
 * {@link CreationSupport} that implements this interface can be used for create "live component" by
 * {@link AbstractLiveManager}. "Live components" are used to fetch some data used during visual
 * creation of the component, such as image, preferred size, etc.
 * <p>
 * We need this interface because we need to create full copy of {@link JavaInfo} during adding it
 * in temporary hierarchy. So, we should also create copy of {@link CreationSupport} that also
 * should be aware, that it will be used for "live component".
 * </p>
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.model.creation
 */
public interface ILiveCreationSupport {
  CreationSupport getLiveComponentCreation();
}
