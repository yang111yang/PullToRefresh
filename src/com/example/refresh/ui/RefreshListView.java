package com.example.refresh.ui;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.refresh.R;

/**
 * 带有下拉刷新和上拉加载更多的ListView
 * 
 * @author 刘建阳
 * @date 2016-10-19 上午10:50:22
 */
@SuppressLint("NewApi")
public class RefreshListView extends ListView implements OnScrollListener {

	public static final int PULL_TO_REFRESH = 0;
	public static final int RELEASE_REFRESH = 1;
	public static final int REFRESHING = 2;

	private static final String tag = "RefreshListView";
	private View mHeaderView;
	private float downY;
	private float moveY;
	private int currentState = PULL_TO_REFRESH;
	private RotateAnimation rotateUpAnim;
	private RotateAnimation rotateDownAnim;
	private ImageView iv_arrow;
	private ProgressBar pb;
	private TextView tv_title;
	private TextView tv_desc;
	private int mHeaderViewHeight;
	private OnRefreshListener onRefreshListener;
	private View mFooterView;
	private int mFooterViewHeight;
	private boolean isLoadingMore;

	public RefreshListView(Context context) {
		super(context);
		init();
	}

	public RefreshListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	/**
	 * 初始化头布局、脚布局、滚动监听
	 */
	private void init() {

		// 初始化头布局
		initHeaderView();

		// 初始化脚步局
		initFooterView();

		// 初始化头布局的动画
		initAnimation();

		setOnScrollListener(this);
	}

	private void initFooterView() {
		mFooterView = View.inflate(getContext(), R.layout.layout_footer_list,
				null);
		mFooterView.measure(0, 0);
		mFooterViewHeight = mFooterView.getMeasuredHeight();
		mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);

		addFooterView(mFooterView);
	}

	/**
	 * 初始化头布局的动画
	 */
	private void initAnimation() {
		// 向上转，180度
		rotateUpAnim = new RotateAnimation(0, -180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		rotateUpAnim.setDuration(500);
		rotateUpAnim.setFillAfter(true);
		// 向下转，-180-->-360
		rotateDownAnim = new RotateAnimation(-180f, -360f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotateDownAnim.setDuration(500);
		rotateDownAnim.setFillAfter(true);

	}

	/**
	 * 初始化头布局
	 */
	private void initHeaderView() {
		mHeaderView = View.inflate(getContext(), R.layout.layout_header_list,
				null);
		iv_arrow = (ImageView) mHeaderView.findViewById(R.id.iv_arrow);
		pb = (ProgressBar) mHeaderView.findViewById(R.id.pb);
		tv_title = (TextView) mHeaderView.findViewById(R.id.tv_title);
		tv_desc = (TextView) mHeaderView.findViewById(R.id.tv_desc);
		// 提前手动测量
		mHeaderView.measure(0, 0);

		// 获取当前控件的测量高度
		mHeaderViewHeight = mHeaderView.getMeasuredHeight();

		// 设置内边距，可以隐藏当前控件
		mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);

		// 添加头布局
		addHeaderView(mHeaderView);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			// 判断当前状态，如果是正在刷新中，则执行父类中的方法
			if (currentState == REFRESHING) {
				return super.onTouchEvent(ev);
			}

			moveY = ev.getY();
			float offset = moveY - downY;
			Log.i(tag, "offset==" + offset);
			if (offset > 0 && getFirstVisiblePosition() == 0) {
				int paddingTop = (int) (-mHeaderView.getMeasuredHeight() + offset);
				mHeaderView.setPadding(0, paddingTop, 0, 0);

				if (paddingTop >= 0 && currentState != RELEASE_REFRESH) { // 头布局完全显示
					// 切换成释放刷新模式
					currentState = RELEASE_REFRESH;
					updateHeader(); // 根据最新的状态值更新头布局的内容
				} else if (paddingTop < 0 && currentState != PULL_TO_REFRESH) { // 头布局不完全显示
					// 切换成下拉刷新模式
					currentState = PULL_TO_REFRESH;
					updateHeader(); // 根据最新的状态值更新头布局的内容
				}

				return true; // 当前事件被消费
			}
			break;
		case MotionEvent.ACTION_UP:
			if (currentState == PULL_TO_REFRESH) {
				mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
			} else if (currentState == RELEASE_REFRESH) {
				mHeaderView.setPadding(0, 0, 0, 0);
				currentState = REFRESHING;
				updateHeader(); // 根据最新的状态值更新头布局的内容
			}
			break;

		default:
			break;
		}
		return super.onTouchEvent(ev);
	}

	private void updateHeader() {
		switch (currentState) {
		case PULL_TO_REFRESH: // 切换回下拉刷新
			// 做动画，改标题
			iv_arrow.startAnimation(rotateDownAnim);
			tv_title.setText("下拉刷新");

			break;
		case RELEASE_REFRESH: // 切换成释放刷新
			// 做动画，改标题
			iv_arrow.startAnimation(rotateUpAnim);
			tv_title.setText("释放刷新");
			break;
		case REFRESHING: // 刷新中...
			iv_arrow.clearAnimation();
			iv_arrow.setVisibility(View.INVISIBLE);
			pb.setVisibility(View.VISIBLE);
			tv_title.setText("正在刷新中...");

			if (onRefreshListener != null) {
				onRefreshListener.onRefresh();
			}

			break;

		default:
			break;
		}
	}

	public interface OnRefreshListener {
		void onRefresh(); // 下拉刷新

		void onLoadMore(); // 加载更多
	}

	public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
		this.onRefreshListener = onRefreshListener;

	}

	/**
	 * 刷新完成后调用的方法
	 */
	public void onRefreshComplete() {
		if (isLoadingMore) { 
			//加载更多
			mFooterView.setPadding(0, -mFooterViewHeight, 0, 0);
			isLoadingMore = false;
		} else {
			//下拉刷新
			currentState = PULL_TO_REFRESH;
			iv_arrow.setVisibility(View.VISIBLE);
			pb.setVisibility(View.INVISIBLE);
			mHeaderView.setPadding(0, -mHeaderViewHeight, 0, 0);
			updateLastRefreshDate();
		}
	}

	private void updateLastRefreshDate() {
		String time = getTime();
		tv_desc.setText("最后刷新时间：" + time);

	}

	@SuppressLint("NewApi")
	private String getTime() {
		long currentTime = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(currentTime);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (isLoadingMore) {
			return;
		}
		// 当滚动状态发生改变时调用
		if (scrollState == SCROLL_STATE_IDLE
				&& getLastVisiblePosition() >= (getCount() - 1)) { // 开始加载更多
			isLoadingMore = true;
			mFooterView.setPadding(0, 0, 0, 0);

			setSelection(getCount());

			onRefreshListener.onLoadMore(); // 通知加载更多了
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// 在滚动的过程中调用
	}
}
