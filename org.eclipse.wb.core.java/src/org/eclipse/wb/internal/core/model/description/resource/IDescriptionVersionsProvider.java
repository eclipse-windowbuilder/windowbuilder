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
package org.eclipse.wb.internal.core.model.description.resource;

import java.util.List;

/**
 * Components may change during when toolkit or library evolve, so we add new features into
 * descriptions in "wbp-meta" folder. However when we reference some method or property that does
 * not exist in older version of component, this will cause critical exception. So, we should have
 * separate descriptions for older versions of components and try to load them first from some
 * sub-folders of "wbp-meta", before loading directly from "wbp-meta" folder.
 * <p>
 * This class provides possible sub-folders with versions of component resources that should be
 * checked.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public interface IDescriptionVersionsProvider {
	/**
	 * @return the {@link List} of versions, or sub-folders in "wbp-meta" where component description
	 *         may be found, may be empty {@link List}, but not <code>null</code>.
	 */
	List<String> getVersions(Class<?> componentClass) throws Exception;
}