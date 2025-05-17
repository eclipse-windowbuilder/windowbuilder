/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.rcp.model.rcp;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.EmptyTransfer;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.pde.core.plugin.IPluginElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * {@link Dialog} for editing categories and moving views between them.
 *
 * @author scheglov_ke
 * @coverage rcp.model.rcp
 */
public class CategoriesAndViewsDialog extends ResizableDialog {
	private static final Object OTHER_CATEGORY = new Object();
	private final PdeUtils m_utils;
	////////////////////////////////////////////////////////////////////////////
	//
	// GUI fields
	//
	////////////////////////////////////////////////////////////////////////////
	private TreeViewer m_viewer;
	private ViewsContentProvider m_contentProvider;
	private Tree m_tree;
	private Button m_newCategoryButton;
	private Button m_editCategoryButton;
	private Button m_removeCategoryButton;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public CategoriesAndViewsDialog(Shell parentShell, PdeUtils utils) {
		super(parentShell, Activator.getDefault());
		m_utils = utils;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GUI creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.create(container).columns(2);
		// title label
		{
			Label label = new Label(container, SWT.NONE);
			GridDataFactory.create(label).spanH(2);
			label.setText(ModelMessages.CategoriesAndViewsDialog_viewerTitle);
		}
		// viewer with categories/views
		{
			m_viewer = new TreeViewer(container, SWT.BORDER);
			m_tree = m_viewer.getTree();
			GridDataFactory.create(m_tree).grab().fill().hintC(50, 20);
			m_contentProvider = new ViewsContentProvider();
			m_viewer.setContentProvider(m_contentProvider);
			m_viewer.setLabelProvider(new ViewsLabelProvider());
			m_viewer.setComparator(new ViewerComparator());
			setPluginElementComparer();
			m_viewer.setInput("myInput");
			// events
			m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					onSelectionChanged();
				}
			});
			m_viewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					if (m_editCategoryButton.isEnabled()) {
						editCategory();
					}
				}
			});
			// DND
			setupDragAndDropSupport();
		}
		// buttons
		{
			Composite buttonsComposite = new Composite(container, SWT.NONE);
			GridDataFactory.create(buttonsComposite).alignVT();
			GridLayoutFactory.create(buttonsComposite).noMargins();
			{
				m_newCategoryButton = new Button(buttonsComposite, SWT.NONE);
				GridDataFactory.create(m_newCategoryButton).grabH().fillH();
				m_newCategoryButton.setText(ModelMessages.CategoriesAndViewsDialog_newCategoryButton);
				m_newCategoryButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						newCategory();
					}
				});
			}
			{
				m_editCategoryButton = new Button(buttonsComposite, SWT.NONE);
				GridDataFactory.create(m_editCategoryButton).grabH().fillH();
				m_editCategoryButton.setText(ModelMessages.CategoriesAndViewsDialog_editCategoryButton);
				m_editCategoryButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						editCategory();
					}
				});
			}
			{
				m_removeCategoryButton = new Button(buttonsComposite, SWT.NONE);
				GridDataFactory.create(m_removeCategoryButton).grabH().fillH();
				m_removeCategoryButton.setText(ModelMessages.CategoriesAndViewsDialog_removeCategoryButton);
				m_removeCategoryButton.addListener(SWT.Selection, new Listener() {
					@Override
					public void handleEvent(Event event) {
						removeCategory();
					}
				});
			}
		}
		// hint label
		{
			Label label = new Label(container, SWT.WRAP);
			GridDataFactory.create(label).grabH().fillH().spanH(2).hintHC(60);
			label.setText(ModelMessages.CategoriesAndViewsDialog_hintText);
		}
		// done
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(ModelMessages.CategoriesAndViewsDialog_title);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL, false);
	}

	/**
	 * {@link IPluginElement#equals(Object)} has bug, see
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=259761 So we have to use our own
	 * {@link IElementComparer}.
	 */
	private void setPluginElementComparer() {
		m_viewer.setComparer(new IElementComparer() {
			@Override
			public int hashCode(Object element) {
				return 0;
			}

			@Override
			public boolean equals(Object a, Object b) {
				if (a instanceof IPluginElement elementA && b instanceof IPluginElement) {
					IPluginElement elementB = (IPluginElement) b;
					String idA = PdeUtils.getAttribute(elementA, "id");
					String idB = PdeUtils.getAttribute(elementB, "id");
					return Objects.equals(idA, idB);
				}
				return a == null ? b == null : a.equals(b);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Views drag and drop
	//
	////////////////////////////////////////////////////////////////////////////
	private void setupDragAndDropSupport() {
		final IPluginElement dragElement[] = new IPluginElement[1];
		int ops = DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[]{EmptyTransfer.INSTANCE};
		// drag
		m_viewer.addDragSupport(ops, transfers, new DragSourceAdapter() {
			@Override
			public void dragStart(DragSourceEvent event) {
				IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
				if (selection.isEmpty() || !(selection.getFirstElement() instanceof IPluginElement)) {
					event.doit = false;
					return;
				}
				dragElement[0] = (IPluginElement) selection.getFirstElement();
				event.doit = dragElement[0].getName().equals("view");
			}

			@Override
			public void dragSetData(DragSourceEvent event) {
			}

			@Override
			public void dragFinished(DragSourceEvent event) {
			}
		});
		// drop
		ViewerDropAdapter adapter = new ViewerDropAdapter(m_viewer) {
			private int m_location;
			private Object m_target;

			@Override
			protected int determineLocation(DropTargetEvent event) {
				m_location = super.determineLocation(event);
				if (m_location == LOCATION_AFTER || m_location == LOCATION_BEFORE) {
					m_location = LOCATION_ON;
				}
				return m_location;
			}

			@Override
			public boolean validateDrop(Object target, int operation, TransferData transferType) {
				m_target = target;
				// check for category "other"
				if (target == OTHER_CATEGORY) {
					return true;
				}
				// check for some category
				if (target instanceof IPluginElement targetElement) {
					return isCategoryElement(targetElement);
				}
				//
				return false;
			}

			@Override
			public boolean performDrop(Object data) {
				// update "category" attribute of moved "view"
				ExecutionUtils.runLog(new RunnableEx() {
					@Override
					public void run() throws Exception {
						if (m_target == OTHER_CATEGORY) {
							m_utils.setAttribute(dragElement[0], "category", null);
						} else if (m_target instanceof IPluginElement targetElement) {
							String categoryId = PdeUtils.getAttribute(targetElement, "id");
							m_utils.setAttribute(dragElement[0], "category", categoryId);
						}
					}
				});
				// show and select dragged object
				{
					m_viewer.refresh();
					m_viewer.setExpandedState(m_target, true);
					m_viewer.setSelection(new StructuredSelection(dragElement[0]));
				}
				return true;
			}
		};
		adapter.setFeedbackEnabled(true);
		m_viewer.addDropSupport(ops | DND.DROP_DEFAULT, transfers, adapter);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events handling
	//
	////////////////////////////////////////////////////////////////////////////
	private void onSelectionChanged() {
		m_editCategoryButton.setEnabled(false);
		m_removeCategoryButton.setEnabled(false);
		//
		Object selectedObject = getSelectedObject();
		if (isCategoryObject(selectedObject)) {
			m_editCategoryButton.setEnabled(true);
			m_removeCategoryButton.setEnabled(!m_contentProvider.hasChildren(selectedObject));
		}
	}

	private void newCategory() {
		InputDialog inputDialog = new InputDialog(getShell(),
				ModelMessages.CategoriesAndViewsDialog_newCategoryTitle,
				ModelMessages.CategoriesAndViewsDialog_newCategoryMessage,
				"",
				null);
		if (inputDialog.open() != OK) {
			return;
		}
		//
		final String categoryName = inputDialog.getValue();
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				String id = m_utils.generateUniqueID(m_utils.getProject().getName() + ".category");
				IPluginElement category = m_utils.createViewCategoryElement(id, categoryName);
				// select created category
				m_viewer.refresh();
				m_viewer.setSelection(new StructuredSelection(category));
			}
		});
	}

	private void editCategory() {
		final IPluginElement category = (IPluginElement) getSelectedObject();
		// ask new name
		final String newName;
		{
			String oldName = PdeUtils.getAttribute(category, "name");
			InputDialog inputDialog = new InputDialog(getShell(),
					ModelMessages.CategoriesAndViewsDialog_editCategoryTitle,
					ModelMessages.CategoriesAndViewsDialog_editCategoryMessage,
					oldName,
					null);
			if (inputDialog.open() != OK) {
				return;
			}
			newName = inputDialog.getValue();
		}
		// update category
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				m_utils.setAttribute(category, "name", newName);
			}
		});
		// show modifications
		m_viewer.refresh();
	}

	private void removeCategory() {
		IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
		final IPluginElement element = (IPluginElement) selection.getFirstElement();
		// ask confirmation
		{
			String messages = MessageFormat.format(
					ModelMessages.CategoriesAndViewsDialog_removeCategoryConfirmationMessage,
					PdeUtils.getAttribute(element, "name"));
			if (!MessageDialog.openConfirm(
					getShell(),
					ModelMessages.CategoriesAndViewsDialog_removeCategoryConfirmationText,
					messages)) {
				return;
			}
		}
		// remove
		ExecutionUtils.runLog(new RunnableEx() {
			@Override
			public void run() throws Exception {
				m_utils.removeElement(element);
			}
		});
		// refresh
		m_viewer.refresh();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Content provider
	//
	////////////////////////////////////////////////////////////////////////////
	private class ViewsContentProvider implements IStructuredContentProvider, ITreeContentProvider {
		private List<IPluginElement> m_views;
		private List<IPluginElement> m_categories;

		@Override
		public Object[] getElements(Object inputElement) {
			m_views = m_utils.getExtensionElements("org.eclipse.ui.views", "view");
			m_categories = m_utils.getExtensionElements("org.eclipse.ui.views", "category");
			return ArrayUtils.add(m_categories.toArray(), OTHER_CATEGORY);
		}

		@Override
		public Object[] getChildren(Object parentElement) {
			List<IPluginElement> categoryViews = new ArrayList<>();
			//
			if (parentElement == OTHER_CATEGORY) {
				for (IPluginElement view : m_views) {
					String viewCategoryId = PdeUtils.getAttribute(view, "category");
					if (StringUtils.isEmpty(viewCategoryId)) {
						categoryViews.add(view);
					}
				}
			} else if (parentElement instanceof IPluginElement pluginElement) {
				if (isCategoryElement(pluginElement)) {
					String thisCategoryId = PdeUtils.getAttribute(pluginElement, "id");
					for (IPluginElement view : m_views) {
						String viewCategoryId = PdeUtils.getAttribute(view, "category");
						if (thisCategoryId.equals(viewCategoryId)) {
							categoryViews.add(view);
						}
					}
				}
			}
			//
			return categoryViews.toArray();
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof IPluginElement view) {
				String viewCategoryId = PdeUtils.getAttribute(view, "category");
				// check for category "other"
				if (StringUtils.isEmpty(viewCategoryId)) {
					return OTHER_CATEGORY;
				}
				// find category with given id
				for (IPluginElement category : m_categories) {
					if (Objects.equals(PdeUtils.getAttribute(category, "id"), viewCategoryId)) {
						return category;
					}
				}
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

		@Override
		public void dispose() {
		}
	}
	////////////////////////////////////////////////////////////////////////////
	//
	// Label provider
	//
	////////////////////////////////////////////////////////////////////////////
	private class ViewsLabelProvider extends LabelProvider {
		private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

		@Override
		public void dispose() {
			super.dispose();
			m_resourceManager.dispose();
		}

		@Override
		public String getText(Object element) {
			if (element == OTHER_CATEGORY) {
				return "Other";
			}
			//
			IPluginElement pluginElement = (IPluginElement) element;
			if (isCategoryElement(pluginElement)) {
				return PdeUtils.getAttribute(pluginElement, "name");
			} else if (pluginElement.getName().equals("view")) {
				return PdeUtils.getAttribute(pluginElement, "name");
			}
			return super.getText(element);
		}

		@Override
		public Image getImage(Object element) {
			if (element == OTHER_CATEGORY) {
				return DesignerPlugin.getImage("folder_open.gif");
			}
			//
			IPluginElement pluginElement = (IPluginElement) element;
			if (isCategoryElement(pluginElement)) {
				return DesignerPlugin.getImage("folder_open.gif");
			}
			if (pluginElement.getName().equals("view")) {
				return m_resourceManager.createImageWithDefault(PdeUtils.getElementIcon(pluginElement, "icon", null));
			}
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Object getSelectedObject() {
		IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
		return selection.getFirstElement();
	}

	private static boolean isCategoryObject(Object o) {
		if (o instanceof IPluginElement element) {
			return element.getName().equals("category");
		}
		return false;
	}

	private static boolean isCategoryElement(IPluginElement element) {
		return element.getName().equals("category");
	}
}
