/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.android.orientationfaker.orientation;

import android.content.pm.ActivityInfo;

import net.mm2d.android.orientationfaker.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:ryo@mm2d.net">大前良介 (OHMAE Ryosuke)</a>
 */
public class OrientationIdManager {
    public static class OrientationId {
        private final int mOrientation;
        private final int mViewId;
        private final int mIconId;

        OrientationId(
                final int orientation,
                final int viewId,
                final int iconId) {
            mOrientation = orientation;
            mViewId = viewId;
            mIconId = iconId;
        }

        public int getOrientation() {
            return mOrientation;
        }

        public int getViewId() {
            return mViewId;
        }

        public int getIconId() {
            return mIconId;
        }
    }

    private static final List<OrientationId> ID_LIST = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            new OrientationId(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, R.id.button_unspecified, R.drawable.ic_unspecified),
            new OrientationId(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, R.id.button_portrait, R.drawable.ic_portrait),
            new OrientationId(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, R.id.button_landscape, R.drawable.ic_landscape),
            new OrientationId(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, R.id.button_reverse_portrait, R.drawable.ic_reverse_portrait),
            new OrientationId(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, R.id.button_reverse_landscape, R.drawable.ic_reverse_landscape)
    )));

    public static List<OrientationId> getList() {
        return ID_LIST;
    }

    public static int getViewIdFromOrientation(final int orientation) {
        for (final OrientationId id : ID_LIST) {
            if (id.getOrientation() == orientation) {
                return id.getViewId();
            }
        }
        return 0;
    }

    public static int getIconIdFromOrientation(final int orientation) {
        for (final OrientationId id : ID_LIST) {
            if (id.getOrientation() == orientation) {
                return id.getIconId();
            }
        }
        return 0;
    }
}
