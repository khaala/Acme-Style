<%@page language="java" contentType="text/html; charset=ISO-8859-1"
        pageEncoding="ISO-8859-1"%>

<%@taglib prefix="jstl" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="security"	uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@taglib prefix="acme" tagdir="/WEB-INF/tags" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<div class="container">
    <div class="col-md-10 col-md-offset-1">

        <display:table name="stores" id="row" pagesize="5" class="table table-striped table-hover" requestURI="${requestURI}" >

        <security:authorize access="hasRole('MANAGER')">
            <display:column>
                <jstl:if test="${ empty row.servises}">
                    <a href="store/manager/edit.do?storeId=${row.id}">
                        <spring:message code="general.edit" />
                    </a>
                </jstl:if>
            </display:column>
        </security:authorize>

        <spring:message code="store.title" var="headerTag" />
        <display:column property="title" title="${headerTag}"/>

        <spring:message code="store.banner" var="pic"/>
        <display:column title="${pic}"><img src="${row.banner}" alt="no image" width="130" height="100"></display:column>

        <spring:message code="event.location" var="headerTag" />
        <display:column property="gpsCoordinates.name" title="${headerTag}"/>

        <security:authorize access="hasRole('MANAGER')">
            <display:column>
                   <acme:button code="general.display" url="/store/display.do?storeId=${row.id}"/>
            </display:column>
        </security:authorize>

    </display:table>

        <security:authorize access="hasRole('MANAGER')">
            <acme:button code="general.create" url="store/manager/create.do"/>
        </security:authorize>

        <acme:cancel code="general.cancel" url="welcome/index.do"/>

    </div>
</div>