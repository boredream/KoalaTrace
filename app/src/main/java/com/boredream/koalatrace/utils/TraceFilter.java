package com.boredream.koalatrace.utils;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.boredream.koalatrace.data.TraceLocation;

import java.util.ArrayList;
import java.util.List;

// TODO: need check
public class TraceFilter {

    private List<TraceLocation> mListPoint = new ArrayList<>();

    final int MAX_SPEED = 5; // 最大速度 米/秒。走路1.5。跑步3。骑车5。
    private Boolean isFirst = true; // 是否是第一次定位点
    private TraceLocation weight1 = new TraceLocation(0.0, 0.0, 0); // 权重点1
    private TraceLocation weight2; // 权重点2
    private List<TraceLocation> w1TempList = new ArrayList<>(); // w1的临时定位点集合
    private List<TraceLocation> w2TempList = new ArrayList<>(); // w2的临时定位点集合
    private int w1Count = 0; // 统计w1Count所统计过的点数

    /**
     * @param aMapLocation
     * @return 是否获得有效点，需要存储和计算距离
     */
    public Boolean filterPos(TraceLocation aMapLocation) {
        String filterString = "";

        try {
            // 获取的第一个定位点不进行过滤
            if (isFirst) {
                isFirst = false;
                weight1.setLatitude(aMapLocation.getLatitude());
                weight1.setLongitude(aMapLocation.getLongitude());
                weight1.setTime(aMapLocation.getTime());

                /****************存储数据到文件中，后面好分析******************/

                filterString += "第一次" + " : ";

                /**********************************/

                // 将得到的第一个点存储入w1的缓存集合
                final TraceLocation traceLocation = new TraceLocation(0.0, 0.0, 0);
                traceLocation.setLatitude(aMapLocation.getLatitude());
                traceLocation.setLongitude(aMapLocation.getLongitude());
                traceLocation.setTime(aMapLocation.getTime());
                w1TempList.add(traceLocation);
                w1Count++;

                return true;

            } else {
                filterString += "非第一次" + " : ";
//                // 过滤静止时的偏点，在静止时速度小于1米就算做静止状态
//                if (aMapLocation.getSpeed() < 1) {
//                    return false;
//                }

                /****************存储数据到文件中，后面好分析******************/
//				SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
//				Date date1 = new Date(aMapLocation.getTime());
//				String time1 = df1.format(date1); // 定位时间
//				String testString = time1 + ":" + aMapLocation.getTime() + "," + aMapLocation.getLatitude() + "," + aMapLocation.getLongitude() + "," + aMapLocation.getSpeed() + "\r\n";
////				FileWriteUtil.getInstance().save("tutu_driver_true.txt", testString);
//				Log.d("TraceFilter",testString);

                if (weight2 == null) {
                    filterString += "weight2=null" + " : ";
                    // 计算w1与当前定位点p1的时间差并得到最大偏移距离D
                    long offsetTimeMils = aMapLocation.getTime() - weight1.getTime();
                    long offsetTimes = (offsetTimeMils / 1000);
                    long MaxDistance = offsetTimes * MAX_SPEED;
                    float distance = AMapUtils.calculateLineDistance(
                            new LatLng(weight1.getLatitude(), weight1.getLongitude()),
                            new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));
                    filterString += "distance = " + distance + ",MaxDistance = " + MaxDistance + " : ";

                    if (distance > MaxDistance) {
                        filterString += "distance > MaxDistance" + "当前点 距离大: 设置w2位新的点，并添加到w2TempList";
                        // 将设置w2位新的点，并存储入w2临时缓存
                        weight2 = new TraceLocation(0.0, 0.0, 0);
                        weight2.setLatitude(aMapLocation.getLatitude());
                        weight2.setLongitude(aMapLocation.getLongitude());
                        weight2.setTime(aMapLocation.getTime());
                        w2TempList.add(weight2);
                        return false;
                    } else {
                        filterString += "distance < MaxDistance" + "当前点 距离小 : 添加到w1TempList";
                        // 将p1加入到做坐标集合w1TempList
                        TraceLocation traceLocation = new TraceLocation(0.0, 0.0, 0);
                        traceLocation.setLatitude(aMapLocation.getLatitude());
                        traceLocation.setLongitude(aMapLocation.getLongitude());
                        traceLocation.setTime(aMapLocation.getTime());
                        w1TempList.add(traceLocation);
                        w1Count++;
                        // 更新w1权值点
                        weight1.setLatitude(weight1.getLatitude() * 0.2 + aMapLocation.getLatitude() * 0.8);
                        weight1.setLongitude(weight1.getLongitude() * 0.2 + aMapLocation.getLongitude() * 0.8);
                        weight1.setTime(aMapLocation.getTime());
//                        weight1.setSpeed(aMapLocation.getSpeed());

//						if (w1TempList.size() > 3) {
//							filterString += "d1TempList.size() > 3" + " : 更新";
//							// 将w1TempList中的数据放入finalList，并将w1TempList清空
//							mListPoint.addAll(w1TempList);
//							w1TempList.clear();
//							return true;
//						} else {
//							filterString += "d1TempList.size() < 3" + " : 不更新";
//							return false;
//						}
                        if (w1Count > 3) {
                            filterString += " : 更新";
                            mListPoint.addAll(w1TempList);
                            w1TempList.clear();
                            return true;
                        } else {
                            filterString += " w1Count<3: 不更新";
                            return false;
                        }
                    }

                } else {
                    filterString += "weight2 != null" + " : ";
                    // 计算w2与当前定位点p1的时间差并得到最大偏移距离D
                    long offsetTimeMils = aMapLocation.getTime() - weight2.getTime();
                    long offsetTimes = (offsetTimeMils / 1000);
                    long MaxDistance = offsetTimes * 16;
                    float distance = AMapUtils.calculateLineDistance(
                            new LatLng(weight2.getLatitude(), weight2.getLongitude()),
                            new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()));

                    filterString += "distance = " + distance + ",MaxDistance = " + MaxDistance + " : ";

                    if (distance > MaxDistance) {
                        filterString += "当前点 距离大: weight2 更新";
                        w2TempList.clear();
                        // 将设置w2位新的点，并存储入w2临时缓存
                        weight2 = new TraceLocation(0.0, 0.0, 0);
                        weight2.setLatitude(aMapLocation.getLatitude());
                        weight2.setLongitude(aMapLocation.getLongitude());
                        weight2.setTime(aMapLocation.getTime());

                        w2TempList.add(weight2);

                        return false;
                    } else {
                        filterString += "当前点 距离小: 添加到w2TempList";

                        // 将p1加入到做坐标集合w2TempList
                        TraceLocation traceLocation = new TraceLocation(0.0, 0.0, 0);
                        traceLocation.setLatitude(aMapLocation.getLatitude());
                        traceLocation.setLongitude(aMapLocation.getLongitude());
                        traceLocation.setTime(aMapLocation.getTime());
                        w2TempList.add(traceLocation);

                        // 更新w2权值点
                        weight2.setLatitude(weight2.getLatitude() * 0.2 + aMapLocation.getLatitude() * 0.8);
                        weight2.setLongitude(weight2.getLongitude() * 0.2 + aMapLocation.getLongitude() * 0.8);
                        weight2.setTime(aMapLocation.getTime());
//                        weight2.setSpeed(aMapLocation.getSpeed());

                        if (w2TempList.size() > 4) {
                            // 判断w1所代表的定位点数是否>4,小于说明w1之前的点为从一开始就有偏移的点
                            if (w1Count > 4) {
                                filterString += "w1Count > 4" + "计算增加W1";
                                mListPoint.addAll(w1TempList);
                            } else {
                                filterString += "w1Count < 4" + "计算丢弃W1";
                                w1TempList.clear();
                            }
                            filterString += "w2TempList.size() > 4" + " : 更新到偏移点";

                            // 将w2TempList集合中数据放入finalList中
                            mListPoint.addAll(w2TempList);

                            // 1、清空w2TempList集合 2、更新w1的权值点为w2的值 3、将w2置为null
                            w2TempList.clear();
                            weight1 = weight2;
                            weight2 = null;
                            return true;

                        } else {
                            filterString += "w2TempList.size() < 4" + "\r\n";
                            return false;
                        }
                    }
                }
            }
        } finally {
            // System.out.println("TraceFilter" + filterString);
        }
    }

}
