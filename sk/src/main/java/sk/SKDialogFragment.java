package sk;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;
import sk.livedata.SKNetworkState;
import sk.livedata.SKObserver;
import sk.livedata.SKViewState;
import sky.SKInput;

/**
 * @author sky
 * @version 1.0 on 2019-01-24 10:22 PM
 * @see SKDialogFragment
 */
public abstract class SKDialogFragment<B extends SKBiz> extends DialogFragment {

	private SKDialogFragmentBuilder	skBuilder;

	@SKInput SKViewModelFactory		skViewModelFactory;

	/**
	 * 创建Dialog
	 *
	 * @param savedInstanceState
	 *            参数
	 * @return 返回值 返回值
	 */
	@Override public Dialog onCreateDialog(Bundle savedInstanceState) {
		// 创建对话框
		Dialog dialog = new Dialog(getActivity(), style());
		return dialog;
	}

	@Override public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/** 初始化编辑器 **/
		skBuilder = new SKDialogFragmentBuilder(this, getLifecycle(), savedInstanceState);
	}

	/**
	 * 自定义样式
	 *
	 * @return 返回值
	 */
	protected abstract int style();

	/**
	 * 定制
	 *
	 * @param skBuilder
	 *            参数
	 * @return 返回值
	 **/
	protected abstract SKDialogFragmentBuilder build(SKDialogFragmentBuilder skBuilder);

	/**
	 * 初始化数据
	 *
	 * @param savedInstanceState
	 *            数据
	 */
	protected abstract void initData(Bundle savedInstanceState);

	@Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		return skBuilder.viewCreate();
	}

	@Override public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		skBuilder.activityCreate();
	}

	/**
	 * home
	 *
	 * @param item
	 * @return
	 */
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public B biz() {
		return skBuilder.bizProxy();
	}

	public <PB extends SKBiz> PB biz(Class<PB> bizClass) {
		return SKHelper.biz(bizClass);
	}

	/**
	 * 进度条
	 */
	public void loading() {
		SKHelper.interceptor().fragmentInterceptor().onShowLoading(this);
	}

	/**
	 * 关闭进度条
	 */
	public void closeLoading() {
		SKHelper.interceptor().fragmentInterceptor().onCloseLoading(this);
	}

	/**
	 * 延迟显示键盘
	 *
	 * @param et
	 */
	protected void showSoftInputDelay(final EditText et) {
		et.postDelayed(new Runnable() {

			@Override public void run() {
				if (et == null) return;
				et.requestFocus();
				InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(et, InputMethodManager.RESULT_UNCHANGED_SHOWN);
			}
		}, 300);
	}

	/**
	 * 设置标记
	 */
	public void setLanding() {
		SKHelper.screen().setAsLanding(getActivity());
	}

	protected final RecyclerView.LayoutManager layoutManager() {
		return skBuilder.skRecyclerViewBuilder.layoutManager;
	}

	protected final <R extends RecyclerView> R recyclerView() {
		return (R) skBuilder.skRecyclerViewBuilder.recyclerView;
	}

	protected final <A extends SKAdapter> A adapter() {
		return (A) skBuilder.skRecyclerViewBuilder.skAdapter;
	}

	protected <T extends SKViewModel> T find(Class<T> modelClazz) {
		return SKHelper.find(modelClazz);
	}

	public void showContent() {
		skBuilder.skLayoutBuilder.layoutContent();
	}

	public void showLoading() {
		skBuilder.skLayoutBuilder.layoutLoading();
	}

	public void showError() {
		skBuilder.skLayoutBuilder.layoutError();
	}

	public void showEmpty() {
		skBuilder.skLayoutBuilder.layoutEmpty();
	}

	protected void handleViewState(SKViewState state) {
		switch (state) {
			case CONTENT:
				showContent();
				break;
			case ERROR:
				showError();
				break;
			case EMPTY:
				showEmpty();
				break;
			case LOAD:
				showLoading();
				break;
			case LOADING:
				loading();
				break;
			case CLOSE_LOADING:
				closeLoading();
				break;
		}
	}

	public abstract class SKViewObserver<T> implements SKObserver<T> {

		@Override public void onAction(int state, Object... args) {}

		@Override public void onAction(SKViewState state) {
			handleViewState(state);
		}

		@Override public void onAction(SKNetworkState networkState) {
			if (adapter() != null) {
				adapter().setNetworkState(networkState);
			}
		}
	}

}