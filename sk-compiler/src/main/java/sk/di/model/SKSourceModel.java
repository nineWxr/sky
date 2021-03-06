package sk.di.model;

import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.List;

/**
 * @author sky
 * @version 1.0 on 2018-07-05 下午3:36
 * @see SKSourceModel
 */
public class SKSourceModel {

	public HashMap<String, SKSourceClassModel>	skSourceClassModelHashMap	= new HashMap<>();

	public ClassName							className;

	public ClassName							classNameLibrary;

	public boolean								isSingle;

	public boolean								isLibrary;

	public boolean								isSingleGenerate;

	public boolean								isMoreProviders;

	public List<SKConstructorsModel>			skConstructorsModelList;
}
