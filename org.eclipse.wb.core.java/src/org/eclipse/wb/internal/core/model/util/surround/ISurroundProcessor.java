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
package org.eclipse.wb.internal.core.model.util.surround;

import org.eclipse.wb.core.model.IAbstractComponentInfo;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 * When {@link SurroundSupport} performs surround with some {@link ISurroundTarget}, it is possible
 * that for some combination of source/target containers we may want to perform special action.
 * <p>
 * For example, when source container has absolute layout and target is plain container (for example
 * {@link JPanel}, in contrast to {@link JTabbedPane}), then we can set absolute layout for target
 * container too, and place components in s that their visual position will stay same.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public interface ISurroundProcessor<C extends IAbstractComponentInfo, T extends IAbstractComponentInfo> {
	/**
	 * @return the <code>true</code> if this {@link ISurroundProcessor} wants to perform move
	 *         operation.
	 */
	boolean filter(C sourceContainer, C targetContainer) throws Exception;
	/**
	 * Moves components from source to target container.
	 */
	void move(C sourceContainer, C targetContainer, List<T> components) throws Exception;
}
