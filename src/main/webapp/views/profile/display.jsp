<%--
  Created by IntelliJ IDEA.
  User: yuzi
  Date: 5/21/18
  Time: 4:05 PM
  To change this template use File | Settings | File Templates.
--%>

<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<%@taglib prefix="jstl"	uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="display" uri="http://displaytag.sf.net"%>
<%@ taglib prefix="acme" tagdir="/WEB-INF/tags" %>


<div class="container well">
    <div class="row user-menu-container square">
        <div class="col-md-7 user-details">
            <div class="row coralbg white">
                <div class="col-md-6 no-pad">
                    <div class="user-pad">
                        <h3>${profile.fullName}</h3>
                        <h4 class="white"><i class="fa fa-twitter"></i> ${actor.postalAddresses}</h4>
                        <jstl:if test="${actor.profile.id eq profile.id}">
                        <a  class="btn btn-labeled btn-info" href="profile/actor/edit.do?profileId=${profile.id}">
                            <span class="btn-label"><i class="fa fa-pencil"></i></span><spring:message code="general.edit"/>
                        </a>
                        </jstl:if>
                    </div>
                </div>
                <div class="col-md-6 no-pad">
                    <div class="user-image">
                        <jstl:if test="${profile.profilePhoto ne null}">
                            <img src="${profile.profilePhoto}" class="img-responsive thumbnail" />
                        </jstl:if>
                        <jstl:if test="${profile.profilePhoto eq null}">
                            <img src="http://nwsid.net/wp-content/uploads/2015/05/dummy-profile-pic-300x300.png" class="img-responsive thumbnail" />
                        </jstl:if>
                    </div>
                </div>
            </div>
            <div class="row overview">
                <div class="col-md-4 user-pad text-center">
                    <h3>FOLLOWERS</h3>
                    <h4>${followers_num}</h4>
                </div>
                <div class="col-md-4 user-pad text-center">
                    <h3>FOLLOWING</h3>
                    <h4>${followings_num}</h4>
                </div>
            </div>
        </div>
    </div>
</div>


