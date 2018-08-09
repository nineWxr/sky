package sk.livedata;

import android.arch.paging.DataSource;
import android.arch.paging.PagedList;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.concurrent.Executor;

import sk.SKHelper;
import sk.livedata.list.SKComputableLiveData;
import sk.livedata.list.factory.SKRetryInterface;

/**
 * @author sky
 * @version 1.0 on 2018-08-03 下午9:38
 * @see SKPagedBuilder
 */
public class SKPagedBuilder<Key, Value> {

	private Key								mInitialLoadKey;

	private PagedList.Config				mConfig;

	private DataSource.Factory<Key, Value>	mDataSourceFactory;

	private PagedList.BoundaryCallback		mBoundaryCallback;

	private Executor						mFetchExecutor	= SKHelper.executors().network();

	@NonNull public SKPagedBuilder<Key, Value> setFactory(@NonNull DataSource.Factory<Key, Value> dataSourceFactory) {
		mDataSourceFactory = dataSourceFactory;
		return this;
	}

	@NonNull public SKPagedBuilder<Key, Value> setConfig(@NonNull PagedList.Config config) {
		mConfig = config;
		return this;
	}

	@NonNull public SKPagedBuilder<Key, Value> setPageSie(@NonNull int pageSie) {
		mConfig = new PagedList.Config.Builder().setPageSize(pageSie).setInitialLoadSizeHint(pageSie).build();
		return this;
	}

	@NonNull public SKPagedBuilder<Key, Value> setInitialLoadKey(@Nullable Key key) {
		mInitialLoadKey = key;
		return this;
	}

	@SuppressWarnings("unused") @NonNull public SKPagedBuilder<Key, Value> setBoundaryCallback(@Nullable PagedList.BoundaryCallback<Value> boundaryCallback) {
		mBoundaryCallback = boundaryCallback;
		return this;
	}

	/**
	 * Constructs the {@code LiveData<PagedList>}.
	 * <p>
	 * No work (such as loading) is done immediately, the creation of the first
	 * PagedList is is deferred until the LiveData is observed.
	 *
	 * @return The LiveData of PagedLists
	 * @param skData
	 */
	@NonNull public SKData<PagedList<Value>> build(SKData<PagedList<Value>> skData) {
		if (mConfig == null) {
			throw new IllegalArgumentException("PagedList.Config must be provided");
		} else if (mDataSourceFactory == null) {
			throw new IllegalArgumentException("DataSource.Factory must be provided");
		}
		return create(mInitialLoadKey, mConfig, mBoundaryCallback, mDataSourceFactory, SKHelper.executors().mainThread(), mFetchExecutor, skData);
	}

	@AnyThread @NonNull private static <Key, Value> SKData<PagedList<Value>> create(@Nullable final Key initialLoadKey, @NonNull final PagedList.Config config,
			@Nullable final PagedList.BoundaryCallback boundaryCallback, @NonNull final DataSource.Factory<Key, Value> dataSourceFactory, @NonNull final Executor notifyExecutor,
			@NonNull final Executor fetchExecutor, SKData<PagedList<Value>> skData) {
		return new SKComputableLiveData<PagedList<Value>>(fetchExecutor, skData) {

			@Nullable private PagedList<Value>				mList;

			@Nullable private DataSource<Key, Value>		mDataSource;

			private final DataSource.InvalidatedCallback	mCallback	= new DataSource.InvalidatedCallback() {

																			@Override public void onInvalidated() {
																				invalidate();
																			}
																		};

			@Override protected PagedList<Value> compute() {
				@Nullable
				Key initializeKey = initialLoadKey;
				if (mList != null) {
					// noinspection unchecked
					initializeKey = (Key) mList.getLastKey();
				}

				do {
					if (mDataSource != null) {
						mDataSource.removeInvalidatedCallback(mCallback);
					}

					mDataSource = dataSourceFactory.create();
					if (mDataSource instanceof SKRetryInterface) {
						getLiveData().skRetryInterface = (SKRetryInterface) mDataSource;
					}
					mDataSource.addInvalidatedCallback(mCallback);

					mList = new PagedList.Builder<>(mDataSource, config).setNotifyExecutor(notifyExecutor).setFetchExecutor(fetchExecutor).setBoundaryCallback(boundaryCallback)
							.setInitialKey(initializeKey).build();
				} while (mList.isDetached());
				return mList;
			}
		}.getLiveData();
	}
}
