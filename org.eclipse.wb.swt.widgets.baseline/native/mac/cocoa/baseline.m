#include <jni.h>

#define JHANDLE jlong
#define OS_NATIVE(func) Java_org_eclipse_wb_swt_widgets_baseline_CocoaBaseline_##func

JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1fetchBaseline)
(JNIEnv * env, jobject this, JHANDLE widgetHandle) {
	if (widgetHandle == 0) {
		return -1;
	}
	
	id widgetID = (id)widgetHandle;
	NSCell *widgetCell = nil;
	NSView *converterView = nil;
	
	if ([widgetID isKindOfClass:[NSControl class]]) {
		// most of controls
		NSControl *widget = (NSControl*)widgetID;
		widgetCell = [widget cell];
	} else if ([widgetID isKindOfClass:[NSBox class]]) {
		// label
		NSBox *widget = (NSBox*)widgetID;
		if ([widget boxType] == NSBoxSeparator) {
			return -1;
		}
		NSView *view = [widget contentView];
		if ([view isKindOfClass:[NSTextField class]]) {
			NSTextField *contentView = (NSTextField *)view;
			widgetCell = [contentView cell];
			converterView = contentView;
		} else {
			widgetCell = [widget titleCell];
		}
	} else {
		// something unknown, skip it
		return -1;
	}
	
	NSView *widgetView = (NSView*)widgetID;
	NSRect titleRect = [widgetCell titleRectForBounds:[widgetView bounds]];
	
	if (converterView != nil) {
		titleRect = [converterView convertRect:titleRect toView:widgetView];
	}
	CGFloat baseline = ceil(NSMinY(titleRect) + [[widgetCell font] ascender]);
	return baseline;
}


