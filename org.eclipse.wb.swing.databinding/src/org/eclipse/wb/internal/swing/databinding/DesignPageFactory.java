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
package org.eclipse.wb.internal.swing.databinding;

import org.eclipse.wb.core.editor.IDesignerEditor;
import org.eclipse.wb.core.editor.IEditorPage;
import org.eclipse.wb.core.editor.IEditorPageFactory;
import org.eclipse.wb.internal.core.databinding.ui.BindingDesignPage;
import org.eclipse.wb.internal.swing.databinding.model.DataBindingsCodeUtils;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaProject;

import java.util.List;

/**
 * Bindings design page factory.
 * 
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class DesignPageFactory implements IEditorPageFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IDesignPageFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public void createPages(IDesignerEditor editor, List<IEditorPage> pages) {
    if (isSwingDB(editor.getCompilationUnit())) {
      BindingDesignPage.addPage(pages);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  public static boolean isSwingDB(ICompilationUnit compilationUnit) {
    IJavaProject javaProject = compilationUnit.getJavaProject();
    if (DataBindingsCodeUtils.isDBAvailable(javaProject)) {
      try {
        IImportDeclaration[] imports = compilationUnit.getImports();
        for (IImportDeclaration importDeclaration : imports) {
          String elementName = importDeclaration.getElementName();
          if (elementName.startsWith("java.awt") || elementName.startsWith("javax.swing")) {
            return true;
          }
        }
      } catch (Throwable e) {
      }
    }
    return false;
  }
}