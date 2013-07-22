/*******************************************************************************
 * Copyright (c) 2007 SAS Institute. All rights reserved. This program and the accompanying
 * materials are made available under the terms of the Eclipse Public License v1.0 which accompanies
 * this distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: SAS Institute - initial API and implementation
 *******************************************************************************/
package swingintegration.example;

import org.eclipse.wb.internal.swing.utils.SwingImageUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.im.InputContext;

import javax.swing.JApplet;
import javax.swing.JComponent;
import javax.swing.RootPaneContainer;

/**
 * A SWT composite widget for embedding Swing components in a SWT composite within an RCP or
 * standalone-SWT application. The Eclipse platform provides limited support for embedding Swing
 * components through {@link org.eclipse.swt.awt.SWT_AWT}. This class extends that support by
 * <ul>
 * <li>Using the platform-specific system Look and Feel.
 * <li>Ensuring AWT modal dialogs are modal across the SWT application.
 * <li>Reducing flicker, especially on window resizes
 * <li>Allowing Tab Traversal to and from the Embedded Frame
 * <li>Dismissing most Pop-Up Menus when focus leaves the AWT frame.
 * <li>Synchronizing Font Changes from system settings
 * <li>Working around various AWT/Swing bugs
 * </ul>
 * <P>
 * If, rather than embedding Swing components, you are integrating with Swing by opening Swing
 * dialogs, see the {@link AwtEnvironment} class.
 * <p>
 * This is an abstract that is normally used by extending it and implementing the
 * {@link #createSwingComponent()} method. For example,
 * 
 * <pre>
 *        embeddedComposite = new EmbeddedSwingComposite(parent, SWT.NONE) {
 *            protected JComponent createSwingComponent() {
 *                scrollPane = new JScrollPane();
 *                table = new JTable();
 *                scrollPane.setViewportView(table);
 *                return scrollPane;
 *            }
 *        }; 
 *        embeddedComposite.populate();
 * </pre>
 * 
 * <p>
 * The Swing component is created inside a standard Swing containment hierarchy, rooted in a
 * {@link javax.swing.RootPaneContainer}. The root pane container is placed inside an AWT frame, as
 * returned by {@link org.eclipse.swt.awt.SWT_AWT#new_Frame(Composite)}
 * <p>
 * <b>Note:</b> When you mix components from Swing/AWT and SWT toolkits, there will be two UI event
 * threads, one for AWT, one for SWT. Most SWT APIs require that you call them from the SWT thread.
 * Swing has similar restrictions though it does not enforce them as much as SWT.
 * <p>
 * Applications need to be aware of the current thread, and, where necessary, schedule tasks to run
 * on another thread. This has always been required in the pure Swing or SWT environments, but when
 * mixing Swing and SWT, more of this scheduling will be necessary.
 * <p>
 * To schedule work on the AWT event thread, you can use:
 * <ul>
 * <li>{@link javax.swing.SwingUtilities#invokeLater(Runnable)}
 * <li>{@link javax.swing.SwingUtilities#invokeAndWait(Runnable)}
 * </ul>
 * <p>
 * (or similar methods in {@link java.awt.EventQueue})
 * <p>
 * To schedule work on the SWT event thread, use:
 * <ul>
 * <li>{@link org.eclipse.swt.widgets.Display#asyncExec(Runnable)}
 * <li>{@link org.eclipse.swt.widgets.Display#syncExec(Runnable)}
 * </ul>
 * 
 * Of course, as in single-toolkit environments, long-running tasks should be offloaded from either
 * UI thread to a background thread. The Eclipse jobs API can be used for this purpose.
 */
public abstract class EmbeddedSwingComposite2 extends Composite {
  private static class AwtContext {
    private final Frame frame;
    private JComponent swingComponent;

    AwtContext(Frame frame) {
      assert frame != null;
      this.frame = frame;
    }

    Frame getFrame() {
      return frame;
    }

    void setSwingComponent(JComponent swingComponent) {
      this.swingComponent = swingComponent;
    }

    JComponent getSwingComponent() {
      return swingComponent;
    }
  }

  private AwtContext awtContext;
  private AwtFocusHandler awtHandler;
  // This listener helps ensure that Swing popup menus are properly dismissed when
  // a menu item off the SWT main menu bar is shown.
  private final Listener menuListener = new Listener() {
    public void handleEvent(Event event) {
      assert awtHandler != null;
      awtHandler.postHidePopups();
    }
  };

  /**
   * Constructs a new instance of this class given its parent and a style value describing its
   * behavior and appearance.
   * <p>
   * This method must be called from the SWT event thread.
   * <p>
   * The style value is either one of the style constants defined in class <code>SWT</code> which is
   * applicable to instances of this class, or must be built by <em>bitwise OR</em>'ing together
   * (that is, using the <code>int</code> "|" operator) two or more of those <code>SWT</code> style
   * constants. The class description lists the style constants that are applicable to the class.
   * Style bits are also inherited from superclasses.
   * </p>
   * <p>
   * The styles SWT.EMBEDDED and SWT.NO_BACKGROUND will be added to the specified style. Usually, no
   * other style bits are needed.
   * 
   * @param parent
   *          a widget which will be the parent of the new instance (cannot be null)
   * @param style
   *          the style of widget to construct
   * 
   * @exception IllegalArgumentException
   *              <ul>
   *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
   *              </ul>
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the SWT event thread
   *              </ul>
   * 
   * @see Widget#getStyle
   */
  public EmbeddedSwingComposite2(Composite parent, int style) {
    super(parent, style | SWT.EMBEDDED | SWT.NO_BACKGROUND);
    setLayout(new FillLayout());
    // set listeners
    addListener(SWT.Dispose, new Listener() {
      public void handleEvent(Event event) {
        dispose_AWT();
      }
    });
  }

  /**
   * Populates the embedded composite with the Swing component.
   * <p>
   * This method must be called from the SWT event thread.
   * <p>
   * The Swing component will be created by calling {@link #createSwingComponent()}. The creation is
   * scheduled asynchronously on the AWT event thread. This method does not wait for completion of
   * this asynchronous task, so it may return before createSwingComponent() is complete.
   * <p>
   * The Swing component is created inside a standard Swing containment hierarchy, rooted in a
   * {@link javax.swing.RootPaneContainer}. Clients can override
   * {@link #addRootPaneContainer(Frame)} to provide their own root pane container implementation.
   * <p>
   * This method can be called multiple times for a single instance. If an embedded frame exists
   * from a previous call, it is disposed.
   * 
   * @exception SWTException
   *              <ul>
   *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li> <li>
   *              ERROR_THREAD_INVALID_ACCESS - if not called from the SWT event thread
   *              </ul>
   */
  public void populate() {
    checkWidget();
    createFrame();
    scheduleComponentCreation();
  }

  @Override
  public Point computeSize(int wHint, int hHint, boolean changed) {
    // wait for Swing component creation
    while (awtContext.swingComponent == null) {
      getDisplay().readAndDispatch();
    }
    // return size of Swing component
    try {
      final java.awt.Dimension prefSize[] = new java.awt.Dimension[1];
      SwingImageUtils.runInDispatchThread(new Runnable() {
        public void run() {
          prefSize[0] = awtContext.swingComponent.getPreferredSize();
        }
      });
      //
      int width = wHint == SWT.DEFAULT ? prefSize[0].width : wHint;
      int height = hHint == SWT.DEFAULT ? prefSize[0].height : hHint;
      return new Point(width, height);
    } catch (Throwable e) {
    }
    // if exception, use (0, 0)
    return new Point(0, 0);
  }

  /**
   * Creates the embedded Swing component. This method is called from the AWT event thread.
   * <p>
   * Implement this method to provide the Swing component that will be shown inside this composite.
   * The returned component will be added to the Swing content pane. At least one component must be
   * created by this method; null is not a valid return value.
   * 
   * @return a non-null Swing component
   */
  protected abstract JComponent createSwingComponent();

  /**
   * Adds a root pane container to the embedded AWT frame. Override this to provide your own
   * {@link javax.swing.RootPaneContainer} implementation. In most cases, it is not necessary to
   * override this method.
   * <p>
   * This method is called from the AWT event thread.
   * <p>
   * If you are defining your own root pane container, make sure that there is at least one
   * heavyweight (AWT) component in the frame's containment hierarchy; otherwise, event processing
   * will not work correctly. See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4982522 for
   * more information.
   * 
   * @param frame
   *          the frame to which the root pane container is added
   * @return a non-null Swing component
   */
  protected RootPaneContainer addRootPaneContainer(Frame frame) {
    assert EventQueue.isDispatchThread(); // On AWT event thread
    assert frame != null;
    // It is important to set up the proper top level components in the frame:
    // 1) For Swing to work properly, Sun documents that there must be an implementor of 
    // javax.swing.RootPaneContainer at the top of the component hierarchy. 
    // 2) For proper event handling there must be a heavyweight 
    // an AWT frame must contain a heavyweight component (see 
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4982522)
    // 3) The Swing implementation further narrows the options by expecting that the 
    // top of the hierarchy be a JFrame, JDialog, JWindow, or JApplet. See javax.swing.PopupFactory.
    // All this drives the choice of JApplet for the top level Swing component. It is the 
    // only single component that satisfies all the above. This does not imply that 
    // we have a true applet; in particular, there is no notion of an applet lifecycle in this
    // context. 
    //
    // We need to intercept call of "getInputContext" because it make native call
    // and causes dead-lock in SWT.
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=376561
    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=291326
    JApplet applet = new JApplet() {
      @Override
      public InputContext getInputContext() {
        return null;
      }
    };
    // In JRE 1.4, the JApplet makes itself a focus cycle root. This
    // interferes with the focus handling installed on the parent frame, so
    // change it back to a non-root here. 
    // TODO: consider moving the focus policy from the Frame down to the JApplet
    applet.setFocusCycleRoot(false);
    frame.add(applet);
    return applet;
  }

  /**
   * Performs custom updates to newly set fonts. This method is called whenever a change to the
   * system font through the system settings (i.e. control panel) is detected.
   * <p>
   * This method is called from the AWT event thread.
   * <p>
   * In most cases it is not necessary to override this method. Normally, the implementation of this
   * class will automatically propogate font changes to the embedded Swing components through
   * Swing's Look and Feel support. However, if additional special processing is necessary, it can
   * be done inside this method.
   * 
   * @param newFont
   *          New AWT font
   */
  protected void updateAwtFont(java.awt.Font newFont) {
  }

  /**
   * Returns the embedded AWT frame. The returned frame is the root of the AWT containment hierarchy
   * for the embedded Swing component. This method can be called from any thread.
   * 
   * @return the embedded frame
   */
  public Frame getFrame() {
    // Intentionally leaving out checkWidget() call. This may need to be called from within user's 
    // createSwingComponent() method. Accessing from a non-SWT thread is OK, but we still check
    // for disposal
    if (getDisplay() == null || isDisposed()) {
      SWT.error(SWT.ERROR_WIDGET_DISPOSED);
    }
    return awtContext != null ? awtContext.getFrame() : null;
  }

  private void createFrame() {
    assert Display.getCurrent() != null; // On SWT event thread
    // Make sure Awt environment is initialized. 
    //AwtEnvironment.getInstance(getDisplay());
    if (awtContext != null) {
      final Frame oldFrame = awtContext.getFrame();
      // Schedule disposal of old frame on AWT thread so that there are no problems with
      // already-scheduled operations that have not completed.
      // Note: the implementation of Frame.dispose() would schedule the use of the AWT 
      // thread even if it was not done here, but it uses invokeAndWait() which is 
      // prone to deadlock (and not necessary for this case). 
      EventQueue.invokeLater(new Runnable() {
        public void run() {
          oldFrame.dispose();
        }
      });
    }
    Frame frame = SWT_AWT.new_Frame(this);
    frame.getInputContext();
    awtContext = new AwtContext(frame);
    // Glue the two frameworks together. Do this before anything is added to the frame
    // so that all necessary listeners are in place.
    createFocusHandlers();
    // This listener clears garbage during resizing, making it looker much cleaner 
    addControlListener(new CleanResizeListener());
  }

  private void createFocusHandlers() {
    assert awtContext != null;
    assert Display.getCurrent() != null; // On SWT event thread
    Frame frame = awtContext.getFrame();
    awtHandler = new AwtFocusHandler(frame);
    SwtFocusHandler swtHandler = new SwtFocusHandler(this);
    awtHandler.setSwtHandler(swtHandler);
    swtHandler.setAwtHandler(awtHandler);
    // Ensure that AWT popups are dismissed whenever a SWT menu is shown
    getDisplay().addFilter(SWT.Show, menuListener);
    EmbeddedChildFocusTraversalPolicy policy = new EmbeddedChildFocusTraversalPolicy(awtHandler);
    frame.setFocusTraversalPolicy(policy);
  }

  private void scheduleComponentCreation() {
    assert awtContext != null;
    // Create AWT/Swing components on the AWT thread. This is 
    // especially necessary to avoid an AWT leak bug (6411042).
    final AwtContext currentContext = awtContext;
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        RootPaneContainer container = addRootPaneContainer(currentContext.getFrame());
        JComponent swingComponent = createSwingComponent();
        currentContext.setSwingComponent(swingComponent);
        container.getRootPane().getContentPane().add(swingComponent);
      }
    });
  }

  private boolean isFocusable() {
    if (awtContext == null) {
      return false;
    }
    JComponent swingComponent = awtContext.getSwingComponent();
    return swingComponent != null && swingComponent.isFocusable();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#setFocus()
   */
  @Override
  public boolean setFocus() {
    checkWidget();
    if (!isFocusable()) {
      return false;
    }
    return super.setFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Control#forceFocus()
   */
  @Override
  public boolean forceFocus() {
    checkWidget();
    if (!isFocusable()) {
      return false;
    }
    return super.forceFocus();
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose() {
    if (!isDisposed()) {
      dispose_AWT();
      super.dispose();
    }
  }

  private void dispose_AWT() {
    // remove listeners
    getDisplay().removeFilter(SWT.Show, menuListener);
    // dispose frame to avoid lock down in EventQueue.invokeAndWait() later 
    if (awtContext != null) {
      Frame oldFrame = awtContext.getFrame();
      oldFrame.dispose();
    }
  }

  /**
   * There is problem with running SWT_AWT on OSX with Java 1.7
   * <p>
   * https://bugs.eclipse.org/bugs/show_bug.cgi?id=374199
   * <p>
   * It is marked "fixed", but still does not work.
   */
  public static boolean canUseAwt() {
    Shell shell = new Shell();
    try {
      try {
        Frame frame = SWT_AWT.new_Frame(shell);
        frame.dispose();
        return true;
      } catch (Throwable e) {
        return false;
      }
    } finally {
      shell.dispose();
    }
  }
}
