# OWLGen
Repositorio que contiene el proyecto Eclipse y la documentación del código Java de OWL Generator y aplicaciones de ejemplo.

## Detalles del contenido:
  - Documentación disponible en línea en: https://oscarmateos.github.io/OWLGen/
  - Es posible consultar el código fuente de las clases comentadas en la memoria. Para ello dirigirse al directorio **_src/owlgenerator_**:
    * En el directorio **_java_** se encuentran los subdirectorios relativos a cada uno de los paquetes comentados en la memoria. Para ver las clases Java de cada módulo, basta con una vez dentro del subdirectorio correspondiente, hacer click sobre el fichero con extensión *.java* y esperar a que se abra o directamente descargarlo (solamente el fichero con el botón derecho y *"Guardar como"* o el repositorio completo comprimido). 
    * En el directorio **_resources_** se encuentran los recursos utilizados, estos son los ficheros RegexNER creados (ficheros con extensión *.rgx*) o los clasificadores para el idioma inglés utilizados para el reconocimiento de entidades (*Namer Entity Recognition*) por Stanford Core NLP.
    * En el directorio **_tests_** se encuentran los ficheros fuente para cada uno de los programas realizados para probar la librería y para regenerar cada ontología o para generar la ontología unión.
    
  - En el directorio **_lib_** se encuentran las librerías que son dependencias necesarias para el mismo. 
    * __Es posible que al clonar el repositorio o descargar el proyecto como fichero comprimido, la librería *stanford-english-corenlp-2016-01-10-models.jar* que se haya descargado pese alrededor de 1KB, en este caso probar a descargarla manualmente por separado seleccionandola del directorio **_lib_** y seleccionando la opción *"Guardar como..."* tras hacer click con el botón derecho. Sustituír el fichero del mismo nombre de 1KB. 
    Esto es así por el límite de GitHub de 100 MB de los fichero individuales, y para subir esta librería se ha utilizado la herramienta GitHub Large Files cuya integración a día de hoy no es perfecta.__ 
    
  - En el directorio **_docs_** se encuentra la documentación en HTML (Javadoc). 
