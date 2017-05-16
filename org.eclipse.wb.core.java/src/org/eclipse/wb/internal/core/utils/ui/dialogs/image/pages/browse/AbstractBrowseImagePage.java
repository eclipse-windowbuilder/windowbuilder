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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse;

import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImageDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.AbstractImagePage;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IHasChildren;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageContainer;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageElement;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageRoot;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link AbstractImagePage} that supports browsing.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public abstract class AbstractBrowseImagePage extends AbstractImagePage {
  private final TreeViewer m_viewer;
  private final IImageRoot m_root;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractBrowseImagePage(Composite parent,
      int style,
      AbstractImageDialog imageDialog,
      IImageRoot root) {
    super(parent, style, imageDialog);
    m_root = root;
    //
    GridLayoutFactory.create(this);
    addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        m_root.dispose();
      }
    });
    // create viewer
    {
      m_viewer = new TreeViewer(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
      GridDataFactory.create(m_viewer.getTree()).hintC(50, 20).grab().fill();
      m_viewer.getTree().setData("org.eclipse.jface.viewers.TreeViewer", m_viewer);
      // set providers
      m_viewer.setContentProvider(new ImageContentProvider());
      m_viewer.setLabelProvider(new ImageLabelProvider());
      // add listeners
      m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
        public void selectionChanged(SelectionChangedEvent event) {
          IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
          if (selection.getFirstElement() instanceof IImageResource) {
            IImageResource resource = (IImageResource) selection.getFirstElement();
            m_imageDialog.setResultImageInfo(resource.getImageInfo());
          } else {
            m_imageDialog.setResultImageInfo(null);
          }
        }
      });
      m_viewer.addDoubleClickListener(new IDoubleClickListener() {
        public void doubleClick(DoubleClickEvent event) {
          m_imageDialog.closeOk();
        }
      });
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AbstractImagePage
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    m_viewer.setInput(m_root);
    m_imageDialog.setResultImageInfo(null);
  }

  @Override
  public void setInput(Object data) {
    Object[] selectionPath = m_root.getSelectionPath(data);
    if (selectionPath != null) {
      m_viewer.setExpandedElements(selectionPath);
      m_viewer.setSelection(new StructuredSelection(selectionPath[selectionPath.length - 1]));
    }
  }

  protected final void refresh() {
    m_viewer.refresh();
  }

  protected final TreeViewer getViewer() {
    return m_viewer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Content provider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link ITreeContentProvider} for {@link IImageElement}.
   *
   * @author scheglov_ke
   */
  private static final class ImageContentProvider implements ITreeContentProvider {
    public Object[] getElements(Object inputElement) {
      try {
        IImageRoot root = (IImageRoot) inputElement;
        return root.elements();
      } catch (Throwable e) {
      }
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public boolean hasChildren(Object element) {
      if (element instanceof IHasChildren) {
        IHasChildren tester = (IHasChildren) element;
        return tester.hasChildren();
      }
      return getChildren(element).length != 0;
    }

    public Object[] getChildren(Object parentElement) {
      try {
        if (parentElement instanceof IImageContainer) {
          IImageContainer container = (IImageContainer) parentElement;
          return container.elements();
        }
      } catch (Throwable e) {
      }
      return ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public Object getParent(Object element) {
      return null;
    }

    public void dispose() {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Label provider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link LabelProvider} for {@link IImageElement}.
   *
   * @author scheglov_ke
   */
  private static final class ImageLabelProvider extends LabelProvider {
    @Override
    public Image getImage(Object element) {
      if (element instanceof IImageElement) {
        IImageElement imageElement = (IImageElement) element;
        return imageElement.getImage();
      }
      return null;
    }

    @Override
    public String getText(Object element) {
      if (element instanceof IImageContainer) {
        IImageContainer container = (IImageContainer) element;
        return container.getName();
      } else if (element instanceof IImageResource) {
        IImageResource resource = (IImageResource) element;
        return resource.getName();
      }
      return "???";
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given extension is valid image extension.
   */
  public static boolean isImageExtension(String extension) {
    if (extension == null) {
      return false;
    }
    return extension.equalsIgnoreCase("gif")
        || extension.equalsIgnoreCase("png")
        || extension.equalsIgnoreCase("jpg")
        || extension.equalsIgnoreCase("jpeg")
        || extension.equalsIgnoreCase("bmp")
        || extension.equalsIgnoreCase("ico");
  }
}
