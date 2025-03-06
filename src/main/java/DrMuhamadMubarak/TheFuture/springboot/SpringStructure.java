package DrMuhamadMubarak.TheFuture.springboot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static DrMuhamadMubarak.TheFuture.utils.Utils.capitalize;
import static DrMuhamadMubarak.TheFuture.utils.Utils.createDirectory;

public class SpringStructure {
    public static void generateSpringBootProjectStructure(String projectName, String frontendType, String databaseType) throws IOException {
        String baseDir = "./" + projectName;
        createDirectory(baseDir);

        createDirectory(baseDir + "/src/main/java/com/example/" + projectName.toLowerCase() + "/controllers");
        createDirectory(baseDir + "/src/main/java/com/example/" + projectName.toLowerCase() + "/models");
        createDirectory(baseDir + "/src/main/java/com/example/" + projectName.toLowerCase() + "/repositories");
        createDirectory(baseDir + "/src/main/java/com/example/" + projectName.toLowerCase() + "/services");
        createDirectory(baseDir + "/src/main/resources/templates");
        createDirectory(baseDir + "/src/main/resources/static");
        createDirectory(baseDir + "/src/test/java/com/example/" + projectName.toLowerCase());

        Files.write(Paths.get(baseDir + "/src/main/resources/application.properties"), getSpringApplicationPropertiesContent(databaseType, projectName).getBytes());
        Files.write(Paths.get(baseDir + "/pom.xml"), getSpringPomXmlContent(projectName, frontendType, databaseType).getBytes());
        Files.write(Paths.get(baseDir + "/src/main/java/com/example/" + projectName.toLowerCase() + "/" + capitalize(projectName) + "Application.java"), getSpringMainClassContent(projectName).getBytes());

    }

    public static String getSpringMainClassContent(String projectName) {
        String capitalizedProjectName = capitalize(projectName);
        return "package com.example." + projectName.toLowerCase() + ";\n\n" + "import org.springframework.boot.SpringApplication;\n" + "import org.springframework.boot.autoconfigure.SpringBootApplication;\n\n" + "@SpringBootApplication\n" + "public class " + capitalizedProjectName + "Application {\n\n" + "    public static void main(String[] args) {\n" + "        SpringApplication.run(" + capitalizedProjectName + "Application.class, args);\n" + "    }\n" + "}";
    }

    public static String getSpringApplicationPropertiesContent(String databaseType, String projectName) {
        return "server.port=8081\n" +
               "spring.datasource.url=jdbc:mysql://localhost:3306/" + projectName.toLowerCase() + "?createDatabaseIfNotExist=true\n" +
               "spring.datasource.username=root\n" +
               "spring.datasource.password=Sql01276084$\n" +
               "spring.jpa.hibernate.ddl-auto=update";
    }

    public static String getSpringPomXmlContent(String projectName, String frontendType, String databaseType) {
        return String.format("""
                <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                    <modelVersion>4.0.0</modelVersion>
                    <parent>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-parent</artifactId>
                        <version>3.2.3</version>
                        <relativePath/>
                    </parent>
                    <groupId>com.example</groupId>
                    <artifactId>%s</artifactId>
                    <version>0.0.1-SNAPSHOT</version>
                    <name>%s</name>
                    <description>Demo project for Spring Boot</description>
                    <properties>
                        <java.version>22</java.version>
                        <lombok.version>1.18.34</lombok.version> <!-- Update the version as needed -->
                    </properties>
                    <dependencies>
                                               <dependency>
                                                   <groupId>org.springframework.boot</groupId>
                                                   <artifactId>spring-boot-starter-data-jpa</artifactId>
                                               </dependency>
                                               <dependency>
                                                   <groupId>org.springframework.boot</groupId>
                                                   <artifactId>spring-boot-starter-web</artifactId>
                                               </dependency>
                                               <dependency>
                                                   <groupId>org.springframework.boot</groupId>
                                                   <artifactId>spring-boot-starter-test</artifactId>
                                                   <scope>test</scope>
                                               </dependency>
                                               <dependency>
                                                   <groupId>org.projectlombok</groupId>
                                                   <artifactId>lombok</artifactId>
                                                   <scope>provided</scope>
                                               </dependency>
                                               <dependency>
                                                   <groupId>com.mysql</groupId>
                                                   <artifactId>mysql-connector-j</artifactId>
                                                   <scope>runtime</scope>
                                               </dependency>
                                           </dependencies>
                    <build>
                        <plugins>
                            <plugin>
                                <groupId>org.springframework.boot</groupId>
                                <artifactId>spring-boot-maven-plugin</artifactId>
                            </plugin>
                        </plugins>
                    </build>
                </project>
                """, projectName.toLowerCase(), projectName);
    }
}
