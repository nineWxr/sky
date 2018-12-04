package android.arch.lifecycle;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import static android.arch.lifecycle.SKHolderFragment.holderFragmentFind;
import static android.arch.lifecycle.SKHolderFragment.holderFragmentFor;

/**
 * @author sky
 * @version 1.0 on 2018-07-25 下午2:08
 * @see SKViewModelStores
 */
public class SKViewModelStores {

	private SKViewModelStores() {}

	@NonNull @MainThread public static SKViewModelStore of(@NonNull FragmentActivity activity) {
		if (activity instanceof SKViewModelStoreOwner) {
			return ((SKViewModelStoreOwner) activity).getSKViewModelStore();
		}
		return holderFragmentFor(activity).getSKViewModelStore();
	}

	@NonNull @MainThread public static SKViewModelStore of(@NonNull Fragment fragment) {
		if (fragment instanceof SKViewModelStoreOwner) {
			return ((SKViewModelStoreOwner) fragment).getSKViewModelStore();
		}
		return holderFragmentFor(fragment).getSKViewModelStore();
	}

	@NonNull @MainThread public static SKViewModelStore find(@NonNull FragmentActivity activity) {
		if (activity instanceof SKViewModelStoreOwner) {
			return ((SKViewModelStoreOwner) activity).getSKViewModelStore();
		}
		SKHolderFragment skHolderFragment = holderFragmentFind(activity);

		if (skHolderFragment == null) {
			return null;
		}
		return skHolderFragment.getSKViewModelStore();
	}

	@NonNull @MainThread public static SKViewModelStore find(@NonNull Fragment fragment) {
		if (fragment instanceof SKViewModelStoreOwner) {
			return ((SKViewModelStoreOwner) fragment).getSKViewModelStore();
		}
		SKHolderFragment skHolderFragment = holderFragmentFind(fragment);

		if (skHolderFragment == null) {
			return null;
		}
		return skHolderFragment.getSKViewModelStore();
	}
}
