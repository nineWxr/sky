package sk.plugins;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import sk.SKFragment;

/**
 * @author sky
 * @version 版本
 */
public interface SKFragmentInterceptor {

	void onFragmentCreated(SKFragment skyFragment, Bundle bundle, Bundle savedInstanceState);

	void onFragmentStart(SKFragment skyFragment);

	void onFragmentResume(SKFragment skyFragment);

	void onFragmentPause(SKFragment skyFragment);

	void onFragmentStop(SKFragment skyFragment);

	void onFragmentDestroy(SKFragment skyFragment);

	void onShowLoading(Fragment skyFragment);

	void onCloseLoading(Fragment skyFragment);

	class AdapterInterceptor implements SKFragmentInterceptor {

		@Override public void onFragmentCreated(SKFragment skyFragment, Bundle bundle, Bundle savedInstanceState) {

		}


		@Override public void onFragmentStart(SKFragment skyFragment) {

		}

		@Override public void onFragmentResume(SKFragment skyFragment) {

		}

		@Override public void onFragmentPause(SKFragment skyFragment) {

		}

		@Override public void onFragmentStop(SKFragment skyFragment) {

		}

		@Override public void onFragmentDestroy(SKFragment skyFragment) {

		}

		@Override public void onShowLoading(Fragment skyFragment) {

		}

		@Override public void onCloseLoading(Fragment skyFragment) {

		}
	}

	SKFragmentInterceptor NONE = new SKFragmentInterceptor() {


		@Override public void onFragmentCreated(SKFragment skyFragment, Bundle bundle, Bundle savedInstanceState) {

		}

		@Override public void onFragmentStart(SKFragment skyFragment) {

		}

		@Override public void onFragmentResume(SKFragment skyFragment) {

		}

		@Override public void onFragmentPause(SKFragment skyFragment) {

		}

		@Override public void onFragmentStop(SKFragment skyFragment) {

		}

		@Override public void onFragmentDestroy(SKFragment skyFragment) {

		}

		@Override public void onShowLoading(Fragment skyFragment) {

		}

		@Override public void onCloseLoading(Fragment skyFragment) {

		}
	};

}
