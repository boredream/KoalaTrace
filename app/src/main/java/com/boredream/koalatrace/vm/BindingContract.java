package com.boredream.koalatrace.vm;

import androidx.databinding.BindingAdapter;

import com.boredream.koalatrace.common.vmcompose.RefreshUiState;
import com.boredream.koalatrace.data.TraceLocation;
import com.boredream.koalatrace.data.TraceRecord;
import com.boredream.koalatrace.view.RefreshListView;
import com.boredream.koalatrace.view.TraceMapView;

import java.util.ArrayList;

/**
 * 用于解决自定义组件的DataBinding问题
 */
public class BindingContract {

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

}
