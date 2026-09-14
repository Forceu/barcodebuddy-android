package de.bulling.barcodebuddyscanner.Helper;

import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Since the app targets SDK 35+ (Android 15), the system now draws the app
 * edge-to-edge by default: window content is placed behind the status bar
 * and the navigation/gesture bar instead of being automatically inset from
 * them. Without this, views pinned to the top or bottom of the screen (e.g.
 * a toolbar, or buttons anchored to the bottom of the layout) end up
 * partially or fully hidden under the system bars, and touches on that area
 * either miss the view or get intercepted by the system gesture bar instead.
 * <p>
 * This helper re-applies the system bar insets as padding on a given view,
 * so content is pushed clear of the bars again while still allowing the
 * rest of the window (e.g. a camera preview) to extend underneath them.
 */
public final class EdgeToEdgeHelper {

	private EdgeToEdgeHelper() {
	}

	/**
	 * Pads {@code view} by the current system bar (status bar + navigation
	 * bar) and display cutout insets, added on top of whatever padding the
	 * view already had in its layout, on the requested sides only.
	 */
	public static void applyInsetsAsPadding(final View view,
											 final boolean left,
											 final boolean top,
											 final boolean right,
											 final boolean bottom) {
		final int basePaddingLeft   = view.getPaddingLeft();
		final int basePaddingTop    = view.getPaddingTop();
		final int basePaddingRight  = view.getPaddingRight();
		final int basePaddingBottom = view.getPaddingBottom();

		ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
			Insets bars = windowInsets.getInsets(
					WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());

			v.setPadding(
					basePaddingLeft + (left ? bars.left : 0),
					basePaddingTop + (top ? bars.top : 0),
					basePaddingRight + (right ? bars.right : 0),
					basePaddingBottom + (bottom ? bars.bottom : 0));

			return windowInsets;
		});

		if (view.isAttachedToWindow()) {
			view.requestApplyInsets();
		}
	}
}
