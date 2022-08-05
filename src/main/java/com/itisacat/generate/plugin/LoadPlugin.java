package com.itisacat.generate.plugin;

import com.itisacat.generate.constant.MapperXmlKey;
import com.itisacat.generate.constant.MapperXmlValue;
import com.itisacat.generate.constant.StatementIdValue;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class LoadPlugin extends PluginAdapter {

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
        // id="load"
        statement.getAttributes().add(0, new Attribute(MapperXmlKey.ATTRIBUTE_ID, StatementIdValue.STATEMENT_LOAD));
        // resultMap="BaseResultMap"
        statement.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_RESULT_MAP, MapperXmlValue.ATTRIBUTE_BASE_RESULT_MAP));

        TextElement select = new TextElement("select");
        XmlElement include = new XmlElement(MapperXmlKey.ELEMENT_INCLUDE);
        include.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_REFID, MapperXmlValue.ATTRIBUTE_BASE_COLUMN_LIST));
        TextElement from = new TextElement("from " + introspectedTable.getTableConfiguration().getTableName());
        TextElement where = new TextElement("where " + introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName() + " = #{" + introspectedTable.getPrimaryKeyColumns().get(0).getJavaProperty() + "}");

        statement.addElement(select);
        statement.addElement(include);
        statement.addElement(from);
        statement.addElement(where);
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
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
        FullyQualifiedJavaType paramType = new FullyQualifiedJavaType("int");
        Method method = new Method();
        method.setName(StatementIdValue.STATEMENT_LOAD);
        method.setReturnType(returnType);
        method.setVisibility(JavaVisibility.DEFAULT);
        method.addParameter(new Parameter(paramType, "id"));
        interfaze.addMethod(method);
        return super.clientGenerated(interfaze, topLevelClass, introspectedTable);
    }
}
