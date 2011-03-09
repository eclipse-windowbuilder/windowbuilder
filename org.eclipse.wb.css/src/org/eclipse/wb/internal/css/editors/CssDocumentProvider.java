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
package org.eclipse.wb.internal.css.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * {@link IDocumentProvider} for CSS.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public class CssDocumentProvider extends FileDocumentProvider {
  @Override
  protected IDocument createDocument(Object element) throws CoreException {
    IDocument document = super.createDocument(element);
    if (document != null) {
      CssPartitionScanner.configurePartitions(document);
    }
    return document;
  }
}
