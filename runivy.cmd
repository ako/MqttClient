rm userlib/*
java -jar build\apache-ivy-2.4.0\ivy-2.4.0.jar -ivy ivy.xml -retrieve "userlib/[artifact]-[revision].[ext]"
rem java -jar build\apache-ivy-2.4.0\ivy-2.4.0.jar -help