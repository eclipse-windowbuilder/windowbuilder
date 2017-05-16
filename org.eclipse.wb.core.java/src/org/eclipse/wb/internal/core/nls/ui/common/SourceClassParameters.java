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
package org.eclipse.wb.internal.core.nls.ui.common;

import org.eclipse.wb.internal.core.nls.ui.common.AbstractFieldsSourceNewComposite.ClassSelectionGroup;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Value object for class parameters selected using {@link ClassSelectionGroup}.
 *
 * @author scheglov_ke
 * @coverage core.nls
 */
public class SourceClassParameters {
  public IPackageFragmentRoot m_sourceFolder;
  public IPackageFragment m_package;
  public IFolder m_packageFolder;
  public String m_packageName;
  public String m_className;
  public String m_fullClassName;
  public boolean m_exists;
}
