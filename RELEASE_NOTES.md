# Eclipse WindowBuilder: Migration Guide

This page describes all changes introduced by each release which may impact user
code.

## 1.15.0 (2024-03)

### Support of lambda expressions for event Listeners

It is now possible to use lambda expressions to implement (event) listeners,
in addition to anonymous classes.

Example:
addHelpListener(e -> {
   ...
});

With limited functionality, it is also possible to use factory methods
used by e.g. SWT.

The name of the factory method is expected to be of the form
`&lt;method&gt;Adapter(Consumer&lt;Event&gt; c)`

Example:
addMouseListener(MouseListener.mouseUpAdapter(e -> {
   ...
}));

### Deprecation and removal of XWT editor

The technology is effectively dead and the project no longer actively maintained.
In prevent compilation errors inside the workspace, those components have been
removed from the project.

## 1.14.0 (2023-12)

### Deprecation of SWTResourceManager

WindowBuilder now uses class-local instances of LocalResourceManager, instead of
the global SWTResourceManager, to keep track of SWT resources.

Note that WindowBuilder is still able to recognize usage of the
SWTResourceManager in user code, but any further modifications will exclusively
use the LocalResourceManager.

Users can migrate their code by manually creating a local LocalResourceManager
variable, by initializing it with `JFaceResources.managerFor(...)` and by then
replacing all calls to ASTResourceManager with calls to the local variable.

## 1.13.0 (2023-09)

### Java 8 no longer supported

It is no longer possible to use the Design editor with a Java 8 JRE. If your
application requires to compile against this Java version, consider upgrading to
the latest JDK and instead simply setting an appropriate project-specific
compiler compliance.
