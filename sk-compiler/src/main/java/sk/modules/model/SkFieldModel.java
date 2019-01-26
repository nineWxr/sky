package sk.modules.model;

import com.squareup.javapoet.TypeName;

import java.util.ArrayList;

import javax.lang.model.type.TypeMirror;

public final class SkFieldModel {

	public String					name;

	public TypeName					type;				// 数据类型

	public ArrayList<TypeMirror>	params = new ArrayList<>();

	public String					describe	= "";
}
