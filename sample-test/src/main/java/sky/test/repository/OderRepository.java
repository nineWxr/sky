package sky.test.repository;

import android.view.Display;

import java.util.ArrayList;

import sk.L;
import sk.SKRepository;
import sk.livedata.SKData;
import sk.livedata.SKPaged;
import sky.SKHTTP;
import sky.SKInput;
import sky.SKProvider;
import sky.SKSingleton;
import sky.test.model.Model;

/**
 * @author sky
 * @version 1.0 on 2019-02-12 4:10 PM
 * @see OderRepository
 */
@SKProvider
@SKSingleton
public class OderRepository extends SKRepository<OderRepository> {

	@SKInput SKPaged	skPaged;

	@SKInput Model		model;

	public void inta() {
		skPaged.pagedBuilder();
	}

}
