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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.internal.core.model.description.ConstructorDescription;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

import java.util.List;

import javax.swing.AbstractAction;

/**
 * Interface for working with {@link AbstractAction} extended properties.
 *
 * @author sablin_aa
 * @coverage swing.model
 */
public interface IActionSupport {
	/**
	 * @return the {@link ASTNode} of action.
	 */
	ASTNode getCreation();

	/**
	 * @return list {@link Block} containing initialization source code.
	 */
	List<Block> getInitializationBlocks();

	/**
	 * @return {@link ConstructorDescription} for action instance creation.
	 */
	ConstructorDescription getConstructorDescription();
}
