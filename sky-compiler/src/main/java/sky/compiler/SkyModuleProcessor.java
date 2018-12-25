package sky.compiler;

import com.google.auto.common.SuperficialValidation;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.sun.source.util.Trees;

import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import sk.compiler.SkLogger;
import sk.compiler.model.SKProviderModel;
import sky.OpenMethod;
import sky.compiler.model.SkyModuleModel;
import sky.compiler.model.SkyParamProviderModel;

import static com.google.auto.common.MoreElements.getPackage;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.STATIC;
import static sky.compiler.SkyUtils.bestGuess;

@AutoService(Processor.class)
public final class SkyModuleProcessor extends AbstractProcessor {

	private static final String	OPTION_SDK_INT	= "sk.minSdk";

	private int					sdk				= 1;

	private Elements			elementUtils;

	private Types				typeUtils;

	private Filer				filer;

	private Trees				trees;

	private SkLogger			logger;

	@Override public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		String sdk = env.getOptions().get(OPTION_SDK_INT);
		if (sdk != null) {
			try {
				this.sdk = Integer.parseInt(sdk);
			} catch (NumberFormatException e) {
				env.getMessager().printMessage(Diagnostic.Kind.WARNING, "Unable to parse supplied minSdk option '" + sdk + "'. Falling back to API 1 support.");
			}
		}
		this.sdk += 1;
		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
		logger = new SkLogger(processingEnv.getMessager()); // Package the log utils.

		logger.info(">>> SkProcessor 初始化. <<<");

		try {
			trees = Trees.instance(processingEnv);
		} catch (IllegalArgumentException ignored) {
			logger.error(ignored);
		}
	}

	@Override public Set<String> getSupportedOptions() {
		return Collections.singleton(OPTION_SDK_INT);
	}

	@Override public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	@Override public Set<String> getSupportedAnnotationTypes() {
		Set<String> types = new LinkedHashSet<>();
		for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
			types.add(annotation.getCanonicalName());
		}
		return types;
	}

	private Set<Class<? extends Annotation>> getSupportedAnnotations() {
		Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
		annotations.add(OpenMethod.class);
		return annotations;
	}

	/**
	 * 主流程
	 */
	@Override public boolean process(Set<? extends TypeElement> elements, RoundEnvironment env) {
		// 如果没有注解
		if (CollectionUtils.isEmpty(elements)) {
			logger.info(">>> SkyModuleProvider 没有注解. <<<");
			return false;
		}
		logger.info(">>> Found SkyModuleProvider, 开始... <<<");
		ArrayList<SkyModuleModel> skProviderModels = findMethodsProvider(env, OpenMethod.class);
		if (skProviderModels == null) {
			logger.error(">>> Found SkyModuleProvider 异常... <<<");
			return false;
		}

		SkyProviderCreate skyProviderCreate = new SkyProviderCreate();

		for (SkyModuleModel item : skProviderModels) {
			JavaFile javaIFile = skyProviderCreate.brewProvider(item);
			try {
				javaIFile.writeTo(filer);
			} catch (IOException e) {
				logger.error(e);
			}
		}
		SkyCreateModule skyCreateModule = new SkyCreateModule(skProviderModels);
		JavaFile javaIFile = skyCreateModule.brewModuleBiz();
		try {
			javaIFile.writeTo(filer);
		} catch (IOException e) {
			logger.error(e);
		}
		logger.info(">>> Found SkyModuleProvider 结束... <<<");
		return false;
	}



	private ArrayList<SkyModuleModel> findMethodsProvider(RoundEnvironment env, Class<? extends Annotation> annotationClass) {

		ArrayList<SkyModuleModel> modelMap = new ArrayList<>();
		List<SkyModuleModel> allModel = new ArrayList<>();

		for (Element element : env.getElementsAnnotatedWith(annotationClass)) {
			if (!SuperficialValidation.validateElement(element)) continue;
			try {
				parseProviderAnnotation(annotationClass, element, allModel, modelMap);
			} catch (Exception e) {
				e.printStackTrace();
				logger.info("解析 @SKProvider 注解出现问题~~");
			}
		}

		return modelMap;

	}

	/**
	 * 解析provider注解
	 *
	 * @param annotationClass
	 * @param element
	 * @param allModelMap
	 * @param providerModels
	 */
	private void parseProviderAnnotation(Class<? extends Annotation> annotationClass, Element element, List<SkyModuleModel> allModelMap, ArrayList<SkyModuleModel> providerModels) {
		if (!(element instanceof ExecutableElement) || element.getKind() != METHOD) {
			throw new IllegalStateException(String.format("@%s annotation must be on a .", annotationClass.getSimpleName()));
		}
		boolean hasError = isInaccessibleViaGeneratedCode(element);

		if (hasError) {
			return;
		}

		ExecutableElement executableElement = (ExecutableElement) element;
		TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

		String packageName = getPackage(enclosingElement).getQualifiedName().toString();

		String name = executableElement.getSimpleName().toString();

		List<? extends VariableElement> methodParameters = executableElement.getParameters();

		SkyModuleModel skyModuleModel = new SkyModuleModel();

		skyModuleModel.name = name;
		skyModuleModel.nameCode = element.getAnnotation(OpenMethod.class).value();
		skyModuleModel.packageName = packageName;
		skyModuleModel.className = ClassName.get(packageName, enclosingElement.getSimpleName().toString());
		skyModuleModel.returnType = bestGuess(executableElement.getReturnType());
		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(STATIC)) {
			skyModuleModel.isStatic = true;
		}
		skyModuleModel.parameters = new ArrayList<>();
		for (VariableElement item : methodParameters) {
			SkyParamProviderModel skyParamProviderModel = new SkyParamProviderModel();

			skyParamProviderModel.name = item.getSimpleName().toString();
			skyParamProviderModel.packageName = getPackage(item).getQualifiedName().toString();
			skyParamProviderModel.classType = bestGuess(item.asType());

			skyModuleModel.parameters.add(skyParamProviderModel);
		}

		// 生成key
		skyModuleModel.buildKey();

		providerModels.add(skyModuleModel);
	}

	private boolean isInaccessibleViaGeneratedCode(Element element) {
		boolean hasError = false;
		TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

		// Verify method modifiers.
		Set<Modifier> modifiers = element.getModifiers();
		if (modifiers.contains(PRIVATE)) {
			hasError = true;
		}

		// Verify containing type.
		if (enclosingElement.getKind() != CLASS) {
			hasError = true;
		}

		// Verify containing class visibility is not private.
		if (enclosingElement.getModifiers().contains(PRIVATE)) {
			hasError = true;
		}

		return hasError;
	}
}
