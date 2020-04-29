build:
	mvn clean compile assembly:single
	mv target/myJar-jar-with-dependencies.jar myjar.jar
	rsync -azv --progress -e "ssh -i ~/Code/School/semester_8/DSP/keys/myKeyPair.pem"  myjar.jar ec2-user@$(EC2):.

connect:
	ssh -i "~/Code/School/semester_8/DSP/ec2_helloworld/myKeyPair.pem" ec2-user@$(EC2)



