package co.andrea.ubl;

import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.Outline;
import org.xml.sax.ErrorHandler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class UblPlugin extends Plugin {

    public static final String OPT = "Xandrea-ubl";
    public static final String AUTHOR = "<br>\nNote: automatically created by andrea-ubl-plugin -" + OPT;

    @Override
    public String getOptionName() {
        return OPT;
    }

    @Override
    public String getUsage() {
        return new StringBuilder(" -")
                .append(OPT)
                .append(" : ")
                .append("create additional constructors with the 'value' as argument + getter and setter for the value")
                .toString();
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler) {
        addDefaultConstructors(model);
        // Add set methods for wrapper classes
        addValueConstructors(model);
        return true;
    }

    /**
     * @param aOutline
     * @param jParentClass
     * @param aValueType
     * @param aAllRelevantClasses
     */
    private void recursiveAddValueConstructorToDerivedClasses(final Outline aOutline,
                                                              final JDefinedClass jParentClass,
                                                              final JType aValueType,
                                                              final Set<JClass> aAllRelevantClasses) {
        aOutline.getClasses()
                .stream()
                .filter(aClassOutline -> aClassOutline.implClass._extends() == jParentClass)
                .forEach(aClassOutline -> {
                    final JDefinedClass jCurClass = aClassOutline.implClass;
                    aAllRelevantClasses.add(jCurClass);
                    final JMethod aValueCtor = jCurClass.constructor(JMod.PUBLIC);
                    final JVar aParam = aValueCtor.param(JMod.FINAL, aValueType, "valueParam");
                    aValueCtor.body().invoke("super").arg(aParam);
                    aValueCtor.javadoc()
                            .add("Constructor for value of type " + aValueType.name() + " calling super class constructor.");
                    aValueCtor.javadoc()
                            .addParam(aParam)
                            .add("The value to be set." + (aValueType.isPrimitive() ? "" : " May be <code>null</code>."));
                    aValueCtor.javadoc().add(AUTHOR);
                    recursiveAddValueConstructorToDerivedClasses(aOutline, jCurClass, aValueType, aAllRelevantClasses);
                });
    }

    /**
     * @param aOutline
     * @param aValueType
     * @param aAllRelevantClasses
     */
    private void addValueSetterInUsingClasses(final Outline aOutline,
                                              final JType aValueType,
                                              final Set<JClass> aAllRelevantClasses) {
        aOutline.getClasses()
                .stream()
                .forEach(aClassOutline -> {
                    final JDefinedClass jClass = aClassOutline.implClass;
                    List<JMethod> newList = new ArrayList<>(jClass.methods());
                    newList
                            .stream()
                            .filter(aMethod -> aMethod.name().startsWith("set"))
                            .forEach(aMethod -> {
                                final List<JVar> aParams = aMethod.params();
                                if (aParams.size() == 1 && aAllRelevantClasses.contains(aParams.get(0).type())) {
                                    final JType aImplType = aParams.get(0).type();
                                    final JMethod aSetter = jClass.method(JMod.PUBLIC, aImplType, aMethod.name());
                                    final JVar aParam = aSetter.param(JMod.FINAL, aValueType, "valueParam");
                                    final JVar aObj = aSetter.body().decl(aImplType,
                                            "aObj",
                                            JExpr.invoke("get" + aMethod.name().substring(3)));
                                    final JConditional aIf = aSetter.body()._if(aObj.eq(JExpr._null()));
                                    aIf._then().assign(aObj, JExpr._new(aImplType).arg(aParam));
                                    aIf._then().invoke(aMethod).arg(aObj);
                                    aIf._else().invoke(aObj, "setValue").arg(aParam);
                                    aSetter.body()._return(aObj);
                                    aSetter.javadoc().add("Special setter with value of type " + aParam.type().name());
                                    aSetter.javadoc()
                                            .addParam(aParam)
                                            .add("The value to be set." + (aValueType.isPrimitive() ? "" : " May be <code>null</code>."));
                                    aSetter.javadoc().addReturn().add("The created intermediary object of type " +
                                            aImplType.name() +
                                            " and never <code>null</code>");
                                    aSetter.javadoc().add(AUTHOR);
                                }
                            });
                });
    }

    /**
     * @param paramOutline
     */
    private void addDefaultConstructors(Outline paramOutline) {
        paramOutline.getClasses().stream().forEach(localClassOutline -> {
            JDefinedClass localJDefinedClass = localClassOutline.implClass;
            JMethod localJMethod = localJDefinedClass.constructor(JMod.PUBLIC);
            localJMethod.javadoc().add("Default constructor");
            localJMethod.javadoc().add("<br>\nNote: automatically created by andrea-bul-plugin");
            localJDefinedClass.javadoc().add("<p>This class contains methods created by andrea-bul-plugin</p>\n");
        });
    }

    /**
     * @param paramOutline
     */
    private void addValueConstructors(Outline paramOutline) {
        final JCodeModel cm = paramOutline.getCodeModel();
        final Hashtable<String, JType> aAllSuperClassNames = new Hashtable<>();
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.AmountType", cm.ref(BigDecimal.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2", cm.ref(byte[].class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.CodeType", cm.ref(String.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.DateTimeType", cm.ref(String.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.IdentifierType", cm.ref(String.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.MeasureType", cm.ref(BigDecimal.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.NumericType", cm.ref(BigDecimal.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.QuantityType", cm.ref(BigDecimal.class));
        aAllSuperClassNames.put("un.unece.uncefact.data.specification.unqualifieddatatypesschemamodule._2.TextType", cm.ref(String.class));
        paramOutline.getClasses()
                .stream()
                .filter(localClassOutline ->
                        localClassOutline.implClass._extends() != null
                                && !(localClassOutline.implClass._extends() instanceof JDefinedClass)
                                && !localClassOutline.implClass._extends().fullName().startsWith("java.")
                )
                .forEach(localClassOutline -> {
                    final String sSuperClassName = localClassOutline.implClass._extends().fullName();
                    try {
                        final Class<?> aSuperClass = Class.forName(sSuperClassName);
                        Arrays.stream(aSuperClass.getFields())
                                .filter(field -> field.getName().equals("value"))
                                .findFirst()
                                .ifPresent(field -> {
                                    aAllSuperClassNames.put(sSuperClassName, paramOutline.getCodeModel()._ref(field.getType()));
                                });
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                });
        final Hashtable<JClass, JType> aAllCtorClasses = new Hashtable<>();
        paramOutline.getClasses()
                .stream()
                .forEach(localClassOutline -> {
                    final JDefinedClass jClass = localClassOutline.implClass;
                    JType aValueType = null;
                    Optional<JFieldVar> optField = jClass.fields().values()
                            .stream()
                            .filter(field -> field.name().compareTo("value") == 0)
                            .findFirst();
                    if (optField.isPresent()) {
                        aValueType = optField.get().type();
                    }
                    if (aValueType == null && jClass._extends() != null && !(jClass._extends() instanceof JDefinedClass)) {
                        aValueType = aAllSuperClassNames.get(jClass._extends().fullName());
                    }
                    if (aValueType != null) {
                        final JType finalValueType = aValueType;
                        final JMethod aValueCtor = jClass.constructor(JMod.PUBLIC);
                        final JVar aParam = aValueCtor.param(JMod.FINAL, finalValueType, "valueParam");
                        aValueCtor.body().invoke("setValue").arg(aParam);
                        aValueCtor.javadoc().add("Constructor for value of type " + finalValueType.name());
                        aValueCtor.javadoc()
                                .addParam(aParam)
                                .add("The value to be set." + (finalValueType.isPrimitive() ? "" : " May be <code>null</code>."));
                        aValueCtor.javadoc().add(AUTHOR);
                        final Set<JClass> aAllRelevantClasses = ConcurrentHashMap.newKeySet();
                        aAllRelevantClasses.add(jClass);
                        recursiveAddValueConstructorToDerivedClasses(paramOutline, jClass, finalValueType, aAllRelevantClasses);
                        aAllRelevantClasses
                                .stream()
                                .forEach(jRelevantClass -> {
                                    aAllCtorClasses.put(jRelevantClass, finalValueType);
                                });
                        addValueSetterInUsingClasses(paramOutline, finalValueType, aAllRelevantClasses);
                    }
                });
    }

}
