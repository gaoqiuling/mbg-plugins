package com.itisacat.generate.plugin;

import com.itisacat.generate.constant.MapperXmlKey;
import com.itisacat.generate.constant.MapperXmlValue;
import com.itisacat.generate.constant.StatementIdValue;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;

import java.util.List;

public class MapperSqlPlugin extends PluginAdapter {

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
        //region <sql id="Base_Column_List">
        //<sql></sql>
        XmlElement baseColumns = new XmlElement(MapperXmlKey.ELEMENT_SQL);
        //id="BaseResultMap"
        baseColumns.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_ID, MapperXmlValue.ATTRIBUTE_BASE_COLUMN_LIST));
        int length = introspectedTable.getBaseColumns().size();
        baseColumns.addElement(new TextElement(introspectedTable.getPrimaryKeyColumns().get(0).getActualColumnName() + (length > 0 ? "," : "")));
        for (int i = 0; i < length; i++) {
            baseColumns.addElement(new TextElement(introspectedTable.getBaseColumns().get(i).getActualColumnName() + (i == length - 1 ? "" : ",")));
        }
        rootElement.addElement(baseColumns);
        //endregion

        //region <sql id="listWhere">
        //<sql></sql>
        XmlElement listWhere = new XmlElement(MapperXmlKey.ELEMENT_SQL);
        listWhere.getAttributes().add(0, new Attribute(MapperXmlKey.ATTRIBUTE_ID, MapperXmlValue.ATTRIBUTE_LIST_WHERE));
        XmlElement where = new XmlElement(MapperXmlValue.ATTRIBUTE_WHERE);
        List<IntrospectedColumn> allColumns = introspectedTable.getNonBLOBColumns();
        allColumns.forEach(t -> {
            String strCheck = "";
            if (t.getJdbcTypeName().equals("VARCHAR")) {
                strCheck = " and " + t.getJavaProperty() + " !=''";
            }
            XmlElement ifElement = new XmlElement(MapperXmlKey.ELEMENT_IF);
            ifElement.addAttribute(new Attribute(MapperXmlKey.ATTRIBUTE_TEST, t.getJavaProperty() + " != null" + strCheck));
            ifElement.addElement(new TextElement("AND " + t.getActualColumnName() + " = #{" + t.getJavaProperty() + "}"));
            where.addElement(ifElement);
        });
        listWhere.addElement(where);
        rootElement.addElement(listWhere);
        //endregion

        //region <sql id="listOrder">
        //<sql></sql>
        XmlElement listOrder = new XmlElement(MapperXmlKey.ELEMENT_SQL);
        //id="listOrder"
        listOrder.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_ID, MapperXmlValue.ATTRIBUTE_LIST_ORDER));
        XmlElement ifElement = new XmlElement(MapperXmlKey.ELEMENT_IF);
        ifElement.addAttribute(new Attribute(MapperXmlKey.ATTRIBUTE_TEST, StatementIdValue.ATTRIBUTE_ORDER_FIELDS + " != null and " + StatementIdValue.ATTRIBUTE_ORDER_FIELDS + ".size > 0"));
        ifElement.addElement(new TextElement(MapperXmlKey.ATTRIBUTE_ORDER_BY));

        XmlElement outerForeach = new XmlElement(MapperXmlKey.ELEMENT_FOREACH);
        outerForeach.getAttributes().add(0, new Attribute(MapperXmlKey.ATTRIBUTE_COLLECTION, StatementIdValue.ATTRIBUTE_ORDER_FIELDS));
        outerForeach.getAttributes().add(1, new Attribute(MapperXmlKey.ATTRIBUTE_ITEM, "item"));
        outerForeach.getAttributes().add(2, new Attribute(MapperXmlKey.ATTRIBUTE_SEPARATOR, ","));
        outerForeach.addElement(new TextElement("${item.key} ${item.value}"));

        ifElement.addElement(outerForeach);
        listOrder.addElement(ifElement);
        rootElement.addElement(listOrder);
        //endregion

        //region <sql id="query">
        //<sql></sql>
        XmlElement query = new XmlElement(MapperXmlKey.ELEMENT_SQL);
        //id="query"
        query.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_ID, MapperXmlValue.ATTRIBUTE_QUERY));
        query.addElement(new TextElement(MapperXmlKey.ELEMENT_SELECT));

        XmlElement include = new XmlElement(MapperXmlKey.ELEMENT_INCLUDE);
        include.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_REFID, MapperXmlValue.ATTRIBUTE_BASE_COLUMN_LIST));
        query.addElement(include);

        query.addElement(new TextElement("from " + introspectedTable.getTableConfiguration().getTableName()));

        XmlElement includeWhere = new XmlElement(MapperXmlKey.ELEMENT_INCLUDE);
        includeWhere.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_REFID, MapperXmlValue.ATTRIBUTE_LIST_WHERE));
        query.addElement(includeWhere);

        XmlElement includeOrder = new XmlElement(MapperXmlKey.ELEMENT_INCLUDE);
        includeOrder.getAttributes().add(new Attribute(MapperXmlKey.ATTRIBUTE_REFID, MapperXmlValue.ATTRIBUTE_LIST_ORDER));
        query.addElement(includeOrder);
        rootElement.addElement(query);
        //endregion
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }


}
