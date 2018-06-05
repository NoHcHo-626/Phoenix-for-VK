package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.AudioRecyclerAdapter;
import biz.dealnote.messenger.fragment.base.BasePresenterFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.mvp.presenter.AudiosPresenter;
import biz.dealnote.messenger.mvp.view.IAudiosView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

/**
 * Audio is not supported :-(
 */
public class AudiosFragment extends BasePresenterFragment<AudiosPresenter, IAudiosView>
        implements IAudiosView {

    public static AudiosFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudiosFragment fragment = new AudiosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mBlockedRoot;
    private androidx.swiperefreshlayout.widget.SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioRecyclerAdapter mAudioRecyclerAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_music, container, false);
        ((AppCompatActivity)requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mBlockedRoot = root.findViewById(R.id.blocked_root);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        root.findViewById(R.id.button_details).setOnClickListener(v -> openPost());

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mAudioRecyclerAdapter = new AudioRecyclerAdapter(getActivity(), Collections.emptyList());
        mAudioRecyclerAdapter.setClickListener((position, audio) -> getPresenter().playAudio(getActivity(), position));
        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    private void openPost(){
        PlaceFactory.getPostPreviewPlace(requireArguments().getInt(Extra.ACCOUNT_ID), 7927, -72124992).tryOpenWith(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.music);
            actionBar.setSubtitle(null);
        }

        if (getActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) getActivity()).onSectionResume(NavigationFragment.SECTION_ITEM_AUDIOS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setBlockNavigationDrawer(false)
                .setStatusBarColored(getActivity(),true)
                .build()
                .apply(requireActivity());
    }

    @Override
    protected String tag() {
        return AudiosFragment.class.getSimpleName();
    }

    @Override
    public IPresenterFactory<AudiosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if(nonNull(mAudioRecyclerAdapter)){
            mAudioRecyclerAdapter.setData(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if(nonNull(mAudioRecyclerAdapter)){
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void setBlockedScreen(boolean visible) {
        if(nonNull(mBlockedRoot)){
            mBlockedRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }

        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.setVisibility(visible ? View.GONE : View.VISIBLE);
        }
    }
}