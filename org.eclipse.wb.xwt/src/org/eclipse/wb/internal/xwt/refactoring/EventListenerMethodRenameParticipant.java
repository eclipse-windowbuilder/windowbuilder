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
package org.eclipse.wb.internal.xwt.refactoring;

import org.eclipse.wb.internal.core.utils.refactoring.RefactoringUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.utils.xml.FileDocumentEditContext;
import org.eclipse.wb.internal.xwt.editor.XwtPairResourceProvider;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

/**
 * Participates in rename of event listener method.
 *
 * @author scheglov_ke
 * @coverage XWT.refactoring
 */
public class EventListenerMethodRenameParticipant extends AbstractRenameParticipant {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Change
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Change createChangeEx(IProgressMonitor pm) throws Exception {
    IFile javaFile = (IFile) m_method.getUnderlyingResource();
    if (javaFile != null) {
      IFile xwtFile = XwtPairResourceProvider.INSTANCE.getPair(javaFile);
      if (xwtFile != null) {
        final String oldName = m_method.getElementName();
        final String newName = getArguments().getNewName();
        return RefactoringUtils.modifyXML(xwtFile, new DocumentModelVisitor() {
          @Override
          public void visit(DocumentAttribute attribute) {
            if (attribute.getName().endsWith("Event") && attribute.getValue().equals(oldName)) {
              attribute.setValue(newName);
            }
          }
        }, new FileDocumentEditContext(xwtFile));
      }
    }
    return null;
  }
}
