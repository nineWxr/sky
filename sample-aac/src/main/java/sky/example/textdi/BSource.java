package sky.example.textdi;

import sky.SKProvider;
import sky.SKSingleton;

/**
 * @author sky
 * @version 1.0 on 2018-06-23 下午10:02
 * @see BSource
 */
public class BSource {

//	@SKProvider public A providerA() {
//		return new A();
//	}
//
//	@SKProvider public B providerB() {
//		return new B();
//	}

	@SKProvider public C providerC(A a) {
		return new C();
	}


	@SKSingleton @SKProvider public bbb providerBBB() {
		return new bbb();
	}

}
