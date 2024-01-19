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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import junit.framework.AssertionFailedError;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Helper for testing SWT UI.
 *
 * @author scheglov_ke
 */
public class UiContext {
	private final Display m_display;
	private Shell m_shell;
	private final LinkedList<Shell> m_shells = new LinkedList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public UiContext() {
		m_display = Display.getCurrent();
	}

	public UiContext(Display display) {
		m_display = display;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link Shell} with given text, may be <code>null</code>.
	 */
	public Shell getShell(String text) {
		for (Shell shell : m_display.getShells()) {
			if (text.equals(shell.getText())) {
				return shell;
			}
		}
		return null;
	}

	/**
	 * Specifies that {@link Shell} with given text should be used to search {@link Widget}'s.
	 */
	public Shell useShell(String text) {
		// remember current Shell
		m_shells.addFirst(getShell());
		// set new Shell
		for (Shell shell : m_display.getShells()) {
			if (text.equals(shell.getText())) {
				m_shell = shell;
				return m_shell;
			}
		}
		throw new IllegalArgumentException("Unable to find Shell: " + text);
	}

	/**
	 * Specifies that it is expected that current {@link Shell} was closed, so we return to previous.
	 */
	public void popShell() {
		m_shell = m_shells.removeFirst();
	}

	/**
	 * @return the {@link Shell} to use as start to searching {@link Widget}'s.
	 */
	private Shell getShell() {
		if (m_shell != null) {
			if (m_shell.isDisposed()) {
				m_shell = null;
			} else {
				return m_shell;
			}
		}
		return getActiveShell();
	}

	/**
	 * @return the active {@link Shell}.
	 */
	public Shell getActiveShell() {
		return m_display.getActiveShell();
	}

	/**
	 * @return the first {@link Widget} with compatible {@link Class}.
	 */
	public <T extends Widget> T findFirstWidget(Class<T> classToFind) {
		return findFirstWidget(getShell(), classToFind);
	}

	/**
	 * @return the first {@link Widget} with compatible {@link Class}.
	 */
	public static <T extends Widget> T findFirstWidget(Widget start, Class<T> classToFind) {
		List<T> widgets = findWidgets(start, classToFind);
		if (!widgets.isEmpty()) {
			return widgets.get(0);
		}
		return null;
	}

	/**
	 * @return the {@link List} of {@link Widget}'s with compatible {@link Class}.
	 */
	public <T extends Widget> List<T> findWidgets(final Class<T> classToFind) {
		Shell shell = getShell();
		return findWidgets(shell, classToFind);
	}

	/**
	 * @return the {@link List} of {@link Widget}'s with compatible {@link Class}.
	 */
	public static <T extends Widget> List<T> findWidgets(Widget start, final Class<T> classToFind) {
		final List<T> widgets = new ArrayList<>();
		visit(start, new IWidgetsVisitor() {
			@Override
			@SuppressWarnings("unchecked")
			public void endVisit(Widget widget) {
				if (classToFind.isAssignableFrom(widget.getClass())) {
					widgets.add((T) widget);
				}
			}
		});
		//
		return widgets;
	}

	/**
	 * @return the {@link Button} with given text.
	 */
	public Button getButtonByText(String text) {
		return getButtonByText(getShell(), text);
	}

	/**
	 * @return the {@link Button} with given text.
	 */
	public Button getButtonByTextPrefix(final String prefix) {
		return getButton(getShell(), input -> input != null && input.startsWith(prefix));
	}

	/**
	 * @return the {@link Button} with given text.
	 */
	public Button getButtonByText(Widget start, final String text) {
		return getButton(start, input -> isSameText(input, text));
	}

	/**
	 * @return the {@link Button} with given text.
	 */
	public Button getButton(Widget start, final Predicate<String> predicate) {
		final Button[] result = new Button[1];
		visit(start, new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof Button button) {
					if (predicate.test(button.getText()) || predicate.test(button.getToolTipText())) {
						result[0] = button;
					}
				}
			}
		});
		return result[0];
	}

	/**
	 * @return the {@link ToolItem} with given text.
	 */
	public ToolItem getToolItem(final String text) {
		final ToolItem[] result = new ToolItem[1];
		visit(getShell(), new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof ToolItem item) {
					if (text.equals(item.getText()) || text.equals(item.getToolTipText())) {
						result[0] = item;
					}
				}
			}
		});
		return result[0];
	}

	/**
	 * @return the {@link TreeItem} with given text.
	 */
	public TreeItem getTreeItem(final String text) {
		final TreeItem[] result = new TreeItem[1];
		visit(getShell(), new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof TreeItem item) {
					if (text.equals(item.getText())) {
						result[0] = item;
					}
				}
			}
		});
		return result[0];
	}

	/**
	 * @return the {@link TabItem} with given text.
	 */
	public TabItem getTabItem(final String text) {
		final TabItem[] result = new TabItem[1];
		visit(getShell(), new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof TabItem item) {
					if (text.equals(item.getText())) {
						result[0] = item;
					}
				}
			}
		});
		return result[0];
	}

	/**
	 * @return the {@link Text} widget that has {@link Label} with given text.
	 */
	public Text getTextByLabel(final String labelText) {
		final Text[] result = new Text[1];
		visit(getShell(), new IWidgetsVisitor() {
			private boolean m_labelFound;

			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof Label label) {
					m_labelFound = isSameText(label.getText(), labelText);
				}
				if (widget instanceof Text && m_labelFound) {
					result[0] = (Text) widget;
				}
			}
		});
		return result[0];
	}

	/**
	 * @return the {@link Text} widget that has given text.
	 */
	public Text getTextByText(final String textText) {
		final Text[] result = new Text[1];
		visit(getShell(), new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof Text text) {
					if (text.getText().equals(textText)) {
						result[0] = (Text) widget;
					}
				}
			}
		});
		return result[0];
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link Widget}.
	 */
	public void click(Widget widget) {
		widget.notifyListeners(SWT.Selection, new Event());
	}

	/**
	 * Sends {@link SWT#Selection} event to the {@link Button} with given text.
	 */
	public void clickButton(String text) {
		Button button = getButtonByText(text);
		Assert.isNotNull(button, "Can not find button with text |" + text + "|");
		clickButton(button);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link Button}.
	 */
	public void clickButton(Button button) {
		click(button);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link Button}.
	 */
	public void selectButton(String text, boolean selection) {
		Button button = getButtonByText(text);
		Assert.isNotNull(button, "Can not find button with text |" + text + "|");
		button.setSelection(selection);
		clickButton(button);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link Button}.
	 */
	private void selectButton(Button button, boolean selection) {
		button.setSelection(selection);
		clickButton(button);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link Button}.
	 */
	public void selectButton(Button button) {
		// if Button in RADIO, deselect all other RADIO Button's
		if ((button.getStyle() & SWT.RADIO) != 0) {
			Control[] children = button.getParent().getChildren();
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				if (child instanceof Button childButton && (child.getStyle() & SWT.RADIO) != 0) {
					if (childButton != button) {
						selectButton(childButton, false);
					}
				}
			}
		}
		// select needed Button
		button.setFocus();
		selectButton(button, true);
	}

	/**
	 * Selects {@link Button} with given text.
	 */
	public void selectButton(String text) {
		Button button = getButtonByText(text);
		Assert.isNotNull(button, "Can not find button with text |" + text + "|");
		selectButton(button);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link ToolItem} with detail.
	 */
	public void click(ToolItem toolItem, int detail) {
		Event event = new Event();
		event.detail = detail;
		toolItem.notifyListeners(SWT.Selection, event);
	}

	/**
	 * Sends {@link SWT#Selection} event to given {@link MenuItem} as radio item.
	 */
	public void selectMenuItem(MenuItem menuItem, boolean selection) {
		menuItem.setSelection(selection);
		click(menuItem);
	}

	public void selectMenuItem(MenuItem menuItem) {
		selectMenuItem(menuItem, true);
	}

	/**
	 * @return the {@link Control} located in children on its {@link Composite} after {@link Label}
	 *         with given text.
	 */
	public Control getControlAfterLabel(String text) {
		List<Label> labels = findWidgets(Label.class);
		for (Label label : labels) {
			String labelText = label.getText().trim();
			if (isSameText(labelText, text)) {
				Control[] children = label.getParent().getChildren();
				// get next Control
				int index = ArrayUtils.indexOf(children, label);
				if (index < children.length - 1) {
					return children[index + 1];
				}
				// stop it any case
				break;
			}
		}
		// not found
		return null;
	}

	/**
	 * @return the {@link Control} located in children on its {@link Composite} after given one.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getControlAfter(Control reference) {
		Control[] children = reference.getParent().getChildren();
		// get next Control
		int index = ArrayUtils.indexOf(children, reference);
		if (index < children.length - 1) {
			return (T) children[index + 1];
		}
		// not found
		return null;
	}

	/**
	 * Sets given {@link TreeItem} (and only it) expanded in its {@link Tree}.
	 */
	public static void setExpanded(TreeItem treeItem, boolean expanded) {
		treeItem.setExpanded(expanded);
		Event event = new Event();
		event.widget = treeItem.getParent();
		event.item = treeItem;
		treeItem.getParent().notifyListeners(SWT.Expand, event);
	}

	/**
	 * Sets given {@link TreeItem} (and only it) selected in its {@link Tree}.
	 */
	public static void setSelection(TreeItem treeItem) {
		Tree tree = treeItem.getParent();
		tree.setSelection(treeItem);
		treeItem.getParent().notifyListeners(SWT.Selection, null);
	}

	/**
	 * Selects items in {@link org.eclipse.swt.widgets.List}.
	 */
	public static void setSelection(org.eclipse.swt.widgets.List list, String item) {
		list.setSelection(new String[]{item});
		list.notifyListeners(SWT.Selection, null);
	}

	public static void setChecked(TreeItem treeItem, boolean checked) {
		treeItem.setChecked(checked);
		// notify about check
		Event event = new Event();
		event.detail = SWT.CHECK;
		event.item = treeItem;
		treeItem.getParent().notifyListeners(SWT.Selection, event);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Menu
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Menu} widget that active for current {@link Shell}.
	 */
	public Menu getActiveMenu() {
		return (Menu) ReflectionUtils.getFieldObject(getShell(), "activeMenu");
	}

	/**
	 * @return the {@link Menu} widget that active for current {@link Display}.
	 */
	public Menu getLastPopup() {
		Menu menu = null;
		Menu[] popups = (Menu[]) ReflectionUtils.getFieldObject(m_display, "popups");
		for (int i = 0; i < popups.length; i++) {
			if (popups[i] != null) {
				menu = popups[i];
			}
		}
		return menu;
	}

	/**
	 * @return the {@link MenuItem} with given text.
	 */
	public MenuItem getMenuItem(Menu menu, final String text) {
		final MenuItem[] result = new MenuItem[1];
		visit(menu, new IWidgetsVisitor() {
			@Override
			public void endVisit(Widget widget) {
				if (widget instanceof MenuItem item) {
					if (isSameText(item.getText(), text)) {
						result[0] = item;
					}
				}
			}
		});
		return result[0];
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Text
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link String}s have same content. Special characters are
	 *         ignored.
	 */
	private static boolean isSameText(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return false;
		}
		s1 = normalizeTextForComparing(s1);
		s2 = normalizeTextForComparing(s2);
		return s1.equals(s2);
	}

	/**
	 * Normalizes given {@link String} by removing special characters.
	 */
	private static String normalizeTextForComparing(String s) {
		s = s.trim();
		s = StringUtils.remove(s, '&');
		s = StringUtils.substringBefore(s, "\t");
		return s;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Visitor for all {@link Widget}'s.
	 */
	public static class IWidgetsVisitor {
		/**
		 * @return <code>true</code> if the children of this {@link Widget} should be visited, and
		 *         <code>false</code> if the children of this {@link Widget} should be skipped.
		 */
		public boolean visit(Widget widget) {
			return true;
		}

		/**
		 * Invoked after visiting all children of this {@link Control}.
		 */
		public void endVisit(Widget widget) {
		}
	}

	/**
	 * Visits all {@link Widgets}'s starting from given one.
	 */
	public static void visit(Widget widget, IWidgetsVisitor visitor) {
		// ignore invisible Control's
		if (widget instanceof Control control) {
			if (control.getParent() != null
					&& control.getParent().getLayout() instanceof StackLayout
					&& !control.isVisible()) {
				return;
			}
		}
		// visit
		if (visitor.visit(widget)) {
			// Composite
			if (widget instanceof Composite composite) {
				Control[] children = composite.getChildren();
				for (int i = 0; i < children.length; i++) {
					Control child = children[i];
					visit(child, visitor);
				}
			}
			// ToolBar
			if (widget instanceof ToolBar toolBar) {
				for (ToolItem toolItem : toolBar.getItems()) {
					visit(toolItem, visitor);
				}
			}
			// TabFolder
			if (widget instanceof TabFolder tabFolder) {
				for (TabItem tabItem : tabFolder.getItems()) {
					visit(tabItem, visitor);
				}
			}
			// TabItem
			if (widget instanceof TabItem tabItem) {
				Control control = tabItem.getControl();
				if (control != null) {
					visit(control, visitor);
				}
			}
			// Tree
			if (widget instanceof Tree tree) {
				for (TreeItem treeItem : tree.getItems()) {
					visit(treeItem, visitor);
				}
			}
			if (widget instanceof TreeItem parent) {
				for (TreeItem treeItem : parent.getItems()) {
					visit(treeItem, visitor);
				}
			}
			// Menu
			if (widget instanceof Menu menu) {
				for (MenuItem menuItem : menu.getItems()) {
					visit(menuItem, visitor);
				}
			}
			if (widget instanceof MenuItem menuItem) {
				visit(menuItem.getMenu(), visitor);
			}
			// end
			visitor.endVisit(widget);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Execution
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Executes one {@link UIRunnable} (can block UI) and uses other {@link UIRunnable} to check
	 * result.
	 */
	public void executeAndCheck(UIRunnable uiRunnable, final UIRunnable checkRunnable)
			throws Exception {
		// run checkRunnable
		final Throwable[] checkException = new Throwable[1];
		Thread checkThread = new Thread("UIContext_checkThread") {
			@Override
			public void run() {
				m_display.asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							checkRunnable.run(UiContext.this);
						} catch (Throwable e) {
							e.printStackTrace();
							checkException[0] = e;
						}
					}
				});
			}
		};
		checkThread.start();
		// now run uiRunnable, it can block
		uiRunnable.run(this);
		// wait also for 'checkThread'
		checkThread.join();
		// check for exception
		if (checkException[0] != null) {
			if (checkException[0] instanceof AssertionFailedError error) {
				throw error;
			}
			throw new Exception("Exception during running 'check' UIRunnable.", checkException[0]);
		}
	}

	/**
	 * Waits until given condition will be satisfied.
	 */
	public void waitFor(UIPredicate predicate) throws InterruptedException {
		do {
			waitEventLoop(10);
		} while (!predicate.check());
	}

	/**
	 * Waits given number of milliseconds and runs events loop every 1 millisecond.
	 */
	private void waitEventLoop(int time) throws InterruptedException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < time) {
			Thread.sleep(0);
			while (m_display.readAndDispatch()) {
				// do nothing
			}
		}
	}
}
