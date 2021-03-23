package com.miotech.kun.security.saml2;

import org.joda.time.DateTime;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.*;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Jie Chen
 * @created: 2021/3/10
 */
public class XmlUtils {

    public static Map<String, List<Object>> getAssertionAttributes(@NonNull Assertion assertion) {
        Map<String, List<Object>> attributeMap = new LinkedHashMap<>();
        for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
            for (Attribute attribute : attributeStatement.getAttributes()) {
                List<Object> attributeValues = new ArrayList<>();
                for (XMLObject xmlObject : attribute.getAttributeValues()) {
                    Object attributeValue = getXmlObjectValue(xmlObject);
                    if (attributeValue != null) {
                        attributeValues.add(attributeValue);
                    }
                }
                attributeMap.put(attribute.getName(), attributeValues);
            }
        }
        return attributeMap;
    }

    @Nullable
    public static Object getXmlObjectValue(@NonNull XMLObject xmlObject) {
        if (xmlObject instanceof XSAny) {
            return ((XSAny) xmlObject).getTextContent();
        } else if (xmlObject instanceof XSString) {
            return ((XSString) xmlObject).getValue();
        } else if (xmlObject instanceof XSInteger) {
            return ((XSInteger) xmlObject).getValue();
        } else if (xmlObject instanceof XSURI) {
            return ((XSURI) xmlObject).getValue();
        } else if (xmlObject instanceof XSBoolean) {
            XSBooleanValue xsBooleanValue = ((XSBoolean) xmlObject).getValue();
            return xsBooleanValue != null ? xsBooleanValue.getValue() : null;
        } else if (xmlObject instanceof XSDateTime) {
            DateTime dateTime = ((XSDateTime) xmlObject).getValue();
            return dateTime != null ? Instant.ofEpochMilli(dateTime.getMillis()) : null;
        } else {
            return null;
        }
    }
}
