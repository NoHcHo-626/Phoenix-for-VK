package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.PeopleAdapter;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.search.PeopleSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IPeopleSearchView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

public class PeopleSearchFragment extends AbsSearchFragment<PeopleSearchPresenter, IPeopleSearchView, User>
        implements PeopleAdapter.ClickListener, IPeopleSearchView {

    public static PeopleSearchFragment newInstance(int accountId, @Nullable PeopleSearchCriteria initialCriteria){
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        PeopleSearchFragment fragment = new PeopleSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(androidx.recyclerview.widget.RecyclerView.Adapter adapter, List<User> data) {
        ((PeopleAdapter) adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<User> data) {
        PeopleAdapter adapter = new PeopleAdapter(getActivity(), data);
        adapter.setClickListener(this);
        return adapter;
    }

    @Override
    protected androidx.recyclerview.widget.RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public IPresenterFactory<PeopleSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new PeopleSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
                saveInstanceState
        );
    }

    @Override
    protected String tag() {
        return PeopleSearchFragment.class.getSimpleName();
    }

    @Override
    public void onOwnerClick(Owner owner) {
        getPresenter().fireUserClick((User)owner);
    }

    @Override
    public void openUserWall(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(getContext());
    }
}