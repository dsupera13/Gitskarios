package com.alorma.github.ui.fragment.base;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.alorma.github.R;
import com.alorma.github.ui.view.DirectionalScrollListener;
import com.alorma.github.utils.AttributesUtils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.octicons_typeface_library.Octicons;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by Bernat on 05/08/2014.
 */
public abstract class LoadingListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
		AbsListView.OnScrollListener,
		DirectionalScrollListener.OnDetectScrollListener,
		DirectionalScrollListener.OnCancelableDetectScrollListener,
		View.OnClickListener, AdapterView.OnItemClickListener {

	private SwipeRefreshLayout swipe;
	protected static final long FAB_ANIM_DURATION = 400;
	protected TextView emptyText;
	protected ImageView emptyIcon;
	protected View emptyLy;
	protected FloatingActionButton fab;
	private ValueAnimator animator;
	private boolean fabVisible;
	private ListView listView;
	private SmoothProgressBar progressBar;
	private UpdateReceiver updateReceiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		loadArguments();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		View v = inflater.inflate(R.layout.list_fragment, null, false);

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setupListView(view);

		progressBar = (SmoothProgressBar) view.findViewById(R.id.progress);

		int color = AttributesUtils.getPrimaryColor(getActivity());

		if (progressBar != null) {
			progressBar.setSmoothProgressDrawableColor(color);
		}

		emptyIcon = (ImageView) view.findViewById(R.id.emptyIcon);
		emptyText = (TextView) view.findViewById(R.id.emptyText);
		emptyLy = view.findViewById(R.id.emptyLayout);

		fab = (FloatingActionButton) view.findViewById(R.id.fabButton);

		checkFAB();

		swipe = (SwipeRefreshLayout) view.findViewById(R.id.swipe);

		int accent = AttributesUtils.getAttributeId(getActivity(), R.attr.colorAccent);
		int primaryDark = AttributesUtils.getAttributeId(getActivity(), R.attr.colorPrimaryDark);

		if (swipe != null) {
			swipe.setColorSchemeResources(accent,
					R.color.gray_github_light,
					primaryDark,
					R.color.gray_github_light);
			swipe.setOnRefreshListener(this);
		}

		if (autoStart()) {
			executeRequest();
		}
	}

	protected void executeRequest() {
		startRefresh();
	}

	protected void executePaginatedRequest(int page) {
		startRefresh();
	}

	protected void startRefresh() {
		hideEmpty();
		if (swipe != null) {
			swipe.setRefreshing(true);
		}

		if (progressBar != null) {
			progressBar.setVisibility(View.VISIBLE);
			progressBar.progressiveStart();
		}
	}

	protected void stopRefresh() {
		if (swipe != null) {
			swipe.setRefreshing(false);
		}

		if (progressBar != null) {
			progressBar.progressiveStop();
			progressBar.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		stopRefresh();
	}

	protected void setupListView(View view) {
		listView = (ListView) view.findViewById(android.R.id.list);

		if (listView != null) {
			listView.setOnItemClickListener(this);

			listView.setOnScrollListener(new DirectionalScrollListener(this, this, FAB_ANIM_DURATION));

			listView.setDivider(getResources().getDrawable(R.drawable.divider_main));
		}
	}

	protected void checkFAB() {
		if (getActivity() != null && fab != null) {
			if (useFAB()) {
				fab.setVisibility(View.VISIBLE);
				fabVisible = true;
				fab.setOnClickListener(this);
				fab.setSize(FloatingActionButton.SIZE_NORMAL);
				IconicsDrawable iconicsDrawable = new IconicsDrawable(getActivity(), getFABGithubIcon());
				iconicsDrawable.color(Color.WHITE);
				iconicsDrawable.sizeDp(24);

				fab.setIconDrawable(iconicsDrawable);
			} else {
				fab.setVisibility(View.GONE);
			}
		}
	}

	protected abstract void loadArguments();

	protected boolean useFAB() {
		return false;
	}

	public void setEmpty() {
		if (getActivity() != null) {
			if (emptyText != null && emptyIcon != null) {
				if (getNoDataIcon() != null && getNoDataText() > 0) {
					IconicsDrawable iconicsDrawable = new IconicsDrawable(getActivity(), getNoDataIcon());
					iconicsDrawable.colorRes(R.color.gray_github_medium);
					emptyIcon.setImageDrawable(iconicsDrawable);

					emptyText.setText(getNoDataText());

					emptyLy.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public void hideEmpty() {
		if (getActivity() != null) {
			if (emptyLy != null) {
				emptyLy.setVisibility(View.INVISIBLE);
			}
		}
	}

	protected abstract Octicons.Icon getNoDataIcon();

	protected abstract int getNoDataText();

	private void showFab() {
		if (useFAB() && !fabVisible) {
			fabVisible = true;
			PropertyValuesHolder pvh = showAnimator(fab);
			startAnimator(pvh);
		}
	}

	private void hideFab() {
		if (useFAB() && fabVisible & (animator == null || !animator.isRunning())) {
			fabVisible = false;
			PropertyValuesHolder pvh = hideAnimator(fab);
			startAnimator(pvh);
		}
	}

	private void startAnimator(PropertyValuesHolder pvh) {
		if (useFAB() && pvh != null) {
			animator = ObjectAnimator.ofPropertyValuesHolder(fab, pvh);
			animator.setDuration(FAB_ANIM_DURATION);
			animator.setRepeatCount(0);
			animator.start();
		}
	}

	protected PropertyValuesHolder showAnimator(View fab) {
		PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f);
		return pvh;
	}

	protected PropertyValuesHolder hideAnimator(View fab) {
		PropertyValuesHolder pvh = PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f);
		return pvh;
	}

	@Override
	public void onUpScrolling() {
		hideFab();
	}

	@Override
	public void onDownScrolling() {
		hideFab();
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStop() {
		showFab();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.fabButton) {
			fabClick();
		}
	}

	protected void fabClick() {

	}

	protected Octicons.Icon getFABGithubIcon() {
		return Octicons.Icon.oct_squirrel;
	}

	public ListView getListView() {
		return listView;

	}

	public void setListAdapter(ListAdapter adapter) {
		if (listView != null) {
			listView.setAdapter(adapter);
		}
	}

	public ListAdapter getListAdapter() {
		if (listView != null) {
			return listView.getAdapter();
		} else {
			return null;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		onListItemClick(listView, view, position, id);
	}

	public void onListItemClick(ListView l, View v, int position, long id) {

	}

	public void reload() {
		if (getListAdapter() == null || getListAdapter().getCount() == 0) {
			executeRequest();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		updateReceiver = new UpdateReceiver();
		IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		getActivity().registerReceiver(updateReceiver, intentFilter);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(updateReceiver);
	}

	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (listView != null && listView.getAdapter() != null && listView.getAdapter().getCount() == 0 && isOnline(context)) {
				reload();
			}
		}

		public boolean isOnline(Context context) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfoMob = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			NetworkInfo netInfoWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return (netInfoMob != null && netInfoMob.isConnectedOrConnecting()) || (netInfoWifi != null && netInfoWifi.isConnectedOrConnecting());
		}
	}

	protected boolean autoStart() {
		return true;
	}

}