package com.example.refresh;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.refresh.ui.RefreshListView;
import com.example.refresh.ui.RefreshListView.OnRefreshListener;


public class MainActivity extends Activity {

    private RefreshListView listview;
	private ArrayList<String> dataList;
	private MyAdapter mAdapter;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        initData();
        initUI();
    }

	private void initData() {
		dataList = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			dataList.add("这是一条ListView的数据" + i);
			
		}
	}

	private void initUI() {
		listview = (RefreshListView) findViewById(R.id.listview);
		
//		Button button = new Button(this);
//		listview.addHeaderView(button);
		
		mAdapter = new MyAdapter();
		listview.setAdapter(mAdapter);
		
		listview.setOnRefreshListener(new OnRefreshListener(){

			@Override
			public void onRefresh() {
				new Thread(){
					public void run() {
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						dataList.add(0, "我是下拉刷新的ListView数据！");
						
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// 刷新数据
								mAdapter.notifyDataSetChanged();
								// 告诉头布局，更新状态
								listview.onRefreshComplete();
							}
						});
						
					};
				}.start();
			}

			@Override
			public void onLoadMore() {
				new Thread(){
					public void run() {
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						dataList.add("我是加载更多的ListView数据！1");
						dataList.add("我是加载更多的ListView数据！2");
						dataList.add("我是加载更多的ListView数据！3");
						
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// 刷新数据
								mAdapter.notifyDataSetChanged();
								// 告诉头布局，更新状态
								listview.onRefreshComplete();
							}
						});
						
					};
				}.start();
			}
			
		});
		
	}
	
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return dataList.size();
		}

		@Override
		public Object getItem(int position) {
			return dataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView textView = new TextView(getApplicationContext());
			textView.setTextSize(18);
			textView.setTextColor(Color.BLACK);
			textView.setText(dataList.get(position));
			return textView;
		}
		
	}

}
