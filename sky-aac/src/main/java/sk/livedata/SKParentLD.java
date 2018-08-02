package sk.livedata;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import sk.SKHelper;

/**
 * @author sky
 * @version 1.0 on 2018-08-02 下午8:53
 * @see SKParentLD
 */
public abstract class SKParentLD<T> extends MediatorLiveData<T> implements SKAction {

	private Set<SKObserver<T>>	actionObservers	= new HashSet<>();

	private boolean				active;

	private SKActionModel		skActionModel;

	/**
	 * 通知Observer更新事件
	 */
	private void dispatchAction() {
		if (active) {
			for (SKObserver<T> actionObserver : actionObservers) {
				if (skActionModel != null) {
					actionObserver.onAction(skActionModel.state, skActionModel.extra);
				}
			}
		}
	}

	@Override protected void onActive() {
		super.onActive();
		active = true;
		for (Map.Entry<SKParentLD<?>, ActionSource<?>> entry : mHandlers.entrySet()) {
			entry.getValue().plug();
		}
		dispatchAction();
	}

	@Override protected void onInactive() {
		super.onInactive();
		active = false;
		for (Map.Entry<SKParentLD<?>, ActionSource<?>> entry : mHandlers.entrySet()) {
			entry.getValue().unplug();
		}
	}

	@Override public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
		super.observe(owner, observer);
		if (observer instanceof SKObserver) {
			actionObservers.add((SKObserver<T>) observer);
		}
	}

	@Override public void removeObserver(@NonNull Observer<T> observer) {
		super.removeObserver(observer);
		if (observer instanceof SKObserver) {
			actionObservers.remove(observer);
		}
	}

	@Override public void action(int state, Object... args) {
		skActionModel = new SKActionModel(state, args);
		if (SKHelper.isMainLooperThread()) {
			dispatchAction();
		} else {
			SKHelper.executors().mainThread().execute(new Runnable() {

				@Override public void run() {
					dispatchAction();
				}
			});
		}
	}

	/**** 支持Transformations的转换 ***/

	private Map<SKParentLD<?>, ActionSource<?>> mHandlers = new HashMap<>();

	@Override public <S> void addSource(@NonNull LiveData<S> source, @NonNull Observer<S> onChanged) {
		super.addSource(source, onChanged);
		if (source instanceof SKParentLD && onChanged instanceof SKObserver) {
			addActionObserver((SKParentLD<S>) source, (SKObserver<S>) onChanged);
		}
	}

	protected <S> void addActionObserver(SKParentLD<S> source, SKObserver<S> actionObserver) {
		ActionSource<S> actionSource = new ActionSource<>(source, actionObserver);
		ActionSource<?> existing = mHandlers.put(source, actionSource);
		if (existing != null) {
			return;
		}
		if (hasActiveObservers()) {
			actionSource.plug();
		}
	}

	@Override public <S> void removeSource(@NonNull LiveData<S> toRemote) {
		super.removeSource(toRemote);
		if (toRemote instanceof SKParentLD) {
			removeActionSource(toRemote);
		}
	}

	protected <S> void removeActionSource(@NonNull LiveData<S> toRemote) {
		ActionSource<?> source = mHandlers.remove(toRemote);
		if (source != null) {
			source.unplug();
		}
	}

	public static class ActionSource<T> implements SKActionHandler {

		SKParentLD<T>	actionLiveData;

		SKObserver<T>	actionObserver;

		public ActionSource(SKParentLD<T> actionLiveData, SKObserver<T> actionObserver) {
			this.actionLiveData = actionLiveData;
			this.actionObserver = actionObserver;
		}

		void plug() {
			actionLiveData.observeForever(actionObserver);
		}

		void unplug() {
			actionLiveData.removeObserver(actionObserver);
		}

		@Override public void onAction(int id, Object... args) {
			actionObserver.onAction(id, args);
		}
	}
}