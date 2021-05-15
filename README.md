# Andrea UBL Plugin

[Andrea UBL Plugin](https://github.com/rp-consulting/andrea-ubl-plugin/) es un plugin de Maven que permite la generación de clases Java a partir de archivos UBL/XSD. Este proyecto está enfocado a la implementación de aplicaciones de facturación electrónica en Colombia.

## Cómo utilizar
- Para proyectos que utilizan Maven, se necesita agregar la siguiente dependencia al archivo POM:

```xml
<plugins>
    <plugin>
        <groupId>org.jvnet.jaxb2.maven2</groupId>
        <artifactId>maven-jaxb2-plugin</artifactId>
        <version>0.13.1</version>
        <executions>
            <execution>
                <goals>
                    <goal>generate</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <verbose>false</verbose>
            <removeOldOutput>true</removeOldOutput>
            <schemaDirectory>${project.basedir}/src/main/xsd</schemaDirectory>
            <schemaIncludes>
                <include>**/*.xsd</include>
            </schemaIncludes>
            <bindingDirectory>${project.basedir}/src/main/xjb</bindingDirectory>
            <bindingIncludes>
                <include>**/*.xjb</include>
            </bindingIncludes>
            <strict>false</strict>
            <generateDirectory>${project.build.directory}/generated-sources/xjc</generateDirectory>
            <extension>true</extension>
            <args>
                <arg>-Xandrea-ubl</arg>
            </args>
            <plugins>
                <plugin>
                    <groupId>co.andrea</groupId>
                    <artifactId>andrea-ubl-plugin</artifactId>
                    <version>[1.0.0,)</version>
                </plugin>
            </plugins>
        </configuration>
    </plugin>
</plugins>
```
Los elementos destacados dentro de esta configuración son los siguientes:
- **schemaDirectory**: En esta sección se deberá indicar la ruta de los archivos XSD a partir de los cuales se va a generar las clases Java. En el ejemplo anterior, ubicaremos los archivos XSD en la ruta src/main/xsd
- **bindingDirectory**: Aquí se indicarán los archivos xjb que se utilizarán para resolver algunos conflictos dentro de la estructura de los archivos UBL.
- **generateDirectory**: Directorio donde quedarán los archivos java generados a partir de los UBL respectivos.

En [andrea-jaxb-ubl-20](https://github.com/rp-consulting/andrea-jaxb-ubl-20) se puede encontrar un ejemplo de uso de este plugin.

## Cambios
[Log de cambios](https://github.com/rp-consulting/andrea-ubl-plugin/blob/master/CHANGELOG.md)

## Autor
| [![](https://avatars1.githubusercontent.com/u/11875482?v=4&s=80)](https://github.com/rolandopalermo) |
|-|
| [@RolandoPalermo](https://github.com/rolandopalermo) |