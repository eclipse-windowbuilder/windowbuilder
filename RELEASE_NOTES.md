# Eclipse WindowBuilder: Migration Guide

This page describes all changes introduced by each release which may impact user
code.

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