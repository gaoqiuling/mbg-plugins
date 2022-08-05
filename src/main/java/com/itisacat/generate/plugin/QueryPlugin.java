package com.itisacat.generate.plugin;

import com.itisacat.generate.constant.MapperXmlKey;
import com.itisacat.generate.constant.MapperXmlValue;
import com.itisacat.generate.constant.StatementIdValue;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class QueryPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * Mapper.xml文档DOM生成树，可以把自己的Statement挂在DOM树上。
     * 添加load的SQL Statement
     *
     * @param document          SQLMapper.xml 文档树描述对象
     * @param introspectedTable 表描述对象
     * @return 是否生成
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement rootElement = document.getRootElement();
        // <select></select>
        XmlElement statement = new XmlElement(MapperXmlKey.ELEMENT_SELECT);
        // id="queryList"
        statement.getAttributes().add(0, new Attribute(MapperXmlKey.ATTRIBUTE_ID, StatementIdValue.STATEMENT_QUERY_LIST));
        // resultMap="BaseResultMap"
        statement.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_RESULT_MAP, MapperXmlValue.ATTRIBUTE_BASE_RESULT_MAP));
        // <include refid="query" />
        XmlElement include = new XmlElement(MapperXmlKey.ELEMENT_INCLUDE);
        include.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_REFID, MapperXmlValue.ATTRIBUTE_QUERY));
        statement.addElement(include);
        rootElement.addElement(statement);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * Mapper.java接口生成树，可以把自己的方法挂接在此接口上
     * EntityType load(EntityType object);
     *
     * @param interfaze         Mapper接口信息描述对象
     * @param topLevelClass     此数据库表对应的实体类描述对象
     * @param introspectedTable 表描述对象
     * @return 是否生成
     */
    @Override
    public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        interfaze.addImportedType(new FullyQualifiedJavaType(StatementIdValue.JAVA_UTIL_LIST));
        //传入参数
        FullyQualifiedJavaType paramType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        //输出参数
        FullyQualifiedJavaType listEntityJavaType = new FullyQualifiedJavaType(StatementIdValue.JAVA_UTIL_LIST);
        FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        listEntityJavaType.addTypeArgument(entityType);
        FullyQualifiedJavaType returnShortName = new FullyQualifiedJavaType(listEntityJavaType.getShortName());

        Method method = new Method();
        method.setName(StatementIdValue.STATEMENT_QUERY_LIST);
        method.setReturnType(returnShortName);
        method.setVisibility(JavaVisibility.DEFAULT);
        method.addParameter(new Parameter(paramType, "object"));
        interfaze.addMethod(method);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        try {
            topLevelClass.addImportedType(new FullyQualifiedJavaType("javax.persistence.Transient"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("com.google.common.collect.Lists"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType("javafx.util.Pair"));
            topLevelClass.addImportedType(new FullyQualifiedJavaType(StatementIdValue.JAVA_UTIL_LIST));
            FullyQualifiedJavaType entityJavaType = new FullyQualifiedJavaType(StatementIdValue.JAVA_UTIL_LIST);
            FullyQualifiedJavaType entityJavaShortNameType = new FullyQualifiedJavaType(entityJavaType.getShortName());
            FullyQualifiedJavaType entityType = new FullyQualifiedJavaType("javafx.util.Pair<String, String>");
            FullyQualifiedJavaType entityShortNameType = new FullyQualifiedJavaType(entityType.getShortName());
            entityJavaShortNameType.addTypeArgument(entityShortNameType);

            Field field = new Field();
            field.setVisibility(JavaVisibility.PRIVATE);
            field.setName(StatementIdValue.ATTRIBUTE_ORDER_FIELDS);
            field.setType(entityJavaShortNameType);
            field.setInitializationString("Lists.newArrayList()");
            field.addAnnotation("@Transient");
            topLevelClass.addField(field);

            Method defaultOrder = new Method();
            defaultOrder.setVisibility(JavaVisibility.PUBLIC);
            defaultOrder.setReturnType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
            defaultOrder.setName("orderBy" + firstWordUpper(introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty()));
            defaultOrder.addParameter(new Parameter(new FullyQualifiedJavaType("boolean"), "isAsc"));
            defaultOrder.addBodyLine("orderFields.add(new Pair<>(\"" + introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName() + "\", isAsc ? \"ASC\" : \"DESC\"));");
            defaultOrder.addBodyLine("return this;");
            topLevelClass.addMethod(defaultOrder);
        } catch (Exception e) {
            System.out.println(e);
        }
        return true;
    }

    //第一个字母大写
    private String firstWordUpper(String word) {
        return word.substring(0, 1).toUpperCase() + word.substring(1, word.length());
    }
}
