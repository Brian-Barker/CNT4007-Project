default:
	javac -d out *.java
	javac -d project_config_file_large *.java
	javac -d project_config_file_small *.java

clean:
	rm ./project_config_file_large/*.class
	rm ./project_config_file_small/*.class
	rm ./out/*.class
