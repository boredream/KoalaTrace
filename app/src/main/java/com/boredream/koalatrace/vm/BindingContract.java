package com.boredream.koalatrace.vm;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.boredream.koalatrace.common.vmcompose.RefreshUiState;
import com.boredream.koalatrace.data.TraceLocation;
import com.boredream.koalatrace.data.TraceRecord;
import com.boredream.koalatrace.utils.GlideUtils;
import com.boredream.koalatrace.view.RefreshListView;
import com.boredream.koalatrace.view.SyncStatusView;
import com.boredream.koalatrace.view.TraceMapView;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;

/**
 * 用于解决自定义组件的DataBinding问题
 */
public class BindingContract {

    @BindingAdapter("android:src")
    public static void setImageViewGlideUrl(ImageView iv, String newValue) {
        GlideUtils.INSTANCE.load(Glide.with(iv), newValue, iv);
    }

    @BindingAdapter("android:src")
    public static void setPhotoViewImageSource(PhotoView photoView, String imageSource) {
        GlideUtils.INSTANCE.load(Glide.with(photoView), imageSource, photoView);
    }

    @BindingAdapter("myLocation")
    public static void setTraceMapViewMyLocation(TraceMapView traceMapView, TraceLocation location) {
        if (location == null) return;
        traceMapView.setMyLocation(location);
    }

    @BindingAdapter("traceList")
    public static void setTraceMapViewTraceList(TraceMapView traceMapView, TraceRecord traceRecord) {
        if (traceRecord == null) return;
        traceMapView.drawTraceRecord(traceRecord);
    }

    @BindingAdapter("historyTraceList")
    public static void setTraceMapViewHistoryTraceList(TraceMapView traceMapView, ArrayList<TraceRecord> historyTraceList) {
        if (historyTraceList == null) return;
        traceMapView.drawMultiFixTraceList(historyTraceList);
    }

    @BindingAdapter("isFollowing")
    public static void setTraceMapViewFollowingMode(TraceMapView traceMapView, boolean isFollowing) {
        traceMapView.setFollowingMode(isFollowing);
    }

    @BindingAdapter("refreshState")
    public static void setRefreshListViewRefreshState(RefreshListView refreshListView, RefreshUiState refreshUiState) {
        if (refreshUiState != null) refreshListView.updateRefreshState(refreshUiState);
    }

    @BindingAdapter("dataList")
    public static void setRefreshListViewDataList(RefreshListView refreshListView, ArrayList<?> dataList) {
        if (dataList != null) refreshListView.updateDataList(dataList);
    }

    @BindingAdapter("isSyncing")
    public static void setSyncStatusViewStatus(SyncStatusView view, boolean isSyncing) {
        if (view != null && view.isAttachedToWindow()) view.setRefresh(isSyncing);
    }

    // 用于解决需要转换的数据转换问题
    public static class Convert {

//        @InverseMethod("notifyTypeStringToInt")
//        public static String notifyTypeIntToString(Integer value) {
//            return value != null && value == TheDay.NOTIFY_TYPE_YEAR_COUNT_DOWN ? "按年倒计天数" : "累计天数";
//        }
//
//        public static Integer notifyTypeStringToInt(String value) {
//            return Objects.equals(value, "累计天数")
//                    ? TheDay.NOTIFY_TYPE_TOTAL_COUNT
//                    : TheDay.NOTIFY_TYPE_YEAR_COUNT_DOWN;
//        }

    }

}
