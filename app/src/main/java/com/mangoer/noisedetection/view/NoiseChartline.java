package com.mangoer.noisedetection.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.text.TextUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;



/**
 * @ClassName NoiseChartline
 * @Description TODO()
 * @author Mangoer
 * @Date 2017/12/25 17:29
 */
public class NoiseChartline {

	private GraphicalView            mGraphicalView;
	private XYMultipleSeriesDataset  multipleSeriesDataset; // 数据集容器
	private XYMultipleSeriesRenderer multipleSeriesRenderer;// 渲染器容器
	private XYSeries                 mSeries;               // 单条曲线数据集
	private Context                  context;

	
	public NoiseChartline(Context context) {
		this.context = context;
	}

	/**
	 * 获取图表
	 * @return
	 */
	public GraphicalView getGraphicalView() {
		mGraphicalView = ChartFactory.getCubeLineChartView(context,multipleSeriesDataset, multipleSeriesRenderer, 0.3f);//折现图
		return mGraphicalView;
	}
	/**
	 * 获取数据集，及坐xy标的集合
	 * @param curveTitle
	 */
	public void setXYMultipleSeriesDataset(String curveTitle) {
		multipleSeriesDataset = new XYMultipleSeriesDataset();
		mSeries = new XYSeries(curveTitle);
		multipleSeriesDataset.addSeries(mSeries);
	}

	/**
	 * 获取渲染器
	 *
	 * @param maxY y轴最大值
	 * @param chartTitle 曲线的标题
	 * @param xTitle x轴标题
	 * @param yTitle y轴标题
	 */
	public void setXYMultipleSeriesRenderer(double maxY, String chartTitle, String xTitle, String yTitle) {

        if (TextUtils.isEmpty(chartTitle)|| TextUtils.isEmpty(xTitle)|| TextUtils.isEmpty(yTitle)) return;

		multipleSeriesRenderer = new XYMultipleSeriesRenderer();//坐标轴

//        multipleSeriesRenderer.setChartTitle(chartTitle);
//        multipleSeriesRenderer.setChartTitleTextSize(30);//设置整个图表上方标题文字的大小
		multipleSeriesRenderer.setXTitle(xTitle);
		multipleSeriesRenderer.setYTitle(yTitle);

        multipleSeriesRenderer.setAxesColor(Color.BLACK);//设置轴的颜色
        multipleSeriesRenderer.setAxisTitleTextSize(20);//设置轴文字标注的大小
        multipleSeriesRenderer.setLabelsColor(Color.BLACK);//设置轴文字标注的颜色
//        multipleSeriesRenderer.setRange(new double[]{0, 4, 0, maxY });//xy轴的刻度范围
        multipleSeriesRenderer.setYAxisMax(maxY);//设置y轴的最大值
        multipleSeriesRenderer.setYAxisMin(0);//设置y轴的最小值
        multipleSeriesRenderer.setYLabels(20);//Y轴刻度等份
        multipleSeriesRenderer.setXLabels(0);//X轴刻度等份

        multipleSeriesRenderer.setLabelsTextSize(20);//设置横纵坐标刻度值的大小
        multipleSeriesRenderer.setXLabelsColor(Color.BLACK);//横坐标上面刻度值的颜色
        multipleSeriesRenderer.setYLabelsColor(0,Color.BLACK);//纵坐标上面刻度值的颜色
        multipleSeriesRenderer.setXLabelsAlign(Align.CENTER);//刻度值相对于刻度的位置
        multipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);

        multipleSeriesRenderer.setShowLegend(false);//设置图例显示
//        multipleSeriesRenderer.setLegendTextSize(15);//设置图例文字大小
//		  multipleSeriesRenderer.setShowGrid(true);//显示网格
//        multipleSeriesRenderer.setGridColor(gridColor);//显示网格颜色

        multipleSeriesRenderer.setMargins(new int[]{20,50,20,50}); //设置图形四周的留白 top-left-bottom-right
        multipleSeriesRenderer.setMarginsColor(Color.WHITE);//边距背景色，默认背景色为黑色，这里修改为白色
		multipleSeriesRenderer.setPointSize(0.5f);//曲线描点尺寸 设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
        multipleSeriesRenderer.setPanEnabled(false,false);//禁止报表的拖动
		multipleSeriesRenderer.setZoomEnabled(false, false);//禁止报表xy轴缩放

        XYSeriesRenderer r = new XYSeriesRenderer();//设置颜色和点类型
        r.setColor(Color.RED);
        r.setPointStyle(PointStyle.CIRCLE);//描点风格，可以为圆点，方形点等等
        r.setFillPoints(true);
        r.setChartValuesSpacing(3);
        multipleSeriesRenderer.addSeriesRenderer(r);//在坐标上描点
	}


    List<Double> Y = new ArrayList<>();
    float xDate = 0.5f;
    double count;
    int length;
	public void updateChart(double addY){
        //移除数据集中旧的点集  
		multipleSeriesDataset.removeSeries(mSeries);  
        //屏幕总共只绘制12个点
        if (length < 12) {
            length = mSeries.getItemCount();
        }

        count++;

        double data = getScaleNum((count-1)*xDate);
        if (length < 12) {

            mSeries.add(data, addY);//往坐标系中的数据集添加新的点（x，y）
            Y.add(addY);
            multipleSeriesRenderer.addXTextLabel(data,data+"");//在X轴添加横坐标x对应的刻度值

        } else {

            mSeries.clear();//先把坐标系的数据集清空

            multipleSeriesRenderer.clearXTextLabels();//先把X轴刻度全部清空 重新添加刻度
            for(int i = 0; i< Y.size(); i++){
                double d = getScaleNum((count-length+i)*xDate);
                multipleSeriesRenderer.addXTextLabel(d,d +"");//每新增一个点，就将刻度值往左平移一个刻度距离，使横轴刻度随着点X坐标值动态改变
                if (Y.size() > i+1) {
                    mSeries.add(d, Y.get(i+1));//取出原数据集中的后11个点重新添加到数据集中（x，y）
                }
            }
            Y.remove(0);
            Y.add(addY);

            mSeries.add(data, addY);//往坐标系中的数据集添加新的点（x，y）
        }
        //在数据集中添加新的点集  
        multipleSeriesDataset.addSeries(mSeries);

        mGraphicalView.invalidate();
	}


	public Double getScaleNum(double d) {
        BigDecimal decimal = new BigDecimal(d);
        decimal = decimal.setScale(1,BigDecimal.ROUND_DOWN);
        return decimal.doubleValue();
    }
}
