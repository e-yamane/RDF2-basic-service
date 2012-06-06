name := "rdf2-basic-service"

description := "Rough Diamond Framework Basic Service"

organization := "jp.rough_diamond"

version := "2.0.1"

scalaVersion := "2.9.1"

libraryDependencies +=  "jp.rough_diamond"      %%  "rdf2-di"               % "2.0.1"

libraryDependencies +=  "jp.rough_diamond"      %%  "rdf2-resource"         % "2.0.1"

libraryDependencies +=  "jp.rough_diamond"      %%  "rdf2-service-locator"  % "2.0.1"

libraryDependencies +=  "jp.rough_diamond"      %%  "rdf2-service-db"       % "2.0.1"

libraryDependencies +=  "jp.rough_diamond"      %%  "rdf2-util-beans"       % "2.0.1"

//libraryDependencies +=  "org.springframework"   %   "spring-aop"            % "3.1.1.RELEASE"

//libraryDependencies +=  "cglib"                 %   "cglib"                 % "2.2.2"

//libraryDependencies +=  "org.javassist"         %     "javassist"             % "3.16.1-GA"

//libraryDependencies +=  "com.google.guava"      %     "guava"                 % "11.0.2"

libraryDependencies +=  "org.scalatest"         %%  "scalatest"             % "1.7.1"   % "test"

