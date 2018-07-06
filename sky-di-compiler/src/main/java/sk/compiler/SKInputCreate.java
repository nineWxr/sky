package sk.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.Modifier;

import sk.compiler.model.SKInputClassModel;
import sk.compiler.model.SKInputModel;

import static javax.lang.model.element.Modifier.PUBLIC;
import static sk.compiler.SkConsts.NAME_INPUT;
import static sk.compiler.SkConsts.SK_DC;
import static sk.compiler.SkConsts.SK_INTERFACE;
import static sk.compiler.SkConsts.SK_I_INPUT;
import static sk.compiler.SkConsts.SK_I_LAZY;
import static sk.compiler.SkConsts.SK_I_PROVIDER;
import static sk.compiler.SkConsts.WARNING_TIPS;

/**
 * @author sky
 * @version 1.0 on 2018-07-05 上午9:42
 * @see SKInputCreate
 */
class SKInputCreate {

	JavaFile brewInput(SKInputClassModel skInputClassModel) {
        return JavaFile.builder(skInputClassModel.packageName, createInput(skInputClassModel)).addFileComment("Generated code from Sk. Do not modify!").build();
	}

    private TypeSpec createInput(SKInputClassModel skInputClassModel) {

	    String inputName = skInputClassModel.className.simpleName() + NAME_INPUT;

        ClassName currentClassName = ClassName.get(skInputClassModel.packageName, inputName);

        TypeSpec.Builder result = TypeSpec.classBuilder(currentClassName);
        result.addJavadoc(WARNING_TIPS);
        result.addSuperinterface(ParameterizedTypeName.get(SK_I_INPUT, skInputClassModel.className));
        result.addModifiers(PUBLIC);


        // 构造方法
        MethodSpec.Builder constructors = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);

        // 添加当前类create方法
        MethodSpec.Builder create = MethodSpec.methodBuilder("create").addModifiers(Modifier.PUBLIC, Modifier.STATIC).returns(ParameterizedTypeName.get(SK_I_INPUT, skInputClassModel.className));
        StringBuilder createParameter = new StringBuilder("provider");
        // 重写方法
        MethodSpec.Builder input = MethodSpec.methodBuilder("input").addAnnotation(Override.class).addModifiers(Modifier.PUBLIC).addParameter(skInputClassModel.className, "instance");

        for(SKInputModel item : skInputClassModel.skInputModels){
            String name = item.name + "Provider";
            ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(SK_I_PROVIDER, item.type);

            result.addField(parameterizedTypeName, name, Modifier.PRIVATE, Modifier.FINAL);
            constructors.addParameter(parameterizedTypeName, name);
            constructors.addStatement("this.$N = $N", name, name);

            // create
            create.addParameter(parameterizedTypeName, name);
            if (createParameter == null) {
                createParameter = new StringBuilder(name);
            } else {
                createParameter.append(",");
                createParameter.append(name);
            }

            if (item.isLazy) {
                input.addStatement("input$N(instance,$T.lazy($N))", item.methodName, SK_DC, name);
            } else {
                input.addStatement("input$N(instance,$N.get())", item.methodName, name);
            }

            // 每个属性创建方法
            MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("input" + item.methodName).addModifiers(Modifier.PUBLIC, Modifier.STATIC).addParameter(item.className, "instance").addStatement("instance.$N = $N", item.fieldName, item.name);

            //添加自动加载属性
            if(item.isImplInitInterface){
                methodSpec.addStatement("if($N instanceof $T)(($T)$N).initSK()",item.name,SK_INTERFACE,SK_INTERFACE,item.name);
            }

            if (item.isLazy) {
                ParameterizedTypeName lazyTypeName = ParameterizedTypeName.get(SK_I_LAZY, item.type);
                methodSpec.addParameter(lazyTypeName, item.name);
            } else {
                methodSpec.addParameter(item.type, item.name);
            }

            result.addMethod(methodSpec.build());
        }

        result.addMethod(input.build());

        // 添加当前类create方法
        create.addStatement("return new $T($N)", currentClassName, createParameter.toString());

        result.addMethod(constructors.build());

        return result.build();
    }
}