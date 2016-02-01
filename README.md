# Cosmo

## Introduction	

Cosmo Calendar Server is part of an open source project called Chandler which provides calendaring features,
task and note management. Cosmo implements CalDAV protocol. 
It is being used as Calendar Server solution for WEB.DE, GMX.net, GMX.com and mail.com brands. 

This project contains only CalDAV core part from Chandler with its dependencies up to date 
(initial project being closed in 2008).

It also brings a new approach in such a way that Cosmo is a CalDAV runtime environment. The project provides an API
that contains abstractions over CalDAV and iCal specific objects, services implemented in Cosmo Core but also accessible
to developers and a plugin mechanism that allows developers to write custom logic without having to alter Cosmo Core code.


## Main parts

There are two main logical components, each of them residing in its own artifact: cosmo-api and cosmo-core.

 ``cosmo-api`` contains:

 - interfaces which are contracts between the CalDAV runtime environment
   and an implementation of such an interface
 - externalizable services definitions (interfaces) whose actual implementations are placed in cosmo-core and can be injected (externalized) into client components
 - POJOs and interfaces that are abstractions of CalDAV components
 - annotations that are used to specify that an implementation of some contract must be plugged into the CalDAV runtime environment
 - annotations that tells the runtime environment container that it should inject some (public) service implementation.


``cosmo-core`` is the actual CalDAV server and runtime environment for developer custom components. It looks up for such components at 
application context startup and registers them in order to be called during requests. 
It also injects public services into developer components.


## Technical requirements

The application that uses cosmo-api and cosmo-core must be a Java Servlet 3.0 application (and be deployed in a compliant 
Servlet 3.0 container).

Setting up a CalDAV server with cosmo-api and cosmo-core

At least one (concrete) implementation of ``org.springframework.security.core.AuthenticationProvider`` annotated with ``@CalendarSecurity`` 
must be provided. Exactly one (concrete) implementation of ``org.unitedinternet.cosmo.db.DataSourceProvider`` annotated
with ``@CalendarRepository`` must be provided.

Other components can exist without a restriction. All of these are looked up in a Spring context (if exists), 
otherwise are instantiated directly.


###Building and running the existing demo project
The cosmo-webapp application is just for demoing purposes. To launch it,
in the project's root directory just run ``mvn tomcat7:run-war -Dexample=true``.
The calendar server will be packaged, tested and run. It will be available for CalDAV requests at
http://localhost:8080/cosmo/dav/.
The application simply creates a user with an empty calendar called 'calendar' if 
the user doesn't exist and then you can perform CalDAV requests to URI /cosmo/dav/&lt;username>/calendar/ where  &lt;username>
is the username you provided for login.

###Example of Spring Application that creates a user if he doesn't exist:

web.xml:

	<web-app>
	...
	<context-param>
	        <param-name>contextConfigLocation</param-name>
	        <param-value>classpath*:/applicationContext.xml</param-value>
	    </context-param>
	    <listener>
	        <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	    </listener>  
	...
	</web-app>

applicationContext.xml:

	<beans>
	...
	    <bean id="authenticationProvider" class="com.unitedinternet.calendar.security.DummyAuthenticationProvider"/>
	    
	    <bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" lazy-init="true" destroy-method="close">
	         <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
	         <property name="url" value="jdbc:mysql://localhost/os?autoReconnect=true"/>
	         <property name="username" value="os"/> 
	         <property name="password" value="*" />
	         <property name="maxActive" value="100"/> 
	         <property name="maxIdle" value="20" />
	         <property name="maxWait" value="10000"/>
	         <property name="poolPreparedStatements" value="true" />
	         <property name="defaultAutoCommit" value="false" />
	    </bean>   
	    <bean id="dataSourceProviderImpl" class="com.unitedinternet.calendar.repository.DataSourceProviderImpl">
	        <constructor-arg ref="dataSource"/>
	        <constructor-arg value="MySQL5InnoDB"/>
	    </bean>
	...
	</beans>

The required authentication provider which in our case simply (for example's sake) considers each user a valid one 
is the class com.unitedinternet.calendar.security.DummyAuthenticationProvider:

	 	...
	@CalendarSecurity
	public class DummyAuthenticationProvider implements AuthenticationProvider{
	    	...
		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		    String userName = authentication.getName();
			return new UsernamePasswordAuthenticationToken(userName, "somePassword");
		}
	 
		...

If a user component is not found in the Spring application context, it will be instantiated. The same happens
to the successful authentication listener that creates a calendar user if he doesn't already exist with the given credentials after a successful authentication. (Please keep in mind that in this example each authentication is valid):

		...
	@CalendarSecurity
	public class UserCreationAuthenticationListener implements SuccessfulAuthenticationListener{
	    
	    @Provided
		private UserService userService;
		private EntityFactory entityFactory;
		private ContentService contentService;
		
		
		@Override
		public void onSuccessfulAuthentication(Authentication authentication) {
			createUserIfNotPresent(authentication);
		}
		...
		@Provided
		public void setEntityFactory(EntityFactory entityFactory) {
			his.entityFactory = entityFactory;
		}
		@Provided
		public void setContentService(ContentService contentService) {
			this.contentService = contentService;
		}
		...


The @CalendarSecurity annotation and implementation of SuccessfulAuthenticationListener tells the runtime that an 
instance of this class must be invoked after a successful authentication.
@Provided annotation associated with an externalizable service tells the container that it must inject the implementation
into that instance variable. The injection is available via setter and via field. 






