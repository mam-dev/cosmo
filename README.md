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

The application that uses cosmo-api and cosmo-core must be Spring Boot application.

Setting up a CalDAV server with cosmo-api and cosmo-core.

To be able to run the application one needs to configure the following:
 * At least one (concrete) implementation of ``org.springframework.security.core.AuthenticationProvider`` annotated with ``@Component`` 
 * Exactly one (concrete) implementation of ``org.unitedinternet.cosmo.db.DataSourceProvider``
 * One Spring Boot application main class annotated with ``@SpringBootApplication``

To be able to override one default implementation Spring ``@Primary`` annotation can be used.


## Building and running the existing demo application

The cosmo-webapp application is just for demo purposes.
To launch it, in the project's root directory (cosmo-webapp) just run ``mvn spring-boot:run``.
The calendar server will start using an in-memory database (data will be lost when the application stops).
It will be available for CalDAV requests at http://localhost:8080/cosmo/dav/.
The application simply creates a user if it does not exists and it does not check the password.
To create a collection for a new user a MKCALENDAR request is needed like :

`curl -X"MKCALENDAR" -H"Content-Type:application/xml" -u${your_email}:${your_passwd} http://localhost:8080/cosmo/dav/${your_email}/calendar`

The newly created collection can be configured in Mozilla Lightning application for instance by using the URL: `http://localhost:8080/cosmo/dav/${your_email}/calendar`



