/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.property.hierarchy;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.IPropertyTooltipSite;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Property editor that shows class name for component and hierarchy as value hint.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class ComponentClassPropertyEditor extends TextDisplayPropertyEditor {
	private final IJavaProject m_javaProject;
	private final Class<?> m_componentClass;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentClassPropertyEditor(IJavaProject javaProject, Class<?> componentClass) {
		m_javaProject = javaProject;
		m_componentClass = componentClass;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		return m_componentClass.getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tooltip
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected PropertyTooltipProvider createPropertyTooltipProvider() {
		return new PropertyTooltipProvider() {
			@Override
			public Control createTooltipControl(Property property,
					Composite parent,
					int availableWidth,
					final IPropertyTooltipSite site) {
				// prepare hierarchy
				List<Class<?>> classes = new ArrayList<>();
				{
					Class<?> clazz = m_componentClass;
					while (clazz != null) {
						classes.add(clazz);
						clazz = clazz.getSuperclass();
					}
				}
				// create tree that can calculate full size
				Tree tree = new Tree(parent, SWT.NONE) {
					@Override
					public Point computeSize(int wHint, int hHint, boolean changed) {
						Point minLocation = new Point(0, 0);
						Point maxLocation = new Point(0, 0);
						updateMaxSize(minLocation, maxLocation, getItems());
						Point size = new Point(maxLocation.x - minLocation.x, maxLocation.y - minLocation.y);
						int verticalBarSize = getVerticalBar().getSize().x;
						int horizontalBarSize = getHorizontalBar().getSize().y;
						// bug in SWT: for non-windows platforms there is no need to forsibly set the scrollbars (see Tree.checkStyle())
						// the workaround is to add additional space on non-windows platforms
						size.x += EnvironmentUtils.IS_WINDOWS ? verticalBarSize + 10 : verticalBarSize * 3;
						size.y += EnvironmentUtils.IS_WINDOWS ? horizontalBarSize : horizontalBarSize * 2;
						return size;
					}

					private void updateMaxSize(Point minLocation, Point maxLocation, TreeItem rootItems[]) {
						for (int i = 0; i < rootItems.length; i++) {
							TreeItem item = rootItems[i];
							Rectangle bounds = item.getBounds();
							minLocation.x = Math.min(minLocation.x, bounds.x);
							minLocation.y = Math.min(minLocation.y, bounds.y);
							maxLocation.x = Math.max(maxLocation.x, bounds.x + bounds.width);
							maxLocation.y = Math.max(maxLocation.y, bounds.y + bounds.height);
							updateMaxSize(minLocation, maxLocation, item.getItems());
						}
					}

					@Override
					protected void checkSubclass() {
					}
				};
				// copy colors
				tree.setForeground(parent.getForeground());
				tree.setBackground(parent.getBackground());
				// create viewer
				TreeViewer treeViewer = new TreeViewer(tree);
				treeViewer.setContentProvider(new ComponentClassContentProvider());
				treeViewer.setLabelProvider(new ComponentClassLabelProvider(tree));
				treeViewer.setInput(classes);
				treeViewer.expandAll();
				// add double click to open class in editor
				treeViewer.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						try {
							String className;
							{
								IStructuredSelection selection = (IStructuredSelection) event.getSelection();
								Class<?> clazz = (Class<?>) selection.getFirstElement();
								className = clazz.getName();
							}
							//
							IType type = m_javaProject.findType(className);
							if (type != null) {
								site.hideTooltip();
								JavaUI.openInEditor(type);
							}
						} catch (Throwable e) {
							DesignerPlugin.log(e);
						}
					}
				});
				// set listeners
				{
					HideListener listener = new HideListener(site);
					tree.addListener(SWT.MouseExit, listener);
				}
				// return tree as tooltip control
				return tree;
			}
		};
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Component class content provider
	//
	////////////////////////////////////////////////////////////////////////////
	private static class ComponentClassContentProvider implements ITreeContentProvider {
		private List<?> m_classes;

		@Override
		public Object[] getElements(Object inputElement) {
			return new Object[]{m_classes.get(m_classes.size() - 1)};
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			int index = m_classes.indexOf(parentElement);
			if (index == 0) {
				return null;
			}
			return new Object[]{m_classes.get(index - 1)};
		}

		@Override
		public Object getParent(Object element) {
			int index = m_classes.indexOf(element);
			return m_classes.get(index + 1);
		}

		@Override
		public boolean hasChildren(Object element) {
			return m_classes.indexOf(element) != 0;
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			m_classes = (List<?>) newInput;
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Component class label provider
	//
	////////////////////////////////////////////////////////////////////////////
	private class ComponentClassLabelProvider extends LabelProvider implements IFontProvider {
		private final Font m_treeFont;

		////////////////////////////////////////////////////////////////////////////
		//
		// Constructor
		//
		////////////////////////////////////////////////////////////////////////////
		public ComponentClassLabelProvider(final Tree tree) {
			m_treeFont = FontDescriptor.createFrom(tree.getFont())
					.setStyle(SWT.BOLD)
					.createFont(null);
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// LabelProvider
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public String getText(Object element) {
			Class<?> clazz = (Class<?>) element;
			return clazz.getName();
		}

		@Override
		public Image getImage(Object element) {
			return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
		}

		@Override
		public void dispose() {
			m_treeFont.dispose();
		}

		////////////////////////////////////////////////////////////////////////////
		//
		// IFontProvider
		//
		////////////////////////////////////////////////////////////////////////////
		@Override
		public Font getFont(Object element) {
			Class<?> clazz = (Class<?>) element;
			if (clazz == m_componentClass) {
				return m_treeFont;
			}
			return null;
		}
	}
}