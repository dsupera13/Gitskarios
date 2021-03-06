package com.alorma.github.ui.fragment.repos;

import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

import com.alorma.github.sdk.bean.dto.response.ListRepos;
import com.alorma.github.sdk.bean.dto.response.Repo;
import com.alorma.github.sdk.utils.GitskariosSettings;
import com.alorma.github.ui.adapter.repos.ReposAdapter;
import com.alorma.github.ui.fragment.base.PaginatedListFragment;
import com.alorma.github.UrlsManager;
import com.mikepenz.octicons_typeface_library.Octicons;

import retrofit.RetrofitError;

/**
 * Created by Bernat on 17/07/2014.
 */
public abstract class BaseReposListFragment extends PaginatedListFragment<ListRepos> implements SharedPreferences.OnSharedPreferenceChangeListener {

	protected ReposAdapter reposAdapter;
	private GitskariosSettings settings;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		if (reposAdapter != null && reposAdapter.getCount() >= position) {
			Repo item = reposAdapter.getItem(position);
			if (item != null) {
				startActivity(new UrlsManager(getActivity()).manageRepos(Uri.parse(item.html_url)));
			}
		}
	}

	@Override
	protected void onResponse(ListRepos repos, boolean refreshing) {
		if (repos.size() > 0) {
			hideEmpty();
			if (getListAdapter() != null) {
				reposAdapter.addAll(repos, paging);
			} else if (reposAdapter == null) {
				setUpList(repos);
			} else {
				setListAdapter(reposAdapter);
			}
		} else if (reposAdapter == null || reposAdapter.getCount() == 0) {
			setEmpty();
		}
	}

	@Override
	public void onFail(RetrofitError error) {
		super.onFail(error);
		if (reposAdapter == null || reposAdapter.getCount() == 0) {
			setEmpty();
		}
	}

	protected void setUpList(ListRepos repos) {
		settings = new GitskariosSettings(getActivity());
		settings.registerListener(this);

		reposAdapter = new ReposAdapter(getActivity(), repos);

		setListAdapter(reposAdapter);
	}

	@Override
	public void setEmpty() {
		super.setEmpty();
		if (reposAdapter != null) {
			reposAdapter.clear();
		}
	}

	@Override
	protected Octicons.Icon getNoDataIcon() {
		return Octicons.Icon.oct_repo;
	}

	@Override
	protected int getNoDataText() {
		return 0;
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (GitskariosSettings.KEY_REPO_SORT.equals(key)) {
			executeRequest();
		}
	}

	@Override
	public void onDestroy() {
		if (settings != null) {
			settings.unregisterListener(this);
		}
		super.onDestroy();
	}
}
