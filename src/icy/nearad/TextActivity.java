package icy.nearad;

import icy.baixing.entity.Ad;
import icy.nearad.TextFactory.ViewWithDegree;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class TextActivity extends Activity implements SurfaceHolder.Callback, SensorEventListener {
	private RelativeLayout textsParent;
	private TextFactory factory;
	private ArrayList<Ad> adList;
	private ArrayList<View> views;
	private ArrayList<TextView> textViews;//按远近排过序，为了画得遮挡先后顺序
	private ArrayList<Double> degreeList;//与textview按顺序距离对应
	private ArrayList<Integer> marginList;
	double lastDirection;
	SensorManager sensorManager;
	Sensor sensor;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_textlayout);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,   
                WindowManager.LayoutParams.FLAG_FULLSCREEN);  //隐藏手机状态栏
        
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.cameraSurfaceView);
        SurfaceHolder holder = surfaceView.getHolder();
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		holder.addCallback(this);
        
        dHeight = getWindowManager().getDefaultDisplay().getHeight();
        dWidth = getWindowManager().getDefaultDisplay().getWidth();
        textsParent = (RelativeLayout)findViewById(R.id.textsParent);
        adList = SplashCover.getAdsList();
        factory = new TextFactory(this);
        ArrayList<ViewWithDegree> lists= factory.setAds(adList);
        views = new ArrayList<View>();
        textViews = new ArrayList<TextView>();
        degreeList = new ArrayList<Double>();
        marginList = new ArrayList<Integer>();
        for (int i = 0; i < lists.size(); i++) {
        	views.add(lists.get(i).view);
			textViews.add(lists.get(i).textView);
			degreeList.add(lists.get(i).degree);
			marginList.add(lists.get(i).topMargin);
		}
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);  
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	}
	@Override  
    protected void onPause() {
        super.onPause();  
        sensorManager.unregisterListener(this);
    }
	@Override
    protected void onResume() {
        super.onResume();  
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);  
    }
	
	double dHeight, dWidth;
	double coverDegree = 30;
	private void drawTexts(double nowDirect) {
		textsParent.removeAllViews();
		textsParent.invalidate();
		//给left加小范围random，不至于那么呆板
		for (int i = 0; i < degreeList.size(); i++) {
			double degree = degreeList.get(i);
			View viewParent = views.get(i);
			double delta = degree - nowDirect;
			LayoutParams relativeParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			relativeParams.topMargin = marginList.get(i);
			//都集中在左边可能是因为有负数
			/*if (delta >=0 && delta <= coverDegree) {//画在右边
				Log.e("左边", "delta: " + delta);
				//(int)(Math.random() * 400);//每条ad一出生自带
				relativeParams.leftMargin = (int)(dWidth/2 + dWidth/2 * delta/coverDegree + Math.random()*100 - 50);
				
			} else if (delta >=-coverDegree && delta < 0) {//画左边
				relativeParams.leftMargin = (int)(dWidth/2 - dWidth/2 * Math.abs(delta)/coverDegree + Math.random()*100 - 50);
			}*/
			Log.e("", "nowdirect: " + nowDirect + "   delta: " + delta);
			if (delta >=0 && delta <= coverDegree) {//反一下
				relativeParams.leftMargin = (int)(dWidth/2 - dWidth/2 * Math.abs(delta)/coverDegree + Math.random()*100 - 50);
			} else if (delta >=-coverDegree && delta < 0) {
				relativeParams.leftMargin = (int)(dWidth/2 + dWidth/2 * Math.abs(delta)/coverDegree + Math.random()*100 - 50);
			} else {
				continue;
			}
			viewParent.setLayoutParams(relativeParams);
			textsParent.addView(viewParent);
		}
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		double directNow = event.values[0];
		if (Math.abs(directNow - lastDirection) > 10) {//超过10°刷新一下
			lastDirection = directNow;
			drawTexts(directNow);
		}
	}

	private Camera camera;
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		Camera.Parameters param = camera.getParameters();
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			camera.setDisplayOrientation(90);
		} else {
			camera.setDisplayOrientation(0);
		}
		camera.setParameters(param);
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		camera.startPreview();
			
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}

}
